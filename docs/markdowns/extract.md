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