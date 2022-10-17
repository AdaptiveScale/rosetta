# rosetta 
### Declarative Database Management - DDL Transpiler
![Alt text](./logo.png?raw=true "Rosetta")

## Overview
Rosetta is a declarative data modeler and transpiler that converts database objects from one database to another. Define your database in DBML and rosetta generates the target DDL for you.

Rosetta utilizes JDBC to extract schema metadata from a database and generates declarative DBML models that can be used for conversion to alternate database targets.

Generate DDL from a given source and transpile to the desired target.

[Join RosettaDB Slack](https://join.slack.com/t/rosettadb/shared_invite/zt-1fq6ajsl3-h8FOI7oJX3T4eI1HjcpPbw)

## Translation
This module will read the database structure from the source and map it to a target type. For example, source metadata was BigQuery and we want to convert it to Snowflake. This will be done with preset JSON files that contain mapping like in the following example:
```json
{
  "version": "0.0.1",
  "converters": [
    {
      "targetTypeName": "STRING",
      "length": 20,
      "compatibleTypes": [
        {
          "typeName": "STRING"
        }
      ]
    },
    {
      "targetTypeName": "INTEGER",
      "compatibleTypes": [
        {
          "typeName": "INT64"
        }
      ]
    }
  ]
}
```

Currently, supported databases for translation are shown below in the table.

|         | **BigQuery** | **Snowflake** |  **MySQL**  | **Postgres** | **Kinetica** |
|---------|:--------:|:--------:|:-------:|:--------:|:--------:|
| **BigQuery** |     /     |     ✅     |    ✅     |     ✅     |   ✅      |
| **Snowflake** |    ✅     |     /     |   ✅      |    ✅      |   ✅      |
| **MySQL**   |      ✅    |     ✅     |    /    |      ✅     |    ✅      | 
| **Postgres** |     ✅     |     ✅     |    ✅    |    /     |     ✅      | 
| **Kinetica** |     ✅     |     ✅     |    ✅    |     ✅    |     /      |

## Getting Started

### Prerequisites

You need the JDBC drivers to connect to the sources/targets that you will use with the rosetta tool. 
The JDBC drivers for the rosetta supported databases can be downloaded from the following URLs:

- [BigQuery JDBC 4.2](https://storage.googleapis.com/simba-bq-release/jdbc/SimbaJDBCDriverforGoogleBigQuery42_1.3.0.1001.zip)
- [Snowflake JDBC 3.13.19](https://repo1.maven.org/maven2/net/snowflake/snowflake-jdbc/3.13.19/snowflake-jdbc-3.13.19.jar)
- [Postgresql JDBC 42.3.7](https://jdbc.postgresql.org/download/postgresql-42.3.7.jar)
- [MySQL JDBC 8.0.30](https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-8.0.30.zip)
- [Kinetica JDBC 7.1.7.7](https://github.com/kineticadb/kinetica-client-jdbc/archive/refs/tags/v7.1.7.7.zip)

Set the ENV variable `ROSETTA_DRIVERS` to point to the location of your JDBC drivers.

```
export ROSETTA_DRIVERS=<path_to_jdbc_drivers>
```

example:

```
export ROSETTA_DRIVERS=/Users/adaptivescale/drivers/*
```

### rosetta binary (Recommended)

1. Download the rosetta binary for the supported OS ([releases page](https://github.com/AdaptiveScale/rosetta/releases)).
   ```
    rosetta-<version>-linux-x64.zip
    rosetta-<version>-mac_aarch64.zip
    rosetta-<version>-mac_x64.zip
    rosetta-<version>-win_x64.zip
    ```
2. Unzip the downloaded file
3. Run rosetta commands using `./rosetta` which is located inside `bin` directory.
4. On the first run it will create a directory for storing your JDBC drivers, if you haven't set already the ENV variable `ROSETTA_DRIVERS` from the previous step.

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

**Note:** If we are using the **cli** JAR file, we need to specify the location of the JDBC drivers (directory).

### Build from the source (Optional)
 
   ```gradle binary:runtimeZip```


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

### Example connection string configurations for databases

### BigQuery
```
url: jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=<PROJECT-ID>;AdditionalProjects=bigquery-public-data;OAuthType=0;OAuthServiceAcctEmail=<EMAIL>;OAuthPvtKeyPath=<SERVICE-ACCOUNT-PATH>
```

### Snowflake
```
url: jdbc:snowflake://<HOST>:443/?db=<DATABASE>&user=<USER>&password=<PASSWORD>
```

### PostgreSQL
```
url: jdbc:postgresql://<HOST>:15432/<DATABASE>?user=<USER>&password=<PASSWORD>
```

### MySQL
```
url: jdbc:mysql://<USER>:<PASSWORD>@<HOST>:49154/<DATABASE>
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
databaseType: bigquery
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

Example:
```
There are changes between local model and targeted source
Table Changed: Table 'actor' columns changed
Column Changed: Column 'actor_id' in table 'actor' changed 'Precision'. Old value: '1', new value: '5'
Column Changed: Column 'actor_id' in table 'actor' changed 'Autoincrement'. Old value: 'true', new value: 'false'
Column Changed: Column 'actor_id' in table 'actor' changed 'Primary key'. Old value: 'false', new value: 'true'
Column Changed: Column 'actor_id' in table 'actor' changed 'Nullable'. Old value: 'true', new value: 'false'
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
databaseType: "mysql"
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
Gets current model and compares with state of database, generates ddl for changes and applies to database.

    rosetta [-c, --config CONFIG_FILE] apply [-h, --help] [-s, --source CONNECTION_NAME]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection is used to specify which models and connection to use.

Example:

(Actual database)
```yaml
---
databaseType: "mysql"
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
databaseType: "mysql"
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

## Copyright and License Information
Unless otherwise specified, all content, including all source code files and documentation files in this repository are:

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this tool except in compliance with the License. You may obtain a copy of the License at: [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.