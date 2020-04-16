package pl.mini.projectgame.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.net.InetAddress;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
public class Player extends BoardObject {

    //TODO add JsonIgnore

    public enum ActionType {
        MOVE, PICKUP, TEST, PLACE, DESTROY, SEND;
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT;
    }

    public enum PlayerState {
        INITIALIZING, ACTIVE, COMPLETED;
    }

    private Team team;
    private InetAddress ipAddress;
    private int portNumber;
    private String playerName;
    private ActionType lastAction;
    private Direction lastDirection;
    private Piece piece;
    private Position position;
    private Board board;
    private UUID playerUuid;
    private PlayerState playerState;


    public Player(Team _team, InetAddress _ipAddress, int _portNumber, String _playerName){
        this.playerUuid = UUID.randomUUID();
        this.team = _team;
        this.ipAddress = _ipAddress;
        this.portNumber = _portNumber;
        this.playerName = _playerName;
        this.playerState = PlayerState.INITIALIZING;
    }

    public Player(Team team, String playerName) {
        this.playerUuid = UUID.randomUUID();
        this.team = team;
        this.playerName = playerName;
    }
  
    public Player() {
        this.playerUuid = UUID.randomUUID();
        this.playerState = PlayerState.INITIALIZING;
    }

}

