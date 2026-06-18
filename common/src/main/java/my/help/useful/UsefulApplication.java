package my.help.useful;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "my.help.finance",
        "my.help.useful",
        "my.help.food",
        "my.help.kanban"
        })

@EntityScan({"my.help"})
@EnableJpaRepositories({"my.help"})
public class UsefulApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsefulApplication.class, args);
    }

}
