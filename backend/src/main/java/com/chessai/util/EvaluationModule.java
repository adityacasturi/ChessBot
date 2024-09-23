package com.chessai.util;

import static com.chessai.model.Chessboard.Piece;
import static com.chessai.model.Chessboard.Piece.*;

import com.chessai.PestoEval;
import com.chessai.model.Chessboard;
import com.chessai.model.Position;
import com.chessai.model.Position.Turn;

public class EvaluationModule {

    private static final int CENTER_CONTROL_BONUS = 5;

    public static int boardEvaluation(Position position) {
        int score = 0;
        int pestoEval = PestoEval.eval(position.board);

        score += materialEval(position.board) * 10;
        score += centerControlEval(position.board);
//        score += mobilityEval(position);
        score += developmentEval(position.board);


        score += pestoEval;

        return score;
    }

    private static int materialEval(Chessboard board) {
        int[] pieceValues = {100, 320, 330, 500, 900}; // pawn, knight, bishop, rook, queen
        Piece[] types = new Piece[]{pawn, knight, bishop, rook, queen};
        int score = 0;
        long whitePieces = board.bitboards.get(Piece.white);
        long blackPieces = board.bitboards.get(Piece.black);

        // Compute material score
        for (int i = 0; i < pieceValues.length; i++) {
            long whitePiecesOfType = whitePieces & board.bitboards.get(types[i]);
            long blackPiecesOfType = blackPieces & board.bitboards.get(types[i]);
            score += pieceValues[i] *
                    (Long.bitCount(whitePiecesOfType) - Long.bitCount(blackPiecesOfType));
        }

        return score;
    }

    private static int bitCount(long bitboard) {
        return Long.bitCount(bitboard);
    }

    private static int openFileUtilizationCheck(Chessboard board) {
        long whiteRooks = board.bitboards.get(Piece.white) & board.bitboards.get(Piece.rook);
        long blackRooks = board.bitboards.get(Piece.black) & board.bitboards.get(Piece.rook);

        int score = 0;

        for (int i = 0; i < 8; i++) {
            long fileMask = generateFileMask(i);
            long blackPiecesOnFile = board.bitboards.get(Piece.black) & fileMask;
            long whitePiecesOnFile = board.bitboards.get(Piece.white) & fileMask;
            long whiteRooksOnFile = whiteRooks & fileMask;
            long blackRooksOnFile = blackRooks & fileMask;
            if (whiteRooksOnFile != 0 && bitCount(blackPiecesOnFile) < 3) {
                score += 5;
            } else if (blackRooksOnFile != 0 && bitCount(whitePiecesOnFile) < 3) {
                score -= 5;
            }
        }

        return score;
    }

    private static long generateFileMask(int file) {
        long fileMask = 0L;
        for (int i = 0; i < 8; i++) {
            fileMask |= 1L << (file + 8 * i);
        }
        return fileMask;
    }

    private static int mobilityEval(Position position) {
        int score = position.legalMovesInThisPosition;
        score *= position.getTurn() == Turn.WHITE ? 1 : -1;
        return score;
    }

    private static int centerControlEval(Chessboard board) {
        int score = 0;
        long centerSquares = 0b0000000000000000000000000001100000011000000000000000000000000000L; // the four central
        // squares
        long whitePieces = board.bitboards.get(Piece.white);
        long blackPieces = board.bitboards.get(Piece.black);

        long whiteControl = whitePieces & centerSquares;
        int numWhiteControl = Long.bitCount(whiteControl);

        long blackControl = blackPieces & centerSquares;
        int numBlackControl = Long.bitCount(blackControl);

        if (numWhiteControl > numBlackControl) {
            score += CENTER_CONTROL_BONUS;
        } else if (numBlackControl > numWhiteControl) {
            score -= CENTER_CONTROL_BONUS;
        }

        return score;
    }

    private static int developmentEval(Chessboard board) {
        int score = 0;
        long whiteKnights = board.bitboards.get(Piece.white) & board.bitboards.get(Piece.knight);
        long blackKnights = board.bitboards.get(Piece.black) & board.bitboards.get(Piece.knight);
        long whiteBishops = board.bitboards.get(Piece.white) & board.bitboards.get(Piece.bishop);
        long blackBishops = board.bitboards.get(Piece.black) & board.bitboards.get(Piece.bishop);

        long whiteKnightOptimal = 0b0000000000000000000000000000000000000000001001000001100000000000L;
        long blackKnightOptimal = 0b000000000011000001001000000000000000000000000000000000000000000L;

        long whiteBishopOptimal = 0b0000000000000000000000000000000000100100000110000100001000000000L;
        long blackBishopOptimal = 0b0100001000011000001001000000000000000000000000000000000000000000L;

        int numWhiteKnightsDeveloped = Long.bitCount(
                whiteKnights & whiteKnightOptimal);
        int numBlackKnightsDeveloped = Long.bitCount(
                blackKnights & blackKnightOptimal);
        int numWhiteBishopsDeveloped = Long.bitCount(
                whiteBishops & whiteBishopOptimal);
        int numBlackBishopsDeveloped = Long.bitCount(
                blackBishops & blackBishopOptimal);

        score += numWhiteKnightsDeveloped;
        score += numWhiteBishopsDeveloped;
        score -= numBlackKnightsDeveloped;
        score -= numBlackBishopsDeveloped;

        return score;
    }
}
