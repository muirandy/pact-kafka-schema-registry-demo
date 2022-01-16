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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "demoProviderApp", providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V3)
class ConsumerTest {

    @Pact(consumer = "demoConsumerApp", provider = "demoProviderApp")
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

    }
}
