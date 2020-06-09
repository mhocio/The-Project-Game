

# The Project Game
The Project Game server is built Java 14 Spring Boot 2.2.7.
Bot module is written in Python 3.7 for the purpose of connecting to the game and play it.

On branch [*modules*](https://github.com/mhocio/The-Project-Game/tree/modules) there is a seperation of Game Master and Communication Server, where they can be executed seperately.

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

---
### Cooperation between teams

1) Game Master (GM) starts and waits.
2) Communication Server (CS) starts and waits.
~~3) GM sends back {"status":"OK","action":"setup"}~~

4) Player1 (P1) starts and sends though CS to GM message - 
{"playerGuid":"3331f4f6-fa70-472c-80f0-f43f00b83602","portNumber":9999,"action":"connect"}

5) GM replays 
{"playerGuid":"3331f4f6-fa70-472c-80f0-f43f00b83602","portNumber":9999,"status":"OK","action":"connect"}

6) Ready message is deleted as not implemented by everybody.

7) other Players Join

8) Game starts when GM is full (as stated in configuration), or when it 
was triggered (by gui, console, other way if possible (not needed))

9) GM sends to each player message:
{"playerGuid":"f64b383d-6937-41f1-a683-af4948ba8783","team":"Blue","teamRole":"Best 
Team 
Member","teamSize":4,"teamGuids":["d6ed72a0-7db1-42c4-bac7-117d3f91ac41","2d8255d5-a63d-4b03-87d3-9756b4561c9f","52ebbbaa-e4fd-47e3-8c17-266d2b1d66e1","f64b383d-6937-41f1-a683-af4948ba8783"],"position":{"x":3,"y":0},"board":{"boardWidth":32,"taskAreaHeight":26,"goalAreaHeight":3},"action":"start"}

were GUID are correct.....

10) Game will continue until all goal from one team are reviled.

11) To move player sends
{"playerGuid":"a8ed779a-0869-4230-9862-ec4db9ed6cf5","direction":"Up","action":"move"}
{"playerGuid":"a8ed779a-0869-4230-9862-ec4db9ed6cf5","direction":"Down","action":"move"}
{"playerGuid":"a8ed779a-0869-4230-9862-ec4db9ed6cf5","direction":"Left","action":"move"}
{"playerGuid":"a8ed779a-0869-4230-9862-ec4db9ed6cf5","direction":"Right","action":"move"}
11) GM replays
{"playerGuid":"a8ed779a-0869-4230-9862-ec4db9ed6cf5","direction":"Up","position":{"x":28,"y":30},"status":"OK","action":"move"}
and so on... with correct Guid and direction

12) to discover (only possible on target area) Player sends.
{"playerGuid":"d6ed72a0-7db1-42c4-bac7-117d3f91ac41","position":{"x":0,"y":3},"action":"discover"}

In replay from GM will be something like
{"playerGuid":"d6ed72a0-7db1-42c4-bac7-117d3f91ac41","position":{"x":0,"y":3},
"fields":[
{"position":{"x":0,"y":3},
"cell":{"cellState":"Empty","distance":27,"playerGuid":"d6ed72a0-7db1-42c4-bac7-117d3f91ac41"}},{"position":{"x":1,"y":3},
"cell":{"cellState":"Empty","distance":26,"playerGuid":"2d8255d5-a63d-4b03-87d3-9756b4561c9f"}},{"position":{"x":0,"y":4},
"cell":{"cellState":"Empty","distance":26}},{"position":{"x":1,"y":4},
"cell":{"cellState":"Empty","distance":25}}]
,"status":"OK","action":"discover"}

number of cell is low (4) as player stands next to Goal area.
Player is informed about cell it occupy. (in above example - 
"position":{"x":0,"y":3})

13) Pick up action
Sending to GM: 
{"playerGuid":"f64b383d-6937-41f1-a683-af4948ba8783","action":"pickup"}
Sending to player: 
{"playerGuid":"f64b383d-6937-41f1-a683-af4948ba8783","status":"OK","action":"pickup"}

14)
Sending to GM: 
{"playerGuid":"f64b383d-6937-41f1-a683-af4948ba8783","action":"test"}
Sending to player: 
{"playerGuid":"f64b383d-6937-41f1-a683-af4948ba8783","test":true,"status":"OK","action":"test"}

15)
Sending to GM: 
{"playerGuid":"f64b383d-6937-41f1-a683-af4948ba8783","action":"place"}
Sending to player: 
{"playerGuid":"f64b383d-6937-41f1-a683-af4948ba8783","placementResult":"Pointless","status":"OK","action":"place"}

16) When GM thinks its over:
{"result":"Red","action":"end"}
or
{"result":"Blue","action":"end"}
or
{"result":null,"action":"end"} <-- for draw

17) Players are unable to go to goal area of opposite team.

18) To allow game to always be finished,
GM modification:
player that will not move for 90 seconds from cell (after game started) 
will be removed from GM board and all messages from that player will be 
ignored by GM. GM will allow other players to occupy that spot.

