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

## Getting Started

### Prerequisites

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

### ROSETTA_DRIVERS Environment

Set the ENV variable `ROSETTA_DRIVERS` to point to the location of your JDBC drivers.

```
export ROSETTA_DRIVERS=<path_to_jdbc_drivers>
```

example:

```
export ROSETTA_DRIVERS=/Users/adaptivescale/drivers/*
```

### rosetta binary

1. Download the rosetta binary for the supported OS ([releases page](https://github.com/AdaptiveScale/rosetta/releases)).
   ```
    rosetta-<version>-linux-x64.zip
    rosetta-<version>-mac_aarch64.zip
    rosetta-<version>-mac_x64.zip
    rosetta-<version>-win_x64.zip
    ```
2. Unzip the downloaded file
3. Run rosetta commands using `./rosetta` which is located inside `bin` directory.
4. Create new project using `rosetta init` command:

```
   rosetta init database-migration
```

The `rosetta init` command will create a new rosetta project within `database-migration` directory containing the `main.conf` (for configuring the connections to data sources).

5. Configure connections in `main.conf`
example: connections for postgres and mysql

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

6. Extract the schema from postgres and translate it to mysql:

```
   rosetta extract -s pg -t mysql
```

The extract command will create two directories `pg` and `mysql`. `pg` directory will have the extracted schema 
from Postgres DB. The `mysql` directory will contain the translated YAML which is ready to be used in MySQL DB.

7. Migrate the translated schema to MySQL DB:

```
   rosetta apply -s mysql
```

The apply command will migrate the translated Postgres schema to MySQL.


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

### Translation
This module will read the database structure from the source and map it to a target type. For example, source metadata was BigQuery and we want to convert it to Snowflake. This will be done by using a CSV file that contain mappings like in the following example:
```344;;bigquery;;string;;snowflake;;string
345;;bigquery;;timestamp;;snowflake;;timestamp
346;;bigquery;;int64;;snowflake;;integer
347;;bigquery;;float64;;snowflake;;float
348;;bigquery;;array;;snowflake;;array
349;;bigquery;;date;;snowflake;;date
350;;bigquery;;datetime;;snowflake;;datetime
351;;bigquery;;boolean;;snowflake;;boolean
352;;bigquery;;time;;snowflake;;time
353;;bigquery;;geography;;snowflake;;geography
354;;bigquery;;numeric;;snowflake;;numeric
355;;bigquery;;bignumeric;;snowflake;;number
356;;bigquery;;bytes;;snowflake;;binary
357;;bigquery;;struct;;snowflake;;object
```


### Using external translator

RosettaDB allows users to use their own translator. For the supported databases you can extend or create your version
of translation CSV file. To use an external translator you need to set the `EXTERNAL_TRANSLATION_FILE` ENV variable
to point to the external file.

Set the ENV variable `EXTERNAL_TRANSLATION_FILE` to point to the location of your custom translator CSV file.

```
export EXTERNAL_TRANSLATION_FILE=<path_to_csv_translator>
```

example:

```
export EXTERNAL_TRANSLATION_FILE=/Users/adaptivescale/translation.csv
```

Make sure you keep the same format as the CSV example given above.

### Translation Attributes

Rosetta uses an additional file to maintain translation specific attributes.
It stores translation_id, the attribute_name and attribute_value:

```
1;;302;;columnDisplaySize;;38
2;;404;;columnDisplaySize;;30
3;;434;;columnDisplaySize;;17
```

The supported attribute names are:
- ordinalPosition
- autoincrement
- nullable
- primaryKey
- primaryKeySequenceId
- columnDisplaySize
- scale
- precision

Set the ENV variable `EXTERNAL_TRANSLATION_ATTRIBUTE_FILE` to point to the location of your custom translation attribute CSV file.

```
export EXTERNAL_TRANSLATION_ATTRIBUTE_FILE=<path_to_csv_translator>
```

example:

```
export EXTERNAL_TRANSLATION_ATTRIBUTE_FILE=/Users/adaptivescale/translation_attributes.csv
```

Make sure you keep the same format as the CSV example given above.

### Indices (Index)

Indices are supported in Google Cloud Spanner. An example on how they are represented in model.yaml

```
tables:
- name: "ExampleTable"
  type: "TABLE"
  schema: ""
  indices:
  - name: "PRIMARY_KEY"
    schema: ""
    tableName: "ExampleTable"
    columnNames:
    - "Id"
    - "UserId"
    nonUnique: false
    indexQualifier: ""
    type: 1
    ascOrDesc: "A"
    cardinality: -1
  - name: "IDX_ExampleTable_AddressId_299189FB00FDAFA5"
    schema: ""
    tableName: "ExampleTable"
    columnNames:
    - "AddressId"
    nonUnique: true
    indexQualifier: ""
    type: 2
    ascOrDesc: "A"
    cardinality: -1
  - name: "TestIndex"
    schema: ""
    tableName: "ExampleTable"
    columnNames:
    - "ClientId"
    - "DisplayName"
    nonUnique: true
    indexQualifier: ""
    type: 2
    ascOrDesc: "A"
    cardinality: -1
```

## Rosetta Commands
### Available commands
- init
- extract
- compile
- dbt
- diff
- test
- apply
- generate
- query

#### init
This command will generate a project (directory) if specified, a default configuration file located in the current directory with example connections for `bigquery` and `snowflake`, and the model directory.

    rosetta init [PROJECT_NAME]

Parameter | Description
--- | ---
(Optional) PROJECT_NAME | Project name (directory) where the configuration file and model directory will be created.

Example:
```yaml
#example with 2 connections
connections:
  - name: snowflake_weather_prod
    databaseName: SNOWFLAKE_SAMPLE_DATA
    schemaName: WEATHER
    dbType: snowflake
    url: jdbc:snowflake://<account_identifier>.snowflakecomputing.com/?<connection_params>
    userName: bob
    password: bobPassword
  - name: bigquery_prod
    databaseName: bigquery-public-data
    schemaName: breathe
    dbType: bigquery
    url: jdbc:bigquery://[Host]:[Port];ProjectId=[Project];OAuthType= [AuthValue];[Property1]=[Value1];[Property2]=[Value2];...
    userName: user
    password: password
    tables:
      - bigquery_table
```

#### extract
This is the command that extracts the schema from a database and generates declarative DBML models that can be used for conversion to alternate database targets.

    rosetta [-c, --config CONFIG_FILE] extract [-h, --help] [-s, --source CONNECTION_NAME] [-t, --convert-to CONNECTION_NAME]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection name to extract schema from.
-t, --convert-to CONNECTION_NAME (Optional) | The target connection name in which source DBML converts to.

Example:
```yaml
---
safeMode: false
databaseType: bigquery
operationLevel: database
tables:
- name: "profiles"
  type: "TABLE"
  schema: "breathe"
  columns:
  - name: "id"
    typeName: "INT64"
    jdbcDataType: "4"
    ordinalPosition: 0
    primaryKeySequenceId: 1
    columnDisplaySize: 10
    scale: 0
    precision: 10
    primaryKey: false
    nullable: false
    autoincrement: true
  - name: "name"
    typeName: "STRING"
    jdbcDataType: "12"
    ordinalPosition: 0
    primaryKeySequenceId: 0
    columnDisplaySize: 255
    scale: 0
    precision: 255
    primaryKey: false
    nullable: false
    autoincrement: false
```

#### compile
This command generates a DDL for a target database based on the source DBML which was generated by the previous command (`extract`).

    rosetta [-c, --config CONFIG_FILE] compile [-h, --help] [-t, --target CONNECTION_NAME] [-s, --source CONNECTION_NAME]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME (Optional) | The source connection name where models are generated.
-t, --target CONNECTION_NAME | The target connection name in which source DBML converts to.
-d, --with-drop | Add query to drop tables when generating ddl.

Example:
```yaml
CREATE SCHEMA breathe;
CREATE TABLE breathe.profiles(id INTEGER not null AUTO_INCREMENT, name STRING not null);
```

#### dbt
This is the command that generates dbt models for a source DBML which was generated by the previous command (`extract`).

    rosetta [-c, --config CONFIG_FILE] dbt [-h, --help] [-s, --source CONNECTION_NAME]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection name where models are generated.


#### diff 
Show the difference between the local model and the database. Check if any table is removed, or added or if any columns have changed.

    rosetta [-c, --config CONFIG_FILE] diff [-h, --help] [-s, --source CONNECTION_NAME]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection is used to specify which models and connection to use.
-m, --model MODEL_FILE (Optional) | The model file to use for apply. Default is `model.yaml`


Example:
```
There are changes between local model and targeted source
Table Changed: Table 'actor' columns changed
Column Changed: Column 'actor_id' in table 'actor' changed 'Precision'. New value: '1', old value: '5'
Column Changed: Column 'actor_id' in table 'actor' changed 'Autoincrement'. New value: 'true', old value: 'false'
Column Changed: Column 'actor_id' in table 'actor' changed 'Primary key'. New value: 'false', old value: 'true'
Column Changed: Column 'actor_id' in table 'actor' changed 'Nullable'. New value: 'true', old value: 'false'
Table Added: Table 'address'
```

#### test
This command runs tests for columns using assertions. Then they are translated into query commands, executed, and compared with an expected value. Currently supported assertions are: `equals(=), not equals(!=), less than(<), more than(>), less than or equals(<=), more than or equals(>=), contains(in), is null, is not null, like, between`. Examples are shown below:

    rosetta [-c, --config CONFIG_FILE] test [-h, --help] [-s, --source CONNECTION_NAME]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection is used to specify which models and connections to use.

**Note:** Value for BigQuery Array columns should be comma separated value ('a,b,c,d,e').

Example:
```yaml
---
safeMode: false
databaseType: "mysql"
operationLevel: database
tables:
  - name: "actor"
    type: "TABLE"
    columns:
      - name: "actor_id"
        typeName: "SMALLINT UNSIGNED"
        ordinalPosition: 0
        primaryKeySequenceId: 1
        columnDisplaySize: 5
        scale: 0
        precision: 5
        nullable: false
        primaryKey: true
        autoincrement: false
        tests:
          assertion:
            - operator: '='
              value: 16
              expected: 1
      - name: "first_name"
        typeName: "VARCHAR"
        ordinalPosition: 0
        primaryKeySequenceId: 0
        columnDisplaySize: 45
        scale: 0
        precision: 45
        nullable: false
        primaryKey: false
        autoincrement: false
        tests:
          assertion:
            - operator: '!='
              value: 'Michael'
              expected: 1
```

Output example:
```bash
Running tests for mysql. Found: 2

1 of 2, RUNNING test ('=') on column: 'actor_id'                                                    
1 of 2, FINISHED test on column: 'actor_id' (expected: '1' - actual: '1')  ......................... [PASS in 0.288s]
2 of 2, RUNNING test ('!=') on column: 'first_name'                                                 
2 of 2, FINISHED test on column: 'first_name' (expected: '1' - actual: '219')  ..................... [FAIL in 0.091s]
```

#### apply
Gets current model and compares with state of database, generates ddl for changes and applies to database. If you set `git_auto_commit` to `true` in `main.conf` it will automatically push the new model to your Git repo of the rosetta project.

    rosetta [-c, --config CONFIG_FILE] apply [-h, --help] [-s, --source CONNECTION_NAME]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection is used to specify which models and connection to use.
-m, --model MODEL_FILE (Optional) | The model file to use for apply. Default is `model.yaml`


Example:

(Actual database)
```yaml
---
safeMode: false
databaseType: "mysql"
operationLevel: database
tables:
  - name: "actor"
    type: "TABLE"
    columns:
      - name: "actor_id"
        typeName: "SMALLINT UNSIGNED"
        ordinalPosition: 0
        primaryKeySequenceId: 1
        columnDisplaySize: 5
        scale: 0
        precision: 5
        nullable: false
        primaryKey: true
        autoincrement: false
        tests:
          assertion:
            - operator: '='
              value: 16
              expected: 1
```

(Expected database)
```yaml
---
safeMode: false
databaseType: "mysql"
operationLevel: database
tables:
  - name: "actor"
    type: "TABLE"
    columns:
      - name: "actor_id"
        typeName: "SMALLINT UNSIGNED"
        ordinalPosition: 0
        primaryKeySequenceId: 1
        columnDisplaySize: 5
        scale: 0
        precision: 5
        nullable: false
        primaryKey: true
        autoincrement: false
        tests:
          assertion:
            - operator: '='
              value: 16
              expected: 1
      - name: "first_name"
        typeName: "VARCHAR"
        ordinalPosition: 0
        primaryKeySequenceId: 0
        columnDisplaySize: 45
        scale: 0
        precision: 45
        nullable: false
        primaryKey: false
        autoincrement: false
        tests:
          assertion:
            - operator: '!='
              value: 'Michael'
              expected: 1
```

Description: Our actual database does not contain `first_name` so we expect it to alter the table and add the column, inside the source directory there will be the executed DDL and a snapshot of the current database.

#### generate
This command will generate Spark Python (file) or Spark Scala (file), firstly it extracts a schema from a source database and gets connection properties from the source connection, then it creates a python (file) or scala (file) that translates schemas, which is ready to transfer data from source to target.

    rosetta [-c, --config CONFIG_FILE] generate [-h, --help] [-s, --source CONNECTION_NAME] [-t, --target CONNECTION_NAME] [--pyspark] [--scala]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection name to extract schema from.
-t, --target CONNECTION_NAME| The target connection name where the data will be transfered.
--pyspark | Generates the Spark SQL file.
--scala | Generates the Scala SQL file.

#### query
The query command allows you to use natural language commands to query your databases, transforming these commands into SQL SELECT statements. By leveraging the capabilities of AI and LLMs, specifically OpenAI models, it interprets user queries and generates the corresponding SQL queries. For effective use of this command, users need to provide their OpenAI API Key and specify the OpenAI model to be utilized. The output will be written to a CSV file. The max number of rows that will be returned is 200. You can overwrite this value, or remove completely the limit.  The default openai model that is used is gpt-3.5-turbo.

    rosetta [-c, --config CONFIG_FILE] query [-h, --help] [-s, --source CONNECTION_NAME] [-q, --query "Natural language QUERY"] [--output "Output DIRECTORY or FILE"]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection is used to specify which models and connection to use.
-q --query "Natural language QUERY"  | pecifies the natural language query to be transformed into an SQL SELECT statement.
-l --limit Response Row limit (Optional) | Limits the number of rows in the generated CSV file. If not specified, the default limit is set to 200 rows.
--no-limit (Optional) | Specifies that there should be no limit on the number of rows in the generated CSV file.


**Example** (Setting the key and model) :

(Config file)
```
openai_api_key: "sk-abcdefghijklmno1234567890"
openai_model: "gpt-4"
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

***Example*** (Query)
```
   rosetta query -s mysql -q "Show me the top 10 customers by revenue."
```
***CSV Output Example***
```CSV
customer_name,total_revenue,location,email
John Doe,50000,New York,johndoe@example.com
Jane Smith,45000,Los Angeles,janesmith@example.com
David Johnson,40000,Chicago,davidjohnson@example.com
Emily Brown,35000,San Francisco,emilybrown@example.com
Michael Lee,30000,Miami,michaellee@example.com
Sarah Taylor,25000,Seattle,sarahtaylor@example.com
Robert Clark,20000,Boston,robertclark@example.com
Lisa Martinez,15000,Denver,lisamartinez@example.com
Christopher Anderson,10000,Austin,christopheranderson@example.com
Amanda Wilson,5000,Atlanta,amandawilson@example.com

```
**Note:**  When giving a request that will not generate a SELECT statement the query will be generated but will not be executed rather be given to the user to execute on their own.



### Safety Operation
In `model.yaml` you can find the attribute `safeMode` which is by default disabled (false). If you want to prevent any DROP operation during
`apply` command, set `safeMode: true`.

### Operation level
In `model.yaml` you can find the attribute `operationLevel` which is by default set to `schema`. If you want to apply changes on to database level in your model instead of the specific schema in 
`apply` command, set `operationLevel: schema`.

### Fallback Type
In `model.yaml` you can define the attribute `fallbackType` for columns that are of custom types, not supported for translations or not included in the translation matrix.
If a given column type cannot be translated then the fallbackType will be used for the translation. `fallbackType` is optional. 

## RosettaDB CLI JAR and RosettaDB Source

### Setting Up the CLI JAR (Optional)

1. Download the rosetta CLI JAR ([releases page](https://github.com/AdaptiveScale/rosetta/releases))
2. Create an alias command

```bash
alias rosetta='java -cp "<path_to_our_cli_jar>:<path_to_our_drivers>" com.adaptivescale.rosetta.cli.Main'
```

example:

```bash
alias rosetta='java -cp "/Users/adaptivescale/cli-1.0.0.jar:/Users/adaptivescale/drivers/*" com.adaptivescale.rosetta.cli.Main'
```

**Note:** If we are using the **cli** JAR file, we need to specify the location of the JDBC drivers (directory). See the Getting Started section.

### Build from the source (Optional)

```gradle binary:runtimeZip```

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