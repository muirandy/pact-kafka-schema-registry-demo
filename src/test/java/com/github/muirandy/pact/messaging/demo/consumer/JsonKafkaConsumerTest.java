package com.github.muirandy.pact.messaging.demo.consumer;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonDeserializerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "jsonKafkaProviderApp", providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V3)
class JsonKafkaConsumerTest {

    @Pact(consumer = "jsonKafkaConsumerApp", provider = "jsonKafkaProviderApp")
    MessagePact simpleJsonPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();
        body.stringType("name", "almost-anything");

        return builder.expectsToReceive("A simple message")
                      .withMetadata(Map.of("contentType", "application/json"))
                      .withContent(body)
                      .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "simpleJsonPact", providerType = ProviderType.ASYNCH)
    void simpleMessage(List<Message> messages) {
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
        Deserializer<ConsumerDomainRecord> deserializer = getProductionKafkaDeserializer();
        return deserializer.deserialize("", kafkaBytes);
    }

    private Deserializer<ConsumerDomainRecord> getProductionKafkaDeserializer() {
        KafkaJsonDeserializer<ConsumerDomainRecord> domainRecordKafkaJsonDeserializer = new KafkaJsonDeserializer<>();
        Map<String, Object> props = Map.of(
                KafkaJsonDeserializerConfig.JSON_VALUE_TYPE, ConsumerDomainRecord.class.getName()
        );
        domainRecordKafkaJsonDeserializer.configure(props, false);
        return domainRecordKafkaJsonDeserializer;
    }
}
