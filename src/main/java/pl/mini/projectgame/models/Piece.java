package pl.mini.projectgame.models;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.UUID;

@Getter
@Setter
public class Piece extends BoardObject {
    private UUID pieceUuid;
    private Boolean isGood;
    private HashSet<Player> testedPlayers;

    public Piece() {
        pieceUuid=UUID.randomUUID();
        isGood= !(Math.random() < 0.25);
        testedPlayers=new HashSet<>();
    }
}