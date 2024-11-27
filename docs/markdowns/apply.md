## Apply generated DDL to the target database

### Command: apply
The apply command compares the current database state with the model defined in your Rosetta project. It generates the necessary DDL to align the database with the model and applies the changes to the database. If the git_auto_commit setting in main.conf is set to true, Rosetta will also automatically commit and push the updated model to the associated Git repository.

    rosetta [-c, --config CONFIG_FILE] apply [-h, --help] [-s, --source CONNECTION_NAME]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection is used to specify which models and connection to use.
-m, --model MODEL_FILE (Optional) | The model file to use for apply. Default is `model.yaml`


Example:

Actual Database (Current State)
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

Expected Database (Target State)
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

The apply command detects that the first_name column is missing in the actual database. It generates a DDL statement to alter the actor table and add the first_name column.

**Outputs**:
- A snapshot of the updated database schema is saved in the source directory.
- The executed DDL is logged for reference.