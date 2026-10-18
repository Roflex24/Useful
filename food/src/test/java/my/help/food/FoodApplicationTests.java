package my.help.food;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:food;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.ai.chat.memory.repository.jdbc.initialize-schema=always",
        "telegram.enabled=false",
        "management.health.redis.enabled=false"
})
class FoodApplicationTests {
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired JdbcTemplate jdbc;
    @LocalServerPort int port;

    @Test
    void runsWithOnlyItsOwnEntitiesAndPages() throws Exception {
        assertThat(entityManagerFactory.getMetamodel().getEntities()).isNotEmpty().allSatisfy(entity ->
                assertThat(entity.getJavaType().getPackageName()).startsWith("my.help.food"));
        assertThat(jdbc.queryForObject("select count(*) from products", Long.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from information_schema.tables where lower(table_name) = 'projects'", Long.class)).isZero();
        var client = HttpClient.newHttpClient();
        var response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/page/food")).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).containsIgnoringCase("<html");
    }
}
