package pl.mini.projectgame.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.ProjectGameApplication;

import java.io.File;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
        assertEquals(configFromFile.getShamProbability(), 0.7, 0.0);
    }

    @Test
    void testParserFromExistingFile_maxTeamSize() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getMaxTeamSize(),  12);
    }

    @Test
    void testParserFromExistingFile_maxPieces() {
        GameMasterConfiguration configFromFile = createTestConfigFromFile();
        assertEquals(configFromFile.getMaxPieces(), 2);
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
