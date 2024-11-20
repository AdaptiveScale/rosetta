## Validate connections

#### validate
This command validates the configuration and tests if rosetta can connect to the configured source.

    rosetta [-c, --config CONFIG_FILE] validate [-h, --help] [-s, --source CONNECTION_NAME]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection name to extract schema from.