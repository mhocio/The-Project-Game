package pl.mini.projectgame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pl.mini.projectgame.exceptions.EmptyTeamException;
import pl.mini.projectgame.exceptions.FullTeamException;
import pl.mini.projectgame.exceptions.TeamSquadChangeException;
import pl.mini.projectgame.models.Player;
import pl.mini.projectgame.models.Team;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TeamTests {
    Team team;

    @BeforeEach
    void initTeam() {
        team = new Team();
    }

    @Test
    void testTeamInit(){
        assertNotNull(team);
        assertEquals(0,team.getSize());
        assertNotNull(team.getPlayers());
        assertNull(team.getColor());
    }

    @Test
    void testSetColor(){
        team.setColor(Team.TeamColor.RED);
        assertEquals(team.getColor(), Team.TeamColor.RED);
    }

    @Test
    void testAddPlayer() throws TeamSquadChangeException, FullTeamException {
        Player toAdd=new Player();
        team.addPlayer(toAdd);
        assertNotNull(team.getPlayers().containsKey(toAdd));
    }

    @Test
    void testAddExtraPlayer() throws TeamSquadChangeException, FullTeamException {
        for(int i=0;i<4;i++)
            team.addPlayer(new Player());
        assertThrows(FullTeamException.class,()->team.addPlayer(new Player()));
    }

    @Test
    void testAddNullPlayer() throws TeamSquadChangeException, FullTeamException {
        int initSize=team.getSize();
        team.addPlayer(null);
        assertEquals(initSize,team.getSize());
    }

    @Test
    void testRemovePlayer() throws TeamSquadChangeException, FullTeamException, EmptyTeamException {
        team.addPlayer(new Player());
        Player toRemove=new Player();
        team.addPlayer(toRemove);
        assertTrue(team.getPlayers().containsKey(toRemove));
        team.removePlayer(toRemove);
        assertFalse(team.getPlayers().containsKey(toRemove));
    }

    @Test
    void testRemoveLastPlayer() throws TeamSquadChangeException, FullTeamException {
        Player toRemove=new Player();
        team.addPlayer(toRemove);
        assertThrows(EmptyTeamException.class,()->team.removePlayer(toRemove));
    }

    @Test
    void testRemoveNonExistingPlayer() throws TeamSquadChangeException, FullTeamException, EmptyTeamException {
        team.addPlayer(new Player());
        Player toRemove=new Player();
        team.removePlayer(toRemove);
        assertEquals(false,team.getPlayers().containsKey(toRemove));
    }

    @Test
    void removeNullPlayer() throws TeamSquadChangeException, FullTeamException, EmptyTeamException {
        team.addPlayer(new Player());
        int initSize=team.getSize();
        team.removePlayer(null);
        assertEquals(initSize,team.getSize());
    }

    @Test
    void testGetPlayers(){
        assertNotNull(team.getPlayers());
    }

    @Test
    void testGetRole() throws TeamSquadChangeException, FullTeamException {
        Player leader=new Player();
        Player member=new Player();
        team.addPlayer(leader);
        team.addPlayer(member);
        assertEquals(Team.TeamRole.LEADER,team.getPlayerRole(leader));
        assertEquals(Team.TeamRole.MEMBER,team.getPlayerRole(member));
    }

    @Test
    void testSetNewLeader() throws TeamSquadChangeException, FullTeamException {
        Player firstLeader=new Player();
        team.addPlayer(firstLeader);
        assertEquals(Team.TeamRole.LEADER,team.getPlayerRole(firstLeader));

        Player secondLeader=new Player();
        team.addPlayer(secondLeader);
        team.setPlayerRole(secondLeader, Team.TeamRole.LEADER);
        assertEquals(Team.TeamRole.LEADER,team.getPlayerRole(secondLeader));
        assertEquals(Team.TeamRole.MEMBER,team.getPlayerRole(firstLeader));
    }

    @Test
    void testGetLeader() throws TeamSquadChangeException, FullTeamException {
        Player leader=new Player();
        team.addPlayer(leader);
        assertEquals(leader,team.getLeader());
    }

    @Test
    void testRemoveLeader() throws FullTeamException, TeamSquadChangeException, EmptyTeamException {
        Player leader=new Player();
        Player newLeader=new Player();
        team.addPlayer(leader);
        team.addPlayer(newLeader);
        team.removePlayer(leader);
        assertEquals(team.getLeader(),newLeader);
        assertEquals(Team.TeamRole.LEADER,team.getPlayerRole(newLeader));
    }

}
