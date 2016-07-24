package com.premonition;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.premonition.web.Message;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.util.ExceptionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.http.Http;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.integration.dsl.http.Http.outboundGateway;
import static org.springframework.integration.dsl.support.Transformers.fromJson;

@SpringBootApplication
public class Application {

    private static final String SHOPPING_CART_CHANNEL = "shoppingCartChannel";
    private static final String ADD_LINE_ITEM_CHANNEL = "addLineItemChannel";
    private static final String CHECKOUT_CHANNEL = "checkoutChannel";
    private static final int THREADS = Runtime.getRuntime().availableProcessors() + 1;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    static {
        ExceptionUtils.setFail(true);
    }
    @Bean
    public MessagingGatewaySupport inboundGateway() {
        return Http.inboundGateway("/checkout")
                .requestMapping(
                        r -> r.methods(POST)
                                .consumes(APPLICATION_JSON_VALUE)
                                .produces(APPLICATION_JSON_VALUE)
                )
                .requestChannel("inputChannel")
                .get();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new Netty4ClientHttpRequestFactory());
    }

    @Bean
    public IntegrationFlow fullCheckoutFlow(Executor executor) {
        return f -> f.channel("inputChannel")
                .transform(fromJson(ShoppingCart.class))
                .enrich(e -> e.requestChannel(SHOPPING_CART_CHANNEL))
                .split(ShoppingCart.class, ShoppingCart::getLineItems)
                .channel(c -> c.executor(executor))
                .enrich(e -> e.requestChannel(ADD_LINE_ITEM_CHANNEL))
                .aggregate(aggregator -> aggregator
                        .outputProcessor(g -> g.getMessages()
                                .stream()
                                .map(m -> (LineItem) m.getPayload())
                                .map(LineItem::getName)
                                .collect(joining(", "))))
                .enrich(e -> e.requestChannel(CHECKOUT_CHANNEL))
                .<String>handle((p, h) -> Message.called("We have " + p + " line items!!"));
    }

    @Bean
    public IntegrationFlow createShoppingCart() {
        return f -> f.channel(SHOPPING_CART_CHANNEL)
                .handle(outboundGateway("http://localhost:8080/api/shopping-cart", restTemplate())
                        .httpMethod(POST)
                        .expectedResponseType(String.class));
    }

    @Bean
    public IntegrationFlow checkout() {
        return f -> f.channel(CHECKOUT_CHANNEL)
                .handle(outboundGateway("http://localhost:8080/api/checkout", restTemplate())
                        .httpMethod(POST)
                        .expectedResponseType(String.class));
    }

    @Bean
    public IntegrationFlow addLineItem() {
        return f -> f.channel(ADD_LINE_ITEM_CHANNEL)
                .handle(outboundGateway("http://localhost:8080/api/add-line-item", restTemplate())
                        .httpMethod(POST)
                        .expectedResponseType(String.class));
    }

    @Bean
    public Executor executor() {
        return newFixedThreadPool(THREADS, new ThreadFactoryBuilder()
                .setNameFormat("lineitems-pool")
                .build());
    }
}
