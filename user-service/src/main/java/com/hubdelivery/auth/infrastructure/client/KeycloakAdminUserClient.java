package com.hubdelivery.auth.infrastructure.client;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.hubdelivery.auth.domain.exception.KeycloakUnavailableException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAdminUserClient {

    private static final String MASTER_REALM = "master";

    private final KeycloakProperties keycloakProperties;
    private final RestClient restClient = RestClient.create();

    public Optional<String> findUserIdByUsername(String adminAccessToken, String username) {
        String uri = UriComponentsBuilder.fromHttpUrl(usersUri())
                .queryParam("username", username)
                .queryParam("exact", true)
                .build()
                .toUriString();

        try {
            List<KeycloakUserRepresentation> users = restClient.get()
                    .uri(uri)
                    .headers(headers -> headers.setBearerAuth(adminAccessToken))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<KeycloakUserRepresentation>>() {
                    });

            if (users == null) {
                return Optional.empty();
            }

            return users.stream()
                    .filter(user -> username.equals(user.username()))
                    .map(KeycloakUserRepresentation::id)
                    .filter(StringUtils::hasText)
                    .findFirst();
        } catch (RestClientResponseException e) {
            log.error("Failed to query Keycloak user by username. status={}, body={}",
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new KeycloakUnavailableException();
        } catch (RestClientException e) {
            log.error("Failed to query Keycloak user by username.", e);
            throw new KeycloakUnavailableException();
        }
    }

    public String createUser(String adminAccessToken, String username) {
        try {
            ResponseEntity<Void> response = restClient.post()
                    .uri(usersUri())
                    .headers(headers -> headers.setBearerAuth(adminAccessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new KeycloakCreateUserRequest(username, true))
                    .retrieve()
                    .toBodilessEntity();

            String userIdFromLocation = extractUserId(response.getHeaders().getLocation());
            if (StringUtils.hasText(userIdFromLocation)) {
                return userIdFromLocation;
            }

            return findUserIdByUsername(adminAccessToken, username)
                    .orElseThrow(KeycloakUnavailableException::new);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 409) {
                return findUserIdByUsername(adminAccessToken, username)
                        .orElseThrow(KeycloakUnavailableException::new);
            }

            log.error("Failed to create Keycloak user. status={}, body={}",
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new KeycloakUnavailableException();
        } catch (RestClientException e) {
            log.error("Failed to create Keycloak user.", e);
            throw new KeycloakUnavailableException();
        }
    }

    public void enableUser(String adminAccessToken, String userId) {
        try {
            restClient.put()
                    .uri(usersUri() + "/" + userId)
                    .headers(headers -> headers.setBearerAuth(adminAccessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new KeycloakEnableUserRequest(true))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.error("Failed to enable Keycloak user. userId={}, status={}, body={}",
                    userId, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new KeycloakUnavailableException();
        } catch (RestClientException e) {
            log.error("Failed to enable Keycloak user. userId={}", userId, e);
            throw new KeycloakUnavailableException();
        }
    }

    public void setPassword(String adminAccessToken, String userId, String rawPassword) {
        try {
            restClient.put()
                    .uri(usersUri() + "/" + userId + "/reset-password")
                    .headers(headers -> headers.setBearerAuth(adminAccessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new KeycloakPasswordResetRequest("password", rawPassword, false))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.error("Failed to reset Keycloak password. userId={}, status={}, body={}",
                    userId, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new KeycloakUnavailableException();
        } catch (RestClientException e) {
            log.error("Failed to reset Keycloak password. userId={}", userId, e);
            throw new KeycloakUnavailableException();
        }
    }

    public void assignRealmRole(String adminAccessToken, String userId, String roleName) {
        KeycloakRealmRoleRepresentation role = getRealmRole(adminAccessToken, roleName);

        try {
            restClient.post()
                    .uri(usersUri() + "/" + userId + "/role-mappings/realm")
                    .headers(headers -> headers.setBearerAuth(adminAccessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(role))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.error("Failed to assign Keycloak realm role. userId={}, role={}, status={}, body={}",
                    userId, roleName, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new KeycloakUnavailableException();
        } catch (RestClientException e) {
            log.error("Failed to assign Keycloak realm role. userId={}, role={}", userId, roleName, e);
            throw new KeycloakUnavailableException();
        }
    }

    private KeycloakRealmRoleRepresentation getRealmRole(String adminAccessToken, String roleName) {
        String uri = UriComponentsBuilder.fromHttpUrl(realmRolesUri())
                .pathSegment(roleName)
                .build()
                .toUriString();

        try {
            KeycloakRealmRoleRepresentation role = restClient.get()
                    .uri(uri)
                    .headers(headers -> headers.setBearerAuth(adminAccessToken))
                    .retrieve()
                    .body(KeycloakRealmRoleRepresentation.class);

            if (role == null || !StringUtils.hasText(role.name())) {
                throw new KeycloakUnavailableException();
            }
            return role;
        } catch (RestClientResponseException e) {
            log.error("Failed to query Keycloak realm role. role={}, status={}, body={}",
                    roleName, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new KeycloakUnavailableException();
        } catch (RestClientException e) {
            log.error("Failed to query Keycloak realm role. role={}", roleName, e);
            throw new KeycloakUnavailableException();
        }
    }

    private String usersUri() {
        return keycloakProperties.baseUrl() + "/admin/realms/" + realm() + "/users";
    }

    private String realmRolesUri() {
        return keycloakProperties.baseUrl() + "/admin/realms/" + realm() + "/roles";
    }

    private String realm() {
        return StringUtils.hasText(keycloakProperties.realm()) ? keycloakProperties.realm() : MASTER_REALM;
    }

    private String extractUserId(URI location) {
        if (location == null) {
            return null;
        }

        String path = location.getPath();
        if (!StringUtils.hasText(path)) {
            return null;
        }

        int idx = path.lastIndexOf('/');
        if (idx == -1 || idx == path.length() - 1) {
            return null;
        }
        return path.substring(idx + 1);
    }

    private record KeycloakUserRepresentation(
            String id,
            String username
    ) {
    }

    private record KeycloakCreateUserRequest(
            String username,
            Boolean enabled
    ) {
    }

    private record KeycloakEnableUserRequest(
            Boolean enabled
    ) {
    }

    private record KeycloakPasswordResetRequest(
            String type,
            String value,
            Boolean temporary
    ) {
    }

    private record KeycloakRealmRoleRepresentation(
            String id,
            String name
    ) {
    }
}
