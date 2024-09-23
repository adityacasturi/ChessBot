import React, {useState} from 'react'
import Chessboard from './components/Chessboard'
import './styles/App.css'

export default function App() {
    const [positionsAnalyzed, setPositionsAnalyzed] = useState('');

    const updateInfoBug = (data) => {
        setPositionsAnalyzed(data);
    };

    return (
        <div>
            <div id="info-bug">{positionsAnalyzed}</div>
            <Chessboard updateInfoBug={updateInfoBug}></Chessboard>
        </div>
    )
}