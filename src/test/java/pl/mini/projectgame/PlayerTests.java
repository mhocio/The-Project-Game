package pl.mini.projectgame;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pl.mini.projectgame.models.Piece;
import pl.mini.projectgame.models.Player;

import static org.junit.Assert.*;

@SpringBootTest
public class PlayerTests {
    static Player player;

    @BeforeAll
    static void initPlayer(){
        player=new Player();
    }

    @Test
    void testNullPiece(){
        assertNull(player.testPiece(null));
    }

    @Test
    void testCorrectPiece(){
        Piece piece=new Piece(0.5);
        assertTrue(piece.getTestedPlayers().contains(player));
    }

    @Test
    void testPieceAgain(){
        Piece piece=new Piece(0.5);
        player.testPiece(piece);
        assertTrue(piece.getTestedPlayers().contains(player));
    }
}