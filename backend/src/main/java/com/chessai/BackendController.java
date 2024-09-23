package com.chessai;

import com.chessai.model.Chessboard;
import com.chessai.model.Move;
import com.chessai.model.Position;
import com.chessai.model.Position.Turn;
import com.chessai.util.ChessboardUtilityModule;
import com.chessai.util.EvaluationModule;
import com.chessai.util.OpeningsUtilityModule;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.chessai.util.ChessboardUtilityModule.*;

@Controller
@RequestMapping("/api")
public class BackendController {

  private final int DEPTH = 4;
  private boolean pestoInitialized = false;
  private int moveNumber = 1;
  private String notation = "";
  private static int positionsAnalyzed;
  private static long startTime;

  private final String[] OPENING_POSITION = {
          "wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR",
          "wP", "wP", "wP", "wP", "wP", "wP", "wP", "wP",
          "",   "",   "",   "",   "",   "",   "",   "",
          "",   "",   "",   "",   "",   "",   "",   "",
          "",   "",   "",   "",   "",   "",   "",   "",
          "",   "",   "",   "",   "",   "",   "",   "",
          "bP", "bP", "bP", "bP", "bP", "bP", "bP", "bP",
          "bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR",
  };

  public static boolean whiteKingHasMoved = false;
  public static boolean blackKingHasMoved = false;

  @RequestMapping(value = "/getComputerMove", method = RequestMethod.POST)
  @ResponseBody
  @CrossOrigin(origins = "http://localhost:3000")
  public String getComputerMove(@RequestBody Map<String, String> requestBody) {
    if (!pestoInitialized) {
      PestoEval.initTables();
      OpeningsUtilityModule.initOpeningLines();
      pestoInitialized = true;
    }

    Gson gson = new Gson();
    String json = requestBody.get("jsonPayload");
    String userMove = requestBody.get("notation");
    String hasUserKingMoved = requestBody.get("whiteKingHasMoved");
    String[] inputChessboard = gson.fromJson(json, String[].class);

    if (hasUserKingMoved.equals("true")) {
      whiteKingHasMoved = true;
    }

    if (countDifferences(inputChessboard, OPENING_POSITION) == 2) {
      moveNumber = 1;
      notation = "";
    }

    Chessboard chessboard = convertStringArrayToChessboard(inputChessboard);

    notation += moveNumber + ". " + userMove + " ";

    positionsAnalyzed = 0;
    startTime = System.currentTimeMillis();
    if (moveNumber < 20) {
      return generateMoveFromOpeningBook(chessboard, gson);
    } else {
      return generateMoveWithAlphaBeta(chessboard, gson);
    }
  }

  private String generateMoveWithAlphaBeta(Chessboard chessboard, Gson gson) {
    Map<String, Object> returnData = new HashMap<>();

    Position rootPosition = new Position(Turn.BLACK, chessboard, null);
    positionsAnalyzed++;
    int score = alphaBeta(rootPosition, DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

    Position bestChild = null;
    if (rootPosition.children.size() == 0) {
      returnData.put("gameStatus", "1-0");
      return gson.toJson(returnData);
    }

    for (Position child : rootPosition.children) {
      if (child.getScore() == score) {
        bestChild = child;
        break;
      }
    }

    if (bestChild == null) {
      bestChild = rootPosition.children.get(0);
    }
    Move bestMove = bestChild.lastMove;

    if (bestMove.getPieceType() == Chessboard.Piece.king) {
      blackKingHasMoved = true;
    }

    notation += bestChild.lastMove.getNotation() + " ";

    returnData.put("gameStatus", "ongoing");
    returnData.put("updatedBoard", convertChessboardToStringArray(bestChild.board));
    returnData.put("fromSquare", bestMove.getFromSquare());
    returnData.put("toSquare", bestMove.getToSquare());
    returnData.put("score", bestChild.getScore());
    returnData.put("positionsAnalyzed", formatNumberWithCommas(positionsAnalyzed));
    returnData.put("time", (System.currentTimeMillis() - startTime)/1000.0);

    moveNumber++;

    return gson.toJson(returnData);
  }

  private String formatNumberWithCommas(int number) {
    NumberFormat numberFormat = NumberFormat.getInstance();
    return numberFormat.format(number);
  }

  private String generateMoveFromOpeningBook(Chessboard chessboard, Gson gson) {
    Map<String, Object> returnData = new HashMap<>();

    String openingBookSuggestedMoveNotation = OpeningsUtilityModule.scanGames(notation);
    if (openingBookSuggestedMoveNotation.equals("") || openingBookSuggestedMoveNotation == null) {
      return generateMoveWithAlphaBeta(chessboard, gson);
    } else {
      Move openingBookSuggestedMove = ChessboardUtilityModule.convertNotationToMove(chessboard,
          openingBookSuggestedMoveNotation);

      if (openingBookSuggestedMove == null) {
        return generateMoveWithAlphaBeta(chessboard, gson);
      }

      chessboard.makeMove(openingBookSuggestedMove);

      notation += openingBookSuggestedMoveNotation + " ";

      returnData.put("gameStatus", "ongoing");
      returnData.put("updatedBoard", convertChessboardToStringArray(chessboard));
      returnData.put("fromSquare", openingBookSuggestedMove.getFromSquare());
      returnData.put("toSquare", openingBookSuggestedMove.getToSquare());
      returnData.put("score",
          EvaluationModule.boardEvaluation(new Position(Turn.WHITE, chessboard, openingBookSuggestedMove)));
      returnData.put("positionsAnalyzed", "Move from openings book.");

      moveNumber++;

      return gson.toJson(returnData);
    }
  }

  private static int alphaBeta(Position position, int depth, int alpha, int beta, boolean maximizingPlayer) {
    if (depth == 0) {
      return position.evaluate();
    }

    int score = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
    position.whiteKingHasMoved = whiteKingHasMoved;
    position.blackKingHasMoved = blackKingHasMoved;
    List<Move> legalMoves = position.getLegalMoves();
    position.legalMovesInThisPosition = legalMoves.size();

    for (Move legalMove : legalMoves) {
      Position childPos = convertMoveToPosition(legalMove, position);
      position.children.add(childPos);
      positionsAnalyzed++;
      int childScore = alphaBeta(childPos, depth - 1, alpha, beta, !maximizingPlayer);
      score = maximizingPlayer ? Math.max(score, childScore) : Math.min(score, childScore);
      if (maximizingPlayer) {
        alpha = Math.max(alpha, score);
        if (beta <= alpha) {
          break; // Beta cutoff
        }
      } else {
        beta = Math.min(beta, score);
        if (beta <= alpha) {
          break; // Alpha cutoff
        }
      }
    }

    position.setScore(score);
    return score;
  }
}
