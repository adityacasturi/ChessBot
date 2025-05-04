# Chess Bot

A full-stack (React.js + Spring Boot) chess application featuring a bot opponent using multiple evaluation techniques and opening book strategies.

## Features

- Chessboard UI: Developed from scratch with real-time legal move highlighting and bot opponent insights (evaluation time + positions evaluated)
- Bitboard-Based State Management: Uses a bitboard representation for performant game state storage, enabling rapid evaluation and manipulation
- Bot Opponent: Implemented a bot using minimax search with alpha-beta pruning, optimized to analyze over 1,000,000+ positions in 3–4 seconds
- Dynamic Evaluation Engine: Integrated various heuristics for mid and endgame evaluation including piece mobility, center control, and more
- Opening Book Support: Embedded a database of master-level games from lichess.org for strong and accurate early game play

## Installation

1. Clone the repository:
```bash
git clone https://github.com/adityacasturi/ChessBot.git
cd ChessBot
```

2. Install dependencies by running:
```bash
cd client
npm install
```

## Running the Application

You can start both frontend and backend servers simultaneously using:

```bash
npm start
```

The application will be available at `http://localhost:3000`

## Demos

https://github.com/user-attachments/assets/79bdb594-cd14-45cc-b34e-12958d3758d5

https://github.com/user-attachments/assets/089ba4de-c889-488a-a68a-cb4f7b73c712



