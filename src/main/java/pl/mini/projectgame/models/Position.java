package pl.mini.projectgame.models;

import lombok.*;

/**
 * @author buensons
 */

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class Position {
    private Integer x;
    private Integer y;

    public Position(String key) {
        String[] args = key.split("-");
        x = Integer.parseInt(args[0]);
        y = Integer.parseInt(args[1]);
    }

    @Override
    public String toString() {
        return x + "-" + y;
    }
}
