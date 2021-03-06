## File Standalone
* Start kafka cluster using landoop docker-compose
* Go inside docker image
  ```
  docker exec -it <container-id> bash
  ```
* Create the topic with 3 partitions
  ```
  kafka-topics --create --topic demo-file-standalone --partitions 3 --replication-factor 1 --zookeeper 127.0.0.1:2181
  ```
* Run connect-standalone <worker-properties> <connectors-properties 1 or many>
  ```
  connect-standalone worker-standalone.properties file-stream-standalone.properties
  ```
  
## File Distributed
* Start kafka cluster using landoop docker-compose
* Go inside docker image
  ```
  docker exec -it <container-id> bash
  ```
* Create the topic with 3 partitions
  ```
  kafka-topics --create --topic demo-file-distributed --partitions 3 --replication-factor 1 --zookeeper 127.0.0.1:2181
  ```
* Use Landoop UI to create connector dynamically. It will be default to distributed mode. you dont have to provide any 
  worker properties. Provide JSON key and value. 

* To confirm that the topics data are indeed in json format run
  ```
  kafka-console-consumer --topic demo-file-distributed --from-beginning --bootstrap-server 127.0.0.1:9092
  ```
## Twitter 
* Start kafka cluster using landoop docker-compose
* Go inside docker image
  ```
  docker exec -it <container-id> bash
  ```
* Create the topic with 3 partitions
  ```
  kafka-topics --create --topic demo-twitter-distributed --partitions 3 --replication-factor 1 --zookeeper 127.0.0.1:2181
  ```
* Download twitter connector from here and put it in volume - https://www.confluent.io/hub/jcustenborder/kafka-connect-twitter
  Note - Details about how to put more connector in Landoop - https://github.com/lensesio/fast-data-dev#enable-additional-connectors

* To confirm that the topics data are indeed in json format run
  ```
  kafka-console-consumer --topic demo-twitter-distributed --from-beginning --bootstrap-server 127.0.0.1:9092
  ```
* https://github.com/Eneco/kafka-connect-twitter although included in Landoop is not working. 

* Use https://github.com/jcustenborder/kafka-connect-twitter/releases/tag/0.3.33 . Build locally and put jar in volume.
