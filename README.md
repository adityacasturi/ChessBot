# Chess Bot

A full-stack (React.js + Spring Boot) chess application featuring a bot opponent using multiple evaluation techniques and opening book strategies.

## Features

- Interactive from-scratch chess board with drag-and-drop functionality
- Bot opponent using multiple techniques for middle and late game evaluation
- Opening book integration for clinical early game play
- Capable of analyzing 1,000,000+ positions in ~3-4 seconds
- Real-time move validation that is reflected on the board

## Installation

1. Clone the repository:
```bash
git clone https://github.com/adityacasturi/ChessBot.git
cd ChessBot
```

2. Install frontend dependencies:
```bash
cd client
npm install
```

3. Install backend dependencies:
```bash
cd backend
mvn clean install
```

## Running the Application

You can start both frontend and backend servers simultaneously using:

```bash
npm start
```

The application will be available at `http://localhost:3000`