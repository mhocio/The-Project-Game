package pl.mini.projectgame.models;

import lombok.Getter;
import lombok.Setter;


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

    public Piece(double shamProbability) {
        pieceUuid = UUID.randomUUID();
        if (Math.random() < shamProbability)
            isGood = false;
        else
            isGood = true;
        testedPlayers = new HashSet<>();
    }
}