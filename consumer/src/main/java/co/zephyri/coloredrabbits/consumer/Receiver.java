package co.zephyri.coloredrabbits.consumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.zephyri.coloredrabbits.producer.Message;

@Component
public class Receiver {

    private final Logger log = LoggerFactory.getLogger(Receiver.class);
    private final Map<String,Integer> counts = new ConcurrentHashMap<>();

    public void receiveMessage(Message msg) throws InterruptedException {
        log.info("Received <" + msg.text() + ">");
        counts.compute(msg.batchId(), (k, v) -> v == null ? 1 : v+1);
        if (msg.sleep() > 0)
            Thread.sleep(msg.sleep());
    }


    public Map<String,Integer> receivedCounts() {
        return Map.copyOf(counts);
    }
}