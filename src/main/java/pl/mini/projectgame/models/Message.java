package pl.mini.projectgame.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * @author buensons
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Message {
    public enum Status {OK, DENIED, YES, NO}

    public enum Direction {UP, DOWN, LEFT, RIGHT}

    private Player player;
    private Team.TeamColor teamColor;
    private Team.TeamRole teamRole;
    private UUID playerUuid;
    private String action;
    private Status status;
    private Boolean test;
    private Position position;
    private List<Field> fields;
    private Direction direction;
    private Board board;
}
