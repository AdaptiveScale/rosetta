# rosetta
![Alt text](./logo.png?raw=true "Rosetta")

Declarative Database Management - DDL Transpiler

## Overview
Rosetta is a declarative data modeler and transpiler that converts database objects from one database to another. Define your database in DBML and rosetta generates the target DDL for you.

Rosetta utilizes JDBC to extract schema metadata from a database and generates declarative DBML models that can be used for conversion to alternate database targets.

Generate DDL from a given source and transpile to a desired target.

### Translation
This module will read structure provided as in the format in the source module. Once the structure is received it will be mapped to a target type i.e. source metadata was BigQuery and we want to convert it to Snowflake. This will be done with preset of JSON files that contain mapping like in the following example:
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

Currently supported databases are shown below on the table.

|         | **BigQuery** | **Snowflake** |  **MySQL**  | **Postgres** |
|---------|:--------:|:--------:|:-------:|:--------:|
| **BigQuery** |    ❌     |     ✅     |         |          |
| **Snowflake** |    ✅     |     ❌     |         |          |
| **MySQL**   |          |     ✅     |    ❌    |          |
| **Postgres** |          |     ✅     |    ✅    |    ❌     |

## Usage

### Setting Up The CLI
First, we need to download the cli jar file in order to get started. This can be achieved by downloading the latest release from [releases page](https://github.com/AdaptiveScale/rosetta/releases).
After we have downloaded the jar file, we need to specify where our JDBC drivers(directory) are located. We are going to create an alias command in order to not specify the same command multiple times.
```bash
alias rosetta='java -cp "<path_to_our_cli_dir>/rosetta-0.0.1.jar:<path_to_our_drivers>" com.adaptivescale.rosetta.cli.Main'
```

After setting an alias command we are ready to use our cli.

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

### Command Line Arguments

Rosetta supports the following subcommands:

#### init
This command will generate a project (directory) if specified, a default configuration file located in the current directory with example connections for `bigquery` and `snowflake`, and the model directory.

`usage: rosetta init [PROJECT_NAME]`

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

`usage: rosetta [-c, --config CONFIG_FILE] extract [-h, --help] [-s, --source CONNECTION_NAME] [-t, --convert-to CONNECTION_NAME]`

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
This is the main command that generates a DDL for a target database based on the source DBML which was generated by the previous command (`extract`).

`usage: rosetta [-c, --config CONFIG_FILE] compile [-h, --help] [-t, --target CONNECTION_NAME] [-s, --source CONNECTION_NAME]`

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME (Optional) | The source connection name where models are generated.
-t, --target CONNECTION_NAME | The target connection name in which source DBML converts to.

Example:
```yaml
CREATE SCHEMA breathe;
CREATE TABLE breathe.profiles(id INTEGER not null AUTO_INCREMENT, name STRING not null);
```

#### dbt
This is the command that generates dbt models for a source DBML which was generated by the previous command (`extract`).

`usage: rosetta [-c, --config CONFIG_FILE] dbt [-h, --help] [-s, --source CONNECTION_NAME]`

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection name where models are generated.


## Copyright and License Information
Unless otherwise specified, all content, including all source code files and documentation files in this repository are:

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this tool except in compliance with the License. You may obtain a copy of the License at: [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.