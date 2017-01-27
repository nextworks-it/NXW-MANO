# App Catalogue #

The App Catalogue is a Maven project and implements the SELFNET NFV-app and SDN-app repository.
Main functions include:

* VNFs, SDN apps and SDN ctrl apps onboarding in the form of App Packages (i.e. tar archives)
* VNFs, SDN apps and SDN ctrl apps query
* App Package query
* App Package enable/disable
* App Package offboard


## UBUNTU prerequisites ##
* **java JDK >= 7 (JAVA 8 preferred)**

  sudo apt-get install default-jdk

  sudo add-apt-repository ppa:webupd8team/java

  sudo apt-get update

  sudo apt-get install oracle-java8-installer

  sudo apt-get install oracle-java7-installer

  sudo update-alternatives --config java

  sudo echo "JAVA_HOME="/usr/lib/jvm/java-8-oracle" >> /etc/environment

  source /etc/environment

* **Install postgres**

  sudo apt-get install postgresql postgresql-contrib

* **Install gradle and maven**

  sudo apt-get install gradle maven



## Pre-requisites and Dependencies ##

* **PostgreSQL >=9.3.10**

  after installation:

  sudo -u postgres psql

  alter user postgres password 'postgres';

  \q

  createdb -h localhost -p 5432 -U postgres appcataloguedb

* **rabbitMq >=3.2.4**

  after installation:

  rabbitmqctl add_user selfnet selfnet

  rabbitmqctl set_user_tags selfnet administrator

  rabbitmqctl set_permissions -p / selfnet ".*" ".*" ".*"

* **openbaton-libs version 2.1.2 (later versions may be supported as well)**

  git clone https://github.com/openbaton/openbaton-libs

  git checkout tags/2.1.2 -b openbaton-libs-2.1.2

  echo "apply plugin: 'maven-publish'" >> build.gradle

  ./gradlew build

  ./gradlew install

  ./gradlew publish

