package com.chessai.model;

import com.chessai.util.ChessboardUtilityModule;

import static com.chessai.util.BitboardUtilityModule.generateBitboardFromIndex;

import java.util.HashMap;
import java.util.Map;

public class Chessboard {

  public Map<Piece, Long> bitboards;

  public enum Piece {
    pawn,
    knight,
    bishop,
    rook,
    queen,
    king,
    white,
    black,
    empty,
  }

  public Chessboard() {
    this.bitboards = new HashMap<>();
    this.bitboards.put(
        Piece.pawn,
        0b0000000000000000000000000000000000000000000000000000000000000000L
      );
    this.bitboards.put(
        Piece.knight,
        0b0000000000000000000000000000000000000000000000000000000000000000L
      );
    this.bitboards.put(
        Piece.bishop,
        0b0000000000000000000000000000000000000000000000000000000000000000L
      );
    this.bitboards.put(
        Piece.rook,
        0b0000000000000000000000000000000000000000000000000000000000000000L
      );
    this.bitboards.put(
        Piece.queen,
        0b0000000000000000000000000000000000000000000000000000000000000000L
      );
    this.bitboards.put(
        Piece.king,
        0b0000000000000000000000000000000000000000000000000000000000000000L
      );
    this.bitboards.put(
        Piece.white,
        0b0000000000000000000000000000000000000000000000000000000000000000L
      );
    this.bitboards.put(
        Piece.black,
        0b0000000000000000000000000000000000000000000000000000000000000000L
      );
    this.bitboards.put(
        Piece.empty,
        0b0000000000000000000000000000000000000000000000000000000000000000L
      );
  }

  public Chessboard(Chessboard old) {
    this.bitboards = new HashMap<>(old.bitboards);
  }

  public void makeMove(Move move) {
    if (move.isMoveLongCastle() || move.isMoveShortCastle()) {
      long kingBitboard = this.bitboards.get(Piece.king);
      long rooksBitboard = this.bitboards.get(Piece.rook);
      if (move.getPieceColor() == Position.Turn.WHITE) {
        if (move.isMoveShortCastle()) {
          bitboards.put(Piece.king, kingBitboard ^ ChessboardUtilityModule.WHITE_SHORT_CASTLE_NEW_KING_POSITION);
          bitboards.put(Piece.rook, rooksBitboard ^ ChessboardUtilityModule.WHITE_SHORT_CASTLE_NEW_ROOK_POSITION);
        } else if (move.isMoveLongCastle()) {
          bitboards.put(Piece.king, kingBitboard ^ ChessboardUtilityModule.WHITE_LONG_CASTLE_NEW_KING_POSITION);
          bitboards.put(Piece.rook, rooksBitboard ^ ChessboardUtilityModule.WHITE_LONG_CASTLE_NEW_ROOK_POSITION);
        }
        bitboards.put(Piece.white, bitboards.get(Piece.white) |
                ((bitboards.get(Piece.rook)) | bitboards.get(Piece.king) ^ bitboards.get(Piece.black)));
      } else {
        if (move.isMoveShortCastle()) {
          this.bitboards.put(Piece.king, kingBitboard ^ ChessboardUtilityModule.BLACK_SHORT_CASTLE_NEW_KING_POSITION);
          this.bitboards.put(Piece.rook, rooksBitboard ^ ChessboardUtilityModule.BLACK_SHORT_CASTLE_NEW_ROOK_POSITION);
        } else if (move.isMoveLongCastle()) {
          this.bitboards.put(Piece.king, kingBitboard ^ ChessboardUtilityModule.BLACK_LONG_CASTLE_NEW_KING_POSITION);
          this.bitboards.put(Piece.rook, rooksBitboard ^ ChessboardUtilityModule.BLACK_LONG_CASTLE_NEW_ROOK_POSITION);
        }
        bitboards.put(Piece.black, bitboards.get(Piece.black) |
                ((bitboards.get(Piece.rook)) | bitboards.get(Piece.king) ^ bitboards.get(Piece.white)));
      }
      bitboards.put(Piece.empty, ~(bitboards.get(Chessboard.Piece.white) | bitboards.get(Chessboard.Piece.black)));
    } else {
      long fromMask = 1L << move.getFromSquare();
      long toMask = 1L << move.getToSquare();

      Chessboard.Piece pieceType = move.getPieceType();
      Chessboard.Piece pieceColor = move.getPieceColor() == Position.Turn.WHITE
              ? Chessboard.Piece.white
              : Chessboard.Piece.black;

      long pieceTypeBitboard = this.bitboards.get(pieceType);
      pieceTypeBitboard &= (~fromMask);
      pieceTypeBitboard |= toMask;
      this.bitboards.put(pieceType, pieceTypeBitboard);

      long pieceColorBitboard = this.bitboards.get(pieceColor);
      pieceColorBitboard &= (~fromMask);
      pieceColorBitboard |= toMask;
      this.bitboards.put(pieceColor, pieceColorBitboard);

      Chessboard.Piece capturedPiece = getPieceType(
              move.getToSquare(),
              move.getPieceColor() == Position.Turn.WHITE
                      ? Position.Turn.BLACK
                      : Position.Turn.WHITE
      );

      if (capturedPiece != Chessboard.Piece.empty) {
        long capturedPieceBitboard = this.bitboards.get(capturedPiece);
        capturedPieceBitboard &= (~toMask);

        // ensure that the same piece's bitboard is not overwritten/toggled twice
        if (capturedPiece != pieceType) this.bitboards.put(
                capturedPiece,
                capturedPieceBitboard
        );

        Chessboard.Piece opponentColor = move.getPieceColor() ==
                Position.Turn.WHITE
                ? Chessboard.Piece.black
                : Chessboard.Piece.white;
        long opponentColorBitboard = this.bitboards.get(opponentColor);
        opponentColorBitboard &= (~toMask);
        this.bitboards.put(opponentColor, opponentColorBitboard);
      }

      this.bitboards.put(
              Chessboard.Piece.empty,
              ~(
                      this.bitboards.get(Chessboard.Piece.white) |
                              this.bitboards.get(Chessboard.Piece.black)
              )
      );
    }
  }

