package com.github.muirandy.pact.messaging.demo.provider.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.muirandy.pact.messaging.demo.provider.ProviderDomainRecord;
import jakarta.websocket.*;
import org.glassfish.tyrus.client.ClientManager;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

public class WebsocketEndpoint {
    private ClientManager manager;
    protected Session session;

    public void connect(URI uri) {
        this.manager = ClientManager.createClient();
        try {
            Endpoint e = new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig endpointConfig) {
                    System.out.println("Opened websocket");
                    session.addMessageHandler(new MessageHandler.Whole<String>() {
                        @Override
                        @OnMessage
                        public void onMessage(String message) {
                            System.out.println("Received message from server: " + message);
                        }
                    });
                }

                @Override
                @OnClose
                public void onClose(Session session, CloseReason reason) {
                    System.out.println("Closed session: " + reason.getReasonPhrase());
                }
            };

            session = manager.connectToServer(e, uri);
        } catch (DeploymentException | IOException e) {
            System.exit(-1);
        }
    }

    public void send(ProviderDomainRecord message) {
        try {
            ByteBuffer buffer = getBytesForMessage(message);
            session.getBasicRemote().sendBinary(buffer);
        } catch (IOException e) {
            throw new UnableToSendMessageException(e);
        }
    }

    private ByteBuffer getBytesForMessage(ProviderDomainRecord message) throws JsonProcessingException {
        String json = convertToJson(message);
        ByteBuffer byteBuffer = ByteBuffer.wrap(json.getBytes());
        return byteBuffer;
    }

    private String convertToJson(ProviderDomainRecord message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(message);
    }

    private static class UnableToSendMessageException extends RuntimeException {
        public UnableToSendMessageException(IOException e) {
            super(e);
        }
    }
}
