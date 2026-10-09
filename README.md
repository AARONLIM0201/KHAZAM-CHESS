# KHAZAM CHESS

A desktop Java implementation of **Kwazam Chess**, a custom chess variant played on an **8x5 board** with unique piece behavior, turn-based board flipping, and save/load support.

## Summary
KHAZAM CHESS is a GUI-based strategy game built with Java Swing using an MVC-style architecture. It features custom pieces (`Sau`, `Ram`, `Biz`, `Tor`, `Xor`), dynamic move rules, a pause menu, and persistent game saves.

## How to Play
### Objective
Capture the opponent's **Sau** to win.

### Basic Flow
- Blue moves first.
- Click one of your pieces to highlight valid moves.
- Click a highlighted square to move (or capture).
- The board view flips after each move.

### Special Rules
- **Tor** and **Xor** swap types every 2 turns.
- **Ram** moves one square forward and reverses direction after reaching the board edge.
- Capturing a **Sau** ends the game immediately.

### In-Game Controls
- Use the **pause button** (top-right) to open the pause menu.
- From pause, you can **resume**, **save**, or **exit to main menu**.

## Key Tech Stack
- **Java** (core language)
- **Java Swing / AWT** (GUI)
- **Object-Oriented Design**
- **MVC pattern** for game structure
- **Observer pattern** via button action listeners

## Project Structure
```text
KHAZAM-CHESS/
├── assets/
│   └── images/
│       ├── Blue_*.png
│       ├── Red_*.png
│       ├── Invert_*.png
│       ├── KwazamChessLogo.png
│       └── pause.png
├── data/
│   └── save.txt
├── src/
│   └── KwazamChess.java
└── README.md
```

## Setup & Run
### Requirements
- JDK 8+ installed

### Compile
From the repository root:
```bash
javac src/KwazamChess.java
```

### Run
```bash
java -cp src KwazamChess
```

## Save Data
Game saves are stored at:
```text
data/save.txt
```
