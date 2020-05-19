package pl.mini.projectgame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class ProjectGameApplication {

    public static void main(String[] args) {

        System.out.println("args...: ");
        for (String arg: args)
            System.out.println(arg);

        SpringApplication app = new SpringApplication(ProjectGameApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        ConfigurableApplicationContext ctx = app.run(args);
    }
}
