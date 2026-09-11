package com.infobean.productcatalog.config;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

/**
 * Spring Boot 4.x dropped {@code H2ConsoleAutoConfiguration}, and H2's servlet
 * wrapper ({@code org.h2.server.web.WebServlet}) still targets {@code javax.servlet},
 * which doesn't exist on a Jakarta EE 10 / Tomcat 11 classpath. H2's standalone
 * web server has no servlet dependency and runs in-process, so it can see this
 * app's in-memory database directly.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.h2.console", name = "enabled", havingValue = "true")
public class H2ConsoleConfig {

    /**
     * Starts H2's standalone web console as a managed bean.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2ConsoleServer(@Value("${spring.h2.console.web-port:8090}") String webPort) throws SQLException {
        return Server.createWebServer("-webPort", webPort);
    }
}
