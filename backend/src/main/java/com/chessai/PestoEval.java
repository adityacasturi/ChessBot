package com.chessai;

import com.chessai.model.Chessboard;

import static com.chessai.model.Chessboard.Piece.pawn;
import static com.chessai.model.Chessboard.Piece.knight;
import static com.chessai.model.Chessboard.Piece.bishop;
import static com.chessai.model.Chessboard.Piece.rook;
import static com.chessai.model.Chessboard.Piece.queen;
import static com.chessai.model.Chessboard.Piece.king;

import static com.chessai.model.Chessboard.Piece.white;
import static com.chessai.model.Chessboard.Piece.black;

// Pesto Eval Method adapted from: https://www.chessprogramming.org/PeSTO%27s_Evaluation_Function
public class PestoEval {
    static final int PAWN = 0;
    static final int KNIGHT = 1;
    static final int BISHOP = 2;
    static final int ROOK = 3;
    static final int QUEEN = 4;
    static final int KING = 5;
    static final int WHITE = 0;
    static final int BLACK = 1;
    static final int WHITE_PAWN = 2 * PAWN + WHITE;
    static final int BLACK_PAWN = 2 * PAWN + BLACK;
    static final int WHITE_KNIGHT = 2 * KNIGHT + WHITE;
    static final int BLACK_KNIGHT = 2 * KNIGHT + BLACK;
    static final int WHITE_BISHOP = 2 * BISHOP + WHITE;
    static final int BLACK_BISHOP = 2 * BISHOP + BLACK;
    static final int WHITE_ROOK = 2 * ROOK + WHITE;
    static final int BLACK_ROOK = 2 * ROOK + BLACK;
    static final int WHITE_QUEEN = 2 * QUEEN + WHITE;
    static final int BLACK_QUEEN = 2 * QUEEN + BLACK;
    static final int WHITE_KING = 2 * KING + WHITE;
    static final int BLACK_KING = 2 * KING + BLACK;
    static final int EMPTY = BLACK_KING + 1;

    static int[] mg_value = { 82, 337, 365, 477, 1025, 0 };
    static int[] eg_value = { 94, 281, 297, 512, 936, 0 };

    public static int PCOLOR(int p) {
        return (p & 1);
    }

    static int sideTwoMove;

    public static int FLIP(int sq) {
        return (sq ^ 56);
    }

    public static int OTHER(int side) {
        return (side ^ 1);
    }

    static int[] mg_pawn_table = {
            0, 0, 0, 0, 0, 0, 0, 0,
            98, 134, 61, 95, 68, 126, 34, -11,
            -6, 7, 26, 31, 65, 56, 25, -20,
            -14, 13, 6, 21, 23, 12, 17, -23,
            -27, -2, -5, 12, 17, 6, 10, -25,
            -26, -4, -4, -10, 3, 3, 33, -12,
            -35, -1, -20, -23, -15, 24, 38, -22,
            0, 0, 0, 0, 0, 0, 0, 0,
    };

    static int[] eg_pawn_table = {
            0, 0, 0, 0, 0, 0, 0, 0,
            178, 173, 158, 134, 147, 132, 165, 187,
            94, 100, 85, 67, 56, 53, 82, 84,
            32, 24, 13, 5, -2, 4, 17, 17,
            13, 9, -3, -7, -7, -8, 3, -1,
            4, 7, -6, 1, 0, -5, -1, -8,
            13, 8, 8, 10, 13, 0, 2, -7,
            0, 0, 0, 0, 0, 0, 0, 0,
    };

    static int[] mg_knight_table = {
            -167, -89, -34, -49, 61, -97, -15, -107,
            -73, -41, 72, 36, 23, 62, 7, -17,
            -47, 60, 37, 65, 84, 129, 73, 44,
            -9, 17, 19, 53, 37, 69, 18, 22,
            -13, 4, 16, 13, 28, 19, 21, -8,
            -23, -9, 12, 10, 19, 17, 25, -16,
            -29, -53, -12, -3, -1, 18, -14, -19,
            -105, -21, -58, -33, -17, -28, -19, -23,
    };

    static int[] eg_knight_table = {
            -58, -38, -13, -28, -31, -27, -63, -99,
            -25, -8, -25, -2, -9, -25, -24, -52,
            -24, -20, 10, 9, -1, -9, -19, -41,
            -17, 3, 22, 22, 22, 11, 8, -18,
            -18, -6, 16, 25, 16, 17, 4, -18,
            -23, -3, -1, 15, 10, -3, -20, -22,
            -42, -20, -10, -5, -2, -20, -23, -44,
            -29, -51, -23, -15, -22, -18, -50, -64,
    };

