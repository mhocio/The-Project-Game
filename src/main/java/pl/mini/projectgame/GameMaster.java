package pl.mini.projectgame;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pl.mini.projectgame.exceptions.DeniedMoveException;
import pl.mini.projectgame.models.*;
import pl.mini.projectgame.server.CommunicationServer;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Getter
@Setter
public class GameMaster {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public enum GameMasterStatus {
        ACTIVE, FINISHED, IDLE
    }

    public enum gmMode {
        NONE, LOBBY, GAME
    }
    public gmMode mode;

    private Map<UUID, Player> playerMap;

    private boolean lastTeamWasRed;
    private int portNumber;
    private InetAddress ipAddress;

    // redTeam is DOWN of the map
    private Team redTeam;
    private List<Goal> redTeamGoals;
    // blueTeam is UP of the map
    private Team blueTeam;
    private List<Goal> blueTeamGoals;

    private MasterBoard masterBoard;
    private GameMasterConfiguration configuration;
    private CommunicationServer server;
    private List<Piece> pieces;
    private int requiredPointsToWin;
    private ScheduledExecutorService scheduler;

    @Autowired
    public GameMaster(GameMasterConfiguration config, MasterBoard board, @Lazy CommunicationServer server) {

        this.server = server;
        playerMap = new HashMap<>();
        lastTeamWasRed = false;
        configuration = config;
        masterBoard = board;
        blueTeam = new Team(Team.TeamColor.BLUE);
        redTeam = new Team(Team.TeamColor.RED);

        blueTeamGoals = new ArrayList<>();
        redTeamGoals = new ArrayList<>();

        pieces = new ArrayList<>();
        mode = gmMode.NONE;
        scheduler = Executors.newSingleThreadScheduledExecutor();

        try {
            File file = new File(
                    //Objects.requireNonNull(ProjectGameApplication.class.getClassLoader().getResource("gameMasterConfig.json")).getFile()
                    Objects.requireNonNull(ProjectGameApplication.class.getClassLoader().getResource("gameMasterScenarioConfig1.json")).getFile()

            );
            config.configureFromFile(file.getPath());
            blueTeam.setMaxTeamSize(config.getMaxTeamSize());
            redTeam.setMaxTeamSize(config.getMaxTeamSize());
            masterBoard.configure(config);
        } catch (NullPointerException e) {
            logger.error(e.getMessage());
        }
    }

    public void reset() {
        // TODO: reset all variables, delete all players and connections
        //  prepare for the next game
    }

    public void startGame() {
        Random random = new Random();
        var board = new Board(configuration);
        Position position;

        mode = gmMode.GAME;
        requiredPointsToWin = configuration.getPredefinedGoalPositions().size();

        if (requiredPointsToWin <= 0) {
            logger.error("some error with configuration.getPredefinedGoalPositions ");
            requiredPointsToWin = 5;
        }

        // set goals from config
        List<Position> predefinedGoals = configuration.getPredefinedGoalPositions();
        for (Position goalPos: predefinedGoals) {
            int x = goalPos.getX();
            int y = goalPos.getY();

            if (x >= masterBoard.getWidth() || y >= masterBoard.getTaskAreaHeight()) {
                Position pos;
                do {
                    x = random.nextInt(board.getWidth());
                    y = random.nextInt(board.getGoalAreaHeight());
                    pos = new Position(x, y);
                } while (masterBoard.getCellByPosition(pos).getContent().containsKey(Goal.class));
            }

            Position pos1 = new Position(x, y);
            Goal goal1 = new Goal(true, pos1);
            goal1.setTeam(redTeam);
            masterBoard.addBoardObject(goal1, pos1);
            redTeamGoals.add(goal1);

            y += configuration.boardTaskHeight + configuration.boardGoalHeight;
            Position pos2 = new Position(x, y);
            Goal goal2 = new Goal(true, pos2);
            goal1.setTeam(blueTeam);
            masterBoard.addBoardObject(goal2, pos2);
            blueTeamGoals.add(goal2);
        }

        // set pieces
        for (int i = 0; i < configuration.getMaxPieces(); i++) {
            putNewPiece();
        }

        scheduler.scheduleAtFixedRate(this::putNewPiece, 30, 30, TimeUnit.SECONDS);

        for (Player player : playerMap.values()) {
            do {
                if (player.getTeam().getColor() == Team.TeamColor.RED) {
                    position = new Position(
                            random.nextInt(board.getWidth()),
                            random.nextInt(board.getGoalAreaHeight()));

                } else {
                    position = new Position(
                            random.nextInt(board.getWidth()),
                            random.nextInt() % board.getGoalAreaHeight()
                                    + board.getGoalAreaHeight()
                                    + board.getTaskAreaHeight());
                }
            } while (masterBoard.getCellByPosition(position).getContent().containsKey(Player.class));

            masterBoard.addBoardObject(player, position);
            board.addBoardObject(player, position);
            player.setBoard(board);
            player.setPosition(position);
        }
        sendStartGameMessage();
        logger.info("The game has started!");
    }

