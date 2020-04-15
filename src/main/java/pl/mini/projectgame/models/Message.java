package pl.mini.projectgame.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author buensons
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Message {

    public enum Status { OK, DENIED, YES, NO };
    public enum Direction { UP, DOWN, LEFT, RIGHT };

    private String action;
    private Long playerGuid;
    private Status status;
    private Boolean test;
    private Position position;
//    private List<Field> fields;
    private Cell cell;
    private Direction direction;
}