    static int[] mg_bishop_table = {
            -29,   4, -82, -37, -25, -42,   7,  -8,
            -26,  16, -18, -13,  30,  59,  18, -47,
            -16,  37,  43,  40,  35,  50,  37,  -2,
            -4,   5,  19,  50,  37,  37,   7,  -2,
            -6,  13,  13,  26,  34,  12,  10,   4,
            0,  15,  15,  15,  14,  27,  18,  10,
            4,  15,  16,   0,   7,  21,  33,   1,
            -33,  -3, -14, -21, -13, -12, -39, -21,
    };

    static int[] eg_bishop_table = {
            -14, -21, -11, -8, -7, -9, -17, -24,
            -8, -4, 7, -12, -3, -13, -4, -14,
            2, -8, 0, -1, -2, 6, 0, 4,
            -3, 9, 12, 9, 14, 10, 3, 2,
            -6, 3, 13, 19, 7, 10, -3, -9,
            -12, -3, 8, 10, 13, 3, -7, -15,
            -14, -18, -7, -1, 4, -9, -15, -27,
            -23, -9, -23, -5, -9, -16, -5, -17,
    };

    static int[] mg_rook_table = {
            32, 42, 32, 51, 63, 9, 31, 43,
            27, 32, 58, 62, 80, 67, 26, 44,
            -5, 19, 26, 36, 17, 45, 61, 16,
            -24, -11, 7, 26, 24, 35, -8, -20,
            -36, -26, -12, -1, 9, -7, 6, -23,
            -45, -25, -16, -17, 3, 0, -5, -33,
            -44, -16, -20, -9, -1, 11, -6, -71,
            -19, -13, 1, 17, 16, 7, -37, -26,
    };

    static int[] eg_rook_table = {
            13, 10, 18, 15, 12, 12, 8, 5,
            11, 13, 13, 11, -3, 3, 8, 3,
            7, 7, 7, 5, 4, -3, -5, -3,
            4, 3, 13, 1, 2, 1, -1, 2,
            3, 5, 8, 4, -5, -6, -8, -11,
            -4, 0, -5, -1, -7, -12, -8, -16,
            -6, -6, 0, 2, -9, -9, -11, -3,
            -9, 2, 3, -1, -5, -13, 4, -20,
    };

    static int[] mg_queen_table = {
            -28, 0, 29, 12, 59, 44, 43, 45,
            -24, -39, -5, 1, -16, 57, 28, 54,
            -13, -17, 7, 8, 29, 56, 47, 57,
            -27, -27, -16, -16, -1, 17, -2, 1,
            -9, -26, -9, -10, -2, -4, 3, -3,
            -14, 2, -11, -2, -5, 2, 14, 5,
            -35, -8, 11, 2, 8, 15, -3, 1,
            -1, -18, -9, 10, -15, -25, -31, -50,
    };

    static int[] eg_queen_table = {
            -9, 22, 22, 27, 27, 19, 10, 20,
            -17, 20, 32, 41, 58, 25, 30, 0,
            -20, 6, 9, 49, 47, 35, 19, 9,
            3, 22, 24, 45, 57, 40, 57, 36,
            -18, 28, 19, 47, 31, 34, 39, 23,
            -16, -27, 15, 6, 9, 17, 10, 5,
            -22, -23, -30, -16, -16, -23, -36, -32,
            -33, -28, -22, -43, -5, -32, -20, -41,
    };

    static int[] mg_king_table = {
            -65, 23, 16, -15, -56, -34, 2, 13,
            29, -1, -20, -7, -8, -4, -38, -29,
            -9, 24, 2, -16, -20, 6, 22, -22,
            -17, -20, -12, -27, -30, -25, -14, -36,
            -49, -1, -27, -39, -46, -44, -33, -51,
            -14, -14, -22, -46, -44, -30, -15, -27,
            1, 7, -8, -64, -43, -16, 9, 8,
            -15, 36, 12, -54, 8, -28, 24, 14,
    };

    static int[] eg_king_table = {
            -74, -35, -18, -18, -11, 15, 4, -17,
            -12, 17, 14, 17, 17, 38, 23, 11,
            10, 17, 23, 15, 20, 45, 44, 13,
            -8, 22, 24, 27, 26, 33, 26, 3,
            -18, -4, 21, 24, 27, 23, 9, -11,
            -19, -3, 11, 21, 23, 16, 7, -9,
            -27, -11, 4, 13, 14, 4, -5, -17,
            -53, -34, -21, -11, -28, -14, -24, -43
    };

    static int[][] mg_pesto_table = {
            mg_pawn_table,
            mg_knight_table,
            mg_bishop_table,
            mg_rook_table,
            mg_queen_table,
            mg_king_table
    };

    static int[][] eg_pesto_table = {
            eg_pawn_table,
            eg_knight_table,
            eg_bishop_table,
            eg_rook_table,
            eg_queen_table,
            eg_king_table
    };

