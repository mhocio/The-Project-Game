package pl.mini.projectgame.models;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.UUID;

@Getter
@Setter
public class Piece implements BoardObject {
    private UUID pieceUuid;
    private Boolean isGood;
    private HashSet<Player> testedPlayers;

    public Piece() {
        pieceUuid=UUID.randomUUID();
        if(Math.random()<0.25)
            isGood=false;
        else
            isGood=true;
        testedPlayers=new HashSet<>();
    }

}
