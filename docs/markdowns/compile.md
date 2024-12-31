## Compile DBML models into target DDL statements

### Command: compile

The `compile` command generates DDL (Data Definition Language) statements for a target database based on the DBML (Database Markup Language) extracted from a source database by the previous (`extract`) command. It builds schemas and tables in the target database using the extracted database schema.

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

##### Example Command:
Assuming `main.conf` is present in your working directory and configured for both source and target connections, a basic usage example is as follows:

    rosetta compile -s source_db_connection -t target_db_connection

**This command:**
 1. Connects to `source_db_connection` to retrieve DBML data.
 2. Converts the DBML into DDL specific to `target_db_connection`.

##### Additional Notes
- The `--with-drop` option should be used with caution, as it will delete existing tables in the target database.
- Ensure that the target connection name is correctly set in `main.conf` or passed directly as a parameter.