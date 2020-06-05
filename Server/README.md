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
3. Make mysql accessible by root'@'localhost' and make VARCHAR(200) doesn't give error by adding the following lines at the end of /etc/mysql/my.cnf.
```sh
[mysqld]
skip-grant-tables

innodb_default_row_format=dynamic
innodb_file_format=barracuda
innodb_file_per_table=true
innodb_large_prefix=true
```
4. Start Tomcat and Mysql using the following commands.
```sh
sudo systemctl start tomcat
sudo systemctl start mysql
```
4. Create database 'Search_Engine' using mysql.
```sh
mysql -u root
create database Search_Engine;
exit
```

## Run Server (on AWS Cloud9)
1. Install and compile files using the following commands.
```sh
sudo mvn install 
```
2. Copy the files to Tomcat using the following commands.
```sh
sudo cp -r target/classes/* /opt/tomcat/webapps/ROOT/WEB-INF/classes/
sudo cp web.xml /opt/tomcat/webapps/ROOT/WEB-INF/
sudo cp stopwords.txt /opt/tomcat/work/Catalina/localhost/
```
3. Run the following Command to start The Crawler and Indexer.
```sh
mvn exec:java -Dexec.mainClass="com.crawler.Main"
```
