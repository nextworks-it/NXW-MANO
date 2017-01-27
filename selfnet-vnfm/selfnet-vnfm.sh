#!/bin/bash

source gradle.properties

_version=${version}

function check_rabbitmq {
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
	ps -aux | grep -v grep | grep rabbitmq > /dev/null
        if [ $? -ne 0 ]; then
            echo "rabbitmq is not running, please start the service first..."
            exit;
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
	ps aux | grep -v grep | grep rabbitmq > /dev/null
        if [ $? -ne 0 ]; then
            echo "rabbitmq is not running, please start the service first..."
            exit;
        fi
    fi
}

function check_already_running {
        result=$(screen -ls | grep vnfm | wc -l);
        if [ "${result}" -ne "0" ]; then
                echo "selfnet-vnfm is already running.."
		exit;
        fi
}

function start {

    if [ ! -d build/  ]
        then
            compile
    fi

    check_rabbitmq
    check_already_running
    if [ 0 -eq $? ]
        then
	    screen -d -m -S vnfm -t selfnet java -jar "build/libs/selfnet-vnfm-$_version.jar" --spring.config.location=file:build/resources/main/application.properties
    fi
}

function stop {
    if screen -list | grep "vnfm"; then
	    screen -S vnfm -p 0 -X stuff "exit$(printf \\r)"
    fi
}

function restart {
    kill
    start
}


function kill {
    if screen -list | grep "vnfm"; then
	    screen -ls | grep vnfm | cut -d. -f1 | awk '{print $1}' | xargs kill
    fi
}


function compile {
    ./gradlew build -x test 
}

function tests {
    ./gradlew test
}

function clean {
    ./gradlew clean
}

function end {
    exit
}
function usage {
    echo -e "OpenBaton selfnet-vnfm\n"
    echo -e "Usage:\n\t ./selfnet-vnfm.sh [compile|start|stop|test|kill|clean]"
}

##
#   MAIN
##

if [ $# -eq 0 ]
   then
        usage
        exit 1
fi

declare -a cmds=($@)
for (( i = 0; i <  ${#cmds[*]}; ++ i ))
do
    case ${cmds[$i]} in
        "clean" )
            clean ;;
        "sc" )
            clean
            compile
            start ;;
        "start" )
            start ;;
        "stop" )
            stop ;;
        "restart" )
            restart ;;
        "compile" )
            compile ;;
        "kill" )
            kill ;;
        "test" )
            tests ;;
        * )
            usage
            end ;;
    esac
    if [[ $? -ne 0 ]]; 
    then
	    exit 1
    fi
done

