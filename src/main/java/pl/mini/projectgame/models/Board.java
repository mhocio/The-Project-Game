package pl.mini.projectgame.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.exceptions.DeniedMoveException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author buensons
 */
@Getter
@Setter
@ToString
public class Board {

    protected Map<Position, Cell> cells;
    protected int width, height;
    protected int goalAreaHeight;
    protected int taskAreaHeight;

    @Autowired
    public Board(GameMasterConfiguration config) {
        goalAreaHeight = config.getBoardGoalHeight();
        taskAreaHeight = config.getBoardTaskHeight();
        width = config.getBoardWidth();
        height = 2 * goalAreaHeight + taskAreaHeight;

        generateCells();
    }

    protected void generateCells() {
        cells = new HashMap<>();

        for(int w = 0; w < width; w++) {
            for(int h = 0; h < height; h++) {
                var position = new Position(w,h);
                cells.put(position, new Cell(position));
            }
        }
    }
}
