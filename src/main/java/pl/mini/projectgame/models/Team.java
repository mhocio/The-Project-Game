package pl.mini.projectgame.models;

import java.util.Hashtable;
import java.util.Map;

public class Team {
    public TeamColor teamColor;
    //public TeamRole teamRole;
    public int size;
    private Hashtable<Player, TeamRole> players;

    public Team(){
        players=new Hashtable<Player, TeamRole>();
        size=0;
    }

    private enum TeamColor{
        RED,BLUE;
    }

    private enum TeamRole{
        LEADER,MEMBER
    }

    public Hashtable getPlayers(){
        return players;
    }

    public void addPlayer(Player player){
        if(!players.containsKey(player))
        {
            players.put(player,null);
            size++;
        }
    }

    public void removePlayer(Player player){
        if(!players.containsKey(player))
        {
            players.remove(player);
            size--;
        }
    }

    public TeamColor getColor(){
        return teamColor;
    }

    public void setColor(TeamColor color){
        teamColor=color;
    }

    public TeamRole getRole(Player player){
        return players.get(player);
    }

    public void setRole(Player player, TeamRole role){
        if(players.containsKey(player))
            players.replace(player,role);
    }

    public Player getLeader() {
        Player leader=null;
        for (Map.Entry entry : players.entrySet()) {
            if (TeamRole.LEADER.equals(entry.getValue())) {
                leader = entry.getKey();
                break;
            }
        }
        return leader;
    }


}
