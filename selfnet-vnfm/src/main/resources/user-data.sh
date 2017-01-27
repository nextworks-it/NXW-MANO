#!/bin/bash

export MONITORING_IP=
export TIMEZONE=
export BROKER_IP=
export BROKER_PORT=
export USERNAME=
export PASSWORD=
export EXCHANGE_NAME=
export EMS_HEARTBEAT=
export EMS_AUTODELETE=
export EMS_VERSION=
export ENDPOINT=
export MGMT_NET_GW="10.10.10.1"

ip route delete default
ip route add default via $MGMT_NET_GW

mkdir -p /etc/openbaton/ems
echo [ems] > /etc/openbaton/ems/conf.ini
echo broker_ip=$BROKER_IP >> /etc/openbaton/ems/conf.ini
echo broker_port=$BROKER_PORT >> /etc/openbaton/ems/conf.ini
echo username=$USERNAME >> /etc/openbaton/ems/conf.ini
echo password=$PASSWORD >> /etc/openbaton/ems/conf.ini
echo exchange=$EXCHANGE_NAME >> /etc/openbaton/ems/conf.ini
echo heartbeat=$EMS_HEARTBEAT >> /etc/openbaton/ems/conf.ini
echo autodelete=$EMS_AUTODELETE >> /etc/openbaton/ems/conf.ini
export hn=`hostname`
echo type=$ENDPOINT >> /etc/openbaton/ems/conf.ini
echo hostname=$hn >> /etc/openbaton/ems/conf.ini
service ems restart
