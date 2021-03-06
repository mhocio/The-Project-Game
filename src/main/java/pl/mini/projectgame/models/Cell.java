package pl.mini.projectgame.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author buensons
 */

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Cell {
    private Position position;
    private int distance;
    @JsonIgnore
    private Map<Class<? extends BoardObject>, BoardObject> content;

    public Cell(Position position) {
        this.position = position;
        content = new HashMap<>();
    }

    private String cellState;
    private String playerGuid;

    public void addContent(Class<? extends BoardObject> boardClass, BoardObject object) {
        content.put(boardClass, object);
    }

    public void removeContent(Class<? extends BoardObject> boardClass) {
        content.remove(boardClass);
    }

    public int calculateDistance(Position piecePosition) {
        return Math.abs(position.getX() - piecePosition.getX()) + Math.abs(position.getY() - piecePosition.getY());
    }
}
