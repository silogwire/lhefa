package de.ddkfm.plan4ba.utils

import de.ddkfm.plan4ba.models.*
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import org.hibernate.cfg.Environment
import java.util.stream.Collectors


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

            configuration.addAnnotatedClass(HibernateUniversity::class.java)
            configuration.addAnnotatedClass(HibernateUserGroup::class.java)
            configuration.addAnnotatedClass(HibernateUser::class.java)
            configuration.addAnnotatedClass(HibernateLecture::class.java)
            configuration.addAnnotatedClass(HibernateToken::class.java)
            configuration.addAnnotatedClass(HibernateInfotext::class.java)
            configuration.addAnnotatedClass(HibernateNotification::class.java)
            configuration.addAnnotatedClass(HibernateLink::class.java)


            try {
                sessionFactory = configuration.buildSessionFactory()
            } catch (e: Exception) {
                e.printStackTrace()
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
                    if(infotext == null) {
                        session.doInTransaction {
                            it.save(HibernateInfotext(0, key, infotexts.getOrDefault(key, "")))
                        }
                    }
                }
            }
        }

        fun <T> doInHibernate(func : (session : Session) -> T) : T? {
            var session = sessionFactory?.openSession()
            try {
                if(session != null) {
                    return func.invoke(session)
                }
            } catch (e : Exception) {
                e.printStackTrace()
            } finally {
                session?.close()
            }
            return null
        }
        fun doInTransaction(session : Session, func : (session : Session) -> Any) : Any? {
            var transaction = session.beginTransaction()
            try {
                return func.invoke(session)
            } catch (e : Exception) {
                e.printStackTrace()
                transaction.rollback()
            } finally {
                transaction.commit()
            }
            return null
        }
    }
}

fun Session.doInTransaction(func : (session : Session) -> Any?) : Any?{
    var transaction = this.beginTransaction()
    try {
        return func.invoke(this)
    } catch (e : Exception) {
        transaction.rollback()
        throw e
    } finally {
        transaction.commit()
    }
    return null
}