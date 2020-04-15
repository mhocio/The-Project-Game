package pl.mini.projectgame;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.lang.Math;

import org.springframework.stereotype.Component;
import pl.mini.projectgame.models.*;

@Component
public class GameMasterConfiguration {

    double shamProbability;
    int maxTeamSize;
    int maxPieces;
    List<Position> predefinedGoalPositions;

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
        shamProbability = 0.5;
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
        String ret = "";
        ret += "shamProbability: " + shamProbability + "\n";
        ret += "maxTeamSize: " + maxTeamSize + "\n";
        ret += "maxPieces: " + maxPieces + "\n";
        
        ret += "predefinedGoalPositions: ";
        for (Position pos: predefinedGoalPositions) {
            ret += "( " + pos.getX() + ", " + pos.getY() + " ), ";
        }
        ret += "\n";
        
        ret += "boardWidth: " + boardWidth + "\n";
        ret += "boardTaskHeight: " + boardTaskHeight + "\n";
        ret += "boardGoalHeight: " + boardGoalHeight + "\n";
        ret += "DelayDestroyPiece: " + DelayDestroyPiece + "\n";
        ret += "DelayNextPiecePlace: " + DelayNextPiecePlace + "\n";
        ret += "DelayMove: " + DelayMove + "\n";
        ret += "DelayDiscover: " + DelayDiscover + "\n";
        ret += "DelayTest: " + DelayTest + "\n";
        ret += "DelayPick: " + DelayPick + "\n";
        ret += "DelayPlace: " + DelayPlace + "\n";
        return ret;
    }

    public GameMasterConfiguration() {
        this.defaultConfiguration();
        predefinedGoalPositions = new ArrayList<Position>();
        predefinedGoalPositions.add(new Position(15, 15));
    }

    public GameMasterConfiguration(String filePath) {
        this.defaultConfiguration();
        predefinedGoalPositions = new ArrayList<Position>();

        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        try {
            Object obj = parser.parse(new FileReader(filePath));
            jsonObject = (JSONObject) obj;

            try {
                shamProbability = (double) jsonObject.get("shamProbability");
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

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
