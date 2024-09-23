package com.chessai.model;

import static com.chessai.util.EvaluationModule.boardEvaluation;

import com.chessai.util.LegalMovesModule;
import java.util.ArrayList;
import java.util.List;

public class Position {

  public enum Turn {
    BLACK,
    WHITE,
  }

  private Turn turn;
  private int score;
  public List<Position> children;
  public Move lastMove;

  public Chessboard board;

  public int legalMovesInThisPosition;
  public boolean whiteKingHasMoved;
  public boolean blackKingHasMoved;

  public Position() {
    this.score = 0;
    this.turn = Turn.WHITE;
    this.lastMove = null;
    this.children = new ArrayList<>();
    this.board = null;
    this.legalMovesInThisPosition = 0;
    this.whiteKingHasMoved = false;
    this.blackKingHasMoved = false;
  }

  public Position(Turn turn, Chessboard board, Move lastMove) {
    this.turn = turn;
    this.board = board;
    this.children = new ArrayList<>();
    this.lastMove = lastMove;
  }

  public Turn getTurn() {
    return turn;
  }

  public void setTurn(Turn turn) {
    this.turn = turn;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public int evaluate() {
    this.score = boardEvaluation(this);
    return this.score;
  }



  public List<Move> getLegalMoves() {
    return LegalMovesModule.getLegalMoves(this);
  }
}
