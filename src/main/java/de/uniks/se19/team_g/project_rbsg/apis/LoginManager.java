package de.uniks.se19.team_g.project_rbsg.apis;

import de.uniks.se19.team_g.project_rbsg.model.User;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * @author Keanu Stückrad
 */
@Component
public class LoginManager {

    final String BASE_REST_URL = "https://rbsg.uniks.de/api";

    private RestTemplate restTemplate;

    public LoginManager(@NonNull final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CompletableFuture onLogin(@NonNull final User user){
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> restTemplate.postForObject(BASE_REST_URL + "/user/login", user, HashMap.class));
    }

}
