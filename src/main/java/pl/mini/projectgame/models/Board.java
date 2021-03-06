package pl.mini.projectgame.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.utilities.PositionKeyDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author buensons
 */
@Getter
@Setter
public class Board {

    @JsonDeserialize(keyUsing = PositionKeyDeserializer.class)
    @JsonIgnore
    protected Map<Position, Cell> cells;
    protected int boardWidth, height;
    protected int goalAreaHeight;
    protected int taskAreaHeight;

    @Autowired
    public Board(GameMasterConfiguration config) {
        goalAreaHeight = config.getBoardGoalHeight();
        taskAreaHeight = config.getBoardTaskHeight();
        boardWidth = config.getBoardWidth();
        height = 2 * goalAreaHeight + taskAreaHeight;

        generateCells();
    }

    public void configure(GameMasterConfiguration config) {
        goalAreaHeight = config.getBoardGoalHeight();
        taskAreaHeight = config.getBoardTaskHeight();
        boardWidth = config.getBoardWidth();
        height = 2 * goalAreaHeight + taskAreaHeight;

        generateCells();
    }

    public Cell getCellByPosition(Position position) {
        return cells.get(position);
    }

    protected void generateCells() {
        cells = new HashMap<>();

        for (int w = 0; w < boardWidth; w++) {
            for (int h = 0; h < height; h++) {
                var position = new Position(w, h);
                cells.put(position, new Cell(position));
            }
        }
    }

    public synchronized void addBoardObject(BoardObject object, Position position) {
        var cell = cells.get(position);

        if(cell == null || object == null) return;
        cell.addContent(object.getClass(), object);
    }
}
