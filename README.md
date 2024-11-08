# rosetta 
### Declarative Database Management - DDL Transpiler
![Alt text](./logo.png?raw=true "Rosetta")

## Overview
Rosetta is a declarative data modeler and transpiler that converts database objects from one database to another. Define your database in DBML and rosetta generates the target DDL for you.

Rosetta utilizes JDBC to extract schema metadata from a database and generates declarative DBML models that can be used for conversion to alternate database targets.

Generate DDL from a given source and transpile to the desired target.

[Join RosettaDB Slack](https://join.slack.com/t/rosettadb/shared_invite/zt-1fq6ajsl3-h8FOI7oJX3T4eI1HjcpPbw)

Currently, supported databases and translations are shown below in the table.

|                          |  **BigQuery**  | **Snowflake** |  **MySQL**   |  **Postgres**   | **Kinetica** |  **Google Cloud Spanner**  | **SQL Server**  |   **DB2**   |   **Oracle**   |
|--------------------------|:--------------:|:-------------:|:------------:|:---------------:|:------------:|:--------------------------:|:---------------:|:-----------:|:--------------:|
| **BigQuery**             |       /        |       ✅       |      ✅       |        ✅        |      ✅       |             ✅              |        ✅        |      ✅      |       ✅        |
| **Snowflake**            |       ✅        |       /       |      ✅       |        ✅        |      ✅       |             ✅              |        ✅         |      ✅       |        ✅        |
| **MySQL**                |       ✅        |       ✅       |      /       |        ✅        |      ✅       |             ✅              |        ✅        |      ✅      |       ✅        |
| **Postgres**             |       ✅        |       ✅       |      ✅       |        /        |      ✅       |             ✅              |        ✅        |      ✅      |       ✅        |
| **Kinetica**             |       ✅        |       ✅       |      ✅       |        ✅        |      /       |             ✅              |        ✅        |      ✅      |       ✅        |
| **Google Cloud Spanner** |       ✅        |       ✅       |        ✅       |         ✅         |        ✅       |             /              |        ✅        |      ✅      |       ✅        |
| **SQL Server**           |       ✅        |       ✅       |      ✅       |        ✅        |      ✅       |             ✅              |        /        |      ✅      |       ✅        |
| **DB2**                  |       ✅        |       ✅       |      ✅       |        ✅        |      ✅       |             ✅              |        ✅        |      /      |       ✅        |
| **Oracle**               |       ✅        |       ✅        |      ✅       |        ✅        |      ✅       |             ✅              |        ✅        |      ✅      |       /        |


## Quickstart
Get started with Rosetta by following these steps:

### Setting up Rosetta Drivers
You need the JDBC drivers to connect to the sources/targets that you will use with the rosetta tool.

Install the required [drivers](docs/markdowns/download_drivers.md) to enable Rosetta connections.

Set the ENV variable `ROSETTA_DRIVERS` to point to the location of your JDBC drivers.

```
export ROSETTA_DRIVERS=<path_to_jdbc_drivers>
```


### Downloading Rosetta
Download the rosetta binary for the supported OS ([releases page](https://github.com/AdaptiveScale/rosetta/releases)).
   ```
    rosetta-<version>-linux-x64.zip
    rosetta-<version>-mac_aarch64.zip
    rosetta-<version>-mac_x64.zip
    rosetta-<version>-win_x64.zip
   ```
#### For more in depth information on setting up and downloading rosetta please refer to this link [here](docs/markdowns/installation.md).

### Initializing a New Project

```
   rosetta init database-migration
```
The `rosetta init` command will create a new rosetta project within `database-migration` directory containing the `main.conf` (for configuring the connections to data sources).


### Configure connections in `main.conf`
Example: connections for postgres and mysql

```
# If your rosetta project is linked to a Git repo, during apply you can automatically commit/push the new version of your model.yaml
# The default value of git_auto_commit is false
git_auto_commit: false 
connections:
  - name: mysql
    databaseName: sakila
    schemaName:
    dbType: mysql
    url: jdbc:mysql://root:sakila@localhost:3306/sakila
    userName: root
    password: sakila
  - name: pg
    databaseName: postgres
    schemaName: public
    dbType: postgres
    url: jdbc:postgresql://localhost:5432/postgres?user=postgres&password=sakila
    userName: postgres
    password: sakila
```

## Extract and transpile your schemas from one DB to the other
Extract the schema from postgres and translate it to mysql:

```
   rosetta extract -s pg -t mysql
```

Migrate the translated schema to MySQL DB:

```
   rosetta apply -s mysql
```


## Rosetta DB YAML Config

### YAML Config File

Rosetta by default expects the YAML config file to be named `main.conf` and looks for it by default in the current folder. The configuration file can be overridden by using the `--config, -c` command line argument (see [Command Line Arguments](#command-line-arguments) below for more details).

Here is the list of available configurations in the `main.conf` file:

```yaml
connections:
  # The name of the connection
  - name: bigquery_prod
    
    # The name of the default database to use
    databaseName: bigquery-public-data
    
    # The name of the default schema to use
    schemaName: breathe
    
    # The type of the database
    dbType: bigquery
    
    # The connection uri for the database
    url: jdbc:bigquery://[Host]:[Port];ProjectId=[Project];OAuthType= [AuthValue];[Property1]=[Value1];[Property2]=[Value2];...
    
    # The name of the database user
    userName: user

    # The password of the database user
    password: password

    # The name of tables to include which is optional
    tables:
      - table_one
      - table_two
```

In the YAML config file you can also use environment variables. An example usage of environment variables in config file:

```
connections:
  - name: snowflake_weather_prod
    databaseName: SNOWFLAKE_SAMPLE_DATA
    schemaName: WEATHER
    dbType: snowflake
    url: jdbc:snowflake://<account_identifier>.snowflakecomputing.com/?<connection_params>
    userName: ${USER}
    password: ${PASSWORD}
```



###  Using External Translator and Custom Attributes
RosettaDB supports custom translators and translation attributes, allowing users to define or extend database-specific configurations via external CSV files.
 - External Translator: Users can specify a custom CSV file for translations by setting the EXTERNAL_TRANSLATION_FILE environment variable. This file allows adjustments in how database schemas are interpreted.
 - Translation Attributes: Additional attributes like ordinalPosition, autoincrement, nullable, and primaryKey can be defined in a separate attributes CSV file. Set the EXTERNAL_TRANSLATION_ATTRIBUTE_FILE environment variable to the file’s location to apply these attributes.
 - Indices: Rosetta supports index definitions in databases like Google Cloud Spanner, configured directly in model.yaml files to manage primary and secondary keys effectively.
   
For detailed setup instructions and examples, refer [here](docs/markdowns/translation.md).

## Rosetta Commands
### Available commands
- [init](docs/markdowns/init.md)
- [validate](docs/markdowns/validate.md)
- [extract](docs/markdowns/extract.md)
- [compile](docs/markdowns/compile.md)
- [dbt](docs/markdowns/dbt.md)
- [diff](docs/markdowns/diff.md)
- [test](docs/markdowns/test.md)
- [apply](docs/markdowns/apply.md)
- [generate](docs/markdowns/generate.md)
- [query](docs/markdowns/query.md)
- [drivers](docs/markdowns/drivers.md)

### Safety Operation
In `model.yaml` you can find the attribute `safeMode` which is by default disabled (false). If you want to prevent any DROP operation during
`apply` command, set `safeMode: true`.

### Operation level
In `model.yaml` you can find the attribute `operationLevel` which is by default set to `schema`. If you want to apply changes on to database level in your model instead of the specific schema in 
`apply` command, set `operationLevel: schema`.

### Fallback Type
In `model.yaml` you can define the attribute `fallbackType` for columns that are of custom types, not supported for translations or not included in the translation matrix.
If a given column type cannot be translated then the fallbackType will be used for the translation. `fallbackType` is optional. 


### Google Cloud Spanner JDBC Fix

**Note:** If you face one of the following errors with Google Cloud Spanner JDBC

```
java.sql.SQLException: No suitable driver

or

java.lang.SecurityException: Invalid signature file digest for Manifest main attributes
```

you can fix it by running the following command where your driver is located:
```
zip -d google-cloud-spanner-jdbc-2.6.2-single-jar-with-dependencies.jar 'META-INF/.SF' 'META-INF/.RSA' 'META-INF/*SF'
```

## Copyright and License Information
Unless otherwise specified, all content, including all source code files and documentation files in this repository are:

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this tool except in compliance with the License. You may obtain a copy of the License at: [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.