    public void finishGame(Team.TeamColor color) {
        mode = gmMode.NONE;

        logger.info("Red team points: " + redTeam.getPoints());
        logger.info("Blue team points: " + blueTeam.getPoints());

        if(color == null) {
            logger.info("Draw!");
        } else {
            logger.info(color + " wins!");
        }

        Message message = new Message();
        message.setAction("finish");
        scheduler.shutdownNow();
        server.sendToEveryone(message);
        server.close();
        logger.info("Game finished");
    }

    public void loadConfiguration() {

//        shamProbability = 0.5;
//        maxTeamSize = 4;

//        DelayDestroyPiece = 2950;
//        DelayNextPiecePlace = 3000;
//        DelayMove = 100;
//        DelayDiscover = 500;
//        DelayTest = 1000;
//        DelayPick = 100;
//        DelayPlace = 100;

        System.out.println("Configuration loaded.");
    }

    private void putNewPiece() {

        if(pieces.size() == configuration.getMaxPieces()) return;

        var target = new Position();
        Random random = new Random();

        var piece = new Piece(configuration.getShamProbability());

        //target.setY(random.nextInt() % masterBoard.getTaskAreaHeight() + masterBoard.getGoalAreaHeight());
        //target.setX(random.nextInt(masterBoard.getWidth()));

        target.setY(ThreadLocalRandom.current().nextInt(0, masterBoard.getTaskAreaHeight())
                    + masterBoard.getGoalAreaHeight());
        target.setX(ThreadLocalRandom.current().nextInt(0, masterBoard.getWidth()));

        while (masterBoard.getCells().get(target).getContent().containsKey(Player.class)
            || masterBoard.getCells().get(target).getContent().containsKey(Piece.class)) {
            //target.setY(random.nextInt() % masterBoard.getTaskAreaHeight() + masterBoard.getGoalAreaHeight());
            //target.setX(random.nextInt(masterBoard.getWidth()));
            target.setY(ThreadLocalRandom.current().nextInt(0, masterBoard.getTaskAreaHeight())
                    + masterBoard.getGoalAreaHeight());
            target.setX(ThreadLocalRandom.current().nextInt(0, masterBoard.getWidth()));

        }

        piece.setPosition(target);
        pieces.add(piece);
        masterBoard.getCells().get(target).addContent(Piece.class, piece);
    }

    private void printBoard(Board board) {
        System.out.println("Board printed.");
    }

    public void messageHandler(String message) {
        System.out.println("Message handled.");
    }

    public List<Goal> getGoals(Player player) {
        List<Goal> returnGoals = new ArrayList<>();

        if(player == null) return returnGoals;

        Team playerTeam = player.getTeam();
        List<Goal> teamGoals;

        if (playerTeam == redTeam)
            teamGoals = redTeamGoals;
        else
            teamGoals = blueTeamGoals;

        if(teamGoals == null) return returnGoals;

        for (Goal goal: teamGoals) {
            if (goal.getDiscovered() != Goal.goalDiscover.NOT_DISCOVERED)
                returnGoals.add(goal);
        }

        return returnGoals;
    }

