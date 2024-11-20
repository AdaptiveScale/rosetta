## Compare the local model with the state of the Database

#### diff
The diff command shows the differences between the current local model and the state of the database. This can help identify any tables that have been added or removed, or columns that have been modified in the database schema. It’s a valuable tool for tracking schema changes and maintaining consistency between development and production environments.

    rosetta [-c, --config CONFIG_FILE] diff [-h, --help] [-s, --source CONNECTION_NAME]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection is used to specify which models and connection to use.
-m, --model MODEL_FILE (Optional) | The model file to use for apply. Default is `model.yaml`


##### Example Output:
When there are differences between the local model and the targeted database schema, diff provides a detailed report, highlighting table and column changes. Below is a sample output from the `diff` command:
```
There are changes between local model and targeted source
Table Changed: Table 'actor' columns changed
Column Changed: Column 'actor_id' in table 'actor' changed 'Precision'. New value: '1', old value: '5'
Column Changed: Column 'actor_id' in table 'actor' changed 'Autoincrement'. New value: 'true', old value: 'false'
Column Changed: Column 'actor_id' in table 'actor' changed 'Primary key'. New value: 'false', old value: 'true'
Column Changed: Column 'actor_id' in table 'actor' changed 'Nullable'. New value: 'true', old value: 'false'
Table Added: Table 'address'
```
##### Example Command:
To use the `diff` command with the default configuration file and model file, you might run:
    
    rosetta -s source_db_connection

**In this example:**
1. The command compares the `source_db_connection` schema with the specified local model.
2. Differences are displayed, such as table and column changes.

##### Additional Notes
- **Usage of `--model`**: When using a specific model file other than `model.yaml`, specify it with the `--model` parameter.
- **Table and Column Change Detection**: The output categorizes schema differences into table changes, column modifications, and new or removed tables.
- **Precision in Changes**: Each change specifies old and new values, helping identify unintended modifications or updates needed in the target database.