package com.starter.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.mysql.MySQLContainer;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.auth0.issuer-uri=https://issuer.test/",
        "app.auth0.jwk-set-uri=https://issuer.test/.well-known/jwks.json",
        "app.auth0.audience=api",
        "app.openapi.access=PUBLIC",
        "spring.threads.virtual.enabled=true"
})
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:9.7.4");

    static {
        MYSQL.start();
    }
}
