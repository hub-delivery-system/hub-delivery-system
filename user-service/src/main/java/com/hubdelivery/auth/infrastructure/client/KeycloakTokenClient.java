package com.hubdelivery.auth.infrastructure.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.hubdelivery.auth.domain.exception.KeycloakAuthFailedException;
import com.hubdelivery.auth.domain.exception.KeycloakUnavailableException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakTokenClient {

    private final KeycloakProperties properties;
    private final RestClient restClient = RestClient.create();

    public KeycloakTokenResponse issueToken(String slackId, String password) {

        String tokenUri = properties.baseUrl()
                + "/realms/" + properties.realm()
                + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        form.add("username", slackId);
        form.add("password", password);

        try {
            KeycloakTokenResponse response = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new KeycloakUnavailableException();
            }

            return response;
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            if (status == 400 || status == 401) {
                throw new KeycloakAuthFailedException();
            }

            log.error("Keycloak token request failed. status={}, body={}", status, e.getResponseBodyAsString());
            throw new KeycloakUnavailableException();
        } catch (RestClientException e) {
            log.error("Keycloak token request failed.", e);
            throw new KeycloakUnavailableException();
        }
    }
}
