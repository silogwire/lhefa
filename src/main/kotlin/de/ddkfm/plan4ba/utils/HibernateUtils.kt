package de.ddkfm.plan4ba.utils

import de.ddkfm.plan4ba.SentryTurret
import de.ddkfm.plan4ba.capture
import de.ddkfm.plan4ba.models.DatabaseConfig
import de.ddkfm.plan4ba.models.Model
import de.ddkfm.plan4ba.models.database.HibernateInfotext
import de.ddkfm.plan4ba.models.database.HibernateLink
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import org.hibernate.cfg.Environment
import org.reflections.Reflections


class HibernateUtils {
    companion object {
        private var sessionFactory : SessionFactory? = null

        fun setUp(config : DatabaseConfig) {
            var configuration = Configuration()
            configuration.setProperty(Environment.DRIVER, config.type.type)
            configuration.setProperty(Environment.URL, "jdbc:${config.type.toJDBCType()}://${config.host}:${config.port}/${config.database}")
            configuration.setProperty(Environment.USER, config.username)
            configuration.setProperty(Environment.PASS, config.password)

            configuration.setProperty(Environment.HBM2DDL_AUTO, "update")
            configuration.setProperty(Environment.SHOW_SQL, getEnvOrDefault("SHOW_SQL", "false"))


            var reflections = Reflections("de.ddkfm.plan4ba.models.database")

            var databaseTypes = reflections.getSubTypesOf(Model::class.java)
            databaseTypes.forEach { model -> configuration.addAnnotatedClass(model) }


            try {
                sessionFactory = configuration.buildSessionFactory()
            } catch (e: Exception) {
                SentryTurret.log {
                    addTag("Hibernate", "")
                }.capture(e)
            }
            initDatabase()
        }
        private fun initDatabase() {
            val infotexts = mapOf(
                    "login.storehash" to "Soll ihr Campus Dual Hash gespeichert werden, um ihren Stundenplan täglich zu aktualiseren",
                    "login.privacynotice" to "Datenschutzerklärung der Stundenplan-App Plan4BA"
            )
            infotexts.keys.forEach { key ->
                doInHibernate { session ->
                    val infotext = session.createQuery("From HibernateInfotext Where key = '$key'", HibernateInfotext::class.java).list().firstOrNull()
                    doInTransaction(session) {
                        if(infotext == null)
                            it.save(HibernateInfotext(0, key, infotexts.getOrDefault(key, ""), "de"))
                    }
                }
            }
            val abandonedIntotexts = doInHibernate { it.createQuery("From HibernateInfotext Where language is null or language = ''", HibernateInfotext::class.java).list() }
            abandonedIntotexts?.forEach { info ->
                doInHibernate { doInTransaction(it) { session ->
                    session.update(info.copy(language = "de"))
                } }
            }

            val links = doInHibernate { it.createQuery("From HibernateLink Where language is null or language = ''", HibernateLink::class.java).list() }
            links?.forEach { link ->
                doInHibernate { doInTransaction(it) { session ->
                    session.update(link.copy(language = "de"))
                } }
            }

        }

        fun <T> doInHibernate(func : (session : Session) -> T?) : T? {
            var session = sessionFactory?.openSession()
            try {
                if(session != null) {
                    return func.invoke(session)
                }
            } catch (e : Exception) {
                SentryTurret.log {
                    addTag("Hibernate", "")
                }.capture(e)
                throw e
            } finally {
                session?.close()
            }
            return null;
        }
        fun doInTransaction(session : Session, func : (session : Session) -> Any) : Any? {
            var transaction = session.beginTransaction()
            try {
                return func.invoke(session)
            } catch (e : Exception) {
                SentryTurret.log {
                    addTag("Hibernate", "")
                }.capture(e)
                transaction.rollback()
            } finally {
                transaction.commit()
            }
            return null
        }
    }
}

infix fun Session.save(user : Any) {
    this.transaction {
        it.save(user)
    }
}

infix fun Session.update(user : Model<*>) {
    this.transaction {
        it.update(user)
    }
}

infix fun Session.delete(user : Any) {
    this.transaction {
        it.update(user)
    }
}

fun <T> Session.transaction(func : (session : Session) -> T?) : T?{
    var transaction = this.beginTransaction()
    try {
        return func.invoke(this)
    } catch (e : Exception) {
        SentryTurret.log {
            addTag("Hibernate", "")
        }.capture(e)
        transaction.rollback()
        throw e
    } finally {
        transaction.commit()
    }
    return null
}

infix fun String.eq(obj : Any) : Triple<String, String, Any> = Triple(this, "=", obj)
infix fun String.lt(obj : Any) : Triple<String, String, Any> = Triple(this, "<", obj)
infix fun String.gt(obj : Any) : Triple<String, String, Any> = Triple(this, ">", obj)

class Where(val linkType : String = "and") {
    private val criterias = mutableListOf<String>()
    fun add(triple : Triple<String, String, Any>) : Where {
        val criteria = "obj.${triple.first} ${triple.second} ${triple.third}"
        criterias.add(criteria)
        return this
    }

    override fun toString(): String {
        return this.criterias.joinToString(separator = " $linkType ")
    }
    companion object {
        fun or() : Where = Where(linkType = "or")
        fun and() : Where = Where(linkType = "and")
    }
}

inline fun <reified T> Session.single(id : Int) : T? {
    return this.get(T::class.java, id)
}
inline fun <reified T> Session.list(where : String = "1=1") : List<T>? {
    return this.createQuery("From ${T::class.java.simpleName} obj Where $where", T::class.java).list()
}

inline fun <reified T> Session.list(where : Where) :List<T>? {
    val whereString = where.toString()
    return this.list(whereString)
}

fun <T> inSession(lambda : (Session) -> T?) : T? {
    return HibernateUtils.doInHibernate(lambda)
}

