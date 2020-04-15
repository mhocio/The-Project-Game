package pl.mini.projectgame.models;

import lombok.Getter;
import lombok.Setter;
import pl.mini.projectgame.exceptions.DeniedMoveException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author buensons
 */
@Getter
@Setter
public class Board {

    private Map<Position, Cell> cells;
    private int width, height;
    private int goalAreaHeight;
    private int taskAreaHeight;

    public Board(int width, int goalAreaHeight, int taskAreaHeight) {
        this.goalAreaHeight = goalAreaHeight;
        this.taskAreaHeight = taskAreaHeight;
        this.width = width;
        this.height = 2 * goalAreaHeight + taskAreaHeight;

        generateCells();
    }

    private void generateCells() {
        cells = new HashMap<>();

        for(int w = 0; w < width; w++) {
            for(int h = 0; h < height; h++) {
                var position = new Position(w,h);
                cells.put(position, new Cell(position));
            }
        }
    }

    public synchronized void movePlayer(Player player, Position source, Position target)
            throws DeniedMoveException {

        if(!cells.get(source).getContent().containsValue(player)) {
            throw new DeniedMoveException("Requested object is not in the specified position!");
        }

        if(cells.get(target).getContent().containsKey(Player.class)) {
            throw new DeniedMoveException("Target cell is occupied by another player!");
        }

        cells.get(target).addContent(Player.class, player);
        cells.get(source).removeContent(Player.class);
    }
}
