package pl.mini.projectgame;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.Assert.assertEquals;

@SpringBootTest
public class GameMasterConfigurationTests {

    GameMasterConfiguration createTestConfigFromFile() {
        File file = new File(
                ProjectGameApplication.class.getClassLoader().getResource("gameMasterTestConfiguration.json").getFile()
        );
        GameMasterConfiguration configFromFile = new GameMasterConfiguration();
        configFromFile.configureFromFile(file.getPath());

        return configFromFile;
    }

    @Test
    void testParserFromExistingFile_shamProbability() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.shamProbability, 0.7, 0.0);
    }

    @Test
    void testParserFromExistingFile_maxTeamSize() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.maxTeamSize,  12);
    }

    @Test
    void testParserFromExistingFile_maxPieces() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.maxPieces, 2);
    }

    /* TODO: Position needs method equals
    @Test
    void testParserFromExistingFile_predefinedGoalPositions() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();

        List<Position> predefinedGoalPositionsTest = new ArrayList<Position>();
        predefinedGoalPositionsTest.add(new Position(11, 11));
        predefinedGoalPositionsTest.add(new Position(20, 20));
        predefinedGoalPositionsTest.add(new Position(1, 1));
        Collections.sort(predefinedGoalPositionsTest);

        assertThat(configFromFile.predefinedGoalPositions, is(predefinedGoalPositionsTest));
    }
     */

    @Test
    void testParserFromExistingFile_boardWidth() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.boardWidth, 20);
    }

    @Test
    void testParserFromExistingFile_boardTaskHeight() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.boardTaskHeight, 10);
    }

    @Test
    void testParserFromExistingFile_boardGoalHeight() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.boardGoalHeight, 500);
    }

    @Test
    void testParserFromExistingFile_DelayDestroyPiece() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.DelayDestroyPiece, 1001);
    }

    @Test
    void testParserFromExistingFile_DelayNextPiecePlace() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.DelayNextPiecePlace, 2002);
    }

    @Test
    void testParserFromExistingFile_DelayMove() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.DelayMove, 3003);
    }

    @Test
    void testParserFromExistingFile_DelayDiscover() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.DelayDiscover, 4004);
    }

    @Test
    void testParserFromExistingFile_DelayTest() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.DelayTest, 5005);
    }

    @Test
    void testParserFromExistingFile_DelayPick() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.DelayPick, 6006);
    }

    @Test
    void testParserFromExistingFile_DelayPlace() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.DelayPlace, 7007);
    }
}
