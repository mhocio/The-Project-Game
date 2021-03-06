package pl.mini.projectgame.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import pl.mini.projectgame.exceptions.EmptyTeamException;
import pl.mini.projectgame.exceptions.FullTeamException;
import pl.mini.projectgame.exceptions.TeamSquadChangeException;

import java.util.Hashtable;
import java.util.Map;

@Getter
@Setter
public class Team {
    private TeamColor teamColor;
    private int size;
    private long points;
    private int maxTeamSize;

    @JsonIgnore
    private Hashtable<Player, TeamRole> players;

    public Team() {
        players = new Hashtable<Player, TeamRole>();
        size = 0;
        teamColor = null;
        points = 0;
        maxTeamSize = 4;
    }

    public Team(TeamColor color) {
        players = new Hashtable<Player, TeamRole>();
        size = 0;
        teamColor = color;
        points = 0;
        maxTeamSize = 4;
    }

    public boolean isFull() {
        System.out.println(size + " " + maxTeamSize);
        return maxTeamSize == size;
    }

    public enum TeamColor {
        RED, BLUE
    }

    public enum TeamRole {
        LEADER, MEMBER
    }

    private boolean isReady() {
        for (Map.Entry<Player, TeamRole> entry : players.entrySet()) {
            if (!entry.getKey().isReady()) return false;
        }
        return true;
    }

    public void addPoints(int num) {
        points += num;
    }

    public int getSize() {
        return size;
    }

    public Hashtable<Player, TeamRole> getPlayers() {
        return players;
    }

    public void addPlayer(Player player)
            throws TeamSquadChangeException, FullTeamException {
        if (this.getSize() == maxTeamSize)
            throw new FullTeamException("This team is full!");
        if (player != null && !players.containsKey(player)) {
            // first player is the team leader
            if (size == 0)
                players.put(player, TeamRole.LEADER);
            else
                players.put(player, TeamRole.MEMBER);

            if (players.containsKey(player))
                size++;
            else
                throw new TeamSquadChangeException("Adding the member failed!");
        }
    }

    public void removePlayer(Player player)
            throws TeamSquadChangeException, EmptyTeamException {
        if (player != null && players.containsKey(player)) {
            players.remove(player);
            if (!players.containsKey(player)) {
                size--;
            } else
                throw new TeamSquadChangeException("Removing the member failed!");
        }
        if (this.getSize() == 0)
            throw new EmptyTeamException("This team is empty!");
        // pick next member to be the leader
        //TODO send message to GM about leader change
        if (this.getLeader() == null) {
            this.setPlayerRole((Player) players.keySet().toArray()[0], TeamRole.LEADER);
        }
    }

    public TeamColor getColor() {
        return teamColor;
    }

    public void setColor(TeamColor color) {
        teamColor = color;
    }

    public TeamRole getPlayerRole(Player player) {
        return players.get(player);
    }

    public void setPlayerRole(Player player, TeamRole role) {
        if (players.containsKey(player)) {
            if (role == TeamRole.LEADER && this.getLeader() != null)
                players.replace(this.getLeader(), TeamRole.MEMBER);
            players.replace(player, role);
        }
    }

    @JsonIgnore
    public Player getLeader() {
        Player leader = null;
        for (Map.Entry entry : players.entrySet()) {
            if (TeamRole.LEADER.equals(entry.getValue())) {
                leader = (Player) entry.getKey();
                break;
            }
        }
        return leader;
    }


}
