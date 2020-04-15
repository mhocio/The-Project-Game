package pl.mini.projectgame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@SpringBootApplication
@Configuration
public class ProjectGameApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ProjectGameApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);
		ConfigurableApplicationContext ctx = app.run(args);

		GameMasterConfiguration defaultConfig = new GameMasterConfiguration();
		System.out.println(defaultConfig); // use Logger instead

		// Please check for null
		File file = new File(
				ProjectGameApplication.class.getClassLoader().getResource("gameMasterConfig.json").getFile()
		);
		GameMasterConfiguration configFromFile = new GameMasterConfiguration();
		configFromFile.configureFromFile(file.getPath());
		System.out.println(configFromFile); // use Logger instead
	}
}
