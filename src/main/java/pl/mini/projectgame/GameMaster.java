package pl.mini.projectgame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pl.mini.projectgame.exceptions.DeniedMoveException;
import pl.mini.projectgame.models.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.*;

import lombok.Setter;
import lombok.Getter;
import pl.mini.projectgame.server.CommunicationServer;

@Component
@Getter
@Setter
public class GameMaster {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public enum GameMasterStatus {
        ACTIVE, FINISHED, IDLE
    }

    private Map<UUID, Player> playerMap;

    private boolean lastTeamWasRed;
    private int portNumber;
    private InetAddress ipAddress;
    private int blueTeamGoals;
    private int redTeamGoals;
    private Team redTeam;
    private Team blueTeam;
    private int currentPieces;
    private MasterBoard masterBoard;
    private GameMasterConfiguration configuration;
    private CommunicationServer server;
    private List<Piece> pieces;

    @Autowired
    public GameMaster(GameMasterConfiguration config, MasterBoard board, @Lazy CommunicationServer server) {
        this.server = server;
        playerMap = new HashMap<>();
        lastTeamWasRed = false;
        configuration = config;
        masterBoard = board;
        blueTeam = new Team(Team.TeamColor.BLUE);
        redTeam = new Team(Team.TeamColor.RED);
        pieces = new ArrayList<>();
        blueTeamGoals = config.getPredefinedGoalPositions().size();
        redTeamGoals = blueTeamGoals;
    }

    public void startGame() {
        Random random = new Random();
        var board = new Board(configuration);
        Position position;

        for(Player player : playerMap.values()) {
            if(player.getTeam().getColor() == Team.TeamColor.RED) {
                position = new Position(
                        Math.abs(random.nextInt(board.getWidth())),
                        Math.abs(random.nextInt(board.getGoalAreaHeight())));

            } else {
                position = new Position(
                        Math.abs(random.nextInt(board.getWidth())),
                        Math.abs(random.nextInt() % board.getGoalAreaHeight()
                                + board.getGoalAreaHeight()
                                + board.getTaskAreaHeight()));
            }
            board.getCellByPosition(position).getContent().put(Player.class, player);
            player.setBoard(board);

            var message = new Message();
            message.setPlayerUuid(player.getPlayerUuid());
            message.setAction("startGame");
            message.setStatus(Message.Status.OK);
            message.setPosition(position);
            message.setBoard(board);

            server.sendToSpecific(message);
        }

        logger.info("The game has started!");
    }

