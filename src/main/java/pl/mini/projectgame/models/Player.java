package pl.mini.projectgame.models;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.RequestHandledEvent;
import jdk.jshell.spi.ExecutionControl;

import java.net.InetAddress;
import java.util.List;
import java.lang.UnsupportedOperationException;
import java.util.UUID;

import pl.mini.projectgame.models.*;


public class Player implements BoardObject {

    public Player(Team _team,InetAddress _ipAddress,int _portNumber,String _playerName){
        this.playerUuid = UUID.randomUUID();
        this.ipAddress = _ipAddress;
        this.portNumber = _portNumber;
        this.playerName = _playerName;
        this.playerState = PlayerState.Initializing;

    }

    public Team team;

    private InetAddress ipAddress;

    private int portNumber;

    public String playerName;

    public ActionType lastAction;

    private enum ActionType {
        Move, Pickup, Test,Place,Destroy,Send;
    }

    public Direction lastDirection;

    private enum Direction {
        Up,Down,Left,Right;
    }

    public Piece piece;

    public Position position;

    public Board board;

    private UUID playerUuid;

    private PlayerState playerState;

    private enum PlayerState {
        Initializing, Active, Completed;
    }
    public int getPortNumber() {
        return portNumber;
    }

    //getters and setters

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }
    public void setTeam(Team team) {
        this.team = team;
    }
    public pl.mini.projectgame.models.Player.ActionType getLastAction() {
        return lastAction;
    }

    public void setLastAction(pl.mini.projectgame.models.Player.ActionType lastAction) {
        this.lastAction = lastAction;
    }

    public Direction getLastDirection() {
        return lastDirection;
    }

    public void setLastDirection(Direction lastDirection) {
        this.lastDirection = lastDirection;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public pl.mini.projectgame.models.Player.PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(pl.mini.projectgame.models.Player.PlayerState playerState) {
        this.playerState = playerState;
    }
    public Team getTeam() {
        return team;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }
    @EventListener
    public void listen(RequestHandledEvent e) {
        System.out.println("RequestHandledEvent");
        System.out.println(e);
    }

    public void makeAction(){
        System.out.println("makeAction");
    }

    private void discover(){

    }

    private void move(){
        lastAction=ActionType.Move;
    }

    private void takePiece(Piece _piece){
        this.piece=_piece;
        this.lastAction=ActionType.Pickup;
    }

    private void testPiece(){
        lastAction=ActionType.Test;
    }

    private void placePiece(){
        lastAction=ActionType.Place;
    }

    @Override
    public int hashCode(){
        return this.playerUuid.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(this == o)
            return true;
        if(o == null || !(o instanceof Player))
            return false;
        Player p=(Player)o;
        return p.playerUuid == this.playerUuid;
    }

}

