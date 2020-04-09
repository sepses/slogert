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

## == process transformation to OTTR format

java -jar target/slogert-1.2.1-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/ftp-config.yaml -t src/test/resources/std-template.yaml
java -jar target/slogert-1.2.1-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/auth-config.yaml -t src/test/resources/std-template.yaml
#java -jar target/slogert-1.2.1-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/kern-config.yaml -t src/test/resources/std-template.yaml
#java -jar target/slogert-1.2.1-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/sys-config.yaml -t src/test/resources/std-template.yaml
#java -jar target/slogert-1.2.1-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/apache-access-config.yaml -t src/test/resources/std-template.yaml
#java -jar target/slogert-1.2.1-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/apache-error-config.yaml -t src/test/resources/std-template.yaml
#java -jar target/slogert-1.2.1-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/apache-host-config.yaml -t src/test/resources/std-template.yaml
#java -jar target/slogert-1.2.1-SNAPSHOT-jar-with-dependencies.jar -c src/test/resources/audit-config.yaml -t src/test/resources/std-template.yaml


## == transform OTTR to Turtle

start=$SECONDS
java -jar exe/lutra.jar --library scenario/output/vsftpd-log_templates.ottr --libraryFormat stottr --inputFormat stottr scenario/output/vsftpd.log_structured.ottr --mode expand --fetchMissing > scenario/output-ttl/vsftpd.log_structured.ttl
duration=$(( SECONDS - start ))
getDuration "ftp" $duration
#
start=$SECONDS
java -jar exe/lutra.jar --library scenario/output/auth-log_templates.ottr --libraryFormat stottr --inputFormat stottr scenario/output/auth.log_structured.ottr --mode expand --fetchMissing > scenario/output-ttl/auth.log_structured.ttl
duration=$(( SECONDS - start ))
getDuration "authlog" $duration
#
#start=$SECONDS
#java -jar exe/lutra.jar --library scenario/output/apache-access-log_templates.ottr --libraryFormat stottr --inputFormat stottr scenario/output/apache-access.log_structured.ottr --mode expand --fetchMissing > scenario/output-ttl/apache-access.log_structured.ttl
#duration=$(( SECONDS - start ))
#getDuration "apache-access" $duration
#
#start=$SECONDS
#java -jar exe/lutra.jar --library scenario/output/kern-log_templates.ottr --libraryFormat stottr --inputFormat stottr scenario/output/kern.log_structured.ottr --mode expand --fetchMissing > scenario/output-ttl/kern.log_structured.ttl
#duration=$(( SECONDS - start ))
#getDuration "kern" $duration
#
#start=$SECONDS
#java -jar exe/lutra.jar --library scenario/output/sys-log_templates.ottr --libraryFormat stottr --inputFormat stottr scenario/output/sys.log_structured.ottr --mode expand --fetchMissing > scenario/output-ttl/sys.log_structured.ttl
#duration=$(( SECONDS - start ))
#getDuration "syslog" $duration
#
#start=$SECONDS
#java -jar exe/lutra.jar --library scenario/output/apache-host-log_templates.ottr --libraryFormat stottr --inputFormat stottr scenario/output/apache-host.log_structured.ottr --mode expand --fetchMissing > scenario/output-ttl/apache-host.log_structured.ttl
#duration=$(( SECONDS - start ))
#getDuration "apache-host" $duration
#
#start=$SECONDS
#java -jar exe/lutra.jar --library scenario/output/apache-error-log_templates.ottr --libraryFormat stottr --inputFormat stottr scenario/output/apache-error.log_structured.ottr --mode expand --fetchMissing > scenario/output-ttl/apache-error.log_structured.ttl
#duration=$(( SECONDS - start ))
#getDuration "apache-error" $duration
#
#start=$SECONDS
#java -jar exe/lutra.jar --library scenario/output/audit-log_templates.ottr --libraryFormat stottr --inputFormat stottr scenario/output/audit.log_structured.ottr --mode expand --fetchMissing > scenario/output-ttl/audit.log_structured.ttl
#duration=$(( SECONDS - start ))
#getDuration "auditlog" $duration

