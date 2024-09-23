package com.chessai.util;

import static com.chessai.util.BitboardUtilityModule.generateBitboardFromIndex;

import java.util.List;

import com.chessai.model.Chessboard;
import com.chessai.model.Chessboard.Piece;
import com.chessai.model.Move;
import com.chessai.model.Position;
import com.chessai.model.Position.Turn;

public class ChessboardUtilityModule {
    public static final long WHITE_SHORT_CASTLE_NEW_KING_POSITION = 0b0000000000000000000000000000000000000000000000000000000001010000L;
    public static final long WHITE_SHORT_CASTLE_NEW_ROOK_POSITION = 0b0000000000000000000000000000000000000000000000000000000010100000L;
    public static final long WHITE_LONG_CASTLE_NEW_KING_POSITION = 0b0000000000000000000000000000000000000000000000000000000000010100L;
    public static final long WHITE_LONG_CASTLE_NEW_ROOK_POSITION = 0b0000000000000000000000000000000000000000000000000000000000001001L;
    public static final long BLACK_SHORT_CASTLE_NEW_KING_POSITION = 0b0101000000000000000000000000000000000000000000000000000000000000L;
    public static final long BLACK_SHORT_CASTLE_NEW_ROOK_POSITION = 0b1010000000000000000000000000000000000000000000000000000000000000L;
    public static final long BLACK_LONG_CASTLE_NEW_KING_POSITION = 0b0001010000000000000000000000000000000000000000000000000000000000L;
    public static final long BLACK_LONG_CASTLE_NEW_ROOK_POSITION = 0b0000100100000000000000000000000000000000000000000000000000000000L;
    public static final String[] squares = {
            "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
            "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
            "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
            "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
            "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
            "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
            "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
            "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8"
    };

    public static int countDifferences(String[] array1, String[] array2) {
        int count = 0;

        if (array1.length != array2.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }

        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != null && !array1[i].equals(array2[i])) {
                count++;
            }
        }

        return count;
    }

    public static Chessboard.Piece getPieceTypeFromChar(char c) {
        return switch (c) {
            case 'P' -> Chessboard.Piece.pawn;
            case 'N' -> Chessboard.Piece.knight;
            case 'B' -> Chessboard.Piece.bishop;
            case 'R' -> Chessboard.Piece.rook;
            case 'Q' -> Chessboard.Piece.queen;
            case 'K' -> Chessboard.Piece.king;
            case 'w' -> Chessboard.Piece.white;
            case 'b' -> Chessboard.Piece.black;
            default -> null;
        };
    }

    public static String getPieceCharFromPieceType(Piece p) {
        return switch (p) {
            case pawn -> "";
            case knight -> "N";
            case bishop -> "B";
            case rook -> "R";
            case queen -> "Q";
            case king -> "K";
            default -> null;
        };
    }

    public static Move convertNotationToMove(Chessboard board, String moveNotation) {
        if (moveNotation.equals("O-O")) {
            return new Move(0, 0, Piece.king, Turn.BLACK, false, true, false);
        } else if (moveNotation.equals("O-O-O")) {
            return new Move(0, 0, Piece.king, Turn.BLACK, false, false, true);
        }

        Piece piece = null;
        switch (moveNotation.charAt(0)) {
            case 'N' -> piece = Chessboard.Piece.knight;
            case 'B' -> piece = Chessboard.Piece.bishop;
            case 'R' -> piece = Chessboard.Piece.rook;
            case 'Q' -> piece = Chessboard.Piece.queen;
            case 'K' -> piece = Chessboard.Piece.king;
            default -> piece = Chessboard.Piece.pawn;
        }

        // get last two characters of move notation
        String to = moveNotation.substring(moveNotation.length() - 2);
        int toSquare = java.util.Arrays.asList(squares).indexOf(to);
        boolean isCapture = moveNotation.contains("x");

        List<Move> legalMoves = LegalMovesModule.getLegalMoves(new Position(Turn.BLACK, board, null));
        for (Move move : legalMoves) {
            if (piece == move.getPieceType() && toSquare == move.getToSquare() && isCapture == move.isCapture()) {
                return move;
            }
        }

        return null;
    }

    public static Position convertMoveToPosition(Move move, Position position) {
        Turn newTurn = position.getTurn() == Turn.WHITE ? Turn.BLACK : Turn.WHITE;
        Position childPos = new Position();

        childPos.setTurn(newTurn);
        childPos.board = new Chessboard(position.board);
        childPos.lastMove = move;
        childPos.board.makeMove(move);

        return childPos;
    }

    public static Chessboard convertStringArrayToChessboard(String[] boardArray) {
        Chessboard board = new Chessboard();
        long whitePieces = 0L;
        long blackPieces = 0L;

        for (int i = 0; i < boardArray.length; i++) {
            String piece = boardArray[i];
            if (piece == null || piece.trim().isEmpty()) {
                continue;
            }
            Chessboard.Piece pieceType = getPieceTypeFromChar(piece.charAt(1));
            long bitboard = generateBitboardFromIndex(i);
            if (piece.charAt(0) == 'w') {
                whitePieces |= bitboard;
            } else {
                blackPieces |= bitboard;
            }
            board.bitboards.put(pieceType, board.bitboards.get(pieceType) | bitboard);
        }

        board.bitboards.put(Chessboard.Piece.white, whitePieces);
        board.bitboards.put(Chessboard.Piece.black, blackPieces);
        board.bitboards.put(Chessboard.Piece.empty, ~(whitePieces | blackPieces));

        return board;
    }

    public static String[] convertChessboardToStringArray(Chessboard board) {
        String[] boardArray = new String[64];

        for (int i = 0; i < 64; i++) {
            long bitboard = generateBitboardFromIndex(i);
            if ((board.bitboards.get(Chessboard.Piece.white) & bitboard) != 0) {
                if ((board.bitboards.get(Chessboard.Piece.pawn) & bitboard) != 0) {
                    boardArray[i] = "wP";
                } else if ((board.bitboards.get(Chessboard.Piece.knight) & bitboard) != 0) {
                    boardArray[i] = "wN";
                } else if ((board.bitboards.get(Chessboard.Piece.bishop) & bitboard) != 0) {
                    boardArray[i] = "wB";
                } else if ((board.bitboards.get(Chessboard.Piece.rook) & bitboard) != 0) {
                    boardArray[i] = "wR";
                } else if ((board.bitboards.get(Chessboard.Piece.queen) & bitboard) != 0) {
                    boardArray[i] = "wQ";
                } else if ((board.bitboards.get(Chessboard.Piece.king) & bitboard) != 0) {
                    boardArray[i] = "wK";
                }
            } else if ((board.bitboards.get(Chessboard.Piece.black) & bitboard) != 0) {
                if ((board.bitboards.get(Chessboard.Piece.pawn) & bitboard) != 0) {
                    boardArray[i] = "bP";
                } else if ((board.bitboards.get(Chessboard.Piece.knight) & bitboard) != 0) {
                    boardArray[i] = "bN";
                } else if ((board.bitboards.get(Chessboard.Piece.bishop) & bitboard) != 0) {
                    boardArray[i] = "bB";
                } else if ((board.bitboards.get(Chessboard.Piece.rook) & bitboard) != 0) {
                    boardArray[i] = "bR";
                } else if ((board.bitboards.get(Chessboard.Piece.queen) & bitboard) != 0) {
                    boardArray[i] = "bQ";
                } else if ((board.bitboards.get(Chessboard.Piece.king) & bitboard) != 0) {
                    boardArray[i] = "bK";
                }
            } else {
                boardArray[i] = "";
            }
        }

        return boardArray;
    }

}
