# Using PACT with Kafka and Schema Registry

## Setup - PACT broker
This section will get you a PACT broker up and running locally, on port 9292.
You need docker and docker-compose. From the command line:
```
docker-compose up
```

## Run the consumer tests from the IDE
Note: PACT files will be found under target/pacts

## Publish the PACTs to the broker:
```
mvn pact:publish
```

## Run the provider tests from the IDE
Note: They will read the PACTs from the broker but not publish their results to it

## Run the provider tests from maven:
```
mvn verify -Dpact.verifier.publishResults=true
```
