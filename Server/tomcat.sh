#!/bin/bash

#Install Tomcat
cd /tmp
wget http://apache.cbox.biz/tomcat/tomcat-9/v9.0.35/bin/apache-tomcat-9.0.35.tar.gz
sudo mkdir /opt/tomcat
sudo tar xzvf apache-tomcat-9*.tar.gz -C /opt/tomcat --strip-components=1
