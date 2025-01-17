#!/bin/sh

FILE=src/main/docker/deploy.yml


if [ -f "$FILE" ]; then
    echo "$FILE exists, performing deployment"
    docker-compose -f $FILE stop
    docker-compose -f $FILE rm -f 
    docker-compose -f $FILE up -d --force-recreate
    echo "Complete finished"
else
    echo "Deployment yaml not found"
fi


