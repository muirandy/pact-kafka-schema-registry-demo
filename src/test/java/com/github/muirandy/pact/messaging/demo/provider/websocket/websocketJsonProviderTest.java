package com.github.muirandy.pact.messaging.demo.provider.websocket;

import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import com.github.muirandy.pact.messaging.demo.provider.ProviderDomainRecord;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Provider("websocketJsonProviderApp")
@Consumer("websocketJsonConsumerApp")
@PactBroker(url = "http://localhost:9292")
class websocketJsonProviderTest {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String KEY_CONTENT_TYPE = "contentType";

    private RemoteEndpoint.Basic basicRemote;

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new MessageTestTarget());
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @PactVerifyProvider("A json message over a websocket")
    MessageAndMetadata verifySimpleWebsocketMessageEvent() {
        ProviderDomainRecord providerDomainRecord = new ProviderDomainRecord("name");

        byte[] websocketBytes = callProductionCode(providerDomainRecord);

        return createPactRepresentationFor(websocketBytes);
    }

    private byte[] callProductionCode(ProviderDomainRecord providerDomainRecord) {
        WebsocketEndpoint websocketEndpoint = new WebsocketEndpoint();
        websocketEndpoint.session = configureMockSession();

        websocketEndpoint.send(providerDomainRecord);

        return tryGetBytesBeingSentOnWebsocket();
    }

    private MessageAndMetadata createPactRepresentationFor(byte[] bytes) {
        Map<String, Object> metadata = Map.of(
                KEY_CONTENT_TYPE, JSON_CONTENT_TYPE
        );
        return new MessageAndMetadata(bytes, metadata);
    }

    private Session configureMockSession() {
        Session session = Mockito.mock(Session.class);
        basicRemote = Mockito.mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basicRemote);

        return session;
    }

    private byte[] tryGetBytesBeingSentOnWebsocket() {
        try {
            return getBytesBeingSentOnWebsocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }

    private byte[] getBytesBeingSentOnWebsocket() throws IOException {
        ByteBuffer byteBuffer = captureBytesBeingSent();
        return obtainBytesFromBuffer(byteBuffer);
    }

    private byte[] obtainBytesFromBuffer(ByteBuffer byteBuffer) {
        byte[] arr = new byte[byteBuffer.remaining()];
        byteBuffer.get(arr);
        return arr;
    }

    private ByteBuffer captureBytesBeingSent() throws IOException {
        ArgumentCaptor<ByteBuffer> argument = ArgumentCaptor.forClass(ByteBuffer.class);
        verify(basicRemote).sendBinary(argument.capture());
        return argument.getValue();
    }
}
