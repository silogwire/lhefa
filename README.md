# DBService
API-Service to capsule all database operations

## Environment-Variables
- DATABASE_DRIVER = (MySQL|PostgreSQL) defaul: MySQL
- DATABASE_HOST = <Host> default: localhost
- DATABASE_PORT = <Databaseport> default: 3306
- DATABASE_NAME = <Database> default: dbservice
- DATABASE_USER = <Username> default: dbservice
- DATABASE_PASSWORD = <Password> default: dbservice
- ENABLE_SWAGGER = (true|false) default: false
  > OpenAPI-Specification(Swagger) is generated under /swagger and /swagger/html 
- SHOW_SQL = (true|false) default: false 
  > should Hibernate log all SQL-Statements?