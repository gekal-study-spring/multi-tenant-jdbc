package cn.gekal.sample.multitenantjdbc.datasource;

import com.zaxxer.hikari.HikariDataSource;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(MultiTenantDataSourceProperties.class)
public class DataSourceConfiguration {

  private final MultiTenantDataSourceProperties properties;

  DataSourceConfiguration(MultiTenantDataSourceProperties properties) {
    this.properties = properties;
  }

  private DataSource dataSource(MultiTenantDataSourceProperties.Target target) {
    return DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .url(target.url())
        .username(properties.username())
        .password(properties.password())
        .build();
  }

  /** 認証用データソース。テナントを判別する前に参照するため、ルーティング対象には含めない。 */
  @Bean
  DataSource authDataSource() {
    return dataSource(properties.auth());
  }

  /** tenantId をルーティングキーに、テナントごとのデータソースへ切り替える。 */
  @Bean
  @Primary
  DataSource multiTenantDataSource() {
    var targets =
        properties.tenants().entrySet().stream()
            .collect(
                Collectors.toMap(e -> (Object) e.getKey(), e -> (Object) dataSource(e.getValue())));

    var mds = new MultiTenantDataSource();
    mds.setTargetDataSources(targets);
    return mds;
  }
}
