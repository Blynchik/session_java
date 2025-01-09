package ru.service.session.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.service.session.config.client.FeignClientConfig;
import ru.service.session.dto.hero.HeroRequest;
import ru.service.session.dto.hero.HeroResponse;

@FeignClient(name = "hero", configuration = FeignClientConfig.class)
public interface HeroApi {

    @PostMapping
    ResponseEntity<HeroResponse> create(@RequestHeader(value = "Authorization") String authorizationHeader,
                                        @RequestBody HeroRequest heroRequest);

    @GetMapping
    ResponseEntity<HeroResponse> getOwn(@RequestHeader(value = "Authorization") String authorizationHeader);
}
