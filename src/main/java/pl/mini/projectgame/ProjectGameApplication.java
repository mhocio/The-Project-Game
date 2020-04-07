package pl.mini.projectgame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class ProjectGameApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectGameApplication.class, args);

		GameMasterConfiguration defaultConfig = new GameMasterConfiguration();
		System.out.println(defaultConfig);

		File file = new File(
				ProjectGameApplication.class.getClassLoader().getResource("gameMasterConfig.json").getFile()
		);
		GameMasterConfiguration configFromFile = new GameMasterConfiguration(file.getPath());
		System.out.println(configFromFile);
	}

}
