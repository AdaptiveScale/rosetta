## Downloading Drivers
You need the JDBC drivers to connect to the sources/targets that you will use with the rosetta tool.
The JDBC drivers for the rosetta supported databases can be downloaded from the following URLs:

- [BigQuery JDBC 4.2](https://storage.googleapis.com/simba-bq-release/jdbc/SimbaJDBCDriverforGoogleBigQuery42_1.3.0.1001.zip)
- [Snowflake JDBC 3.13.19](https://repo1.maven.org/maven2/net/snowflake/snowflake-jdbc/3.13.19/snowflake-jdbc-3.13.19.jar)
- [Postgresql JDBC 42.3.7](https://jdbc.postgresql.org/download/postgresql-42.3.7.jar)
- [MySQL JDBC 8.0.30](https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-8.0.30.zip)
- [Kinetica JDBC 7.1.7.7](https://github.com/kineticadb/kinetica-client-jdbc/archive/refs/tags/v7.1.7.7.zip)
- [Google Cloud Spanner JDBC 2.6.2](https://search.maven.org/remotecontent?filepath=com/google/cloud/google-cloud-spanner-jdbc/2.6.2/google-cloud-spanner-jdbc-2.6.2-single-jar-with-dependencies.jar)
- [SQL Server JDBC 12.2.0](https://go.microsoft.com/fwlink/?linkid=2223050)
- [DB2 JDBC jcc4](https://repo1.maven.org/maven2/com/ibm/db2/jcc/db2jcc/db2jcc4/db2jcc-db2jcc4.jar)
- [Oracle JDBC 23.2.0.0](https://download.oracle.com/otn-pub/otn_software/jdbc/232-DeveloperRel/ojdbc11.jar)
- [Redshift JDBC 42-2.1.0.30](https://s3.amazonaws.com/redshift-downloads/drivers/jdbc/2.1.0.30/redshift-jdbc42-2.1.0.30.jar)

### Example connection string configurations for databases

### BigQuery (service-based authentication OAuth 0)
```
url: jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=<PROJECT-ID>;AdditionalProjects=bigquery-public-data;OAuthType=0;OAuthServiceAcctEmail=<EMAIL>;OAuthPvtKeyPath=<SERVICE-ACCOUNT-PATH>
```

### BigQuery (pre-generated token authentication OAuth 2)
```
jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;OAuthType=2;ProjectId=<PROJECT-ID>;OAuthAccessToken=<ACCESS-TOKEN>;OAuthRefreshToken=<REFRESH-TOKEN>;OAuthClientId=<CLIENT-ID>;OAuthClientSecret=<CLIENT-SECRET>;
```

### BigQuery (application default credentials authentication OAuth 3)
```
jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;OAuthType=3;ProjectId=<PROJECT-ID>;
```

### Snowflake
```
url: jdbc:snowflake://<HOST>:443/?db=<DATABASE>&user=<USER>&password=<PASSWORD>
```

### PostgreSQL
```
url: jdbc:postgresql://<HOST>:5432/<DATABASE>?user=<USER>&password=<PASSWORD>
```

### MySQL
```
url: jdbc:mysql://<USER>:<PASSWORD>@<HOST>:3306/<DATABASE>
```

### Kinetica
```
url: jdbc:kinetica:URL=http://<HOST>:9191;CombinePrepareAndExecute=1
```

### Google Cloud Spanner
```
url: jdbc:cloudspanner:/projects/my-project/instances/my-instance/databases/my-db;credentials=/path/to/credentials.json
```

### Google CLoud Spanner (Emulator)
```
url: jdbc:cloudspanner://localhost:9010/projects/test/instances/test/databases/test?autoConfigEmulator=true
```

### SQL Server
```
url: jdbc:sqlserver://<HOST>:1433;databaseName=<DATABASE>
```

### DB2
```
url: jdbc:db2://<HOST>:50000;<DATABASE>
```

### ORACLE
```
url: jdbc:oracle:thin:<HOST>:1521:<SID>
```