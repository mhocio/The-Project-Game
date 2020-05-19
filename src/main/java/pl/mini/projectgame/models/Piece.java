package pl.mini.projectgame.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Piece extends BoardObject {
    private UUID pieceUuid;
    private Boolean isGood;
    private HashSet<Player> testedPlayers;

    public Piece(double shamProbability) {
        pieceUuid = UUID.randomUUID();
        Random r = new Random();
        isGood = r.nextInt(100) >= shamProbability;
        testedPlayers = new HashSet<>();
    }
}