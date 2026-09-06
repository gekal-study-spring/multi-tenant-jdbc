package cn.gekal.sample.multitenantjdbc.datasource;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DataSource の接続設定。application.yaml の {@code app.datasource.*} を束ねる。
 *
 * <ul>
 *   <li>{@code auth} … 認証DB（テナント判別前に参照する）
 *   <li>{@code tenants} … tenantId をキーにしたテナントDB
 * </ul>
 *
 * username / password は全接続で共通なので Target には持たせない。
 */
@ConfigurationProperties("app.datasource")
public record MultiTenantDataSourceProperties(
    String username, String password, Target auth, Map<Integer, Target> tenants) {

  /** 1つの接続先。 */
  public record Target(String url) {}
}