    static int[] gamephaseInc = { 0, 0, 1, 1, 1, 1, 2, 2, 4, 4, 0, 0 };
    static int[][] mg_table = new int[12][64];
    static int[][] eg_table = new int[12][64];

    public static void initTables() {
        int pc, p, sq;
        for (p = PAWN, pc = WHITE_PAWN; p <= KING; pc += 2, p++) {
            for (sq = 0; sq < 64; sq++) {
                mg_table[pc][sq] = mg_value[p] + mg_pesto_table[p][sq];
                eg_table[pc][sq] = eg_value[p] + eg_pesto_table[p][sq];
                mg_table[pc + 1][sq] = mg_value[p] + mg_pesto_table[p][FLIP(sq)];
                eg_table[pc + 1][sq] = eg_value[p] + eg_pesto_table[p][FLIP(sq)];
            }
        }
    }

    private static int[] convertBitboardsToIntArray(Chessboard chessboard) {
        int[] board = new int[64];

        long whitePawns = chessboard.bitboards.get(white) & chessboard.bitboards.get(pawn);
        long whiteKnights = chessboard.bitboards.get(white) & chessboard.bitboards.get(knight);
        long whiteBishops = chessboard.bitboards.get(white) & chessboard.bitboards.get(bishop);
        long whiteRooks = chessboard.bitboards.get(white) & chessboard.bitboards.get(rook);
        long whiteQueens = chessboard.bitboards.get(white) & chessboard.bitboards.get(queen);
        long whiteKing = chessboard.bitboards.get(white) & chessboard.bitboards.get(king);

        long blackPawns = chessboard.bitboards.get(black) & chessboard.bitboards.get(pawn);
        long blackKnights = chessboard.bitboards.get(black) & chessboard.bitboards.get(knight);
        long blackBishops = chessboard.bitboards.get(black) & chessboard.bitboards.get(bishop);
        long blackRooks = chessboard.bitboards.get(black) & chessboard.bitboards.get(rook);
        long blackQueens = chessboard.bitboards.get(black) & chessboard.bitboards.get(queen);
        long blackKing = chessboard.bitboards.get(black) & chessboard.bitboards.get(king);

        long emptySquares = ~(chessboard.bitboards.get(white) | chessboard.bitboards.get(black));

        for (int i = 0; i < 64; i++) {
            long square = 1L << i;
            if ((whitePawns & square) != 0) {
                board[i] = WHITE_PAWN;
            } else if ((whiteKnights & square) != 0) {
                board[i] = WHITE_KNIGHT;
            } else if ((whiteBishops & square) != 0) {
                board[i] = WHITE_BISHOP;
            } else if ((whiteRooks & square) != 0) {
                board[i] = WHITE_ROOK;
            } else if ((whiteQueens & square) != 0) {
                board[i] = WHITE_QUEEN;
            } else if ((whiteKing & square) != 0) {
                board[i] = WHITE_KING;
            } else if ((blackPawns & square) != 0) {
                board[i] = BLACK_PAWN;
            } else if ((blackKnights & square) != 0) {
                board[i] = BLACK_KNIGHT;
            } else if ((blackBishops & square) != 0) {
                board[i] = BLACK_BISHOP;
            } else if ((blackRooks & square) != 0) {
                board[i] = BLACK_ROOK;
            } else if ((blackQueens & square) != 0) {
                board[i] = BLACK_QUEEN;
            } else if ((blackKing & square) != 0) {
                board[i] = BLACK_KING;
            } else if ((emptySquares & square) != 0) {
                board[i] = EMPTY;
            }
        }

        return board;
    }

    public static int eval(Chessboard chessboard) {
        int[] board = convertBitboardsToIntArray(chessboard);

        int[] mg = new int[2];
        int[] eg = new int[2];
        int gamePhase = 0;

        mg[WHITE] = 0;
        mg[BLACK] = 0;
        eg[WHITE] = 0;
        eg[BLACK] = 0;

        // evaluate each piece
        for (int sq = 0; sq < 64; ++sq) {
            int pc = board[sq];
            if (pc != EMPTY) {
                mg[PCOLOR(pc)] += mg_table[pc][sq];
                eg[PCOLOR(pc)] += eg_table[pc][sq];
                gamePhase += gamephaseInc[pc];
            }
        }

        // tapered eval
        int mgScore = mg[sideTwoMove] - mg[OTHER(sideTwoMove)];
        int egScore = eg[sideTwoMove] - eg[OTHER(sideTwoMove)];
        int mgPhase = gamePhase;
        if (mgPhase > 24)
            mgPhase = 24; // in case of early promotion
        int egPhase = 24 - mgPhase;

        // TODO implement Tapered Eval
        // https://www.chessprogramming.org/Tapered_Eval
        // Taper piece values by game phase [ex: B: 5, M: 10, E:50 for queens)

        return (mgScore * mgPhase + egScore * egPhase) / 24;
    }

}
