package pl.mini.projectgame.models;

import lombok.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.RequestHandledEvent;
import jdk.jshell.spi.ExecutionControl;

import java.net.InetAddress;
import java.util.List;
import java.lang.UnsupportedOperationException;
import java.util.UUID;

import pl.mini.projectgame.models.*;

@Getter
@Setter
@ToString
public class Player extends BoardObject {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT;
    }

    public enum PlayerState {
        INITIALIZING, ACTIVE, COMPLETED;
    }


    private enum ActionType {
        MOVE, PICKUP, TEST, PLACE, DESTROY, SEND;
    }
    private Team team;
    public Position position;
    private InetAddress ipAddress;
    private int portNumber;
    private String playerName;
    private ActionType lastAction;
    private Direction lastDirection;
    private Piece piece;
    private Board board;
    private UUID playerUuid;
    private PlayerState playerState;
    private boolean ready = false;
    private boolean host = false;


    public Player(Team _team, InetAddress _ipAddress, int _portNumber, String _playerName){
        this.playerUuid = UUID.randomUUID();
        this.team = _team;
        this.ipAddress = _ipAddress;
        this.portNumber = _portNumber;
        this.playerName = _playerName;
        this.playerState = PlayerState.INITIALIZING;
    }

    public Player(Team team) {
        this.playerUuid = UUID.randomUUID();
        this.team = team;
        this.playerState = PlayerState.INITIALIZING;
    }
  
    public Player() {
        this.playerUuid = UUID.randomUUID();
        this.playerState = PlayerState.INITIALIZING;
    }
  
    public boolean placePiece(){
        lastAction=ActionType.PLACE;
        if(!piece.getIsGood()){ piece = null; return false; }
        if(board.getCells().get(position).getContent().containsKey(Goal.class)) { piece = null; return true; }
        piece = null; return false;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isHost() {
        return host;
    }

    public Boolean testPiece(Piece piece){
        lastAction=ActionType.TEST;
        if(piece == null || piece.getTestedPlayers().contains(this)){
            return null;
        }
        else{
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
        if(obj.getClass() != Player.class) return false;
        return this.playerUuid.equals(((Player) obj).playerUuid);
    }
}

