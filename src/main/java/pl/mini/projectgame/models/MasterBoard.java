package pl.mini.projectgame.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.exceptions.DeniedMoveException;

@Component
public class MasterBoard extends Board {

    @Autowired
    public MasterBoard(GameMasterConfiguration config) {
        super(config);
    }

    public Cell getCellByPosition(Position position) {
        return cells.get(position);
    }

    public synchronized void movePlayer(Player player, Position source, Position target)
            throws DeniedMoveException {

        if(!cells.get(source).getContent().containsValue(player)) {
            throw new DeniedMoveException("Requested object is not in the specified position!");
        }

        if(cells.get(target).getContent().containsKey(Player.class)) {
            throw new DeniedMoveException("Target cell is occupied by another player!");
        }

        if(!cells.containsKey(target)) {
            throw new DeniedMoveException("You tried to move outside of board bounds!");
        }

        cells.get(target).addContent(Player.class, player);
        cells.get(source).removeContent(Player.class);
    }
}
