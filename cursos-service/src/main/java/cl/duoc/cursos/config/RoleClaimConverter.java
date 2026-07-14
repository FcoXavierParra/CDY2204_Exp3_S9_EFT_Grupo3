package cl.duoc.cursos.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Convierte el custom claim de rol del JWT de Azure AD B2C en autoridades de Spring.
 * Ej.: extension_rol = "instructor" -> ROLE_INSTRUCTOR ; "estudiante" -> ROLE_ESTUDIANTE.
 * Soporta valor String o lista, y el prefijo extension_ de B2C.
 */
public class RoleClaimConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String roleClaim;

    public RoleClaimConverter(String roleClaim) {
        this.roleClaim = roleClaim;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Object raw = resolveClaimValue(jwt);
        if (raw instanceof String valor) {
            addRole(authorities, valor);
        } else if (raw instanceof Collection<?> valores) {
            for (Object v : valores) {
                if (v != null) addRole(authorities, v.toString());
            }
        }
        return authorities;
    }

    private Object resolveClaimValue(Jwt jwt) {
        if (jwt.hasClaim(roleClaim)) {
            return jwt.getClaim(roleClaim);
        }
        for (String clave : jwt.getClaims().keySet()) {
            String lower = clave.toLowerCase();
            if (lower.startsWith("extension_") && (lower.endsWith("rol") || lower.endsWith("role"))) {
                return jwt.getClaim(clave);
            }
        }
        return null;
    }

    private void addRole(List<GrantedAuthority> authorities, String valor) {
        if (valor == null || valor.isBlank()) return;
        authorities.add(new SimpleGrantedAuthority("ROLE_" + valor.trim().toUpperCase()));
    }
}
