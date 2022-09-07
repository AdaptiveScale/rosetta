# Read version
read_version() {
   APP_VERSION=$(awk '/version =/ { print $3 }' build.gradle | tr -d "'")
}
