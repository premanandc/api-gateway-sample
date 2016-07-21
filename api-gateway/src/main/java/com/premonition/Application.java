package com.premonition;

import com.premonition.web.Message;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.http.Http;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.client.RestTemplate;

import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.integration.dsl.http.Http.outboundGateway;
import static org.springframework.integration.dsl.support.Transformers.fromJson;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public MessagingGatewaySupport inboundGateway() {
        return Http.inboundGateway("/checkout")
                .requestMapping(
                        r -> r.methods(POST)
                                .consumes(APPLICATION_JSON_VALUE)
                                .produces(APPLICATION_JSON_VALUE)
                )
                .requestChannel(inputChannel())
                .get();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new Netty4ClientHttpRequestFactory());
    }

    @Bean
    public MessageChannel inputChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public IntegrationFlow fullCheckoutFlow() {
        return f -> f.channel(inputChannel())
                .transform(fromJson(ShoppingCart.class))
                .enrich(e -> e.requestChannel(shoppingCartChannel()))
                .split(ShoppingCart.class, ShoppingCart::getLineItems)
                .enrich(e -> e.requestChannel(addLineItemChannel()))
                .aggregate(aggregator -> aggregator
                        .outputProcessor(g -> g.getMessages()
                                .stream()
                                .map(m -> (LineItem) m.getPayload())
                                .map(LineItem::getName)
                                .collect(joining(", "))))
                .enrich(e -> e.requestChannel(checkoutChannel()))
                .<String>handle((p, h) -> Message.called("We have " + p + " line items!!"));
    }

    @Bean
    public MessageChannel checkoutChannel() {
        return MessageChannels.direct("checkout").get();
    }

    @Bean
    public MessageChannel shoppingCartChannel() {
        return MessageChannels.direct("shopping-cart").get();
    }

    @Bean
    public MessageChannel addLineItemChannel() {
        return MessageChannels.direct("addLineItem").get();
    }

    @Bean
    public IntegrationFlow createShoppingCart() {
        return f -> f.channel(shoppingCartChannel())
                .handle(outboundGateway("http://localhost:8080/api/shopping-cart", restTemplate())
                        .httpMethod(POST)
                        .expectedResponseType(String.class));
    }

    @Bean
    public IntegrationFlow addLineItem() {
        return f -> f.channel(addLineItemChannel())
                .handle(outboundGateway("http://localhost:8080/api/add-line-item", restTemplate())
                        .httpMethod(POST)
                        .expectedResponseType(String.class));
    }

    @Bean
    public IntegrationFlow checkout() {
        return f -> f.channel(checkoutChannel())
                .handle(outboundGateway("http://localhost:8080/api/checkout", restTemplate())
                        .httpMethod(POST)
                        .expectedResponseType(String.class));
    }
}
