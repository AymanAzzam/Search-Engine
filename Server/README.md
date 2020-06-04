## Server Prerequisites (on AWS Cloud9)
1. Install Maven.
```sh
sudo apt-get install maven -y
```
2. Install Tomcat.
Run the script tomcat.sh using the following command.
```sh
./tomcat.sh
```
Then follow Step 5 in the Following link https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-8-on-ubuntu-16-04

## Run Server (on AWS Cloud9)
1. Install and compile files using the following commands.
```sh
sudo mvn install 
```
2. Copy the files to Tomcat using the following commands.
```sh
cp -r target/classes/* /opt/tomcat/webapps/ROOT/WEB-INF/classes/
cp web.xml /opt/tomcat/webapps/ROOT/WEB-INF/
cp stopwords.txt /opt/tomcat/work/Catalina/localhost/
```