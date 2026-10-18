package my.help.useful;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@OpenAPIDefinition(
        info = @Info(
                title = "Life Management API",
                version = "1.0.0",
                description = "API для управления жизнью",
                license = @License(name = "Github", url = "https://github.com/Roflex24/Useful")
        )
)
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class UsefulApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsefulApplication.class, args);
    }

}
