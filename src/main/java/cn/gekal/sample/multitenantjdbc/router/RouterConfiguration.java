package cn.gekal.sample.multitenantjdbc.router;

import cn.gekal.sample.multitenantjdbc.model.Customer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration
public class RouterConfiguration {

    @Bean
    RouterFunction<ServerResponse> routes(JdbcTemplate template) {
        return route().GET("/customers", request -> {
            var results = template.query("select * from customer",
                    (rs, rowNum) -> new Customer(rs.getInt("id"), rs.getString("name")));
            return ServerResponse.ok().body(results);
        }).build();
    }
}
