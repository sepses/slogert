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
time java -jar target/slogert-0.7.0-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/ftp-config.yaml
time java -jar exe/lutra.jar --library scenario/output/vsftpd-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/vsftpd.ottr --mode expand --fetchMissing > scenario/output/vsftpd.ttl
duration=$(( SECONDS - start ))
getDuration "ftp" $duration

start=$SECONDS
time java -jar target/slogert-0.7.0-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/auth-config.yaml
time java -jar exe/lutra.jar --library scenario/output/auth-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/auth.ottr --mode expand --fetchMissing > scenario/output/auth.ttl
duration=$(( SECONDS - start ))
getDuration "authlog" $duration

start=$SECONDS
time java -jar target/slogert-0.7.0-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/kern-config.yaml
time java -jar exe/lutra.jar --library scenario/output/kern-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/kern.ottr --mode expand --fetchMissing > scenario/output/kern.ttl
duration=$(( SECONDS - start ))
getDuration "kern" $duration

start=$SECONDS
time java -jar target/slogert-0.7.0-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/sys-config.yaml
time java -jar exe/lutra.jar --library scenario/output/sys-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/sys.ottr --mode expand --fetchMissing > scenario/output/sys.ttl
duration=$(( SECONDS - start ))
getDuration "syslog" $duration

start=$SECONDS
time java -jar target/slogert-0.7.0-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/apache-error-config.yaml
time java -jar exe/lutra.jar --library scenario/output/apache-error-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/apache-error.ottr --mode expand --fetchMissing > scenario/output/apache-error.ttl
duration=$(( SECONDS - start ))
getDuration "apache-error" $duration

start=$SECONDS
time java -jar target/slogert-0.7.0-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/apache-host-config.yaml
time java -jar exe/lutra.jar --library scenario/output/apache-host-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/apache-host.ottr --mode expand --fetchMissing > scenario/output/apache-host.ttl
duration=$(( SECONDS - start ))
getDuration "apache-host" $duration

start=$SECONDS
time java -jar target/slogert-0.7.0-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/apache-access-config.yaml
time java -jar exe/lutra.jar --library scenario/output/apache-access-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/apache-access.ottr --mode expand --fetchMissing > scenario/output/apache-access.ttl
duration=$(( SECONDS - start ))
getDuration "apache-access" $duration

start=$SECONDS
time java -jar target/slogert-0.7.0-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/audit-config.yaml
time java -jar exe/lutra.jar --library scenario/output/audit-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/audit-0.ottr --mode expand --fetchMissing > scenario/output/audit-0.ttl
time java -jar exe/lutra.jar --library scenario/output/audit-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/audit-1.ottr --mode expand --fetchMissing > scenario/output/audit-1.ttl
time java -jar exe/lutra.jar --library scenario/output/audit-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/audit-2.ottr --mode expand --fetchMissing > scenario/output/audit-2.ttl
time java -jar exe/lutra.jar --library scenario/output/audit-base.ottr --libraryFormat stottr --inputFormat stottr scenario/output/audit-3.ottr --mode expand --fetchMissing > scenario/output/audit-3.ttl
duration=$(( SECONDS - start ))
getDuration "auditlog" $duration

