package co.zephyri.coloredrabbits.producer;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
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
    private final Random random;

    public ProducerController(RabbitTemplate rabbitTemplate, WebClient.Builder builder) {
        this.rabbitTemplate = rabbitTemplate;
        this.webClient = WebClient.create();
        this.random = new Random();
    }


    @PostMapping("/messages")
    public ResponseEntity<String> sendMessages(@RequestBody Messages messages) {
        if (messages.sleepPercent < 0 || messages.sleepPercent > 100) {
            return ResponseEntity.unprocessableEntity().body("sleepPercent must be between 0 and 100 inclusive.");
        }

        var batchId = ZonedDateTime.now().toString();
        for (int i=0; i<messages.count(); i++) {
            // Add the sleep time to the message apprxomiately sleepPercent percent of the time as determined randomly.
            var shouldSleep = random.nextInt(100) < messages.sleepPercent();
            var msg = new Message(batchId, messages.text(), shouldSleep ? messages.sleep() : 0);
            rabbitTemplate.convertAndSend("colored-rabbits-exchange", "carrot.yummy", msg);
        }

        return ResponseEntity.accepted().body("");
    }


    @PostMapping("/green")
    public ResponseEntity<String> setPercentGreen(@RequestBody PercentInService green) {
        log.info(green.toString());

        // Send request to "green".
        webClient
            .post()
            .uri("http://consumer-green/percent-in-service")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(green)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        // Send request to "blue".
        var blue = new PercentInService(100 - green.percent());
        webClient
            .post()
            .uri("http://consumer-blue/percent-in-service")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(blue)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        return ResponseEntity.accepted().body("");
    }


    @GetMapping("/results")
    public ResponseEntity<List<BatchResult>> getResults() {
        // Get results from "blue"
        Map<String,Integer> blueCounts = webClient
                                            .get()
                                            .uri("http://consumer-blue/received-counts")
                                            .retrieve()
                                            .bodyToMono(new ParameterizedTypeReference<Map<String,Integer>>() {})
                                            .block();
        // Get results from "green"
        Map<String,Integer> greenCounts = webClient
                                            .get()
                                            .uri("http://consumer-green/received-counts")
                                            .retrieve()
                                            .bodyToMono(new ParameterizedTypeReference<Map<String,Integer>>() {})
                                            .block();

        // Combine the two
        var results = Stream
                        .concat(blueCounts.keySet().stream(), greenCounts.keySet().stream())
                        .distinct()
                        .map(batchId -> {
                            var blue = blueCounts.get(batchId) != null ? blueCounts.get(batchId) : 0;
                            var green = greenCounts.get(batchId) != null ? greenCounts.get(batchId) : 0;
                            var total = blue + green;
                            return new BatchResult(batchId, blue, blue*100/total, green, green*100/total);
                        })
                        .sorted((r1, r2) -> r1.batchId().compareTo(r2.batchId()))
                        .toList();

        return ResponseEntity.ok().body(results);
    }


    record Messages(String text, int count, int sleep, int sleepPercent) {}
    record PercentInService(int percent) {}
    record BatchResult(String batchId, int blueCount, int bluePercent, int greenCount, int greenPercent) {}
}