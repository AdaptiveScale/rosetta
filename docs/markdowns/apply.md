## Apply Database changes

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
