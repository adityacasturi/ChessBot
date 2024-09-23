package com.chessai.util;

import static com.chessai.util.BitboardUtilityModule.generateBitboardFromIndex;

import com.chessai.model.Chessboard;
import com.chessai.model.Chessboard.Piece;
import com.chessai.model.Move;
import com.chessai.model.Position;
import com.chessai.model.Position.Turn;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LegalMovesModule {
    private static final long NOT_FILE_H = 0b0111111101111111011111110111111101111111011111110111111101111111L;
    private static final long NOT_FILE_A = 0b1111111011111110111111101111111011111110111111101111111011111110L;
    private static final long NOT_FILE_G = 0b1011111110111111101111111011111110111111101111111011111110111111L;
    private static final long NOT_FILE_B = 0b1111110111111101111111011111110111111101111111011111110111111101L;
    private static final long NOT_RANK_1 = 0b1111111111111111111111111111111111111111111111111111111100000000L;
    private static final long NOT_RANK_8 = 0b0000000011111111111111111111111111111111111111111111111111111111L;
    private static final long whitePiecesObstructingShortCastling = 0b0000011000000000000000000000000000000000000000000000000000000000L;
    private static final long whitePiecesObstructingLongCastling = 0b0111000000000000000000000000000000000000000000000000000000000000L;
    private static final long blackPiecesObstructingShortCastling = 0b0000000000000000000000000000000000000000000000000000000000000110L;
    private static final long blackPiecesObstructingLongCastling = 0b0000000000000000000000000000000000000000000000000000000001110000L;

  public static List<Move> getLegalMoves(Position position) {
        long king = position.board.bitboards.get(Piece.king) &
                (position.getTurn() == Turn.WHITE
                        ? position.board.bitboards.get(Piece.white)
                        : position.board.bitboards.get(Piece.black));
        int kingSquare = bitScan(king).get(0);

        Turn turn = position.getTurn();
        Chessboard board = position.board;
        List<Move> moves = new LinkedList<>();
        getLegalPawnMoves(turn, board, moves);
        getLegalKnightMoves(turn, board, moves);
        getLegalBishopMoves(turn, board, moves);
        getLegalRookMoves(turn, board, moves);
        getLegalQueenMoves(turn, board, moves);
        getLegalKingMoves(turn, board, moves, position, position.whiteKingHasMoved, position.blackKingHasMoved);

        List<Move> validMoves = new LinkedList<>();
        for (Move move : moves) {
            Chessboard tempBoard = new Chessboard(board);
            tempBoard.makeMove(move);

            if (!isKingInCheck(tempBoard, turn, kingSquare, position, position.whiteKingHasMoved, position.blackKingHasMoved)) {
                validMoves.add(move);
            }
        }

        removePieceKingCaptureMoves(validMoves, board, turn);

        return validMoves;
    }

    private static boolean isKingInCheck(
            Chessboard board,
            Turn turn,
            int kingSquare,
            Position position,
            boolean whiteKingHasMoved,
            boolean blackKingHasMoved) {
        Turn nextTurn = turn == Turn.WHITE ? Turn.BLACK : Turn.WHITE;
        List<Move> moves = new LinkedList<>();
        getLegalPawnMoves(nextTurn, board, moves);
        getLegalKnightMoves(nextTurn, board, moves);
        getLegalBishopMoves(nextTurn, board, moves);
        getLegalRookMoves(nextTurn, board, moves);
        getLegalQueenMoves(nextTurn, board, moves);
        getLegalKingMoves(turn, board, moves, position, whiteKingHasMoved, blackKingHasMoved);
        return moves.stream().anyMatch(move -> move.getToSquare() == kingSquare);
    }

    private static void removePieceKingCaptureMoves(
            List<Move> moves,
            Chessboard board,
            Turn turn) {
        long opponentKing = board.bitboards.get(Piece.king) &
                (turn == Turn.WHITE
                        ? board.bitboards.get(Piece.black)
                        : board.bitboards.get(Piece.white));
        int opponentKingSquare = bitScan(opponentKing).get(0);
        moves.removeIf(move -> move.getToSquare() == opponentKingSquare);
    }

    private static void getLegalPawnMoves(
            Turn turn,
            Chessboard board,
            List<Move> moves) {
        long pawns = board.bitboards.get(Piece.pawn) &
                (turn == Turn.WHITE
                        ? board.bitboards.get(Piece.white)
                        : board.bitboards.get(Piece.black));
        long emptySquares = board.bitboards.get(Piece.empty);
        long enemyPieces = turn == Turn.WHITE
                ? board.bitboards.get(Piece.black)
                : board.bitboards.get(Piece.white);

        List<Integer> squaresContainingPawns = bitScan(pawns);

        for (int squareContainingPawn : squaresContainingPawns) {
            boolean isIntermediateSquareClear = false;
            boolean isPawnOnStartingRank = isPawnOnStartingRank(
                    squareContainingPawn,
                    turn);

            long bitboardContainingSinglePawn = generateBitboardFromIndex(
                    squareContainingPawn);
            // move forward one square
            if ((turn == Turn.WHITE &&
                    (bitboardContainingSinglePawn << 8 & emptySquares) != 0b0) ||
                    (turn == Turn.BLACK &&
                            (bitboardContainingSinglePawn >> 8 & emptySquares) != 0b0)) {
                isIntermediateSquareClear = true;
                moves.add(
                        new Move(
                                squareContainingPawn,
                                turn == Turn.WHITE
                                        ? squareContainingPawn + 8
                                        : squareContainingPawn - 8,
                                Piece.pawn,
                                turn,
                                false,
                                false,
                                false));
            }

            // move forward two squares
            if (isIntermediateSquareClear && isPawnOnStartingRank) {
                if ((turn == Turn.WHITE &&
                        (bitboardContainingSinglePawn << 16 & emptySquares) != 0b0) ||
                        (turn == Turn.BLACK &&
                                (bitboardContainingSinglePawn >> 16 & emptySquares) != 0b0)) {
                    moves.add(
                            new Move(
                                    squareContainingPawn,
                                    turn == Turn.WHITE
                                            ? squareContainingPawn + 16
                                            : squareContainingPawn - 16,
                                    Piece.pawn,
                                    turn,
                                    false,
                                    false,
                                    false));
                }
            }

            // capture to the left (for white, its +7, for black, its -9)
            if ((turn == Turn.WHITE &&
                    (bitboardContainingSinglePawn << 7 & NOT_FILE_H & enemyPieces) != 0b0) ||
                    (turn == Turn.BLACK &&
                            (bitboardContainingSinglePawn >> 9 & NOT_FILE_H & enemyPieces) != 0b0)) {
                moves.add(
                        new Move(
                                squareContainingPawn,
                                turn == Turn.WHITE
                                        ? squareContainingPawn + 7
                                        : squareContainingPawn - 9,
                                Piece.pawn,
                                turn,
                                true,
                                false,
                                false));
            }

            // capture to the right (for white, its +9, for black, its -7)
            if ((turn == Turn.WHITE &&
                    (bitboardContainingSinglePawn << 9 & NOT_FILE_A & enemyPieces) != 0b0) ||
                    (turn == Turn.BLACK &&
                            (bitboardContainingSinglePawn >> 7 & NOT_FILE_A & enemyPieces) != 0b0)) {
                moves.add(
                        new Move(
                                squareContainingPawn,
                                turn == Turn.WHITE
                                        ? squareContainingPawn + 9
                                        : squareContainingPawn - 7,
                                Piece.pawn,
                                turn,
                                true,
                                false,
                                false));
            }
        }
    }

    private static boolean isPawnOnStartingRank(
            int squareContainingPawn,
            Turn turn) {
        return ((turn == Turn.WHITE &&
                squareContainingPawn >= 8 &&
                squareContainingPawn <= 15) ||
                (turn == Turn.BLACK &&
                        squareContainingPawn >= 48 &&
                        squareContainingPawn <= 55));
    }

    private static List<Integer> bitScan(long bitboard) {
        List<Integer> indices = new ArrayList<>();
        while (bitboard != 0) {
            int index = Long.numberOfTrailingZeros(bitboard);
            indices.add(index);
            bitboard &= bitboard - 1;
        }
        return indices;
    }

    private static void getLegalKnightMoves(
            Turn turn,
            Chessboard board,
            List<Move> moves) {
        long knights = board.bitboards.get(Piece.knight) &
                (turn == Turn.WHITE
                        ? board.bitboards.get(Piece.white)
                        : board.bitboards.get(Piece.black));

        long emptySquares = board.bitboards.get(Piece.empty);
        long enemyPieces = turn == Turn.WHITE
                ? board.bitboards.get(Piece.black)
                : board.bitboards.get(Piece.white);
        long validSquares = emptySquares | enemyPieces;
        boolean isCapture;

        List<Integer> squaresContainingKnights = bitScan(knights);

        for (int squareContainingKnight : squaresContainingKnights) {
            long bitboardContainingSingleKnight = generateBitboardFromIndex(
                    squareContainingKnight);

            // move forward two squares, left one square
            if ((bitboardContainingSingleKnight << 15 & NOT_FILE_H & validSquares) != 0b0) {
                isCapture = (bitboardContainingSingleKnight << 15 & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKnight,
                                squareContainingKnight + 15,
                                Piece.knight,
                                turn,
                                isCapture,
                                false,
                                false));
            }

            // move forward two squares, right one square
            if ((bitboardContainingSingleKnight << 17 & NOT_FILE_A & validSquares) != 0b0) {
                isCapture = (bitboardContainingSingleKnight << 17 & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKnight,
                                squareContainingKnight + 17,
                                Piece.knight,
                                turn,
                                isCapture,
                                false,
                                false));
            }

            // move forward one square, left two squares
            if ((bitboardContainingSingleKnight << 6 & NOT_FILE_H & NOT_FILE_G & validSquares) != 0b0) {
                isCapture = (bitboardContainingSingleKnight << 6 & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKnight,
                                squareContainingKnight + 6,
                                Piece.knight,
                                turn,
                                isCapture,
                                false,
                                false));
            }

            // move forward one square, right two squares
            if ((bitboardContainingSingleKnight << 10 & NOT_FILE_A & NOT_FILE_B & validSquares) != 0b0) {
                isCapture = (bitboardContainingSingleKnight << 10 & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKnight,
                                squareContainingKnight + 10,
                                Piece.knight,
                                turn,
                                isCapture,
                                false,
                                false));
            }

            // move backward two squares, left one square
            if ((bitboardContainingSingleKnight >> 17 & NOT_FILE_H & validSquares) != 0b0) {
                isCapture = (bitboardContainingSingleKnight >> 17 & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKnight,
                                squareContainingKnight - 17,
                                Piece.knight,
                                turn,
                                isCapture,
                                false,
                                false));
            }

            // move backward two squares, right one square
            if ((bitboardContainingSingleKnight >> 15 & NOT_FILE_A & validSquares) != 0b0) {
                isCapture = (bitboardContainingSingleKnight >> 15 & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKnight,
                                squareContainingKnight - 15,
                                Piece.knight,
                                turn,
                                isCapture,
                                false,
                                false));
            }

            // move backward one square, left two squares
            if ((bitboardContainingSingleKnight >> 10 & NOT_FILE_H & NOT_FILE_G & validSquares) != 0b0) {
                isCapture = (bitboardContainingSingleKnight >> 10 & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKnight,
                                squareContainingKnight - 10,
                                Piece.knight,
                                turn,
                                isCapture,
                                false,
                                false));
            }

            // move backward one square, right two squares
            if ((bitboardContainingSingleKnight >> 6 & NOT_FILE_A & NOT_FILE_B & validSquares) != 0b0) {
                isCapture = (bitboardContainingSingleKnight >> 6 & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKnight,
                                squareContainingKnight - 6,
                                Piece.knight,
                                turn,
                                isCapture,
                                false,
                                false));
            }
        }
    }

    private static void getLegalBishopMoves(
            Turn turn,
            Chessboard board,
            List<Move> moves) {
        long bishops = board.bitboards.get(Piece.bishop) &
                (turn == Turn.WHITE
                        ? board.bitboards.get(Piece.white)
                        : board.bitboards.get(Piece.black));

        long emptySquares = board.bitboards.get(Piece.empty);
        long enemyPieces = turn == Turn.WHITE
                ? board.bitboards.get(Piece.black)
                : board.bitboards.get(Piece.white);
        long validSquares = emptySquares | enemyPieces;

        List<Integer> squaresContainingBishops = bitScan(bishops);

        for (int squareContainingBishop : squaresContainingBishops) {
            long bitboardContainingSingleBishop = generateBitboardFromIndex(
                    squareContainingBishop);

            travelDiagonal(
                    bitboardContainingSingleBishop,
                    squareContainingBishop,
                    turn,
                    moves,
                    Piece.bishop,
                    validSquares,
                    enemyPieces);
        }
    }

    private static void getLegalRookMoves(
            Turn turn,
            Chessboard board,
            List<Move> moves) {
        long rooks = board.bitboards.get(Piece.rook) &
                (turn == Turn.WHITE
                        ? board.bitboards.get(Piece.white)
                        : board.bitboards.get(Piece.black));

        long emptySquares = board.bitboards.get(Piece.empty);
        long enemyPieces = turn == Turn.WHITE
                ? board.bitboards.get(Piece.black)
                : board.bitboards.get(Piece.white);
        long allyPieces = turn == Turn.WHITE
                ? board.bitboards.get(Piece.white)
                : board.bitboards.get(Piece.black);
        long validSquares = emptySquares | enemyPieces;

        List<Integer> squaresContainingRooks = bitScan(rooks);
        boolean isCapture;

        for (int squareContainingRook : squaresContainingRooks) {
            long bitboardContainingSingleRook = generateBitboardFromIndex(
                    squareContainingRook);

            // move up
            long bitboardContainingSingleRookCopy = bitboardContainingSingleRook;
            while ((bitboardContainingSingleRookCopy << 8 & NOT_RANK_1 & validSquares) != 0b0) {
                bitboardContainingSingleRookCopy <<= 8;
                isCapture = (bitboardContainingSingleRookCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingRook,
                                Long.numberOfTrailingZeros(bitboardContainingSingleRookCopy),
                                Piece.rook,
                                turn,
                                isCapture,
                                false,
                                false));

                if ((bitboardContainingSingleRookCopy & enemyPieces) != 0b0 ||
                        (bitboardContainingSingleRookCopy & allyPieces) != 0b0) {
                    break;
                }
            }

            // move down
            bitboardContainingSingleRookCopy = bitboardContainingSingleRook;
            while ((bitboardContainingSingleRookCopy >> 8 & NOT_RANK_8 & validSquares) != 0b0) {
                bitboardContainingSingleRookCopy >>= 8;
                isCapture = (bitboardContainingSingleRookCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingRook,
                                Long.numberOfTrailingZeros(bitboardContainingSingleRookCopy),
                                Piece.rook,
                                turn,
                                isCapture,
                                false,
                                false));

                if ((bitboardContainingSingleRookCopy & enemyPieces) != 0b0 ||
                        (bitboardContainingSingleRookCopy & allyPieces) != 0b0) {
                    break;
                }
            }

            // move right
            bitboardContainingSingleRookCopy = bitboardContainingSingleRook;
            while ((bitboardContainingSingleRookCopy << 1 & NOT_FILE_A & validSquares) != 0b0) {
                bitboardContainingSingleRookCopy <<= 1;
                isCapture = (bitboardContainingSingleRookCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingRook,
                                Long.numberOfTrailingZeros(bitboardContainingSingleRookCopy),
                                Piece.rook,
                                turn,
                                isCapture,
                                false,
                                false));

                if ((bitboardContainingSingleRookCopy & enemyPieces) != 0b0 ||
                        (bitboardContainingSingleRookCopy & allyPieces) != 0b0) {
                    break;
                }
            }

            // move left
            bitboardContainingSingleRookCopy = bitboardContainingSingleRook;
            while ((bitboardContainingSingleRookCopy >> 1 & NOT_FILE_H & validSquares) != 0b0) {
                bitboardContainingSingleRookCopy >>= 1;
                isCapture = (bitboardContainingSingleRookCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingRook,
                                Long.numberOfTrailingZeros(bitboardContainingSingleRookCopy),
                                Piece.rook,
                                turn,
                                isCapture,
                                false,
                                false));

                if ((bitboardContainingSingleRookCopy & enemyPieces) != 0b0 ||
                        (bitboardContainingSingleRookCopy & allyPieces) != 0b0) {
                    break;
                }
            }
        }
    }

    private static void getLegalQueenMoves(
            Turn turn,
            Chessboard board,
            List<Move> moves) {
        long queens = board.bitboards.get(Piece.queen) &
                (turn == Turn.WHITE
                        ? board.bitboards.get(Piece.white)
                        : board.bitboards.get(Piece.black));

        long emptySquares = board.bitboards.get(Piece.empty);
        long enemyPieces = turn == Turn.WHITE
                ? board.bitboards.get(Piece.black)
                : board.bitboards.get(Piece.white);
        long allyPieces = turn == Turn.WHITE
                ? board.bitboards.get(Piece.white)
                : board.bitboards.get(Piece.black);
        long validSquares = emptySquares | enemyPieces;

        List<Integer> squaresContainingQueens = bitScan(queens);
        boolean isCapture;

        for (int squareContainingQueen : squaresContainingQueens) {
            long bitboardContainingSingleQueen = generateBitboardFromIndex(
                    squareContainingQueen);

            // move up
            long bitboardContainingSingleQueenCopy = bitboardContainingSingleQueen;
            while ((bitboardContainingSingleQueenCopy << 8 & NOT_RANK_1 & validSquares) != 0b0) {
                bitboardContainingSingleQueenCopy <<= 8;
                isCapture = (bitboardContainingSingleQueenCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingQueen,
                                Long.numberOfTrailingZeros(bitboardContainingSingleQueenCopy),
                                Piece.queen,
                                turn,
                                isCapture,
                                false,
                                false));

                if ((bitboardContainingSingleQueenCopy & enemyPieces) != 0b0 ||
                        (bitboardContainingSingleQueenCopy & allyPieces) != 0b0) {
                    break;
                }
            }

            // move down
            bitboardContainingSingleQueenCopy = bitboardContainingSingleQueen;
            while ((bitboardContainingSingleQueenCopy >> 8 & NOT_RANK_8 & validSquares) != 0b0) {
                bitboardContainingSingleQueenCopy >>= 8;
                isCapture = (bitboardContainingSingleQueenCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingQueen,
                                Long.numberOfTrailingZeros(bitboardContainingSingleQueenCopy),
                                Piece.queen,
                                turn,
                                isCapture,
                                false,
                                false));

                if ((bitboardContainingSingleQueenCopy & enemyPieces) != 0b0 ||
                        (bitboardContainingSingleQueenCopy & allyPieces) != 0b0) {
                    break;
                }
            }

            // move right
            bitboardContainingSingleQueenCopy = bitboardContainingSingleQueen;
            while ((bitboardContainingSingleQueenCopy << 1 & NOT_FILE_A & validSquares) != 0b0) {
                bitboardContainingSingleQueenCopy <<= 1;
                isCapture = (bitboardContainingSingleQueenCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingQueen,
                                Long.numberOfTrailingZeros(bitboardContainingSingleQueenCopy),
                                Piece.queen,
                                turn,
                                isCapture,
                                false,
                                false));

                if ((bitboardContainingSingleQueenCopy & enemyPieces) != 0b0 ||
                        (bitboardContainingSingleQueenCopy & allyPieces) != 0b0) {
                    break;
                }
            }

            // move left
            bitboardContainingSingleQueenCopy = bitboardContainingSingleQueen;
            while ((bitboardContainingSingleQueenCopy >> 1 & NOT_FILE_H & validSquares) != 0b0) {
                bitboardContainingSingleQueenCopy >>= 1;
                isCapture = (bitboardContainingSingleQueenCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingQueen,
                                Long.numberOfTrailingZeros(bitboardContainingSingleQueenCopy),
                                Piece.queen,
                                turn,
                                isCapture,
                                false,
                                false));

                if ((bitboardContainingSingleQueenCopy & enemyPieces) != 0b0 ||
                        (bitboardContainingSingleQueenCopy & allyPieces) != 0b0) {
                    break;
                }
            }

            travelDiagonal(
                    bitboardContainingSingleQueen,
                    squareContainingQueen,
                    turn,
                    moves,
                    Piece.queen,
                    validSquares,
                    enemyPieces);

            moves.removeIf(move -> move.getPieceType() == Piece.queen && move.getPieceColor() == Turn.BLACK &&
                    move.getToSquare() < 16);
        }
    }

    private static void getLegalKingMoves(
            Turn turn,
            Chessboard board,
            List<Move> moves,
            Position position,
            boolean whiteKingHasMoved,
            boolean blackKingHasMoved) {
        long kings = board.bitboards.get(Piece.king) &
                (turn == Turn.WHITE
                        ? board.bitboards.get(Piece.white)
                        : board.bitboards.get(Piece.black));

        long emptySquares = board.bitboards.get(Piece.empty);
        long enemyPieces = turn == Turn.WHITE
                ? board.bitboards.get(Piece.black)
                : board.bitboards.get(Piece.white);
        long allyPieces = turn == Turn.WHITE
                ? board.bitboards.get(Piece.white)
                : board.bitboards.get(Piece.black);
        long validSquares = emptySquares | enemyPieces;

        List<Integer> squaresContainingKings = bitScan(kings);
        boolean isCapture;

        for (int squareContainingKing : squaresContainingKings) {
            long bitboardContainingSingleKing = generateBitboardFromIndex(
                    squareContainingKing);

            boolean kingMoved = false;

            // move up
            long bitboardContainingSingleKingCopy = bitboardContainingSingleKing;
            if ((bitboardContainingSingleKingCopy << 8 & NOT_RANK_1 & validSquares) != 0b0) {
                bitboardContainingSingleKingCopy <<= 8;
                isCapture = (bitboardContainingSingleKingCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKing,
                                Long.numberOfTrailingZeros(bitboardContainingSingleKingCopy),
                                Piece.king,
                                turn,
                                isCapture,
                                false,
                                false));
                kingMoved = true;
            }

            // move down
            bitboardContainingSingleKingCopy = bitboardContainingSingleKing;
            if ((bitboardContainingSingleKingCopy >> 8 & NOT_RANK_8 & validSquares) != 0b0) {
                bitboardContainingSingleKingCopy >>= 8;
                isCapture = (bitboardContainingSingleKingCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKing,
                                Long.numberOfTrailingZeros(bitboardContainingSingleKingCopy),
                                Piece.king,
                                turn,
                                isCapture,
                                false,
                                false));
              kingMoved = true;
            }

            // move right
            bitboardContainingSingleKingCopy = bitboardContainingSingleKing;
            if ((bitboardContainingSingleKingCopy << 1 & NOT_FILE_A & validSquares) != 0b0) {
                bitboardContainingSingleKingCopy <<= 1;
                isCapture = (bitboardContainingSingleKingCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKing,
                                Long.numberOfTrailingZeros(bitboardContainingSingleKingCopy),
                                Piece.king,
                                turn,
                                isCapture,
                                false,
                                false));
              kingMoved = true;
            }

            // move left
            bitboardContainingSingleKingCopy = bitboardContainingSingleKing;
            if ((bitboardContainingSingleKingCopy >> 1 & NOT_FILE_H & validSquares) != 0b0) {
                bitboardContainingSingleKingCopy >>= 1;
                isCapture = (bitboardContainingSingleKingCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKing,
                                Long.numberOfTrailingZeros(bitboardContainingSingleKingCopy),
                                Piece.king,
                                turn,
                                isCapture,
                                false,
                                false));
              kingMoved = true;
            }

            // move up right
            bitboardContainingSingleKingCopy = bitboardContainingSingleKing;
            if ((bitboardContainingSingleKingCopy << 9 & NOT_RANK_1 & NOT_FILE_A & validSquares) != 0b0) {
                bitboardContainingSingleKingCopy <<= 9;
                isCapture = (bitboardContainingSingleKingCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKing,
                                Long.numberOfTrailingZeros(bitboardContainingSingleKingCopy),
                                Piece.king,
                                turn,
                                isCapture,
                                false,
                                false));
              kingMoved = true;
            }

            // move up left
            bitboardContainingSingleKingCopy = bitboardContainingSingleKing;
            if ((bitboardContainingSingleKingCopy << 7 & NOT_RANK_1 & NOT_FILE_H & validSquares) != 0b0) {
                bitboardContainingSingleKingCopy <<= 7;
                isCapture = (bitboardContainingSingleKingCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKing,
                                Long.numberOfTrailingZeros(bitboardContainingSingleKingCopy),
                                Piece.king,
                                turn,
                                isCapture,
                                false,
                                false));
              kingMoved = true;
            }

            // move down right
            bitboardContainingSingleKingCopy = bitboardContainingSingleKing;
            if ((bitboardContainingSingleKingCopy >> 7 & NOT_RANK_8 & NOT_FILE_A & validSquares) != 0b0) {
                bitboardContainingSingleKingCopy >>= 7;
                isCapture = (bitboardContainingSingleKingCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKing,
                                Long.numberOfTrailingZeros(bitboardContainingSingleKingCopy),
                                Piece.king,
                                turn,
                                isCapture,
                                false,
                                false));
              kingMoved = true;
            }

            // move down left
            bitboardContainingSingleKingCopy = bitboardContainingSingleKing;
            if ((bitboardContainingSingleKingCopy >> 9 & NOT_RANK_8 & NOT_FILE_H & validSquares) != 0b0) {
                bitboardContainingSingleKingCopy >>= 9;
                isCapture = (bitboardContainingSingleKingCopy & enemyPieces) != 0b0;
                moves.add(
                        new Move(
                                squareContainingKing,
                                Long.numberOfTrailingZeros(bitboardContainingSingleKingCopy),
                                Piece.king,
                                turn,
                                isCapture,
                                false,
                                false));
              kingMoved = true;
            }

          bitboardContainingSingleKingCopy = bitboardContainingSingleKing;
          if (turn == Turn.WHITE && !whiteKingHasMoved) {
            if ((board.bitboards.get(Piece.white) & whitePiecesObstructingShortCastling) > 0) {
              moves.add(
                      new Move(
                              squareContainingKing,
                              6,
                              Piece.king,
                              turn,
                              false,
                              true,
                              false));
            } else if ((board.bitboards.get(Piece.white) & whitePiecesObstructingLongCastling) > 0) {
              moves.add(
                      new Move(
                              squareContainingKing,
                              2,
                              Piece.king,
                              turn,
                              false,
                              false,
                              true));
            }
          } else if (turn == Turn.BLACK && !blackKingHasMoved) {
            if ((board.bitboards.get(Piece.black) & blackPiecesObstructingShortCastling) > 0) {
              moves.add(
                      new Move(
                              squareContainingKing,
                              62,
                              Piece.king,
                              turn,
                              false,
                              true,
                              false));
            } else if ((board.bitboards.get(Piece.black) & blackPiecesObstructingLongCastling) > 0) {
              moves.add(
                      new Move(
                              squareContainingKing,
                              58,
                              Piece.king,
                              turn,
                              false,
                              false,
                              true));
            }
          }
        }
    }

    private static void travelDiagonal(
            long bitboardContainingSinglePiece,
            int squareContainingPiece,
            Turn turn,
            List<Move> moves,
            Piece piece,
            long validSquares,
            long enemyPieces) {
        boolean isCapture;

        // move up right
        long bitboardContainingSinglePieceCopy = bitboardContainingSinglePiece;
        while ((bitboardContainingSinglePieceCopy << 9 & NOT_RANK_1 & NOT_FILE_A & validSquares) != 0b0) {
            bitboardContainingSinglePieceCopy <<= 9;
            isCapture = (bitboardContainingSinglePieceCopy & enemyPieces) != 0b0;
            moves.add(
                    new Move(
                            squareContainingPiece,
                            Long.numberOfTrailingZeros(bitboardContainingSinglePieceCopy),
                            piece,
                            turn,
                            isCapture,
                            false,
                            false));

            if ((bitboardContainingSinglePieceCopy & enemyPieces) != 0b0) {
                break;
            }
        }

        // move up left
        bitboardContainingSinglePieceCopy = bitboardContainingSinglePiece;
        while ((bitboardContainingSinglePieceCopy << 7 & NOT_RANK_1 & NOT_FILE_H & validSquares) != 0b0) {
            bitboardContainingSinglePieceCopy <<= 7;
            isCapture = (bitboardContainingSinglePieceCopy & enemyPieces) != 0b0;
            moves.add(
                    new Move(
                            squareContainingPiece,
                            Long.numberOfTrailingZeros(bitboardContainingSinglePieceCopy),
                            piece,
                            turn,
                            isCapture,
                            false,
                            false));

            if ((bitboardContainingSinglePieceCopy & enemyPieces) != 0b0) {
                break;
            }
        }

        // move down right
        bitboardContainingSinglePieceCopy = bitboardContainingSinglePiece;
        while ((bitboardContainingSinglePieceCopy >> 7 & NOT_RANK_8 & NOT_FILE_A & validSquares) != 0b0) {
            bitboardContainingSinglePieceCopy >>= 7;
            isCapture = (bitboardContainingSinglePieceCopy & enemyPieces) != 0b0;
            moves.add(
                    new Move(
                            squareContainingPiece,
                            Long.numberOfTrailingZeros(bitboardContainingSinglePieceCopy),
                            piece,
                            turn,
                            isCapture,
                            false,
                            false));

            if ((bitboardContainingSinglePieceCopy & enemyPieces) != 0b0) {
                break;
            }
        }

        // move down left
        bitboardContainingSinglePieceCopy = bitboardContainingSinglePiece;
        while ((bitboardContainingSinglePieceCopy >> 9 & NOT_RANK_8 & NOT_FILE_H & validSquares) != 0b0) {
            bitboardContainingSinglePieceCopy >>= 9;
            isCapture = (bitboardContainingSinglePieceCopy & enemyPieces) != 0b0;
            moves.add(
                    new Move(
                            squareContainingPiece,
                            Long.numberOfTrailingZeros(bitboardContainingSinglePieceCopy),
                            piece,
                            turn,
                            isCapture,
                            false,
                            false));

            if ((bitboardContainingSinglePieceCopy & enemyPieces) != 0b0) {
                break;
            }
        }
    }
}