    public synchronized Message processAndReturn(Message request) {

        Message response;

        try {
            Method method = this.getClass().getDeclaredMethod("action" + StringUtils.capitalize(request.getAction()), Message.class);
            logger.info(method.getName());
            response = (Message) method.invoke(this, request);

            //set goals in players goal area in each response if game is ON
            if (mode == gmMode.GAME) {
                Player player = playerMap.get(request.getPlayerUuid());
                response.setGoals(getGoals(player));
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return createErrorMessage();
        }

        return response;
    }

    private Message actionConnect(Message message) {
        Message response = new Message();
        Team team = lastTeamWasRed ? blueTeam : redTeam;
        lastTeamWasRed = !lastTeamWasRed;
        Player player;

        if (mode == gmMode.GAME) return createErrorMessage();

        try {
            player = new Player(team);
            team.addPlayer(player);
            playerMap.put(player.getPlayerUuid(), player);

            if (mode == gmMode.NONE) {
                player.setHost(true);
                mode = gmMode.LOBBY;
            }

            response.setAction(message.getAction());
            response.setPlayerUuid(player.getPlayerUuid());
            response.setTeamColor(player.getTeam().getTeamColor());
            response.setTeamRole(player.getTeam().getPlayerRole(player));
            response.setHost(player.isHost());
            response.setStatus(Message.Status.OK);

        } catch (Exception e) {
            logger.warn(e.toString());
            return createErrorMessage();
        }

        // TODO: set host to some player if host disconnects

        return response;
    }

    private Message actionFinish(Message message) {
        var player = playerMap.get(message.getPlayerUuid());

        if(player == null || !player.isHost()) return createErrorMessage();

        if(redTeam.getPoints() > blueTeam.getPoints()) {
            finishGame(redTeam.getColor());
        } else if(redTeam.getPoints() < blueTeam.getPoints()) {
            finishGame(blueTeam.getColor());
        } else {
            finishGame(null);
        }

        return message;
    }

    /**
     * discover distance to the nearest pieces
     * for each neighbour cell
     * return the Manhattan distance to the nearest piece
     */
    private Message actionDiscover(Message message) {
        List<Field> fields = new ArrayList<>();
        Message response = new Message();

        if(mode != gmMode.GAME) return createErrorMessage();

        try {
            Position playerPosition = message.getPosition();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    var position = new Position(
                            playerPosition.getX() + x,
                            playerPosition.getY() + y);

                    if (position.equals(playerPosition)) continue;

                    if (position.getX() >= masterBoard.getWidth()
                            || position.getX() < 0
                            || position.getY() < masterBoard.getGoalAreaHeight()
                            || position.getY() >= masterBoard.getGoalAreaHeight() + masterBoard.getTaskAreaHeight()) {

                        continue;
                    }

                    var currentCell = masterBoard.getCellByPosition(position);
                    int minDistance = Integer.MAX_VALUE;

                    for (Piece piece : pieces) {
                        int distance = currentCell.calculateDistance(piece.getPosition());
                        if (distance < minDistance) minDistance = distance;
                    }

                    // if there is no pieces on the board
                    minDistance = minDistance == Integer.MAX_VALUE ? -1 : minDistance;
                    currentCell.setDistance(minDistance);
                    fields.add(new Field(currentCell));
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return createErrorMessage();
        }

        response.setAction(message.getAction());
        response.setPosition(message.getPosition());
        response.setFields(fields);

        return response;
    }

    private Message actionMove(Message message) {
        Message response = new Message();
        Position target = new Position();

        if(mode != gmMode.GAME) return createErrorMessage();

        Player player;
        Message.Direction direction;
        Position source;

        try {
            player = playerMap.get(message.getPlayerUuid());
            direction = message.getDirection();
            source = message.getPosition();
            response.setAction(message.getAction());
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return createErrorMessage();
        }

        if(direction == null || source == null) return createErrorMessage();

        switch (direction) {
            case UP:
                target.setX(source.getX());
                target.setY(source.getY() + 1);
                break;
            case DOWN:
                target.setX(source.getX());
                target.setY(source.getY() - 1);
                break;
            case LEFT:
                target.setX(source.getX() - 1);
                target.setY(source.getY());
                break;
            case RIGHT:
                target.setX(source.getX() + 1);
                target.setY(source.getY());
                break;
        }

        try {
            masterBoard.movePlayer(player, source, target);
        } catch (Exception e) {
            logger.warn(e.toString());
            response.setStatus(Message.Status.DENIED);
            response.setPosition(null);
            return response;
        }

        response.setPosition(target);
        response.setStatus(Message.Status.OK);
        return response;
    }

    private Message actionTest(Message message) {

        if(mode != gmMode.GAME) return createErrorMessage();

        Message response = new Message();
        try {
            Player player = playerMap.get(message.getPlayerUuid());
            Piece piece = (Piece) masterBoard.getCellByPosition(message.getPosition()).getContent().get(Piece.class);
            var testResult = player.testPiece(piece);
            response.setTest(testResult);
            // if player tests the first time
            if (testResult != null) {
                response.setStatus(Message.Status.OK);
            } else {
                response.setStatus(Message.Status.DENIED);
            }
            response.setAction(message.getAction());
        } catch (Exception e) {
            logger.warn(e.toString());
            return createErrorMessage();
        }

        return response;
    }

    private void checkWinningState() {
        if (redTeam.getPoints() == requiredPointsToWin) {
            finishGame(Team.TeamColor.RED);
        }

        if (blueTeam.getPoints() == requiredPointsToWin) {
            finishGame(Team.TeamColor.BLUE);
        }
    }

    private Message actionPlace(Message message) {

        if(mode != gmMode.GAME)
            return createErrorMessage();

        Message response = new Message();
        response.setAction(message.getAction());
        response.setStatus(Message.Status.OK);
        response.setPlacementResult(Message.placementResult.CORRECT);

        Player player;
        Position playerPosition;
        Piece playerPiece;
        Goal goal = null;
        Team playerTeam;

        try {
            player = playerMap.get(message.getPlayerUuid());
            response.setPlayerUuid(message.getPlayerUuid());
            playerPosition = player.getPosition();
            playerPiece = player.getPiece();
            playerTeam = player.getTeam();

            if (playerPiece == null)
                return createErrorMessage();

            if (masterBoard.getCellByPosition(player.getPosition()).getContent().containsKey(Goal.class)) {
                goal = (Goal) masterBoard.getCellByPosition(player.getPosition()).getContent().get(Goal.class);
            }

        } catch (Exception ex) {
            return createErrorMessage();
        }

        player.placePiece(masterBoard);

        // good piece
        if (playerPiece.getIsGood()) {
            // empty cell
            if (goal == null) {
                response.setPlacementResult(Message.placementResult.POINTLESS);
                if (playerTeam == blueTeam) {
                    Goal newGoal = new Goal(false, playerPosition, blueTeam);
                    masterBoard.addBoardObject(new Goal(false, playerPosition), playerPosition);
                    blueTeamGoals.add(newGoal);
                } else {
                    Goal newGoal = new Goal(false, playerPosition, redTeam);
                    masterBoard.addBoardObject(new Goal(false, playerPosition), playerPosition);
                    redTeamGoals.add(newGoal);
                }
                return response;
            }

            // discovered cell
            if (goal.getDiscovered().equals(Goal.goalDiscover.DISCOVERED_NON_GOAL)
                    || goal.getDiscovered().equals(Goal.goalDiscover.DISCOVERED_GOAL)) {
                response.setPlacementResult(Message.placementResult.POINTLESS);
                return response;
            }

            // good place
            if (goal.getDiscovered().equals(Goal.goalDiscover.NOT_DISCOVERED)) {
                // good team
                if (goal.getTeam() == playerTeam) {
                    playerTeam.addPoints(1);
                    goal.setDiscovered(Goal.goalDiscover.DISCOVERED_GOAL);
                    checkWinningState();
                } else {
                    // wrong team
                    response.setPlacementResult(Message.placementResult.POINTLESS);
                    response.setStatus(Message.Status.DENIED);
                }
                return response;
            }

        } else {
            // sham piece
            response.setPlacementResult(Message.placementResult.POINTLESS);
            return response;
        }

        return response;
    }

    private Message actionReady(Message message) {

        if(mode != gmMode.LOBBY) return createErrorMessage();

        //TODO edge case - disconnection before the start of the game

        var playerId = message.getPlayerUuid();

        if(playerId == null) return createErrorMessage();

        playerMap.get(playerId).setReady(true);
        message.setStatus(Message.Status.OK);
        return message;
    }

    private Message actionStart(Message message) {
        Player playerMessaged;

        try {
            playerMessaged = playerMap.get(message.getPlayerUuid());
        } catch (Exception ex) {
            return createErrorMessage();
        }

        if (!playerMessaged.isHost() || mode != gmMode.LOBBY) {
            return createErrorMessage();
        }

        List<Player> players = new ArrayList<>();
        redTeam.getPlayers().forEach((k, v) -> players.add(k));
        blueTeam.getPlayers().forEach((k, v) -> players.add(k));

        boolean allPlayersReady = true;

        for (Player player : players) {
            if (!player.isReady()) {
                allPlayersReady = false;
                break;
            }
        }

        if (!allPlayersReady) return createErrorMessage();

        startGame();
        message.setPlayerUuid(playerMessaged.getPlayerUuid());
        message.setAction("startGame");
        message.setStatus(Message.Status.OK);
        message.setPosition(playerMessaged.getPosition());
        message.setBoard(playerMessaged.getBoard());

        return message;
    }

    private Message actionPickUp(Message message) {

        if(mode != gmMode.GAME) return createErrorMessage();

        try {
            Piece pickupPiece = (Piece) masterBoard.getCellByPosition(message.getPosition()).getContent().get(Piece.class);

            if (pickupPiece == null) throw new DeniedMoveException("there is no piece at given position");
            var player = playerMap.get(message.getPlayerUuid());

            if (player.getPiece() == null) {
                player.setPiece(pickupPiece);
                masterBoard.getCellByPosition(message.getPosition()).removeContent(Piece.class);
                pieces.remove(pickupPiece);
            } else {
                message.setStatus(Message.Status.DENIED);
                return message;
            }
        } catch (Exception e) {
            logger.warn(e.toString());
            return createErrorMessage();
        }

        message.setStatus(Message.Status.OK);
        return message;
    }

    /**
     * Create a lobby?
     * Now player can join to the game
     * @param message
     * @deprecated
     */
    private Message actionSetup(Message message) {
        Message response = new Message();
        Player player;

        if (mode == gmMode.GAME) return createErrorMessage();

        if (configuration == null || masterBoard == null) {
            return  createErrorMessage();
        }

        mode = gmMode.LOBBY;

        response.setAction(message.getAction());
        response.setStatus(Message.Status.OK);
        return response;
    }

    private Message createErrorMessage() {
        var message = new Message();
        message.setAction("error");
        message.setStatus(Message.Status.DENIED);
        return message;
    }

    private void sendStartGameMessage() {

        for(Player p : playerMap.values()) {
            if(!p.isHost()) {
                var message = new Message();
                message.setPlayerUuid(p.getPlayerUuid());
                message.setAction("startGame");
                message.setStatus(Message.Status.OK);
                message.setPosition(p.getPosition());
                message.setBoard(p.getBoard());

                server.sendToSpecific(message);
            }
        }
    }
}