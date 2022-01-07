package co.zephyri.coloredrabbits.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    private final Logger log = LoggerFactory.getLogger(Receiver.class);

    public void receiveMessage(String message) {
        log.info("Received <" + message + ">");
    }
}