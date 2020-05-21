#!/usr/bin/env bash

## == transformation duration

function getDuration() {
    if (( $2 > 3600 )) ; then
        let "hours=$2/3600"
        let "minutes=($2%3600)/60"
        let "seconds=($2%3600)%60"
        echo "$1 completed in $hours hour(s), $minutes minute(s) and $seconds second(s)"
    elif (( duration > 60 )) ; then
        let "minutes=($2%3600)/60"
        let "seconds=($2%3600)%60"
        echo "$1 completed in $minutes minute(s) and $seconds second(s)"
    else
        echo "$1 completed in $2 seconds"
    fi
}

start=$SECONDS
java -jar target/slogert-0.6.0-jar-with-dependencies.jar -c src/test/resources/ftp-config.yaml
java -jar exe/lutra.jar --library scenario/config-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/vsftpd.log_structured.ottr --mode expand --fetchMissing > scenario/output/vsftpd.ttl
duration=$(( SECONDS - start ))
getDuration "ftp" $duration

start=$SECONDS
java -jar target/slogert-0.6.0-jar-with-dependencies.jar -c src/test/resources/auth-config.yaml
java -jar exe/lutra.jar --library scenario/config-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/auth.log_structured.ottr --mode expand --fetchMissing > scenario/output/auth.ttl
duration=$(( SECONDS - start ))
getDuration "authlog" $duration

start=$SECONDS
java -jar target/slogert-0.6.0-jar-with-dependencies.jar -c src/test/resources/kern-config.yaml
java -jar exe/lutra.jar --library scenario/config-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/kern.log_structured.ottr --mode expand --fetchMissing > scenario/output/kern.ttl
duration=$(( SECONDS - start ))
getDuration "kern" $duration

start=$SECONDS
java -jar target/slogert-0.6.0-jar-with-dependencies.jar -c src/test/resources/sys-config.yaml
java -jar exe/lutra.jar --library scenario/config-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/sys.log_structured.ottr --mode expand --fetchMissing > scenario/output/sys.ttl
duration=$(( SECONDS - start ))
getDuration "syslog" $duration

start=$SECONDS
java -jar target/slogert-0.6.0-jar-with-dependencies.jar -c src/test/resources/apache-error-config.yaml
java -jar exe/lutra.jar --library scenario/config-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/apache-error.log_structured.ottr --mode expand --fetchMissing > scenario/output/apache-error.ttl
duration=$(( SECONDS - start ))
getDuration "apache-error" $duration

start=$SECONDS
java -jar target/slogert-0.6.0-jar-with-dependencies.jar -c src/test/resources/apache-host-config.yaml
java -jar exe/lutra.jar --library scenario/config-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/apache-host.log_structured.ottr --mode expand --fetchMissing > scenario/output/apache-host.ttl
duration=$(( SECONDS - start ))
getDuration "apache-host" $duration

start=$SECONDS
java -jar target/slogert-0.6.0-jar-with-dependencies.jar -c src/test/resources/apache-access-config.yaml
java -jar exe/lutra.jar --library scenario/config-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/apache-access.log_structured.ottr --mode expand --fetchMissing > scenario/output/apache-access.ttl
duration=$(( SECONDS - start ))
getDuration "apache-access" $duration

start=$SECONDS
java -jar target/slogert-0.6.0-jar-with-dependencies.jar -c src/test/resources/audit-config.yaml
java -jar exe/lutra.jar --library scenario/config-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/audit.log_structured.ottr --mode expand --fetchMissing > scenario/output/audit.ttl
duration=$(( SECONDS - start ))
getDuration "auditlog" $duration

