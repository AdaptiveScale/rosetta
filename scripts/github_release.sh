#! /bin/bash

source $(dirname "$0")/version.sh

# Set default version to develop
APP_VERSION=develop

# Trigger read version
read_version
echo "Version: $APP_VERSION"

create_release() {
    user="AdaptiveScale"
    repo="rosetta"
    token=$TOKEN
    tag="v$APP_VERSION"

    command="curl -s -o release.json -w '%{http_code}' \
         --request POST \
         --header 'Accept: application/vnd.github.v3+json' \
         --header 'Authorization: token ${token}' \
         --header 'content-type: application/json' \
         --data '{\"tag_name\": \"${tag}\", \"name\": \"${tag}\", \"body\":\"Release ${tag}\"}' \
         https://api.github.com/repos/$user/$repo/releases"
    http_code=`eval $command`
    if [ $http_code == "201" ]; then
        echo "created release:"
        cat release.json
    else
        echo "create release failed with code '$http_code':"
        cat release.json
        echo "command:"
        echo $command
        return 1
    fi
}

upload_custom_release_file() {
    token=$TOKEN
    name=$1
    file="binary/build/$name"

    url=`jq -r .upload_url release.json | cut -d{ -f'1'`
    command="\
      curl -s -o upload.json -w '%{http_code}' \
           --request POST \
           --header 'Accept: application/vnd.github.v3+json' \
           --header 'Authorization: token ${token}' \
           --header 'Content-Type: application/octet-stream' \
           --data-binary @\"${file}\"
           ${url}?name=${name}"
    http_code=`eval $command`
    if [ $http_code == "201" ]; then
        echo "asset $name uploaded:"
        jq -r .browser_download_url upload.json
    else
        echo "upload failed with code '$http_code':"
        cat upload.json
        echo "command:"
        echo $command
        return 1
    fi
}

create_release
upload_custom_release_file "rosetta-0.0.1-linux-x64.zip"
upload_custom_release_file "rosetta-0.0.1-mac_aarch64.zip"
upload_custom_release_file "rosetta-0.0.1-mac_x64.zip"
upload_custom_release_file "rosetta-0.0.1-win_x64.zip"
