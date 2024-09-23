package com.chessai.model;

import static com.chessai.model.Chessboard.Piece.*;
import static com.chessai.util.ChessboardUtilityModule.getPieceCharFromPieceType;
import static com.chessai.util.ChessboardUtilityModule.squares;

public class Move {

  private int fromSquare;
  private int toSquare;
  private Chessboard.Piece pieceType;
  private Position.Turn pieceColor;
  private boolean isCapture;
  private boolean isMoveShortCastle;
  private boolean isMoveLongCastle;

  public Move(
      int fromSquare,
      int toSquare,
      Chessboard.Piece pieceType,
      Position.Turn pieceColor,
      boolean isCapture,
      boolean isMoveShortCastle,
      boolean isMoveLongCastle) {
    this.fromSquare = fromSquare;
    this.toSquare = toSquare;
    this.pieceType = pieceType;
    this.pieceColor = pieceColor;
    this.isCapture = isCapture;
    this.isMoveShortCastle = isMoveShortCastle;
    this.isMoveLongCastle = isMoveLongCastle;
  }

  public Move(Move old) {
    this.fromSquare = old.getFromSquare();
    this.toSquare = old.getToSquare();
    this.pieceType = old.getPieceType();
    this.pieceColor = old.getPieceColor();
    this.isCapture = old.isCapture();
  }

  public int getFromSquare() {
    return fromSquare;
  }

  public void setFromSquare(int fromSquare) {
    this.fromSquare = fromSquare;
  }

  public int getToSquare() {
    return toSquare;
  }

  public void setToSquare(int toSquare) {
    this.toSquare = toSquare;
  }

  public Chessboard.Piece getPieceType() {
    return pieceType;
  }

  public void setPieceType(Chessboard.Piece pieceType) {
    this.pieceType = pieceType;
  }

  public Position.Turn getPieceColor() {
    return pieceColor;
  }

  public void setPieceColor(Position.Turn pieceColor) {
    this.pieceColor = pieceColor;
  }

  public boolean isCapture() {
    return isCapture;
  }

  public String getNotation() {
    String notation = "";

    if (isMoveShortCastle) {
      return "O-O";
    } else if (isMoveLongCastle) {
      return "O-O-O";
    }

    if (!pieceType.equals(pawn)) {
      notation += getPieceCharFromPieceType(this.pieceType);
    }

    if (this.isCapture) {
      notation += "x";
    }

    notation += squares[this.toSquare];

    return notation;
  }

  public boolean isMoveShortCastle() {
    return isMoveShortCastle;
  }

  public boolean isMoveLongCastle() {
    return isMoveLongCastle;
  }
}
