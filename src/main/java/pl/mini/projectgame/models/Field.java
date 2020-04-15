package pl.mini.projectgame.models;

import lombok.Getter;
        import lombok.Setter;

@Getter
@Setter
public class Field {

    private int x, y;
    private Cell cell;

    public Field(Cell cell) {
        this.x = cell.getPosition().getX();
        this.y = cell.getPosition().getY();
        this.cell = cell;
    }
}