## Kafka using CLI

Open 2 terminals for producer and consumer by running the following command

```
docker exec -it broker bash
```

#### Produce Messages

```
kafka-console-producer --broker-list localhost:9092 --topic test-topic
```

#### Consume Messages

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic
```

## Kafka using AVRO Records

#### Produce AVRO Messages

In another terminal run the following:

```
docker exec -it schema-registry bash
```

- Run the **kafka-avro-console-producer** with the Schema

```
kafka-avro-console-producer --broker-list broker:29092 --topic greetings --property value.schema='{"type": "record","name":"Greeting","fields": [{"name": "greeting","type": "string"}]}'
```

- Publish the **Greeting** message

```
{"greeting": "Good Morning!, AVRO"}
```

```
{"greeting": "Good Evening!, AVRO"}
```

```
{"greeting": "Good Night!, AVRO"}
```

### Consume AVRO Messages

```
docker exec -it schema-registry bash

```

- Run the kafka-avro-console-consumer

```
kafka-avro-console-consumer --bootstrap-server broker:29092 --topic greetings --from-beginning
```
