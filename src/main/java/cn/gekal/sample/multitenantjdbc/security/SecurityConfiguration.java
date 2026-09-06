package cn.gekal.sample.multitenantjdbc.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class SecurityConfiguration {

    private static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    /** ユーザーの元データ。パスワードは一度だけエンコードして保持する。 */
    private record UserSource(String username, String encodedPassword, Integer tenantId) {

        /**
         * ルックアップごとに新しいインスタンスを生成する。
         * 認証成功後に {@code ProviderManager} が principal の credentials を消去する（password が null になる）ため、
         * 同一インスタンスを使い回すと 2 回目以降の認証で
         * 「There is no PasswordEncoder mapped for the id "null"」が発生する。
         */
        MultiTenantUser toUser() {
            return new MultiTenantUser(username, encodedPassword, true, true, true, true, tenantId);
        }
    }

    private static UserSource createUserSource(String name, Integer tenantId) {

        return new UserSource(name, PASSWORD_ENCODER.encode("pw"), tenantId);
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

        Map<String, UserSource> sources = Stream.of(createUserSource("user11", 1), createUserSource("user21", 2))
                .collect(Collectors.toMap(UserSource::username, source -> source));

        return username -> {
            var source = sources.get(username);
            if (source == null) {
                throw new UsernameNotFoundException("couldn't find " + username + "!;");
            }
            return source.toUser();
        };
    }
}
