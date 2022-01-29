package com.github.muirandy.pact.messaging.demo.consumer.kafka;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.muirandy.pact.messaging.demo.consumer.ConsumerDomainRecord;
import com.github.muirandy.pact.messaging.demo.consumer.ProductionCode;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchemaUtils;
import io.confluent.kafka.schemaregistry.json.SpecificationVersion;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(PactConsumerTestExt.class)
@PactBroker(url = "http://localhost:9292")
@PactTestFor(providerName = "jsonSchemaKafkaProviderApp", providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V3)
class JsonSchemaKafkaConsumerTest {

    private static final String TOPIC_NAME = "myKafkaTopic";
    private static final boolean FAIL_UNKNOWN_PROPERTIES = true;
    private static final boolean USE_ONEOF_FOR_NULLABLES = true;
    private static final boolean IS_KEY = false;
    private static final String IGNORED_TOPIC = "";

    private MockSchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();

    @Pact(consumer = "jsonSchemaKafkaConsumerApp")
    MessagePact schemaJsonPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();
        body.stringType("name", "almost-anything2");

        return builder.expectsToReceive("A json schema message")
                      .withMetadata(Map.of("contentType", "application/vnd.schemaregistry.v1+json"))
                      .withContent(body)
                      .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "schemaJsonPact", providerType = ProviderType.ASYNCH)
    void jsonSchemaMessage(List<Message> messages) {
        byte[] kafkaBytes = convertToKafkaBytes(messages.get(0));

        assertDoesNotThrow(() -> {
            expectApplicationToConsumeKafkaBytesSuccessfully(kafkaBytes);
        });
    }

    private byte[] convertToKafkaBytes(Message message) {
        return message.contentsAsBytes();
    }

    private void expectApplicationToConsumeKafkaBytesSuccessfully(byte[] kafkaBytes) {
        ConsumerDomainRecord consumerDomainRecord = useProductionCodeToDeserializeKafkaBytesToDomain(kafkaBytes);

        ProductionCode productionCode = new ProductionCode();
        productionCode.handle(consumerDomainRecord);
    }

    private ConsumerDomainRecord useProductionCodeToDeserializeKafkaBytesToDomain(byte[] kafkaBytes) {
        KafkaJsonSchemaDeserializer<ConsumerDomainRecord> deserializer = getProductionKafkaDeserializer();
        return deserializer.deserialize(IGNORED_TOPIC, kafkaBytes);
    }

    private KafkaJsonSchemaDeserializer<ConsumerDomainRecord> getProductionKafkaDeserializer() {
        Map<String, Object> props = Map.of(
                KafkaJsonSchemaDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://anything",
                KafkaJsonSchemaDeserializerConfig.JSON_VALUE_TYPE, ConsumerDomainRecord.class.getName()
        );
        KafkaJsonSchemaDeserializer<ConsumerDomainRecord> deserializer = new KafkaJsonSchemaDeserializer<>(schemaRegistryClient);
        deserializer.configure(props, IS_KEY);
        tryRegisterSchemaForValue();
        return deserializer;
    }

    private void tryRegisterSchemaForValue() {
        try {
            registerSchemaForValue();
        } catch (IOException | RestClientException e) {
            e.printStackTrace();
        }
    }

    private int registerSchemaForValue() throws IOException, RestClientException {
        return schemaRegistryClient.register(TOPIC_NAME + "-value", getConsumerDomainRecordSchema());
    }

    private JsonSchema getConsumerDomainRecordSchema() throws IOException {
        return JsonSchemaUtils.getSchema(
                new ConsumerDomainRecord("name"),
                SpecificationVersion.DRAFT_7,
                USE_ONEOF_FOR_NULLABLES,
                FAIL_UNKNOWN_PROPERTIES,
                new ObjectMapper(),
                schemaRegistryClient
        );
    }
}
