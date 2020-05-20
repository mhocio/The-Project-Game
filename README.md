

# The Project Game
The Project Game server is built Java 14 Spring Boot 2.2.7.
Bot module is written in Python 3.7 for the purpose of connecting to the game and play it.

##  Running the game
In order to process the game you need to: 
1) Run first the Java server (using JAR file from the command line or from the IDE)
2) When the server is running, then start the bots using Python file
### 1) How to run the server
Below runs the Spring Boot server on 127.0.0.1:8080
#### From the command line using done JAR file:
Run [out](https://github.com/mhocio/The-Project-Game/tree/master/out)/[artifacts](https://github.com/mhocio/The-Project-Game/tree/master/out/artifacts)/[The_Project_Game_jar](https://github.com/mhocio/The-Project-Game/tree/master/out/artifacts/The_Project_Game_jar)/[**The-Project-Game.jar**](out/artifacts/The_Project_Game_jar/The-Project-Game.jar) and pass location of your game configuration to the `-Dconfig-path` option. 
```sh
$ java -cp The-Project-Game.jar -Dconfig-path=$PWD/gameMasterScenarioConfig1.json pl.mini.projectgame.ProjectGameApplication
``` 
In this case make sure that game master config is in your current working directory from which you run the JAR file.

### 2) How to run the bots
Once the server is set, go to *The-Project-Game/src/bot/src* and run the scenario of your choice:
- Run scenario, where 2 bots compete to reach the goal - [**scenario2bots.py**](src/bot/src/scenario2bots.py)
	```sh
	$ python3 scenario2bots.py
	```
- Or run functionalities for passed number of bots - [**master_bot.py**](src/bot/src/master_bot.py)
	```sh
	$ python3 master_bot.py <number of bots> 127.0.0.1
	```
## Architecture
TODO
