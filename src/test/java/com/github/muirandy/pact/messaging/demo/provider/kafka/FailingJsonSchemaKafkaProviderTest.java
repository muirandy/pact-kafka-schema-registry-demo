package com.github.muirandy.pact.messaging.demo.provider.kafka;

import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import com.github.muirandy.pact.messaging.demo.provider.ProviderDomainRecord;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

@Provider("failingJsonSchemaKafkaProviderApp")
@Consumer("failingJsonKafkaConsumerApp")
@PactBroker(url = "http://localhost:9292")
class FailingJsonSchemaKafkaProviderTest {

    private static final String SCHEMA_REGISTRY_JSON_CONTENT_TYPE = "application/vnd.schemaregistry.v1+json";
    private static final String KEY_CONTENT_TYPE = "contentType";
    private static final String DO_NOT_USE_SCHEMA_REGISTRY = "mock://anything";
    private static final boolean IS_KEY = false;

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new MessageTestTarget());
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    /*
     * This test is intended to fail.
     * The consumer (which defines the PACT) expects a JSON message.
     * This producer will produce a Kafka Schema Registry Compliant JSON message.
     * Whats the difference? 5 "magic" bytes, which schema registry messages have before the JSON begins.
     */
    @PactVerifyProvider("A simple json message but will fail")
    MessageAndMetadata verifyJsonSchemaMessageEvent() {
        Map<String, Object> metadata = Map.of(
          KEY_CONTENT_TYPE, SCHEMA_REGISTRY_JSON_CONTENT_TYPE
        );

        ProviderDomainRecord providerDomainRecord = new ProviderDomainRecord("name");

        KafkaJsonSchemaSerializer<ProviderDomainRecord> serializer = createProductionKafkaSerializer();

        byte[] bytes = serializer.serialize("", providerDomainRecord);

        return createPactRepresentationFor(metadata, bytes);
    }

    private KafkaJsonSchemaSerializer<ProviderDomainRecord> createProductionKafkaSerializer() {
        Map<String, Object> config = Map.of(
                KafkaJsonSchemaSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, DO_NOT_USE_SCHEMA_REGISTRY
        );
        SchemaRegistryClient client = new MockSchemaRegistryClient();
        KafkaJsonSchemaSerializer<ProviderDomainRecord> jsonSerializer = new KafkaJsonSchemaSerializer<>(client);
        jsonSerializer.configure(config, IS_KEY);
        return jsonSerializer;
    }

    private MessageAndMetadata createPactRepresentationFor(Map<String, Object> metadata, byte[] bytes) {
        return new MessageAndMetadata(bytes, metadata);
    }
}