  private Chessboard.Piece getPieceType(
    int index,
    Position.Turn opponentColor
  ) {
    long capturedPieceBitboard = generateBitboardFromIndex(index);
    long opponentBitboard = opponentColor == Position.Turn.WHITE
      ? this.bitboards.get(Chessboard.Piece.white)
      : this.bitboards.get(Chessboard.Piece.black);
    long opponentPawnBitboard =
      opponentBitboard & this.bitboards.get(Chessboard.Piece.pawn);
    long opponentKnightBitboard =
      opponentBitboard & this.bitboards.get(Chessboard.Piece.knight);
    long opponentBishopBitboard =
      opponentBitboard & this.bitboards.get(Chessboard.Piece.bishop);
    long opponentRookBitboard =
      opponentBitboard & this.bitboards.get(Chessboard.Piece.rook);
    long opponentQueenBitboard =
      opponentBitboard & this.bitboards.get(Chessboard.Piece.queen);
    long opponentKingBitboard =
      opponentBitboard & this.bitboards.get(Chessboard.Piece.king);

    if ((capturedPieceBitboard & opponentPawnBitboard) != 0) {
      return Chessboard.Piece.pawn;
    } else if ((capturedPieceBitboard & opponentKnightBitboard) != 0) {
      return Chessboard.Piece.knight;
    } else if ((capturedPieceBitboard & opponentBishopBitboard) != 0) {
      return Chessboard.Piece.bishop;
    } else if ((capturedPieceBitboard & opponentRookBitboard) != 0) {
      return Chessboard.Piece.rook;
    } else if ((capturedPieceBitboard & opponentQueenBitboard) != 0) {
      return Chessboard.Piece.queen;
    } else if ((capturedPieceBitboard & opponentKingBitboard) != 0) {
      return Chessboard.Piece.king;
    } else {
      return Chessboard.Piece.empty;
    }
  }
}
