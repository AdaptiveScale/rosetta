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

## Rosetta Commands
### Available commands
- [config](docs/markdowns/config.md)
- [init](docs/markdowns/init.md)
- [validate](docs/markdowns/validate.md)
- [drivers](docs/markdowns/drivers.md)
- [extract](docs/markdowns/extract.md)
- [compile](docs/markdowns/compile.md)
- [apply](docs/markdowns/apply.md)
- [diff](docs/markdowns/diff.md)
- [test](docs/markdowns/test.md)
- [dbt](docs/markdowns/dbt.md)
- [generate](docs/markdowns/generate.md)
- [query](docs/markdowns/query.md)


## Copyright and License Information
Unless otherwise specified, all content, including all source code files and documentation files in this repository are:

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this tool except in compliance with the License. You may obtain a copy of the License at: [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.