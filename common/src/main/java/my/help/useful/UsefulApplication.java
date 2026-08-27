package my.help.useful;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@OpenAPIDefinition(
        info = @Info(
                title = "Life Management API",
                version = "1.0.0",
                description = "API для управления жизнью",
                license = @License(name = "Github", url = "https://github.com/Roflex24/Useful")
        )
)
@SpringBootApplication(scanBasePackages = {
        "my.help.finance",
        "my.help.useful",
        "my.help.food",
        "my.help.kanban"
        })

@EntityScan({"my.help"})
@EnableJpaRepositories({"my.help"})
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class UsefulApplication {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(UsefulApplication.class, args);
    }

}
