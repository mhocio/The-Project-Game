package pl.mini.projectgame.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * @author buensons
 */

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Cell {
    private Position position;
    private Map<Class<? extends BoardObject>, BoardObject> content;

    public Cell(Position position) {
        this.position = position;
        content = new HashMap<>();
    }

    public void addContent(Class<? extends BoardObject> boardClass, BoardObject object) {
        content.put(boardClass, object);
    }

    public void removeContent(Class<? extends BoardObject> boardClass) {
        content.remove(boardClass);
    }
}
