package pl.mini.projectgame.models;

import lombok.*;

import java.util.List;

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

    public enum Direction {Up, Down, Left, Right}

    public enum placementResult {Correct, Pointless}

    private Player player;
    private Team.TeamColor teamColor;
    private String team;
    private Team.TeamRole teamRole;
    private List<String> teamGuids;

    private boolean host;
    private String playerUuid;
    private String playerGuid;
    private int portNumber;

    private String action;
    private Status status;
    private Boolean test;
    private placementResult placementResult;

    private Position position;
    private List<Field> fields;
    private List<Goal> goals;
    private Direction direction;
    private Board board;

    private String result;
}
