package ru.service.session.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.service.session.config.client.FeignClientConfig;
import ru.service.session.dto.event.internal.EventResponse;
import ru.service.session.dto.event.request.EventRequest;


@FeignClient(name = "event", configuration = FeignClientConfig.class)
public interface EventApi {
    @GetMapping("/random")
    @CircuitBreaker(name = "defaultCircuitBreaker")
    ResponseEntity<EventResponse> getRandom(@RequestHeader(value = "Authorization") String authorizationHeader);

    @PostMapping("/admin")
    @CircuitBreaker(name = "defaultCircuitBreaker")
    ResponseEntity<EventResponse> create(@RequestHeader(value = "Authorization") String authorizationHeader,
                                         @RequestBody EventRequest eventRequest);
}
