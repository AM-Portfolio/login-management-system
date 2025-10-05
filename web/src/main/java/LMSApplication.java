import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.modernportfolio.*"})
@EntityScan(basePackages = {"com.modernportfolio.*"})
@EnableJpaRepositories(basePackages = {"com.modernportfolio.*"})
public class LMSApplication {
    public static void main(String[] args) {
        SpringApplication.run(LMSApplication.class, args);
    }
}
