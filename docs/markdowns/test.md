## Data Validation / Data Quality

#### test
This command runs tests for columns using assertions. Then they are translated into query commands, executed, and compared with an expected value. Currently supported assertions are: `equals(=), not equals(!=), less than(<), more than(>), less than or equals(<=), more than or equals(>=), contains(in), is null, is not null, like, between`. Examples are shown below:

    rosetta [-c, --config CONFIG_FILE] test [-h, --help] [-s, --source CONNECTION_NAME]

    rosetta [-c, --config CONFIG_FILE] test [-h, --help] [-s, --source CONNECTION_NAME] [-t, --target CONNECTION_NAME]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection is used to specify which models and connections to use.
-t, --target CONNECTION_NAME (Optional) | The target connection is used to specify the target connection to use for testing the data. The source tests needs to match the values from the tarrget connection.

**Note:** Value for BigQuery Array columns should be comma separated value ('a,b,c,d,e').

Example:
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

When running the tests against a target connection, you don't have to specify the expected value.

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
```

If you need to overwrite the test column query (e.x. for Geospatial data), you can use `columnDef`.
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
      - name: "wkt"
        typeName: "GEOMETRY"
        ordinalPosition: 0
        primaryKeySequenceId: 0
        columnDisplaySize: 1000000000
        scale: 0
        precision: 1000000000
        columnProperties: []
        nullable: true
        primaryKey: false
        autoincrement: false
        tests:
          assertion:
            - operator: '>'
              value: 434747
              expected: 4
              columnDef: 'ST_AREA(wkt, 1)'
```

Output example:
```bash
Running tests for mysql. Found: 2

1 of 2, RUNNING test ('=') on column: 'actor_id'                                                    
1 of 2, FINISHED test on column: 'actor_id' (expected: '1' - actual: '1')  ......................... [PASS in 0.288s]
2 of 2, RUNNING test ('!=') on column: 'first_name'                                                 
2 of 2, FINISHED test on column: 'first_name' (expected: '1' - actual: '219')  ..................... [FAIL in 0.091s]
```