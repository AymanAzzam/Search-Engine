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
hint: you maybe need to change tomcat verison inside tomcat.sh

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
## Helpful Links
1. [Identifying the stem of a word](https://subscription.packtpub.com/book/big_data_and_business_intelligence/9781789801156/1/ch01lvl1sec16/identifying-the-stem-of-a-word).
2. [Download and install jsoup](https://jsoup.org/download).
3. [run Java main from Maven](http://www.vineetmanohar.com/2009/11/3-ways-to-run-java-main-from-maven/).
4. [Building Java Projects with Maven](https://spring.io/guides/gs/maven/#:~:text=Build%20Java%20code,the%20local%20Maven%20dependency%20repository).
5. [BLOB/TEXT column 'message_id' used in key specification without a key length](https://stackoverflow.com/questions/1827063/mysql-error-key-specification-without-a-key-length).
6. [Index column size too large. The maximum column size is 767 bytes](https://stackoverflow.com/questions/42043205/how-to-fix-mysql-index-column-size-too-large-laravel-migrate/52778785#52778785).
7. [Error: java.lang.ClassNotFoundException: com.mysql.jdbc.Driver](https://www.java67.com/2015/07/javalangclassnotfoundexception-com.mysql.jdbc.Driver-solution.html).
8. [MySQL Error: : 'Access denied for user 'root'@'localhost'](https://stackoverflow.com/questions/41645309/mysql-error-access-denied-for-user-rootlocalhost).
9. [mariadb.service: Failed with result 'timeout'](https://stackoverflow.com/questions/40997257/mysql-service-fails-to-start-hangs-up-timeout-ubuntu-mariadb).
