package com.thehecklers.source;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.PollableBean;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SpringBootApplication
public class SourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SourceApplication.class, args);
    }

}

/*
@EnableBinding(Source.class)
@EnableScheduling
@RequiredArgsConstructor
class PositionFeed {
    @NonNull
    private final Source source;

    private final WebClient client = WebClient.create("http://localhost:7634/aircraft");
    private final List<Aircraft> acList = new ArrayList<>();

    @Scheduled(fixedRate = 1000)
    void sendPositions() {
        acList.addAll(client.get()
                .retrieve()
                .bodyToFlux(Aircraft.class)
                .filter(ac -> !ac.getReg().isEmpty())
                .toStream()
                .collect(Collectors.toList()));

        acList.forEach(ac -> {
            System.out.println(ac);
            source.output().send(MessageBuilder.withPayload(ac).build());
        });

        acList.clear();
    }
}
*/

@Configuration
class PositionFeed {
    private final WebClient client = WebClient.create("http://localhost:7634/aircraft");

    /*
        @Bean
        Supplier<List<Aircraft>> sendPositions() {
            return () -> {
                List<Aircraft> acList = client.get()
                        .retrieve()
                        .bodyToFlux(Aircraft.class)
                        .filter(ac -> !ac.getReg().isEmpty())
                        .toStream()
                        .collect(Collectors.toList());

                acList.forEach(System.out::println);

                return acList;
            };
        }
    */
    @PollableBean
    Supplier<Flux<Aircraft>> sendPositions() {
        return () -> client.get()
                .retrieve()
                .bodyToFlux(Aircraft.class)
                .filter(ac -> !ac.getReg().isEmpty())
                .log();
    }
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Aircraft {
    private String callsign, reg, flightno, type;
    private int altitude, heading, speed;
    private double lat, lon;
}