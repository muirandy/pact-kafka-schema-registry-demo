package com.github.muirandy.pact.messaging.demo.consumer;

public class ProductionCode {
    public void handle(ConsumerDomainRecord consumerDomainRecord) {
        System.out.println(consumerDomainRecord);
    }
}
