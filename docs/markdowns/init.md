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