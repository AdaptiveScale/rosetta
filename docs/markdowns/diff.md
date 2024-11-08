#### diff
Show the difference between the local model and the database. Check if any table is removed, or added or if any columns have changed.

    rosetta [-c, --config CONFIG_FILE] diff [-h, --help] [-s, --source CONNECTION_NAME]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection is used to specify which models and connection to use.
-m, --model MODEL_FILE (Optional) | The model file to use for apply. Default is `model.yaml`


Example:
```
There are changes between local model and targeted source
Table Changed: Table 'actor' columns changed
Column Changed: Column 'actor_id' in table 'actor' changed 'Precision'. New value: '1', old value: '5'
Column Changed: Column 'actor_id' in table 'actor' changed 'Autoincrement'. New value: 'true', old value: 'false'
Column Changed: Column 'actor_id' in table 'actor' changed 'Primary key'. New value: 'false', old value: 'true'
Column Changed: Column 'actor_id' in table 'actor' changed 'Nullable'. New value: 'true', old value: 'false'
Table Added: Table 'address'
```
