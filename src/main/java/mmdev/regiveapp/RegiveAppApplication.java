package mmdev.regiveapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RegiveAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegiveAppApplication.class, args);
    }

}
