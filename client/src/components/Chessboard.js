import { React, useState } from 'react'
import '../styles/Chessboard.css'
import Square from './Square'
import axios from 'axios'

const REST_API_URL = 'http://localhost:8080/api';

const squares = [
    "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
    "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
    "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
    "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
    "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
    "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
    "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
    "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8"
];

let turn = "w";
let selectedPiece = "";
let whiteKingHasMoved = false;

export default function Chessboard({ updateInfoBug }) {
    const [board, setBoard] = useState(boardInit());
    const [selectedSquareIndex, setSelectedSquareIndex] = useState(-1)
    const [legalMoves, setLegalMoves] = useState([]);
    const indexes = [
        56, 57, 58, 59, 60, 61, 62, 63,
        48, 49, 50, 51, 52, 53, 54, 55,
        40, 41, 42, 43, 44, 45, 46, 47,
        32, 33, 34, 35, 36, 37, 38, 39,
        24, 25, 26, 27, 28, 29, 30, 31,
        16, 17, 18, 19, 20, 21, 22, 23,
        8, 9, 10, 11, 12, 13, 14, 15,
        0, 1, 2, 3, 4, 5, 6, 7
    ];

    return (
        <>
            <div className="container" id="container">
                {[...Array(64)].map((e, i) => {
                    const squareColor = indexToSquareColor(i)
                    return (
                        <Square
                            piece={board[indexes[i]]}
                            squareColor={squareColor}
                            key={63 - i}
                            squareIndex={indexes[i]}
                            selected={selectedSquareIndex === indexes[i]}
                            handleSquareClick={handleSquareClick}
                            handlePieceDrop={handlePieceDrop}
                            legalSquares={legalMoves} />
                    );
                })}
            </div>
        </>
    );

    function boardInit() {
        const chessboard = [];
        const pieces = ['R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'];

        for (let i = 0; i < 64; i++) {
            const piece = (i < 8 || i > 55) ? pieces[i % 8] : (i < 24 || i > 47) ? 'P' : '';
            let color;
            if (i < 16) color = 'w'; else if (i > 47) color = 'b'; else color = '';
            chessboard[i] = (i < 16 || i > 47) ? color + piece : '';
        }

        return chessboard;
    }

    function indexToSquareColor(index) {
        return `chess-square ${Math.floor(index / 8) % 2 === 0 ?
            (index % 2 === 0 ?
                'white-square' : 'black-square') : (index % 2 === 0 ?
                    'black-square' : 'white-square')}`
    }

    function handleSquareClick(clickedSquareIndex) {
        const newBoard = [];
        for (const i in board) {
            newBoard[i] = board[i];
        }

        const selectedPieceColor = selectedPiece ? selectedPiece[0] : '';
        const squareContents = board[clickedSquareIndex];
        const pieceColor = squareContents ? squareContents[0] : '';

        const isPieceSelected = selectedPiece !== '';
        const isSquareOccupied = squareContents !== '';
        const isPieceOfCorrectColor = selectedPieceColor === turn;
        const isSquareNotSelected = clickedSquareIndex !== selectedSquareIndex;
        const isPieceInSquareOfCorrectColor = pieceColor === turn;
        const isLegalMove = legalMoves.includes(clickedSquareIndex);

        if (isSquareNotSelected && (isPieceSelected || (isSquareOccupied && isPieceInSquareOfCorrectColor))) {
            if (isSquareOccupied && isPieceInSquareOfCorrectColor) {
                selectedPiece = squareContents;
                getLegalMoves(clickedSquareIndex);
            } else if (isPieceOfCorrectColor && selectedPiece === "wK" && (clickedSquareIndex === 6 || clickedSquareIndex === 2)) {
                if (clickedSquareIndex === 6 && !whiteKingHasMoved && newBoard[5] === '' && newBoard[6] === '' && newBoard[7] === 'wR') {
                    renderCastle(true, newBoard);
                } else if (clickedSquareIndex === 2 && !whiteKingHasMoved && newBoard[1] === '' && newBoard[2] === '' && newBoard[3] === '' && newBoard[0] === 'wR') {
                    renderCastle(false, newBoard);
                }
            } else if (isPieceOfCorrectColor && isLegalMove) {
                renderMove(clickedSquareIndex, newBoard);
            }
        }
    }

    function renderCastle(isShortCastle, board) {
        whiteKingHasMoved = true;
        let notation = "";
        if (isShortCastle) {
            notation = "O-O";

            board[5] = "wR";
            board[6] = "wK";
            board[7] = "";
            board[4] = "";

            setSelectedSquareIndex(6)
        } else {
            notation = "O-O-O";

            board[3] = "wR";
            board[2] = "wK";
            board[0] = "";
            board[4] = "";

            setSelectedSquareIndex(2)
        }

        selectedPiece = "";

        setBoard(board)
        setLegalMoves([])

        turn = turn === 'w' ? 'b' : 'w';
        let jsonPayload = JSON.stringify(board);
        updateInfoBug("Thinking...");

        axios({
            method: 'post',
            url: REST_API_URL + '/getComputerMove',
            headers: {},
            data: {
                jsonPayload: jsonPayload,
                turn: turn,
                notation: notation,
                whiteKingHasMoved: whiteKingHasMoved.toString(),
            }
        }).then(response => {
            if (response.data['gameStatus'] !== 'ongoing') {
                endGame(response.data['gameStatus']);
            } else {
                setTimeout(function(){
                    renderComputerMove(response.data);
                }, 250);
            }
        }).catch(error => {
            console.error(error);
        });
    }

    function handlePieceDrop(event, data) {

        const targetSquareIndex = parseInt(event.target.id);

        if (!legalMoves.includes(targetSquareIndex)) {
            return;
        }

        renderMove(targetSquareIndex, [...board])
    }

    function getLegalMoves(pieceSquareIndex) {
        let legalMoves;

        if (selectedPiece.includes('P')) {
            legalMoves = getLegalPawnMoves(pieceSquareIndex)
        } else if (selectedPiece.includes('N')) {
            legalMoves = getLegalKnightMoves(pieceSquareIndex)
        } else if (selectedPiece.includes('B')) {
            legalMoves = getLegalBishopMoves(pieceSquareIndex)
        } else if (selectedPiece.includes('R')) {
            legalMoves = getLegalRookMoves(pieceSquareIndex)
        } else if (selectedPiece.includes('Q')) {
            legalMoves = getLegalRookMoves(pieceSquareIndex).concat(getLegalBishopMoves(pieceSquareIndex));
        } else {
            legalMoves = getLegalKingMoves(pieceSquareIndex);
        }

        setSelectedSquareIndex(pieceSquareIndex)
        setLegalMoves(legalMoves)
    }

    function getLegalRookMoves(pieceSquareIndex) {
        let moves = [];
        let x = Math.floor(pieceSquareIndex / 8);
        let y = pieceSquareIndex % 8;

        let directions = [[-1, 0], [1, 0], [0, 1], [0, -1]];
        for (let i = 0; i < directions.length; i++) {
            let dx = directions[i][0];
            let dy = directions[i][1];
            let targetX = x + dx;
            let targetY = y + dy;
            while (targetX >= 0 && targetX < 8 && targetY >= 0 && targetY < 8) {
                let target = targetX * 8 + targetY;
                let pieceToTakeColor = String(board[target]).charAt(0);
                if (board[target] === '') {
                    moves.push(target);
                } else if (pieceToTakeColor !== turn) {
                    moves.push(target);
                    break;
                } else {
                    break;
                }
                targetX += dx;
                targetY += dy;
            }
        }
        return moves;
    }

    function getLegalBishopMoves(pieceSquareIndex) {
        let moves = [];
        let x = Math.floor(pieceSquareIndex / 8);
        let y = pieceSquareIndex % 8;

        const directions = [
            [-1, 1],
            [-1, -1],
            [1, 1],
            [1, -1]
        ];

        for (const [dx, dy] of directions) {
            let i = x + dx, j = y + dy;
            while (i >= 0 && i < 8 && j >= 0 && j < 8) {
                let target = i * 8 + j;
                let pieceToTakeColor = String(board[target]).charAt(0);
                if (board[target] === '') {
                    moves.push(target);
                } else if (pieceToTakeColor !== turn) {
                    moves.push(target);
                    break;
                } else {
                    break;
                }
                i += dx;
                j += dy;
            }
        }
        return moves;
    }

    function getLegalPawnMoves(pieceSquareIndex) {
        let squaresToCheck = turn === 'b' ? [-8, -9, -7] : [8, 9, 7];
        let legalSquares = [];

        squaresToCheck.forEach(offset => {
            let targetSquare = pieceSquareIndex + offset;
            let squareContents = board[targetSquare];
            let squareColor = " "
            if (squareContents !== "") {
                squareColor = squareContents.charAt(0);
            }

            if (offset === -8 || offset === 8) {
                if (squareContents === '') {
                    legalSquares.push(targetSquare);
                }
            } else if (squareContents !== '' && squareColor !== turn) {
                legalSquares.push(targetSquare);
            }

            targetSquare = (turn === 'b') ? pieceSquareIndex - 16 : pieceSquareIndex + 16;

            if ((turn === 'b' && pieceSquareIndex > 47 && pieceSquareIndex < 56) ||
                (turn === 'w' && pieceSquareIndex > 7 && pieceSquareIndex < 16)) {
                let squareContents = board[targetSquare];
                if (squareContents === '') {
                    if ((turn === 'b' && board[pieceSquareIndex - 8] === '') ||
                        (turn === 'w' && board[pieceSquareIndex + 8] === ''))
                        legalSquares.push(targetSquare);
                }
            }
        });

        return legalSquares;
    }

    function getLegalKnightMoves(pieceSquareIndex) {
        let squaresToCheck = [10, 17, 15, -6, -10, -15, -17, 6];
        let legalSquares = [];

        for (let i = 0; i < squaresToCheck.length; i++) {
            let targetPieceColor = String(board[pieceSquareIndex + squaresToCheck[i]]).charAt(0)
            let isTargetSquareEmpty = board[pieceSquareIndex + squaresToCheck[i]] === ''
            let isMoveToValidFile = Math.abs((pieceSquareIndex % 8) - ((pieceSquareIndex + squaresToCheck[i]) % 8)) <= 2;

            if (((!isTargetSquareEmpty && targetPieceColor !== turn) || isTargetSquareEmpty) && isMoveToValidFile) {
                legalSquares.push(pieceSquareIndex + squaresToCheck[i])
            }
        }

        return legalSquares;
    }

    function getLegalKingMoves(pieceSquareIndex) {
        let squaresToCheck = [-9, -8, -7, -1, 1, 7, 8, 9];
        let legalMoves = []

        for (let i = 0; i < squaresToCheck.length; i++) {
            let pieceToTakeColor = String(board[pieceSquareIndex + squaresToCheck[i]]).charAt(0);
            let squareToMoveIsEmpty = board[pieceSquareIndex + squaresToCheck[i]] === ''
            let moveIsToValidFile = Math.abs((pieceSquareIndex % 8) - ((pieceSquareIndex + squaresToCheck[i]) % 8)) <= 1;

            if (((!squareToMoveIsEmpty && pieceToTakeColor !== turn) || squareToMoveIsEmpty) && moveIsToValidFile) {
                legalMoves.push(pieceSquareIndex + squaresToCheck[i])
            }
        }

        if (!whiteKingHasMoved && board[5] === '' && board[6] === '' && board[7] === 'wR') {
            legalMoves.push(6);
        } else if (!whiteKingHasMoved && board[1] === '' && board[2] === '' && board[3] === '' && board[0] === 'wR') {
            legalMoves.push(2);
        }

        return legalMoves;
    }

    function renderMove(targetSquareIndex, board) {
        let notation = "";
        if (board[targetSquareIndex] !== "") {
            if (selectedPiece === 'wP' || selectedPiece === 'bP')
                notation = squares[selectedSquareIndex].charAt(0) + "x" + squares[targetSquareIndex];
            else
                notation = selectedPiece.charAt(1) + "x" + squares[targetSquareIndex];
        } else {
            notation = squares[targetSquareIndex];
            if (selectedPiece !== 'wP' && selectedPiece !== 'bP')
                notation = selectedPiece.charAt(1) + notation;
        }

        if (selectedPiece === 'wK') {
            whiteKingHasMoved = true;
        }

        board[targetSquareIndex] = board[selectedSquareIndex];
        board[selectedSquareIndex] = '';

        selectedPiece = "";

        setBoard(board)
        setSelectedSquareIndex(targetSquareIndex)
        setLegalMoves([])

        turn = turn === 'w' ? 'b' : 'w';

        let jsonPayload = JSON.stringify(board);
        updateInfoBug("Thinking...");

        axios({
            method: 'post',
            url: REST_API_URL + '/getComputerMove',
            headers: {},
            data: {
                jsonPayload: jsonPayload,
                turn: turn,
                notation: notation,
                whiteKingHasMoved: whiteKingHasMoved.toString(),
            }
        }).then(response => {
            if (response.data['gameStatus'] !== 'ongoing') {
                endGame(response.data['gameStatus']);
            } else {
                setTimeout(function(){
                    renderComputerMove(response.data);
                }, 250);
            }
        }).catch(error => {
            console.error(error);
        });
    }

    function endGame(gameStatus) {
        // disable clicks on chessboard
        document.getElementById("container").style.pointerEvents = "none";

        alert("Game Over! " + gameStatus)
    }

    function renderComputerMove(data) {
        let newBoard = data['updatedBoard']
        let fromSquare = data['fromSquare']
        let toSquare = data['toSquare']
        let score = data['score']

        let stats;
        if (data["time"] === undefined) {
            stats = data["positionsAnalyzed"]
        } else {
            stats = data["positionsAnalyzed"] + " positions analyzed in " + data["time"] + " seconds."
        }

        updateInfoBug(stats);

        selectedPiece = "";

        setBoard(newBoard)
        setSelectedSquareIndex(toSquare);
        setLegalMoves([])

        turn = turn === 'w' ? 'b' : 'w';
    }
}
