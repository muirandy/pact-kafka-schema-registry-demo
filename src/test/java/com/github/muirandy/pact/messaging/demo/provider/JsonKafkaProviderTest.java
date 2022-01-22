package com.github.muirandy.pact.messaging.demo.provider;

import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

@Provider("jsonKafkaProviderApp")
@Consumer("jsonKafkaConsumerApp")
@PactBroker(url = "http://localhost:9292")
class JsonKafkaProviderTest {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String KEY_CONTENT_TYPE = "contentType";

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new MessageTestTarget());
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @PactVerifyProvider("A simple message")
    MessageAndMetadata verifySimpleMessageEvent() {
        Map<String, Object> metadata = Map.of(
          KEY_CONTENT_TYPE, JSON_CONTENT_TYPE
        );

        ProviderDomainRecord providerDomainRecord = new ProviderDomainRecord("name");

        KafkaJsonSerializer<ProviderDomainRecord> serializer = createProductionKafkaSerializer();

        byte[] bytes = serializer.serialize("", providerDomainRecord);

        return createPactRepresentationFor(metadata, bytes);
    }

    private KafkaJsonSerializer<ProviderDomainRecord> createProductionKafkaSerializer() {
        Map<String, Object> config = Map.of(
        );
        KafkaJsonSerializer<ProviderDomainRecord> jsonSerializer = new KafkaJsonSerializer<>();
        jsonSerializer.configure(config, false);
        return jsonSerializer;
    }

    private MessageAndMetadata createPactRepresentationFor(Map<String, Object> metadata, byte[] bytes) {
        return new MessageAndMetadata(bytes, metadata);
    }
}