    public void finishGame() {
        Message message = new Message();
        message.setAction("finish");
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

    private void putNewPiece() throws DeniedMoveException {
        var target = new Position();

        Random random = new Random();
        var piece = new Piece(configuration.getShamProbability());

        target.setY(random.nextInt() % masterBoard.getTaskAreaHeight() + masterBoard.getGoalAreaHeight());
        target.setX(random.nextInt(masterBoard.getWidth()));

        while (masterBoard.getCells().get(target).getContent().containsKey(Player.class)) {
            target.setY(random.nextInt() % masterBoard.getTaskAreaHeight() + masterBoard.getGoalAreaHeight());
            target.setX(random.nextInt(masterBoard.getWidth()));
        }

        masterBoard.getCells().get(target).addContent(Piece.class, piece);

        System.out.println("New piece has been put.");
    }

    private void printBoard(Board board) {
        System.out.println("Board printed.");
    }

    public void messageHandler(String message) {
        System.out.println("Message handled.");
    }

    public synchronized Message processAndReturn(Message request) {

        Message response;

        try {
            Method method = this.getClass().getDeclaredMethod("action" + StringUtils.capitalize(request.getAction()), Message.class);
            logger.info(method.getName());
            response = (Message) method.invoke(this, request);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex1) {
            logger.warn(ex1.getMessage());

            var msg = new Message();
            msg.setAction("error");
            return msg;
        }
        return response;
    }

    private Message actionConnect(Message message) {
        Message response = new Message();
        Team team = lastTeamWasRed ? blueTeam : redTeam;
        lastTeamWasRed = !lastTeamWasRed;
        Player player;

        try {
            player = new Player(team);
            team.addPlayer(player);
            playerMap.put(player.getPlayerUuid(), player);
        } catch (Exception e) {
            logger.warn(e.toString());
            response.setAction("error");
            return response;
        }

        response.setAction(message.getAction());
        response.setPlayerUuid(player.getPlayerUuid());
        response.setStatus(Message.Status.OK);

        return response;
    }

    private Message actionDiscover(Message message) {
        List<Field> fields = new ArrayList<>();
        Message response = new Message();
        try {
            Position playerPosition = message.getPosition();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    var position = new Position(
                            playerPosition.getX() + x,
                            playerPosition.getY() + y);

                    if (position.equals(playerPosition)) continue;

                    if(position.getX() >= masterBoard.getWidth()
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
        } catch(Exception e) {
            logger.warn(e.getMessage());
            response.setAction("error");
            return response;
        }

        response.setAction(message.getAction());
        response.setPosition(message.getPosition());
        response.setFields(fields);

        return response;
    }

    private Message actionMove(Message message) {
        Message response = new Message();
        Position target = new Position();

        Player player;
        Message.Direction direction;
        Position source;

        try {
            player = playerMap.get(message.getPlayerUuid());
            direction = message.getDirection();
            source = message.getPosition();

        } catch (Exception e) {
            logger.warn(e.getMessage());
            response.setAction("error");
            return response;
        }

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
            response = new Message();
            response.setStatus(Message.Status.DENIED);
            response.setPosition(null);
            return response;
        }
        response.setAction(message.getAction());
        response.setPosition(target);
        response.setStatus(Message.Status.OK);

        return response;
    }

    private Message actionTest(Message message) {
        Message response=new Message();
        try {
            Player player = playerMap.get(message.getPlayerUuid());
            Piece piece = (Piece)masterBoard.getCellByPosition(message.getPosition()).getContent().get(Piece.class);
            var testResult = player.testPiece(piece);
            response.setTest(testResult);
            // if player tests the first time
            if (testResult != null) {
                response.setStatus(Message.Status.OK);
            } else {
                response.setStatus(Message.Status.DENIED);
            }
            response.setAction(message.getAction());
        }
        catch(Exception e) {
            logger.warn(e.toString());
            response.setAction("error");
            response.setTest(null);
        }

        return response;
    }

    private Message actionPlace(Message message) {

        var player = playerMap.get(message.getPlayerUuid());

        if(player.placePiece()) {
            player.getTeam().addPoints(1);
            masterBoard.getCellByPosition(message.getPosition()).removeContent(Goal.class);
            if(player.getTeam().getColor() == Team.TeamColor.BLUE) {
                if(player.getTeam().getPoints() == blueTeamGoals) {
                    finishGame();
                }
            } else {
                if(player.getTeam().getPoints() == redTeamGoals) {
                    finishGame();
                }
            }
        }
        //@mhocio wanted some bad status idk
        Message response = new Message();
        response.setAction(message.getAction());
        response.setStatus(Message.Status.OK);
        //TODO send the new score to all players message
        return response;
    }

    private Message actionReady(Message message) {
        //TODO edge case - disconnection before the start of the game

        playerMap.get(message.getPlayerUuid()).setReady(true);
        Message response = new Message();
        response.setAction(message.getAction());
        response.setStatus(Message.Status.OK);
        return response;
    }
    
    private Message actionStart(Message message) {
        Message response = new Message();
        response.setAction(message.getAction());
        Player playerMessaged;

        try {
            playerMessaged = playerMap.get(message.getPlayerUuid());
        } catch (Exception ex) {
            response.setAction("error");
            return response;
        }

        if (!playerMessaged.isHost()) {
            response.setAction("error");
            return response;
        }

        List<Player> players = new ArrayList<>();
        redTeam.getPlayers().forEach((k, v) -> players.add((Player)k));
        blueTeam.getPlayers().forEach((k, v) -> players.add((Player)k));

        boolean allPlayersReady = true;

        for (Player player : players) {
            if (!player.isReady()) {
                allPlayersReady = false;
                break;
            }
        }

        if (!allPlayersReady) {
            response.setAction("error");
            return response;
        }

        startGame();
        response.setStatus(Message.Status.OK);
        return response;
     }

    private Message actionPickUp(Message message) {
        try {
            Piece pickupPiece=(Piece) masterBoard.getCellByPosition(message.getPosition()).getContent().get(Piece.class);

            if(pickupPiece == null) throw new DeniedMoveException("there is no piece at given position");
            var player = playerMap.get(message.getPlayerUuid());

            if(player.getPiece()==null) {
                player.setPiece(pickupPiece);
                masterBoard.getCellByPosition(message.getPosition()).removeContent(Piece.class);
            }
            else {
                Message response = new Message();
                response.setPosition(message.getPosition());
                response.setAction(message.getAction());
                response.setStatus(Message.Status.DENIED);
                return response;
            }
        } catch (Exception e) {
            logger.warn(e.toString());
            Message response = new Message();
            response.setPosition(message.getPosition());
            response.setAction("error");
            response.setStatus(Message.Status.DENIED);
            return response;
        }

        Message response = new Message();
        response.setAction(message.getAction());
        response.setPosition(message.getPosition());
        response.setStatus(Message.Status.OK);
        return response;
    }

    private Message actionSetup(Message message) {
        Message response = new Message();

        if(configuration == null ||  masterBoard == null ){
            response.setAction("error");
            response.setStatus(Message.Status.DENIED);
            return response;
        }
        response.setAction(message.getAction());
        response.setStatus(Message.Status.OK);
        return response;
    }
}
