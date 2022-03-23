package cn.gekal.sample.multitenantjdbc.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class DataSourceConfiguration {

    /** LOGGER */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfiguration.class);

    private static DataSource dataSource(int port) {

        var dsp = new DataSourceProperties();
        dsp.setUsername("user");
        dsp.setPassword("pw");
        dsp.setUrl("jdbc:postgresql://localhost:" + port + "/user");

        return dsp.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    DataSource multiTenantDataSource(Map<String, DataSource> dataSources) {

        var prefix = "ds";
        var map = dataSources.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .collect(Collectors.toMap(
                        e -> (Object) Integer.parseInt(e.getKey().substring(prefix.length())),
                        e -> (Object) e.getValue()));

        map.forEach((tenantId, ds) -> {
            var initializer = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"),
                    new ClassPathResource(prefix + tenantId + "-data.sql"));
            initializer.execute((DataSource) ds);
            LOGGER.info("initialized datasource for {}", tenantId);
        });

        var mds = new MultiTenantDataSource();
        mds.setTargetDataSources(map);

        return mds;
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