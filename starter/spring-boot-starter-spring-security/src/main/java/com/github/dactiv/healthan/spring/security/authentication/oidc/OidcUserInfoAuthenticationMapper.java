package com.github.dactiv.healthan.spring.security.authentication.oidc;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.token.SimpleAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;

import java.security.Principal;
import java.util.*;
import java.util.function.Function;

/**
 * oauth 用户信息映射
 *
 * @author maurice.chen
 */
public class OidcUserInfoAuthenticationMapper implements Function<OidcUserInfoAuthenticationContext, OidcUserInfo> {

    private final List<OidcUserInfoAuthenticationResolver> oidcUserInfoAuthenticationResolvers;

    public OidcUserInfoAuthenticationMapper(List<OidcUserInfoAuthenticationResolver> systemUserResolvers) {
        this.oidcUserInfoAuthenticationResolvers = systemUserResolvers;
    }

    @Override
    public OidcUserInfo apply(OidcUserInfoAuthenticationContext oidcUserInfoAuthenticationContext) {
        OAuth2Authorization oAuth2Authorization = oidcUserInfoAuthenticationContext.getAuthorization();

        Map<String, Object> claims = new LinkedHashMap<>();

        Object principal = oAuth2Authorization.getAttributes().get(Principal.class.getName());

        if (Objects.nonNull(principal) && principal instanceof SimpleAuthenticationToken) {
            SimpleAuthenticationToken authenticationToken = Casts.cast(principal);

            SecurityPrincipal userDetails = Casts.cast(authenticationToken.getPrincipal());

            Optional<OidcUserInfoAuthenticationResolver> optional = oidcUserInfoAuthenticationResolvers
                    .stream()
                    .filter(s -> s.isSupport(userDetails.getType()))
                    .findFirst();

            if (optional.isPresent()) {
                return optional.get().mappingOidcUserInfoClaims(oAuth2Authorization, claims, userDetails);
            }

        }

        return new OidcUserInfo(claims);
    }
}
