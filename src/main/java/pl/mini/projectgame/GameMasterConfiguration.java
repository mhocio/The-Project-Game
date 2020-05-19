package pl.mini.projectgame;

import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Component;
import pl.mini.projectgame.models.Position;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
@Component
public class GameMasterConfiguration {

    int shamProbability;
    int maxTeamSize;
    int maxPieces;
    List<Position> predefinedGoalPositions;
    List<Position> predefinedPiecePositions;

    int boardWidth;
    int boardTaskHeight;
    int boardGoalHeight;

    int DelayDestroyPiece;
    int DelayNextPiecePlace;
    int DelayMove;
    int DelayDiscover;
    int DelayTest;
    int DelayPick;
    int DelayPlace;

    void defaultConfiguration() {
        shamProbability = 50;
        maxTeamSize = 4;
        maxPieces = 3;

        boardWidth = 10;
        boardTaskHeight = 30;
        boardGoalHeight = 5;

        DelayDestroyPiece = 2950;
        DelayNextPiecePlace = 3000;
        DelayMove = 100;
        DelayDiscover = 500;
        DelayTest = 1000;
        DelayPick = 100;
        DelayPlace = 100;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("shamProbability: ").append(shamProbability).append("\n");
        ret.append("maxTeamSize: ").append(maxTeamSize).append("\n");
        ret.append("maxPieces: ").append(maxPieces).append("\n");

        ret.append("predefinedGoalPositions: ");
        for (Position pos : predefinedGoalPositions) {
            ret.append("( ").append(pos.getX()).append(", ").append(pos.getY()).append(" ), ");
        }
        ret.append("\n");

        ret.append("boardWidth: ").append(boardWidth).append("\n");
        ret.append("boardTaskHeight: ").append(boardTaskHeight).append("\n");
        ret.append("boardGoalHeight: ").append(boardGoalHeight).append("\n");
        ret.append("DelayDestroyPiece: ").append(DelayDestroyPiece).append("\n");
        ret.append("DelayNextPiecePlace: ").append(DelayNextPiecePlace).append("\n");
        ret.append("DelayMove: ").append(DelayMove).append("\n");
        ret.append("DelayDiscover: ").append(DelayDiscover).append("\n");
        ret.append("DelayTest: ").append(DelayTest).append("\n");
        ret.append("DelayPick: ").append(DelayPick).append("\n");
        ret.append("DelayPlace: ").append(DelayPlace).append("\n");
        return ret.toString();
    }

    public GameMasterConfiguration() {
        this.defaultConfiguration();
        predefinedGoalPositions = new ArrayList<Position>();
        predefinedGoalPositions.add(new Position(15, 15));
    }

    public void configureFromFile(String filePath) {
        this.defaultConfiguration();
        predefinedGoalPositions = new ArrayList<>();
        predefinedPiecePositions = new ArrayList<>();

        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        try {
            Object obj = parser.parse(new FileReader(filePath));
            jsonObject = (JSONObject) obj;

            try {
                shamProbability = Math.toIntExact((long) jsonObject.get("shamProbability"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                maxTeamSize = Math.toIntExact((long) jsonObject.get("maxTeamSize"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                maxPieces = Math.toIntExact((long) jsonObject.get("maxPieces"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                boardWidth = Math.toIntExact((long) jsonObject.get("boardWidth"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                boardTaskHeight = Math.toIntExact((long) jsonObject.get("boardTaskHeight"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                boardGoalHeight = Math.toIntExact((long) jsonObject.get("boardGoalHeight"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                DelayDestroyPiece = Math.toIntExact((long) jsonObject.get("DelayDestroyPiece"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                DelayNextPiecePlace = Math.toIntExact((long) jsonObject.get("DelayNextPiecePlace"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                DelayMove = Math.toIntExact((long) jsonObject.get("DelayMove"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                DelayDiscover = Math.toIntExact((long) jsonObject.get("DelayDiscover"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                DelayTest = Math.toIntExact((long) jsonObject.get("DelayTest"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                DelayPick = Math.toIntExact((long) jsonObject.get("DelayPick"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                DelayPlace = Math.toIntExact((long) jsonObject.get("DelayPlace"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            JSONArray PositionsJSONArray = (JSONArray) jsonObject.get("predefinedGoalPositions");
            Iterator<JSONObject> goalPositionsIterator = PositionsJSONArray.iterator();

            while (goalPositionsIterator.hasNext()) {
                JSONObject PosJSON = goalPositionsIterator.next();
                Position pos = new Position(Math.toIntExact((long) PosJSON.get("x")), Math.toIntExact((long) PosJSON.get("y")));
                predefinedGoalPositions.add(pos);
            }

            PositionsJSONArray = (JSONArray) jsonObject.get("predefinedPiecePositions");
            goalPositionsIterator = PositionsJSONArray.iterator();

            while (goalPositionsIterator.hasNext()) {
                JSONObject PosJSON = goalPositionsIterator.next();
                Position pos = new Position(Math.toIntExact((long) PosJSON.get("x")), Math.toIntExact((long) PosJSON.get("y")));
                predefinedPiecePositions.add(pos);
            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
