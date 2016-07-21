package com.premonition.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);
    private final Tracer tracer;

    @Autowired
    public ApiController(Tracer tracer) {
        this.tracer = tracer;
    }


    @RequestMapping(value = "/api/{name}", method = POST)
    public ResponseEntity<Message> message(@PathVariable String name, HttpServletRequest request) {
        return new ResponseEntity<>(Message.called("hello " + name), HttpStatus.OK);
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
