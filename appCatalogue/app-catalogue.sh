#!/bin/bash

_log_folder_=/var/log/app-catalogue

function check_rabbit_service {
    ps -aux | grep -v grep | grep rabbitmq > /dev/null
    if [ $? -ne 0 ]; then
	echo "RabbitMQ is not running. Please start it before using app-catalogue..."
	exit;
    fi
}

function check_log_folder {
    if [ ! -d $_log_folder_ ]; then
	echo "Log folder not created. Please cross-check log4j.properties and create it."
	exit;
    fi
}

function check_app_catalogue_running {
        result=$(screen -ls | grep app-catalogue | wc -l);
        if [ "${result}" -ne "0" ]; then
                echo "app-catalogue is already running.."
		exit;
        fi
}

function start {

    if [ ! -d target/  ]; then
	compile
    fi
    check_log_folder
    check_rabbit_service
    check_app_catalogue_running
    if [ 0 -eq $? ]
        then
	    screen -d -m -S app-catalogue -t selfnet java -jar "target/app-catalogue-1.0.0-SNAPSHOT.jar"
    fi
}

function stop {
    if screen -list | grep "app-catalogue"; then
	    screen -S app-catalogue -p 0 -X stuff "exit$(printf \\r)"
    fi
}

function kill_app {
    if screen -list | grep "app-catalogue"; then
	    screen -ls | grep app-catalogue | cut -d. -f1 | awk '{print $1}' | xargs kill
    fi
}


function compile {
    mvn clean package
}

function clean {
    mvn clean
}

function howto {
    echo -e "SELFNET Applications Catalogue Service\n"
    echo -e "Usage:\n\t ./app-catalogue.sh [compile|clean|start|stop|kill] \n"
}

########################
########################

if [ $# -eq 0 ]
   then
        howto
        exit 1
fi

declare -a cmds=($@)
for (( i = 0; i <  ${#cmds[*]}; ++ i ))
do
    case ${cmds[$i]} in
        "clean" )
            clean ;;
        "start" )
            start ;;
        "stop" )
            stop ;;
        "compile" )
            compile ;;
        "kill" )
            kill_app ;;
        * )
            howto
            exit
    esac
    if [[ $? -ne 0 ]]; 
    then
	    exit 1
    fi
done

