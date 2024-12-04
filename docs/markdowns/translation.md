## Using External Translator

RosettaDB allows users to use their own translator. For the supported databases you can extend or create your version
of translation CSV file. To use an external translator you need to set the `EXTERNAL_TRANSLATION_FILE` ENV variable
to point to the external file.

Set the ENV variable `EXTERNAL_TRANSLATION_FILE` to point to the location of your custom translator CSV file.

```
export EXTERNAL_TRANSLATION_FILE=<path_to_csv_translator>
```

example:

```
export EXTERNAL_TRANSLATION_FILE=/Users/adaptivescale/translation.csv
```

Make sure you keep the same format as the CSV example given above.

### Translation Attributes

Rosetta uses an additional file to maintain translation specific attributes.
It stores translation_id, the attribute_name and attribute_value:

```
1;;302;;columnDisplaySize;;38
2;;404;;columnDisplaySize;;30
3;;434;;columnDisplaySize;;17
```

The supported attribute names are:
- ordinalPosition
- autoincrement
- nullable
- primaryKey
- primaryKeySequenceId
- columnDisplaySize
- scale
- precision

Set the ENV variable `EXTERNAL_TRANSLATION_ATTRIBUTE_FILE` to point to the location of your custom translation attribute CSV file.

```
export EXTERNAL_TRANSLATION_ATTRIBUTE_FILE=<path_to_csv_translator>
```

example:

```
export EXTERNAL_TRANSLATION_ATTRIBUTE_FILE=/Users/adaptivescale/translation_attributes.csv
```

Make sure you keep the same format as the CSV example given above.

### Indices (Index)

Indices are supported in Google Cloud Spanner. An example on how they are represented in model.yaml

```
tables:
- name: "ExampleTable"
  type: "TABLE"
  schema: ""
  indices:
  - name: "PRIMARY_KEY"
    schema: ""
    tableName: "ExampleTable"
    columnNames:
    - "Id"
    - "UserId"
    nonUnique: false
    indexQualifier: ""
    type: 1
    ascOrDesc: "A"
    cardinality: -1
  - name: "IDX_ExampleTable_AddressId_299189FB00FDAFA5"
    schema: ""
    tableName: "ExampleTable"
    columnNames:
    - "AddressId"
    nonUnique: true
    indexQualifier: ""
    type: 2
    ascOrDesc: "A"
    cardinality: -1
  - name: "TestIndex"
    schema: ""
    tableName: "ExampleTable"
    columnNames:
    - "ClientId"
    - "DisplayName"
    nonUnique: true
    indexQualifier: ""
    type: 2
    ascOrDesc: "A"
    cardinality: -1
```
