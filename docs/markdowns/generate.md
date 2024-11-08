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
