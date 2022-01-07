package co.zephyri.coloredrabbits.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class ProducerController {

    private final Logger log = LoggerFactory.getLogger(ProducerController.class);
    private final RabbitTemplate rabbitTemplate;

    public ProducerController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/send/{message}")
    public ResponseEntity<String> sendMessage(@PathVariable String message) {
        log.info("Preparing to send <" + message + ">");
        rabbitTemplate.convertAndSend("colored-rabbits-exchange", "carrot.yummy", message);
        return ResponseEntity.accepted().body("");
    }
}