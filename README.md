# Using PACT with Kafka and Schema Registry

## Setup - PACT broker
This section will get you a PACT broker up and running locally, on port 9292.
You need docker and docker-compose. From the command line:
```
docker-compose up
```
The PACT broker will now be reachable at: `http://localhost:9292/`
    
## Run the consumer tests from the IDE
This will generate the PACTs, and the files will be found under target/pacts

## Publish the PACTs to the broker:
```
mvn pact:publish
```
(Take a look on `http://localhost:9292/`)

## Run the provider tests from the IDE
Note: They will read the PACTs from the broker but *not* publish their results to it

## Run the provider tests from maven:
This will publish the provider test results to the broker:
```
mvn verify -Dpact.verifier.publishResults=true
```
(note the green and red fields)

# The tests
These tests demonstrate how PACT deals with "messaging", as opposed to the more
traditional (or at least, longer supported) HTTP Request-Response pairs.

The tests in the [consumer|provider].kafka package show:
 * a Kafka message with a JSON payload/value
 * a Kafka message with a Schema Registry verified JSON payload/value
 * a normal JSON consumer against a schema registry provider (failure!)


The tests also demonstrate some shortcomings. As the messaging layer from PACT does 
not involve you actually *using* that messaging from the tests (unlike the HTTP
implementation), we can get tests to pass in strange ways:
 * We could have a consumer application listening on a websocket.
 * We could have a provider application writing to Kafka.
and we can make the tests pass. Obviously, this would never work in reality,
sad
 * 
