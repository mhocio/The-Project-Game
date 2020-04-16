package pl.mini.projectgame.models;

import lombok.*;

import java.util.Objects;

/**
 * @author buensons
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Position {
    private int x;
    private int y;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x &&
                y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
