package cn.gekal.sample.multitenantjdbc.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * 認証用データベースの users テーブルからユーザーを読み込む {@link UserDetailsService}。
 * <p>
 * ルックアップごとに新しい {@link MultiTenantUser} を生成するため、認証成功後の
 * credentials 消去が後続リクエストに影響しない。
 * <p>
 * {@link JdbcTemplate} は Bean 化せずここで生成する。Bean 化すると Spring Boot の
 * {@code JdbcTemplateAutoConfiguration} が無効化され、業務用の {@link JdbcTemplate}
 * （ルーティングデータソース）が作られなくなるため。
 */
@Service
public class JdbcUserDetailsService implements UserDetailsService {

    private static final String LOAD_USER_SQL = """
            select username, password, enabled, tenant_id
            from users
            where username = ?
            """;

    private final JdbcTemplate authJdbcTemplate;

    public JdbcUserDetailsService(@Qualifier("authDataSource") DataSource authDataSource) {

        this.authJdbcTemplate = new JdbcTemplate(authDataSource);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        try {
            return authJdbcTemplate.queryForObject(LOAD_USER_SQL, (rs, rowNum) -> new MultiTenantUser(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getBoolean("enabled"),
                    true,
                    true,
                    true,
                    rs.getObject("tenant_id", Integer.class)), username);
        } catch (EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("couldn't find " + username + "!;", e);
        }
    }
}
