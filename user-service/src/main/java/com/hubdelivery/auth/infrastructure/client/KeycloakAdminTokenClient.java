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
public class KeycloakAdminTokenClient {

    private final KeycloakProperties keycloakProperties;
    private final KeycloakAdminProperties keycloakAdminProperties;
    private final RestClient restClient = RestClient.create();

    public String issueAdminAccessToken() {
        if (!keycloakAdminProperties.isCredentialConfigured()) {
            log.error("Keycloak admin credentials are not configured.");
            throw new KeycloakUnavailableException();
        }

        String tokenUri = keycloakProperties.baseUrl()
                + "/realms/" + keycloakAdminProperties.resolvedRealm()
                + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", keycloakAdminProperties.resolvedClientId());
        if (keycloakAdminProperties.hasClientSecret()) {
            form.add("client_secret", keycloakAdminProperties.clientSecret());
        }
        form.add("username", keycloakAdminProperties.username());
        form.add("password", keycloakAdminProperties.password());

        try {
            KeycloakAdminTokenResponse response = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KeycloakAdminTokenResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new KeycloakUnavailableException();
            }

            return response.accessToken();
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            if (status == 400 || status == 401) {
                throw new KeycloakAuthFailedException();
            }

            log.error("Keycloak admin token request failed. status={}, body={}", status, e.getResponseBodyAsString());
            throw new KeycloakUnavailableException();
        } catch (RestClientException e) {
            log.error("Keycloak admin token request failed.", e);
            throw new KeycloakUnavailableException();
        }
    }
}
