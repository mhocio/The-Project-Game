package pl.mini.projectgame.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Player extends BoardObject {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public enum PlayerState {
        INITIALIZING, ACTIVE, COMPLETED
    }

    private enum ActionType {
        MOVE, PICKUP, TEST, PLACE, DESTROY, SEND
    }

    private Team team;
    //public Position position;
    private InetAddress ipAddress;
    private int portNumber;
    private String playerName;
    private ActionType lastAction;
    private Direction lastDirection;
    private Piece piece;
    private Board board;
    private String playerUuid;
    private PlayerState playerState;
    private boolean ready = false;
    private boolean host = false;

    public String generateRandomString() {
        int len = 16;
        byte[] array = new byte[len];
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));

        return generatedString;
    }

    public Player(Team _team, InetAddress _ipAddress, int _portNumber, String _playerName) {
        this.playerUuid = generateRandomString();
        this.team = _team;
        this.ipAddress = _ipAddress;
        this.portNumber = _portNumber;
        this.playerName = _playerName;
        this.playerState = PlayerState.INITIALIZING;
        this.host = false;
        this.ready = false;
    }

    public Player(Team team) {
        this.playerUuid = generateRandomString();
        this.team = team;
        this.playerState = PlayerState.INITIALIZING;
        this.host = false;
        this.ready = false;
    }

    public Player() {
        this.playerUuid = generateRandomString();
        this.playerState = PlayerState.INITIALIZING;
        this.host = false;
        this.ready = false;
    }

    public boolean placePiece(MasterBoard masterBoard) {
        lastAction = ActionType.PLACE;
        piece = null;

        return true;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isHost() {
        return host;
    }

    public Boolean testPiece(Piece piece) {
        lastAction = ActionType.TEST;
        if (piece == null || piece.getTestedPlayers().contains(this)) {
            return null;
        } else {
            piece.getTestedPlayers().add(this);
            return piece.getIsGood();
        }
    }

    @Override
    public int hashCode() {
        return this.playerUuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != Player.class) return false;
        return this.playerUuid.equals(((Player) obj).playerUuid);
    }
}

