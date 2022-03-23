package cn.gekal.sample.multitenantjdbc.security;

import cn.gekal.sample.multitenantjdbc.security.MultiTenantUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class SecurityConfiguration {

    private static User createUser(String name, Integer tenantId) {

        return new MultiTenantUser(name, "pw", true, true, true, true, tenantId);
    }

    @Bean
    SecurityFilterChain fileChain(HttpSecurity http) throws Exception {

        http.httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {

        var user11 = createUser("user11", 1);
        var user21 = createUser("user21", 2);

        var users = Stream.of(user11, user21)
                .collect(Collectors.toMap(User::getUsername, u -> u));

        return username -> {
            var user = users.getOrDefault(username, null);
            if (user == null) {
                throw new UsernameNotFoundException("couldn't find " + username + "!;");
            }
            return user;
        };
    }
}
