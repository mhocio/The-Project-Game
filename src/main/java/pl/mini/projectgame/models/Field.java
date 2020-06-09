package pl.mini.projectgame.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Field {

    private int x, y;
    private Position position;
    private Cell cell;

    public Field(Cell cell) {
        this.x = cell.getPosition().getX();
        this.y = cell.getPosition().getY();
        this.cell = cell;

        position = new Position();
        position.setX(x);
        position.setY(y);
    }
}