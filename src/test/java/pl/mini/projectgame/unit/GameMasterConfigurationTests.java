package pl.mini.projectgame.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.models.Position;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameMasterConfigurationTests {

    GameMasterConfiguration createTestConfigFromFile() {
        File file;
        GameMasterConfiguration configFromFile = new GameMasterConfiguration();
        try {
            file = ResourceUtils.getFile("gameMasterTestConfiguration.json");

            System.out.println(file.getAbsolutePath());
            configFromFile.configureFromFile(file.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("error reading config in test: " + e.getMessage());
        }

        return configFromFile;
    }

    @Test
    void testParserFromExistingFile_shamProbability() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(70, configFromFile.getShamProbability());
    }

    @Test
    void testParserFromExistingFile_maxTeamSize() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getMaxTeamSize(), 13);
    }

    @Test
    void testParserFromExistingFile_maxPieces() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getMaxPieces(), 2);
    }

    @Test
    void testParserFromExistingFile_predefinedGoalPositions() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();

        List<Position> predefinedGoalPositionsTest = new ArrayList<>();
        predefinedGoalPositionsTest.add(new Position(11, 11));
        predefinedGoalPositionsTest.add(new Position(20, 20));
        predefinedGoalPositionsTest.add(new Position(1, 1));

        for (Position pos : predefinedGoalPositionsTest) {
            assertTrue(configFromFile.getPredefinedGoalPositions().contains(pos));
        }
    }

    @Test
    void testParserFromExistingFile_predefinedPiecePositions() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();

        List<Position> predefinedPiecePositionsTest = new ArrayList<>();
        predefinedPiecePositionsTest.add(new Position(10, 10));
        predefinedPiecePositionsTest.add(new Position(5, 15));
        predefinedPiecePositionsTest.add(new Position(10, 15));
        predefinedPiecePositionsTest.add(new Position(5, 20));

        for (Position pos : predefinedPiecePositionsTest) {
            assertTrue(configFromFile.getPredefinedPiecePositions().contains(pos));
        }
    }

    @Test
    void testParserFromExistingFile_boardWidth() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getBoardWidth(), 20);
    }

    @Test
    void testParserFromExistingFile_boardTaskHeight() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getBoardTaskHeight(), 10);
    }

    @Test
    void testParserFromExistingFile_boardGoalHeight() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getBoardGoalHeight(), 500);
    }

    @Test
    void testParserFromExistingFile_DelayDestroyPiece() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getDelayDestroyPiece(), 1001);
    }

    @Test
    void testParserFromExistingFile_DelayNextPiecePlace() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getDelayNextPiecePlace(), 2002);
    }

    @Test
    void testParserFromExistingFile_DelayMove() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getDelayMove(), 3003);
    }

    @Test
    void testParserFromExistingFile_DelayDiscover() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getDelayDiscover(), 4004);
    }

    @Test
    void testParserFromExistingFile_DelayTest() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getDelayTest(), 5005);
    }

    @Test
    void testParserFromExistingFile_DelayPick() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getDelayPick(), 6006);
    }

    @Test
    void testParserFromExistingFile_DelayPlace() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getDelayPlace(), 7007);
    }
}
