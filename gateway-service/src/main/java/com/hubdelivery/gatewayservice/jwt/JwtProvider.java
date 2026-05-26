package com.hubdelivery.gatewayservice.jwt;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

	// TODO: Keycloak 시스템 역할 목록 - Keycloak 버전이나 realm 설정에 따라 추가될 수 있음
	private static final Set<String> SYSTEM_ROLES = Set.of("offline_access", "uma_authorization");
	// TODO: Docker Compose에서 realm 이름 확정 후 prefix가 맞는지 확인 (default-roles-{realm명} 형식)
	private static final String DEFAULT_ROLES_PREFIX = "default-roles-";

	public String extractUserId(Jwt jwt) {
		return jwt.getSubject();
	}

	public String extractRole(Jwt jwt) {
		Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
		if (realmAccess == null) {
			return null;
		}

		Object rolesObj = realmAccess.get("roles");
		if (!(rolesObj instanceof List<?> rawRoles)) {
			return null;
		}

		return rawRoles.stream()
			.filter(r -> r instanceof String)
			.map(r -> (String) r)
			.filter(r -> !SYSTEM_ROLES.contains(r))
			.filter(r -> !r.startsWith(DEFAULT_ROLES_PREFIX))
			.findFirst()
			.orElse(null);
	}
}
