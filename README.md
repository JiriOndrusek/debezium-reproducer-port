# Description

Test creates camel route using debezium embedded engine, which receives events from mongo db `test`.

`ManualTest` inserts one record into `test.companies` and expects to receive an event through `debeziun-connector-mongodb`.

## How to run successfully (using version 1.3.0.final)

* Test expects running mongodb on port **30001**. To start db run following command:

> docker run -p 30001:27017 --name mongo1  mongo mongod --replSet my-mongo-set

* Repl set has to be initialized for mongodb db:

> docker run -it --name mongo-init --rm -e REPLICASET=my-mongo-set --link mongo1:mongo1  debezium/mongo-initiator

* Authorization has to be defined:

> docker exec -it mongo1 mongo

* In console execute:

>     use admin;
>     db.createUser({ user: "debezium", pwd:"dbz", roles: [  {role: "userAdminAnyDatabase", db: "admin" }, { role: "dbAdminAnyDatabase", db: "admin" },  { role: "readWriteAnyDatabase", db:"admin" },  { role: "clusterAdmin",  db: "admin" }]});

* Run test:

> mvn clean install

**Test is successful.**

## How to fail (using version 1.4.0-SNAPSHOT and non-default port)

* Change version of `debeziun-connector-mongodb` to `1.4.0-SNAPSHOT` in pom.xml (line 14)

* Follow all steps from successful run

**Test fails.**

## How to run successfully (using version 1.4.0-SNAPSHOT and default port)

* Change version of `debeziun-connector-mongodb` to `1.4.0-SNAPSHOT` in pom.xml (line 14)

* Change value of port to default one - in ManualTest.java (uncomment line 32 and comment line 33)

* Start mongo db with default port by command:

> docker run -p 27017:27017 --name mongo1  mongo mongod --replSet my-mongo-set

* Follow other steps from successful scenario,

**Test is successful.**

 








