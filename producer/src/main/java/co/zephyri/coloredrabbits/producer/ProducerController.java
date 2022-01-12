package co.zephyri.coloredrabbits.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;



@RestController
public class ProducerController {

    private final Logger log = LoggerFactory.getLogger(ProducerController.class);
    private final RabbitTemplate rabbitTemplate;
    private final WebClient webClient;

    public ProducerController(RabbitTemplate rabbitTemplate, WebClient.Builder builder) {
        this.rabbitTemplate = rabbitTemplate;
        this.webClient = builder.baseUrl("http://consumer-green:8080").build();
    }


    @PostMapping("/messages")
    public ResponseEntity<String> sendMessages(@RequestBody Messages messages) {
        for (int i=0; i<messages.count; i++) {
            rabbitTemplate.convertAndSend("colored-rabbits-exchange", "carrot.yummy", messages.text);
        }

        return ResponseEntity.accepted().body("");
    }


    @PostMapping("/green")
    public ResponseEntity<String> setPercentGreen(@RequestBody PercentGreen body) {
        log.info(body.toString());

        // Send request to "green".
        webClient
            .post()
            .uri("/percent-in-service")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        return ResponseEntity.accepted().body("");
    }


    record Messages(String text, int count) {}
    record PercentGreen(int percent) {}
}