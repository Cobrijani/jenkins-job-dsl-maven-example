#!/bin/sh

FILE=${workspace}/src/main/docker/deploy.yml


if [ -f "$FILE" ]; then
    echo "$FILE exists, performing deployment"
    docker-compose -f $FILE stop
    docker-compose -f $FILE rm -f 
    docker-compose up -d --force-recreate
    echo "Complete finished"
fi


