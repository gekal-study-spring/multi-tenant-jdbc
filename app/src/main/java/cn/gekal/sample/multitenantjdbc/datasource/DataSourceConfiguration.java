package cn.gekal.sample.multitenantjdbc.datasource;

import com.zaxxer.hikari.HikariDataSource;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfiguration {

  private static DataSource dataSource(int port) {

    var dsp = new DataSourceProperties();
    dsp.setUsername("user");
    dsp.setPassword("pw");
    dsp.setUrl("jdbc:postgresql://localhost:" + port + "/user");

    return dsp.initializeDataSourceBuilder().type(HikariDataSource.class).build();
  }

  /** テナントごとのデータソースをルーティングキー（tenantId）で引けるようにまとめる。 スキーマ・初期データの投入は migration モジュールの責務であり、ここでは行わない。 */
  @Bean
  @Primary
  DataSource multiTenantDataSource(Map<String, DataSource> dataSources) {

    var prefix = "ds";
    var map =
        dataSources.entrySet().stream()
            .filter(e -> e.getKey().startsWith(prefix))
            .collect(
                Collectors.toMap(
                    e -> (Object) Integer.parseInt(e.getKey().substring(prefix.length())),
                    e -> (Object) e.getValue()));

    var mds = new MultiTenantDataSource();
    mds.setTargetDataSources(map);

    return mds;
  }

  /** 認証用データソース。テナントを判別する前に参照する必要があるため、 ルーティング対象（ds*）には含めず独立したデータベース（db0 / 5430）を使う。 */
  @Bean
  DataSource authDataSource() {
    return dataSource(5430);
  }

  @Bean
  DataSource ds1() {
    return dataSource(5431);
  }

  @Bean
  DataSource ds2() {
    return dataSource(5432);
  }
}
