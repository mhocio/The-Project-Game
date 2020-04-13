package pl.mini.projectgame.models;

import pl.mini.projectgame.exceptions.InvalidTeamSizeException;

import java.util.Hashtable;
import java.util.Map;

public class Team {
    private TeamColor teamColor;
    //public TeamRole teamRole;
    private int size;
    private Hashtable<Player, TeamRole> players;

    public Team() {
        players = new Hashtable<Player, TeamRole>();
        size = 0;
        teamColor = null;
    }

    public enum TeamColor {
        RED, BLUE;
    }

    public enum TeamRole {
        LEADER, MEMBER
    }

    public int getSize() {
        return size;
    }

    public Hashtable getPlayers() {
        return players;
    }

    public void addPlayer(Player player)
            throws InvalidTeamSizeException {
        if (this.getSize() == 4)
            throw new InvalidTeamSizeException("This team is full!");
        if (player != null && !players.containsKey(player)) {
            players.put(player, TeamRole.MEMBER);
            size++;
        }
    }

    public void removePlayer(Player player)
        throws InvalidTeamSizeException{
        if (player != null && players.containsKey(player)) {
            players.remove(player);
            size--;
        }
        if(this.getSize()==0)
            throw new InvalidTeamSizeException("This team is empty!");
    }

    public TeamColor getColor() {
        return teamColor;
    }

    public void setColor(TeamColor color) {
        teamColor = color;
    }

    public TeamRole getRole(Player player) {
        return players.get(player);
    }

    public void setRole(Player player, TeamRole role) {
        if (players.containsKey(player)) {
            if (role == TeamRole.LEADER && this.getLeader() != null)
                players.replace(this.getLeader(), TeamRole.MEMBER);
            players.replace(player, role);
        }
    }

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
