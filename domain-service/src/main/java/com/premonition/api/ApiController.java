package com.premonition.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    @RequestMapping(value = "/api/{name}", method = POST)
    public ResponseEntity<Message> message(@PathVariable String name) {
        final ResponseEntity<Message> response = new ResponseEntity<>(Message.called("hello " + name), HttpStatus.OK);
        log.info("Invoking for " + name);
        return response;
    }

    private static class Message {
        private final String value;

        public static Message called(String value) {
             return new Message(value);
        }

        private Message(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
