package co.zephyri.coloredrabbits.consumer;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class ConsumerController {

    private final Logger log = LoggerFactory.getLogger(ConsumerController.class);
    private final SimpleMessageListenerContainer container;
    private final Receiver receiver;

    private int maxConsumers = 10;

    public ConsumerController(SimpleMessageListenerContainer container, Receiver receiver) {
        this.container = container;
        this.receiver = receiver;
    }


    @PostMapping("/percent-in-service")
    public ResponseEntity<String> setPercentInService(@RequestBody PercentInService body) {
        log.info(body.toString());

        int consumerCount = maxConsumers * body.percent / 100;
        if (consumerCount > 0) {
            log.info("Setting consumer count to {}", consumerCount);
            container.setConcurrentConsumers(consumerCount);

            if (!container.isRunning()) {
                log.info("Starting container");
                container.start();
            }
        } else {
            log.info("Should stop container");

            if (container.isRunning()) {
                log.info("Stopping container");
                container.stop();
            }
        }

        return ResponseEntity.accepted().body("");
    }


    @GetMapping("/received-counts")
    public ResponseEntity<Map<String,Integer>> getReceivedCounts() {
        return ResponseEntity.ok().body(receiver.receivedCounts());
    }


    record PercentInService(int percent) {}
}