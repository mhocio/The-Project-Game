package pl.mini.projectgame.models;

/**
 * @author buensons
 */

public class Cell {
    private Position position;
    private BoardObject content;

    public Cell(Position position) {
        this.position = position;
        content = null;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public BoardObject getContent() {
        return content;
    }

    public void setContent(BoardObject content) {
        this.content = content;
    }
}
