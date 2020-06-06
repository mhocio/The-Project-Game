package pl.mini.projectgame.models;

import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * @author buensons
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Message {
    public enum Status {OK, DENIED, YES, NO}

    public enum Direction {UP, DOWN, LEFT, RIGHT}

    public enum placementResult {CORRECT, POINTLESS}

    private Player player;
    private Team.TeamColor teamColor;
    private Team.TeamRole teamRole;
    private boolean host;
    private UUID playerUuid;
    private String playerGuid;

    private String action;
    private Status status;
    private Boolean test;
    private placementResult placementResult;

    private Position position;
    private List<Field> fields;
    private List<Goal> goals;
    private Direction direction;
    private Board board;
}
