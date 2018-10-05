package de.ddkfm.plan4ba.utils

import de.ddkfm.plan4ba.models.DatabaseConfig
import de.ddkfm.plan4ba.models.UserGroup
import de.ddkfm.plan4ba.models.User
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import org.hibernate.cfg.Environment


class HibernateUtils {
    companion object {
        private var sessionFactory : SessionFactory? = null

        fun setUp(config : DatabaseConfig) {
            var configuration = Configuration()
            configuration.setProperty(Environment.DRIVER, config.type.type)
            configuration.setProperty(Environment.URL, "jdbc:mysql://${config.host}:${config.port}/${config.database}")
            configuration.setProperty(Environment.USER, config.username)
            configuration.setProperty(Environment.PASS, config.password)

            configuration.setProperty(Environment.HBM2DDL_AUTO, "update")
            configuration.setProperty(Environment.SHOW_SQL, getEnvOrDefault("SHOW_SQL", "false"))

            configuration.addAnnotatedClass(UserGroup::class.java)
            configuration.addAnnotatedClass(User::class.java)

            try {
                sessionFactory = configuration.buildSessionFactory()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun doInHibernate(func : (session : Session) -> Any) : Any? {
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
        e.printStackTrace()
        transaction.rollback()
    } finally {
        transaction.commit()
    }
    return null
}