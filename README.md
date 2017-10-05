# WebTree
File tree on Apache Tomcat server.
Page, displaying file tree structure.

## Installing / Getting started

* Download [.war file]
* Download [Apache Tomcat Server 8.5]
* Download [PostgreSQL]
* Download [treeDB.backup] (DB with trigger function)
> .sql file to create database will be added by user request, now you can only create DB from .backup file.

Add role antmog (pw:root) to PostgreSQL.
Add ../PostgreSQL/pg93/bin to path or execute in that folder:

```shell
psql dbname < infile
```
Where infile is the file output by the pg_dump command (treeDB.backup). The database dbname will not be created by this command, 
so you must create it yourself from template0 before executing psql (e.g., with createdb -T template0 dbname). 
psql supports options similar to pg_dump for specifying the database server to connect to and the user name to use. 
See the psql reference page for more information.

More: https://www.postgresql.org/docs/9.1/static/backup-dump.html#BACKUP-DUMP-RESTORE

Then:
* Make sure PostgreSQL is running and you have access to databse
* Launch Apache Tomcat Server with default options
* Put .war file to ../Apache/webapps (file name is web-tree-1_1 by default)

Now you can access app at URL: http://localhost:8080/web-tree-1_1/

## Developing

To get project:

```shell
git clone https://github.com/antmog/WebTree.git
cd WebTree/
(make sure that you have maven/maven IDE plugin installed)
maven clean
maven install
```
To get database/chose server read Installing / Getting started.

## Features
* Selected items have specified color
* Lazy load:2sec delay on expand (w/o freezing main tree)
* User can add/edit/delete tree nodes
* DND (Drag and drop) allowed
* Data attached to database

## Notes

* CSS may look ugly cause of platform issues...

## Contributing

maven,java,js,css,jsp,PostgreSQL(+trigger),tomcat,jQuery(jsTree) used

## About 

Application view:

![view](https://pp.userapi.com/c841326/v841326236/25c9d/lMKU_AT1kZU.jpg)

[//]: #
[.war file]: <https://vk.com/doc1577215_451542032?hash=576d4d57a0bedd97f4&dl=91d29e617cde77571d>
[PostgreSQL]:  <https://www.postgresql.org/download>
[Apache Tomcat Server 8.5]: <https://tomcat.apache.org/download-80.cgi>
[treeDB.backup]: <https://vk.com/doc1577215_451540539?hash=c066326372120b7872&dl=4cd1695c8b2321fca8>
