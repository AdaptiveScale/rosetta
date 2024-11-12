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

##### Example Command:
Here’s a basic example command that uses the `generate` function:

    rosetta generate -s source_db_connection -t target_db_connection --pyspark

**This command will:**

1. Connect to the specified source and target databases using the connection details provided.
2. Extract the schema from the source.
3. Generate a PySpark or Scala script, depending on the selected flag `(--pyspark or --scala)`, which is ready to transfer data from source to target.

##### Additional Notes
- **JDBC Drivers**: Ensure you have the correct JDBC drivers for both the source and target databases. These drivers should be specified in the `spark.driver.extraClassPath`.
- **Database Configuration**: Modify the `source_jdbc_url` ,`target_jdbc_url`, and other connection parameters as per your environment setup.
- **Mode Options**: The `mode("overwrite")` option in `.save()` will overwrite any existing data in the target table. Change it as needed (e.g., `append`, `ignore`, `error`).
