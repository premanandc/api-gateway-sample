package com.premonition.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class MyController {

    private final AsyncRestTemplate restTemplate;

    @Autowired
    public MyController(AsyncRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/api", method = GET)
    public ListenableFuture<ResponseEntity<Message>> get() {
        return restTemplate.getForEntity("http://localhost:8080/api", Message.class);
    }

}


