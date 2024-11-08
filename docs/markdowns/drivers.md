#### drivers
This command can list drivers that are listed in a `drivers.yaml` file and by choosing a driver you can download it to the `ROSETTA_DRIVERS` directory which will be automatically ready to use.

    rosetta drivers [-h, --help] [-f, --file] [--list] <indexToDownload> [-dl, --download]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-f, --file DRIVERS_FILE | YAML drivers file path.  If none is supplied it will use drivers.yaml in the current directory and then fallback to our default one.
--list | Used to list all available drivers.
-dl, --download | Used to download selected driver by index.
indexToDownload | Chooses which driver to download depending on the index of the driver.


***Example*** (drivers.yaml)

```yaml
- name: MySQL 8.0.30
  link: https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-8.0.30.zip
- name: Postgresql 42.3.7
  link: https://jdbc.postgresql.org/download/postgresql-42.3.7.jar
```