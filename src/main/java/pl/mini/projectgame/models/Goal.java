package pl.mini.projectgame.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Goal extends BoardObject {
    // TODO: create 3 states for discovered:
    //  NOT_DISCOVERED, DISCOVERED_NON_GOAL and DISCOVERED_GOAL
    public enum goalDiscover {
        NOT_DISCOVERED, DISCOVERED_NON_GOAL, DISCOVERED_GOAL
    }

    private goalDiscover discovered;
    @JsonIgnore
    private boolean finished = false;
    private boolean real;
    @JsonIgnore
    private Team team;

    public Goal(boolean real, Position pos) {
        this.real = real;
        if (real) {
            discovered = goalDiscover.NOT_DISCOVERED;
        } else {
            discovered = goalDiscover.DISCOVERED_NON_GOAL;
        }
        setPosition(pos);
    }

    public Goal(boolean real, Position pos, Team _team) {
        this.real = real;
        if (real) {
            discovered = goalDiscover.NOT_DISCOVERED;
        } else {
            discovered = goalDiscover.DISCOVERED_NON_GOAL;
        }
        setPosition(pos);
        team = _team;
    }
}
