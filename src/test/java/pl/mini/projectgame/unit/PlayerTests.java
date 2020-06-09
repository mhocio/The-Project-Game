package pl.mini.projectgame.unit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import pl.mini.projectgame.models.Piece;
import pl.mini.projectgame.models.Player;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerTests {
    static Player player;

    @BeforeAll
    static void initPlayer() {
        player = new Player();
    }

    @Test
    void testNullPiece() {
        assertNull(player.testPiece());
    }

    @Test
    void testCorrectPiece() {
        Piece piece = new Piece(0);
        player.setPiece(piece);
        player.testPiece();
        assertTrue(piece.getTestedPlayers().contains(player));
    }

    @Test
    void testPieceAgain() {
        assertNull(player.testPiece());
    }
}