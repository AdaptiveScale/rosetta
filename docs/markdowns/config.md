## Manage RosettaDB configuration settings

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