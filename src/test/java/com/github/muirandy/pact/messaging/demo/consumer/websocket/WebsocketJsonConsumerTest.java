package com.github.muirandy.pact.messaging.demo.consumer.websocket;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "websocketJsonProviderApp", providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V3)
class WebsocketJsonConsumerTest {
    @Pact(consumer = "websocketJsonConsumerApp", provider = "websocketJsonProviderApp")
    MessagePact websocketJsonConsumerPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();
        body.stringType("name", "websocket-json-almost-anything");

        return builder.expectsToReceive("A json message over a websocket")
                      .withMetadata(Map.of("contentType", "application/json"))
                      .withContent(body)
                      .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "websocketJsonConsumerPact", providerType = ProviderType.ASYNCH)
    void websocketJsonConsumerTest(List<Message> messages) {
        byte[] websocketBytes = getBytesFromPactMessage(messages.get(0));

        assertProductionAppConsumesBytesWithoutIssue(websocketBytes);
    }

    private byte[] getBytesFromPactMessage(Message message) {
        return message.contentsAsBytes();
    }

    private void assertProductionAppConsumesBytesWithoutIssue(byte[] websocketBytes) {
        callYourProductionCodeHereRatherThanWhatThisIs(websocketBytes);
    }

    private void callYourProductionCodeHereRatherThanWhatThisIs(byte[] websocketBytes) {
        assertTrue(websocketBytes.length > 0);
    }
}
