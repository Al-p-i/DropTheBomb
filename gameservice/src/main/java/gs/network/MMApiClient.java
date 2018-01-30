package gs.network;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MMApiClient {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MMApiClient.class);
    private static final String URI = "http://localhost:8080/matchmaker";
    private RestTemplate rest;

    public MMApiClient() {
        this.rest = new RestTemplate();
    }

    public String playerLeave() {
        String uri = URI + "/leave";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri);
        ResponseEntity<String> response = rest.exchange(
                builder.build().encode().toUri(),
                HttpMethod.DELETE,
                null,
                String.class);
        logger.info("Create leave request");
        return response.getBody();
    }
}
