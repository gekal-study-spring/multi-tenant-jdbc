package cn.gekal.sample.multitenantjdbc.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.util.List;

public class MultiTenantUser extends User {

    private final Integer tenantId;

    public MultiTenantUser(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Integer tenantId) {
        super(username, PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password), enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, List.of(new SimpleGrantedAuthority("USER")));
        this.tenantId = tenantId;
    }

    public Integer getTenantId() {
        return tenantId;
    }
}
