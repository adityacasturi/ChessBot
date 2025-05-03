package com.chessai.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpeningsUtilityModule {
    public static List<String> openingLines;

    public static void initOpeningLines() {
        openingLines = new ArrayList<>();
        // Download the latest lichess-elite.txt file and add its path here.
        String filePath = "/Users/adityacasturi/Documents/Projects/ChessAI/backend/src/main/java" +
                "/com/chessai/data/lichess-elite.txt";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String openingLine = "";

            while ((openingLine = reader.readLine()) != null) {
                openingLines.add(openingLine);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String scanGames(String currentGameNotation) {
        Collections.shuffle(openingLines);

        for (int i = 0; i < openingLines.size(); i++) {
            String openingLine = openingLines.get(i);
            if (openingLine.startsWith(currentGameNotation)) {
                String openingMove = openingLine.substring(currentGameNotation.length());

                String[] openingMoves = openingMove.split(" ");
                openingMove = openingMoves[0];
                String gameOutcome = openingMoves[openingMoves.length - 1];
                return openingMove;
            }
        }

        return "";
    }
}
