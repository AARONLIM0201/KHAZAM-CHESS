import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/*
Lecture Section: TC2L
Tutorial Section: TT5L
Members: 
1. Teh Yu Qian             1211108855 (Leader)
2. Aaron Lim Cjun Shien    1211111818
3. Lee Hong Yi             1211108881
*/

// Design Pattern used: MVC(GUI), Singleton(chess board), and Observer(ActionListener for buttons)

// Class ChessBoard to control board features
// Done by: Teh Yu Qian,  Lee Hong Yi
class ChessBoard
{
    private TurnManager turnManager;
    private GameStatus gameStatus;
    private PieceMovement pieceMovement;
    
    private int oneCompleteTurn;
    private int turnCount;

    // 2d array to store chess pieces objects
    private static ChessPiece[][] board;

    // Constructor to initialise ChessBoard object
    public ChessBoard() 
    {
        this.turnManager = new TurnManager();
        this.gameStatus = new GameStatus();
        this.pieceMovement = new PieceMovement(this);

        board = new ChessPiece[8][5];   // board is a 2d array that have 8 rows and 5 cols which stores ChessPiece objects
        initialiseBoard();          // method to initialise board
    }

    // Method to initialise board every game
    // Done by: Teh Yu Qian,  Lee Hong Yi
    public void initialiseBoard() 
    {
        // Set turn and oneCompleteTurn to 1 and 0 at the start of the game
        turnManager.setTurnCount(1);
        turnManager.setOneCompleteTurn(0);

        // Set all positions to null (empty)
        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 5; col++) 
            {
                board[row][col] = null; 
            }
        }
        // Place pieces on the board on their respective default positions
        // Red side pieces
        board[0][0] = new Tor("Red", this, turnManager);
        board[0][1] = new Biz("Red");
        board[0][2] = new Sau("Red", this);
        board[0][3] = new Biz("Red");
        board[0][4] = new Xor("Red", this, turnManager);

        for (int col = 0; col < 5; col++) 
        {
            board[1][col] = new Ram("Red", this);
        }

        // Blue side pieces
        for (int col = 0; col < 5; col++) 
        {
            board[6][col] = new Ram("Blue", this);
        }

        board[7][0] = new Xor("Blue", this, turnManager);
        board[7][1] = new Biz("Blue");
        board[7][2] = new Sau("Blue", this);
        board[7][3] = new Biz("Blue");
        board[7][4] = new Tor("Blue", this, turnManager);
    }

    // Method to change Tor to Xor and vice versa every 2 turns
    // Done by:  Teh Yu Qian
    public void changeTorXor(int oneCompleteTurn, int turnCount)
    {
        //Every 2 turns Tor will change into Xor and vice versa
        if (oneCompleteTurn != 1 && turnCount != 0 && turnCount % 2 == 1) 
        {
            for (int row = 0; row < 8; row++) 
            {
                for (int col = 0; col < 5; col++) 
                {
                    if (board[row][col] != null)
                    {
                        // If Tor then = Xor, vice versa
                        if (board[row][col].getType().equals("Tor"))
                        {
                            board[row][col] = new Xor(board[row][col].getColour(), this, turnManager);
                        }
                        else if (board[row][col].getType().equals("Xor"))
                        {
                            board[row][col] = new Tor(board[row][col].getColour(), this, turnManager);
                        }
                    }
                }
            }
        }
    }

    // Method to flip board array (call when next turn)
    // Done by: Aaron Lim , Teh Yu Qian,  Lee Hong Yi
    public void flipBoardArray()
    {
        ChessPiece[][] tempBoard = new ChessPiece[8][5];    // temporary board to store flipped board
        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 5; col++) 
            {
                tempBoard[row][col] = board[7-row][4-col];
            }
        }
        board = tempBoard;  // set board to flipped board
    }

    // Getters and Setters
    public ChessPiece[][] getBoard() {return board;}

    public ChessPiece getPiece(int row, int col) {return board[row][col];}

    public void setPiece(int row, int col, ChessPiece piece) 
    {
        board[row][col] = piece;
    }

    public void setBoard(ChessPiece[][] board) 
    {
        this.board = board;
    }
}

// Class to Load or Save game
// Done by: Aaron Lim , Teh Yu Qian
class LoadSaveGame
{
    private ChessBoard chessboard;
    private TurnManager turnManager;
    private GameStatus gameStatus;

    private String filePath = "save.txt";   // file path to save game
    private File savefile = new File(filePath);

    // Constructor to initialise LoadSaveGame object
    public LoadSaveGame(ChessBoard chessboard, TurnManager turnManager, GameStatus gameStatus)
    {
        this.chessboard = chessboard;
        this.turnManager = turnManager;
        this.gameStatus = gameStatus;
    }

    // Method to load game from text file
    // Done by: Aaron Lim , Teh Yu Qian
    public void loadGame(String saveName) 
    {
        ChessPiece piece;
        try (BufferedReader reader = new BufferedReader(new FileReader(saveName))) 
        {
            String line = reader.readLine();
            int row = 0;
    
            // Validate file header)
            if (line == null || !line.equals("Chess Game Save File")) 
            {
                System.err.println("Invalid save file format");
                return;
            }
    
            // Read the file line by line
            while ((line = reader.readLine()) != null) 
            {
                // Skip separator and empty lines
                if (line.trim().isEmpty() || line.startsWith("-")) 
                {
                    continue;
                }
    
                // Split elements by |
                if (line.startsWith("|")) 
                {
                    // Check if row index is within bounds
                    if (row >= 8) 
                    { 
                        System.err.println("Too many rows in the save file. Skipping extra rows.");
                        break;
                    }
    
                    String[] pieces = line.split("\\|");    // split them everytime | appears
                    // Remove head and tail empty strings
                    pieces = Arrays.stream(pieces)
                                   .filter(pieceInfo -> pieceInfo.length() >= 7 && !pieceInfo.equals(" "))      // filer the elements that have length >= 7 and not equal to " " (one space)
                                   .toArray(String[]::new);
    
                    int col = 0; // Reset column counter for each row
                    for (String pieceInfo : pieces) 
                    {
                        if (col >= 5) 
                        { // Ensure column index is within bounds
                            System.err.println("Invalid position: row " + row + ", col " + col);
                            break;
                        }
        
                        // Check if board location is empty (10 spaces = empty)
                        if (pieceInfo.equals("          "))
                        {   
                            // Set the board location to null
                            chessboard.setPiece(row, col, null);
                        }
                        else    // if not empty
                        {
                            String[] parts = pieceInfo.split("\\s+"); // Split by emptyspace
        
                            // if valid piece has 3 parts: empty location , color and type
                            if (parts.length == 3) 
                            { 
                                String colour = parts[1];   // get the piece color 
                                String type = parts[2];     // get the piece type
        
                                // Add piece based on their respective type
                                if (type.equals("Sau")) 
                                {
                                    piece = new Sau(colour, chessboard);
                                } 
                                else if (type.equals("Ram"))
                                {
                                    piece = new Ram(colour, chessboard);
                                } 
                                else if (type.equals("Biz")) 
                                {
                                    piece = new Biz(colour);
                                } 
                                else if (type.equals("Tor")) 
                                {
                                    piece = new Tor(colour, chessboard, turnManager);
                                } 
                                else 
                                {
                                    piece = new Xor(colour, chessboard, turnManager);
                                }
        
                                // Place the piece on the board
                                chessboard.setPiece(row, col, piece);
                            } 
                            else    // Error handling: if get invalid format
                            {
                                System.err.println("Invalid format!");
                            }
                        }
    
                        col++; // Increment col only after processing
                    }
                    row++;  // Increment row after processing
                } 
                // Get turn from file
                else if (line.startsWith("Turn:")) 
                {
                    // Split the line by colon to get turn count
                    String[] parts = line.split(":");
                    if (parts.length == 2) 
                    {
                        // Get the number
                        int turnCount = Integer.parseInt(parts[1].trim());
                        turnManager.setTurnCount(turnCount);    // Set the turn count
                    }
                } 
                // Get current colour turn
                else if (line.endsWith("Turn")) 
                {
                    // Split line to get current turn color
                    String[] parts = line.split("'s");
                    if (parts.length == 2) 
                    {
                        // Get first element to get current turn color
                        String currentTurn = parts[0].trim();
                        turnManager.setOneCompleteTurn(currentTurn.equals("Blue") ? 0 : 1); // if Blue then 0, if Red then 1 (since blue moves then red every turn)

                        // Update isTurn for all pieces
                        for (int i = 0; i < 8; i++) 
                        {
                            for (int j = 0; j < 5; j++) 
                            {
                                if (chessboard.getBoard()[i][j] != null) 
                                {
                                    if (chessboard.getBoard()[i][j].getColour().equals(currentTurn)) 
                                    {
                                        chessboard.getBoard()[i][j].setTurn(true);
                                    } 
                                    else 
                                    {
                                        chessboard.getBoard()[i][j].setTurn(false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Error handling 
        catch (IOException e) 
        {
            System.err.println("Error loading the game: " + e.getMessage());
        } 
        catch (NumberFormatException e) 
        {
            System.err.println("Error parsing number: " + e.getMessage());
        }
    }
    
    // Method to save current game state to a file
    // Done by: Aaron Lim , Teh Yu Qian
    public void saveGame(ChessBoard chessboard, TurnManager turnManager, GameStatus gameStatus)
    {
        ChessPiece piece;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) 
        {
            writer.write("Chess Game Save File\n\n");

            // Go through all board positions and write to file
            writer.write("--------------------------------------------------------\n");
            for (int row = 0; row < 8; row++) 
            { 
                writer.write("|");
                for (int col = 0; col < 5; col++) 
                {
                    piece = chessboard.getBoard()[row][col];
                    if (piece != null) 
                    {
                        if (piece.getColour().equals("Red"))
                        {
                            writer.write(" " + piece.getColour() + "  " + piece.getType() + " |");
                        } 
                        else 
                        {
                            writer.write(" " + piece.getColour() + " " + piece.getType() + " |");
                        }
                    } 
                    else 
                    {
                        writer.write("          |"); // Empty space
                    }
                }
                writer.write("\n--------------------------------------------------------\n");
            }
            writer.write("\n");
            writer.write("Turn: " + turnManager.getTurnCount() + "\n");

            // Get current turn colour by oneCompleteTurn
            if (turnManager.getOneCompleteTurn() % 2 == 0)
            {
                writer.write("Blue's Turn\n");
            }
            else
            {
                writer.write("Red's Turn\n");
            }
        } 
        // Error handling
        catch (IOException e) 
        {
            System.err.println("Error saving the game: " + e.getMessage());
        }
    }

    
    // Method to check whether there's previously saved game in txt file
    // Done by: Teh Yu Qian
    public boolean checkSaveEmpty()
    {
        if (savefile.length() == 0) {return true;}     //  if file is empty, return true
        else {return false;}                        //  else means file have a save, return false
    }
}

// Class to handle Turn
// Done by: Lee Hong Yi
class TurnManager 
{
    private ChessBoard chessboard;
    protected int oneCompleteTurn;
    protected int turnCount;
    private ChessPiece[][] board;

    // Constructor to initialise TurnManager object
    public TurnManager() 
    {
        // Initialise turn counts to zero at the start of the game
        oneCompleteTurn = 0;
        turnCount = 1;      // when player first saw it will be their first round so start with 1
    }

    // Method to switch turns after re-positions(flip board position in array) between Blue and Red
    // Done by: Lee Hong Yi
    public void nextTurn(ChessBoard chessboard) 
    {
        this.chessboard = chessboard;
        chessboard.flipBoardArray();    //  Call method from chessboard to do flip board array logic

        // Update isTurn for all pieces
        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 5; col++) 
            {
                if (chessboard.getBoard()[row][col] != null) 
                {
                    // if current isTurn = true, change to false, vice versa
                    chessboard.getBoard()[row][col].setTurn(!chessboard.getBoard()[row][col].isTurn());
                }
            }
        }

        // On complete turn is Blue and Red moved, so 2 pieces moved = 1 turn count
        oneCompleteTurn++;
        if (oneCompleteTurn == 2) 
        {
            turnCount++;
            oneCompleteTurn = 0;    // Reset to 0 for next turn
        }
    }

    // Getters and Setters
    public int getOneCompleteTurn() {return oneCompleteTurn;}
    public int getTurnCount() {return turnCount;}

    public void setOneCompleteTurn(int oneCompleteTurn) {this.oneCompleteTurn = oneCompleteTurn;}
    public void setTurnCount(int turnCount) {this.turnCount = turnCount;}
}

// Class to check whether game is over
// Done by: Lee Hong Yi
class GameStatus 
{
    private boolean isGameOver = false;

    // Method to check if game is over
    // Done by: Lee Hong Yi
    public void checkGameOver(int toRow, int toCol, ChessBoard chessboard)
    {
        // If Piece eaten is Sau, game is over
        if(chessboard.getBoard()[toRow][toCol] != null && chessboard.getBoard()[toRow][toCol].getType().equals("Sau"))
        {
            isGameOver = true;
        }
    }

    // Getters and Setters
    public boolean isGameOver() {return isGameOver;}
    public void setGameOver(boolean isGameOver) {this.isGameOver = isGameOver;}
}

// Class to handle chess pieces movement
// Done by: Aaron Lim 
class PieceMovement 
{
    private ChessBoard chessboard;
    private TurnManager turnManager;

    private ChessPiece[][] board;

    // Constructor to initialise PieceMovement object
    public PieceMovement(ChessBoard chessboard) 
    {
        this.chessboard = chessboard;
    }

    // Method to move pieces
    // Done by: Aaron Lim 
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol)
     {
        ChessPiece piece = chessboard.getPiece(fromRow, fromCol);
        chessboard.setPiece(toRow, toCol, piece);   //Set new pos to this piece
        chessboard.setPiece(fromRow, fromCol, null);      // Set prev pos to null (empty)
    }

    // Method to check whether pieces' movement need changes (typically Ram)
    // Done by: Lee hong yi
    public void checkTransform(ChessBoard chessboard, TurnManager turnManager, int toRow, int toCol)
    {
        this.chessboard = chessboard;
        this.turnManager = turnManager;
        this.board = chessboard.getBoard();

        // Check if piece is a Ram
        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 5; col++) 
            {
                if (board[row][col] != null)
                {
                    if (board[row][col].getType().equals("Ram"))
                    {
                        if (row == 0 || row == 7)
                        {
                            // If piece is Ram, and is at top or bottom of board, then transform movement
                            transformRam(row, col);
                        }
                    }
                }
            }
        }
    }

    // Method to transform Ram's movement
    // Done by: Lee hong yi
    public void transformRam(int currentRow, int currentCol)
    {
        if (currentRow == 0)
        {   
            board[currentRow][currentCol].setChange(true);
        }
        else if (currentRow == 7)
        {   
            board[currentRow][currentCol].setChange(false);
        }  
    }
}

// Class to create position object to store row and col of chess pieces
// Done by: Teh Yu Qian
class Position
{
    private int row;
    private int col;

    // Constructor to initialise Position object
    public Position(int row, int col)
    {
        this.row = row;
        this.col = col;
    }

    // Getters
    public int getRow() { return row; }
    public int getCol() { return col; }
}

// -------------------- Chess Pieces -------------------- 
// Base class for inheritence
// Done by: Teh Yu Qian
abstract class ChessPiece 
{
    protected String colour;     // Determine either red or blue side
    protected String type;  
    protected boolean change;   // To check if the piece has achieve requirements to change

    protected boolean isTurn;   // To check whether its the colour's turn
    protected int currentRow;
    protected int currentCol;
    protected static List<Position> possibleMoves = new ArrayList<>();

    // Getters and Setters

    public abstract void getPossibleMoves(int row, int col);    // abstract method to be implemented by each piece object

    public boolean isTurn() { return isTurn; }
    public void setTurn(boolean isTurn) { this.isTurn = isTurn; }

    public boolean getChange() {return change;}
    public void setChange(boolean change) {this.change = change;}

    public String getColour() {return colour;}
    public String getType() {return type;}

    public String getImagePath() 
    {
        // if its current colour's turn, use normal image.
        // else, use the inverted image.
        return isTurn
            ? colour + "_" + type + ".png" : "Invert_" + colour + "_" + type + ".png";
    }

    public List<Position> getPossibleMovesList() { return possibleMoves; }
}

// Class Sau piece
// Done by: Aaron Lim
class Sau extends ChessPiece
{
    private ChessBoard chessboard;

    // Constructor to initialise Sau object
    public Sau(String colour, ChessBoard chessboard)
    {
        this.colour = colour;
        this.type = "Sau";
        this.isTurn = colour.equals("Blue");
        this.currentRow = -1;
        this.currentCol = -1;
        this.chessboard = chessboard;
    }

    // Abstract method from base class to get possible moves for Sau piece
    // Done by: Aaron Lim
    public void getPossibleMoves(int row, int col)
    {
        this.currentRow = row;
        this.currentCol = col;
        possibleMoves.clear();

        // All possible one-step moves in 8 directions
        int[] rowDirections = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colDirections = {-1, 0, 1, -1, 1, -1, 0, 1};

        // Check each possible direction
        for (int i = 0; i < 8; i++) 
        {
            int nextRow = currentRow + rowDirections[i];
            int nextCol = currentCol + colDirections[i];

            // Check if move is within board boundaries
            if (nextRow >= 0 && nextRow < 8 && nextCol >= 0 && nextCol < 5) 
            {
                ChessPiece pieceAtPosition = chessboard.getBoard()[nextRow][nextCol];
                
                // Add move if square is empty or contains enemy piece
                if (pieceAtPosition == null || !pieceAtPosition.getColour().equals(this.colour)) 
                {
                    possibleMoves.add(new Position(nextRow, nextCol));
                }
            }
        }
    }
}

// Class Ram piece
// Done by: Teh Yu Qian
class Ram extends ChessPiece
{
    private ChessBoard chessboard;
    private int forwardDirection;
    private int nextRow;

    private boolean reachedTop = false;

    // Constructor to initialise Ram object
    public Ram(String colour, ChessBoard chessboard)
    {
        this.chessboard = chessboard;

        this.colour = colour;
        this.type = "Ram";
        this.change = false;
        this.isTurn = colour.equals("Blue");
        this.currentRow = -1;
        this.currentCol = -1;
    }

    // Abstract method from base class to get possible moves for Ram piece
    // Done by: Teh Yu Qian
    public void getPossibleMoves(int row, int col)
    {
        // Initialise variables
        this.currentRow = row;
        this.currentCol = col;
        possibleMoves.clear();

        // If current position still not top / at bottom of board
        if (!reachedTop) 
        {
            forwardDirection = -1;  // Moving up
            nextRow = currentRow + forwardDirection;
            
            // Check if reached the top of board
            if (currentRow == 0) 
            {
                reachedTop = true;
                forwardDirection = 1; // Change direction to down (+1)
                nextRow = currentRow + forwardDirection;
            }
        }

        // Moving down (after reaching top)
        else 
        {
            forwardDirection = 1;  // Moving down
            nextRow = currentRow + forwardDirection;
            // Check if reached the bottom of board
            if (currentRow == 7) 
            {
                reachedTop = false;
                forwardDirection = -1; // Change direction to up (-1)
                nextRow = currentRow + forwardDirection;
            }
        }

        // Make sure we're within board boundaries vertically
        if (nextRow >= 0 && nextRow < 8) 
        {
            // Check if possible move have enemy piece
            ChessPiece targetPiece = chessboard.getBoard()[nextRow][currentCol];
            // if targetpiece == null, means position empty, if colour different, position have enemy piece
            if (targetPiece == null || !targetPiece.getColour().equals(this.colour)) 
            {
                possibleMoves.add(new Position(nextRow, currentCol));
            }
        }

    }
}

// Class Biz piece
// Done by: Aaron Lim
class Biz extends ChessPiece
{
    // Constructor to initialise Biz object
    public Biz(String colour)
    {
        this.colour = colour;
        this.type = "Biz";
        this.isTurn = colour.equals("Blue");
        this.currentRow = -1;
        this.currentCol = -1;
    }

    // Abstract method from base class to get possible moves for Biz piece
    // Done by: Aaron Lim
    public void getPossibleMoves(int row, int col)
    {
        this.currentRow = row;
        this.currentCol = col;
        possibleMoves.clear();

        // All possible L-shaped moves of Biz (like a knight)
        int[] X = {2, 1, -1, -2, -2, -1, 1, 2};
        int[] Y = {1, 2, 2, 1, -1, -2, -2, -1};

        // Check each possible L-shaped move
        for (int i = 0; i < 8; i++) 
        {
            int possibleRow = this.currentRow + X[i];
            int possibleCol = this.currentCol + Y[i];

            // Add position if it's within board boundaries (8x5 board)
            if (possibleRow >= 0 && possibleRow < 8 && possibleCol >= 0 && possibleCol < 5) 
            {
                possibleMoves.add(new Position(possibleRow, possibleCol));
            }
        }
    }
}

// Class Tor piece
// Done by: Lee Hong Yi
class Tor extends ChessPiece
{
    private ChessBoard chessboard;
    private TurnManager turnManager;

    // Constructor to initialise Tor object
    public Tor(String colour, ChessBoard chessboard, TurnManager turnManager)
    {
        this.chessboard = chessboard;
        this.turnManager = turnManager;     // Tor(and Xor) need this to check every turn if it needs to change to Xor

        this.colour = colour;
        this.type = "Tor";
        this.change = false;
        this.isTurn = colour.equals("Blue");
        this.currentRow = -1;
        this.currentCol = -1;
    }

    // Abstract method from base class to get possible moves for Tor piece
    // Done by:  Lee Hong Yi
    public void getPossibleMoves(int row, int col)
    {
        this.currentRow = row;
        this.currentCol = col;
        possibleMoves.clear();

        // Check all four orthogonal directions
        for (int i = 0; i < 8; i++) 
        {
            for (int j = 0; j < 5; j++) 
            {
                if (validMove(currentRow, currentCol, i, j)) 
                {
                    // Check if target square is empty or has enemy piece
                    ChessPiece targetPiece = chessboard.getBoard()[i][j];
                    if (targetPiece == null || !targetPiece.getColour().equals(this.colour)) 
                    {
                        possibleMoves.add(new Position(i, j));
                    }
                }
            }
        }

        //Every 2 turns Tor will change into Xor and vice versa
        if (turnManager.getOneCompleteTurn() != 0 && turnManager.getTurnCount() % 2 == 1) 
        {
            change = true;
        }

    }

    // Method to check if move is valid for Tor piece
    // Done by:  Lee Hong Yi
    private boolean validMove(int fromRow, int fromCol, int toRow, int toCol) 
    {
        // Attempt to move to the same cell
        if (fromRow == toRow && fromCol == toCol) {return false;}

        // Must move either horizontally or vertically
        if (fromRow != toRow && fromCol != toCol) {return false;}

        // Check for pieces in the path
        if (fromRow == toRow) 
        {
            // Horizontal move
            int start = Math.min(fromCol, toCol) + 1;
            int end = Math.max(fromCol, toCol);
            
            // Check each square between start and end
            for (int col = start; col < end; col++) 
            {
                if (chessboard.getBoard()[fromRow][col] != null) 
                {
                    return false;  // Path is blocked
                }
            }
        } 
        else 
        {
            // Vertical move
            int start = Math.min(fromRow, toRow) + 1;
            int end = Math.max(fromRow, toRow);
            
            // Check each square between start and end
            for (int row = start; row < end; row++) 
            {
                if (chessboard.getBoard()[row][fromCol] != null) 
                {
                    return false;  // Path is blocked
                }
            }
        }
        return true;
    }
}

// Class Xor piece
// Done by:  Lee Hong Yi
class Xor extends ChessPiece
{
    private ChessBoard chessboard;
    private TurnManager turnManager;

    // Constructor to initialise Xor object
    public Xor(String colour, ChessBoard chessboard, TurnManager turnManager)
    {
        this.chessboard = chessboard;
        this.turnManager = turnManager;

        this.colour = colour;
        this.type = "Xor";
        this.change = false;
        this.isTurn = colour.equals("Blue");
        this.currentRow = -1;
        this.currentCol = -1;
    }

    public void getPossibleMoves(int row, int col)
    {
        this.currentRow = row;
        this.currentCol = col;
        possibleMoves.clear();

        // Check all possible squares for diagonal moves
        for (int i = 0; i < 8; i++) 
        {
            for (int j = 0; j < 5; j++) 
            {
                if (validMove(currentRow, currentCol, i, j)) 
                {
                    // Check if target square is empty or has enemy piece
                    ChessPiece targetPiece = chessboard.getBoard()[i][j];
                    if (targetPiece == null || !targetPiece.getColour().equals(this.colour)) 
                    {
                        possibleMoves.add(new Position(i, j));
                    }
                }
            }
        }

        //Every 2 turns Tor will change into Xor and vice versa
        if (turnManager.getOneCompleteTurn() != 0 && turnManager.getTurnCount() % 2 == 1) 
        {
            change = true;
        }
    }

    // Method to check if move is valid for Xor piece
    // Done by:  Lee Hong Yi
    private boolean validMove(int fromRow, int fromCol, int toRow, int toCol) 
    {
        // Attempt to move to the same cell
        if (fromRow == toRow && fromCol == toCol) {return false;}

        // Must move diagonally (change in row must equal change in col)
        if (Math.abs(fromRow - toRow) != Math.abs(fromCol - toCol)) {return false;}

        // Check for pieces in the diagonal path
        int rowStep = (toRow > fromRow) ? 1 : -1;
        int colStep = (toCol > fromCol) ? 1 : -1;
        
        int row = fromRow + rowStep;
        int col = fromCol + colStep;
        
        // Check each square along the diagonal until target
        while (row != toRow && col != toCol) 
        {
            if (chessboard.getBoard()[row][col] != null) 
            {
                return false;  // Path is blocked
            }
            row += rowStep;
            col += colStep;
        }

        return true;
    }
}

//-------------------- View Classes --------------------

// Class to handle view for frame and create all base panel objects
// Done by: Teh Yu Qian,  Lee Hong Yi
class GameFrameView extends JFrame
{
    private JFrame frame;

    // 4 Base Panels
    private MainPanelView mainPanel;     // main menu panel 
    private GamePanelView gamePanel;   // panel for when game start
    private PausePanelView pausePanel; // panel for when game paused
    private WinPanelView winPanel;   // panel to show winner when game ends
    
    // Constructor to initialise GameFrameView object
    public GameFrameView()
    {
        frame = new JFrame("Kwazam Chess");
        frame.setSize(800, 800);
        frame.setMinimumSize(new Dimension(550, 600)); // set min size of window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setLayout(new CardLayout());  // to switch panels

        // Configure base panels
        mainPanel = new MainPanelView();
        gamePanel = new GamePanelView();
        pausePanel = new PausePanelView();
        winPanel = new WinPanelView();  
        
        // Add base panels to frame
        frame.add(mainPanel, "Main Menu");
        frame.add(gamePanel, "Start Game");
        frame.add(pausePanel, "Pause Game");
        frame.add(winPanel, "Win Game");

        frame.setVisible(true);
    }

    // Method to show selected panel
    // Done by: Teh Yu Qian, Lee Hong Yi
    public void showCard(JFrame frame, String cardName) 
    {
        // Switch to the selected panel passing their 'name'
        CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
        cl.show(frame.getContentPane(), cardName);
    }

    // Getters for each panel/page
    public MainPanelView getMainPanel(){return mainPanel;}
    public GamePanelView getGamePanel(){return gamePanel;}
    public PausePanelView getPausepanel(){return pausePanel;}
    public WinPanelView getWinPanel(){return winPanel;}
    public JFrame getFrame(){return frame;}
}

// Class to handle view for main menu panel
// Done by: Aaron Lim
class MainPanelView extends JPanel
{
    private JButton newGameButton, continueButton, exitGameButton;

    private JButton confirmLoadButton, cancelLoadButton;
    private JDialog loadSaveDialog;

    // Constructor to initialise MainPanelView object
    public MainPanelView()
    {
        setLayout(new GridLayout(2, 1));

        // top panel for game logo
        JPanel mainTopPanel = new JPanel(new GridBagLayout());
        add(mainTopPanel);

        // bottom panel for buttons
        JPanel mainBottomPanel = new JPanel(new GridLayout(1, 3));
        add(mainBottomPanel);

        // left filler panel
        JPanel mainLeftPanel = new JPanel();
        mainBottomPanel.add(mainLeftPanel);

        // panel for buttons
        JPanel mainCenterPanel = new JPanel(new GridBagLayout());
        mainBottomPanel.add(mainCenterPanel);

        // GBC to align component view
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(80, 10, 0, 10); // Padding between buttons

        //Game Logo image
        ImageIcon icon = new ImageIcon("KwazamChessLogo.png");

        // Set the image on a JLabel
        JLabel logo = new JLabel(icon);

        // Add the label
        mainTopPanel.add(logo);

        // Configure buttons
        newGameButton = new JButton("NEW GAME");
        continueButton = new JButton("CONTINUE");
        exitGameButton = new JButton("EXIT GAME");

        newGameButton.setFocusable(false);
        continueButton.setFocusable(false);
        exitGameButton.setFocusable(false);

        Dimension buttonSize = new Dimension(50, 30);
        newGameButton.setPreferredSize(buttonSize);
        continueButton.setPreferredSize(buttonSize);
        exitGameButton.setPreferredSize(buttonSize);

        Font buttonFont = new Font("Arial Black", Font.BOLD, 25);
        newGameButton.setFont(buttonFont);
        continueButton.setFont(buttonFont);
        exitGameButton.setFont(buttonFont);

        newGameButton.setBackground(Color.WHITE);
        continueButton.setBackground(Color.WHITE);
        exitGameButton.setBackground(Color.WHITE);

        // Use GridBagConstraints to stack buttons vertically in the center panel
        gbc.insets = new Insets(0, 10, 10, 10); // Padding between buttons
        gbc.fill = GridBagConstraints.BOTH; // Buttons expand dynamically
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        mainCenterPanel.add(newGameButton, gbc);

        gbc.insets = new Insets(10, 10, 10, 10); 
        mainCenterPanel.add(continueButton, gbc);

        gbc.insets = new Insets(10, 10, 100, 10); 
        mainCenterPanel.add(exitGameButton, gbc);

        // Right filler panel
        JPanel rightPanel = new JPanel();
        mainBottomPanel.add(rightPanel);


        // Configure load save JDialog view (to get confirmation from user to load game)
        loadSaveDialog = new JDialog();
        loadSaveDialog.setTitle("Load Save");
        loadSaveDialog.setLayout(new GridBagLayout());
        loadSaveDialog.setSize(300, 150);
        loadSaveDialog.setLocationRelativeTo(null);
        loadSaveDialog.setModal(true);

        gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 20, 5); // Margins for components

        // Confirmation label
        JLabel idLabel = new JLabel("Do you want to load the game?");
        idLabel.setFont(new Font("Arial", Font.BOLD, 15));
        idLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text

        // Confirmation Buttons configuration
        confirmLoadButton = new JButton("Confirm");
        confirmLoadButton.setFont(new Font("Arial", Font.BOLD, 12));

        cancelLoadButton = new JButton("Cancel");
        cancelLoadButton.setFont(new Font("Arial", Font.BOLD, 12));

        Dimension buttonSize1 = new Dimension(100, 30);
        confirmLoadButton.setPreferredSize(buttonSize1);
        cancelLoadButton.setPreferredSize(buttonSize1);

        confirmLoadButton.setFocusable(false);
        cancelLoadButton.setFocusable(false);

        // Set gbc for label to span the full width
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Make the label span across both columns
        loadSaveDialog.add(idLabel, gbc);

        // Adjust gbc for button placement
        gbc.weightx = 0.5; // Equal weight for both buttons to share horizontal space equally
        gbc.gridwidth = 1; // Reset gridwidth for buttons

        // Add components to the dialog
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 5, 20);
        loadSaveDialog.add(confirmLoadButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 5, 20);
        loadSaveDialog.add(cancelLoadButton, gbc);
    }

    // Getters for buttons and JDialog
    public JButton getNewGameButton() {return newGameButton;}
    public JButton getContinueButton() {return continueButton;}
    public JButton getExitGameButton() {return exitGameButton;}

    public JButton getConfirmLoadButton() {return confirmLoadButton;}
    public JButton getCancelLoadButton() {return cancelLoadButton;}

    public JDialog getLoadSaveDialog() {return loadSaveDialog;}
    
}

// Class to handle view for pause panel
// Done by: Lee Hong Yi
class GamePanelView extends JPanel
{
    private JButton[][] pieceButtons;
    private JButton[][] tempButtons;

    private JPanel gameBoardPanel;

    private JPanel gameCenterPanel;

    private JLabel turnCountLabel;
    private JLabel colourLabel;

    private JButton optionButton;

    // Constructor to initialise GamePanelView object
    public GamePanelView()
    {
        // Configure Game Panel view
        setLayout(new BorderLayout());
        JPanel gameTopPanel = new JPanel();
        gameTopPanel.setPreferredSize(new Dimension(0, 100));
        add(gameTopPanel, BorderLayout.NORTH);

        JPanel fillerPanel1 = new JPanel();
        gameTopPanel.add(fillerPanel1);

        turnCountLabel = new JLabel("Turn: ");
        turnCountLabel.setFont(new Font("Arial", Font.BOLD, 20));
        turnCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 100));
        gameTopPanel.add(turnCountLabel);

        JPanel fillerPanel2 = new JPanel();
        gameTopPanel.add(fillerPanel2); 

        colourLabel = new JLabel("");
        colourLabel.setFont(new Font("Arial", Font.BOLD, 20));
        colourLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gameTopPanel.add(colourLabel);

        JLabel turnLabel = new JLabel(" Turn");
        turnLabel.setFont(new Font("Arial", Font.BOLD, 20));
        turnLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 140));
        gameTopPanel.add(turnLabel);

        JPanel fillerPanel3 = new JPanel();
        gameTopPanel.add(fillerPanel3);

        optionButton = new JButton();
        optionButton.setIcon(loadImage("pause.png", 30, 30));
        optionButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        optionButton.setFocusable(false);
        optionButton.setFocusPainted(false);
        optionButton.setPreferredSize(new Dimension(50, 50));
        optionButton.setBackground(new Color(238, 238, 238));
        gameTopPanel.add(optionButton);

        gameCenterPanel = new JPanel(new GridBagLayout());
        add(gameCenterPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; // Buttons expand dynamically
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        gbc.insets = new Insets(0, 0, 50, 10); // Padding between panels
        gbc.fill = GridBagConstraints.BOTH; // Components expand dynamically
        gbc.weighty = 1;  
        
        gbc.gridx = 0;  
        gbc.weightx = 1;  
        gbc.gridwidth = 1;  
        JPanel gameLeftPanel = new JPanel();
        gameLeftPanel.setPreferredSize(new Dimension(300, 0));
        gameLeftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, -10));
        gameCenterPanel.add(gameLeftPanel, gbc);

        gbc.gridx = 1;  
        gbc.weightx = 1; 
        gbc.insets = new Insets(0, 0, 50, 0); // Padding between panels
        gameBoardPanel = new JPanel(new BorderLayout());

        gameBoardPanel = new JPanel(new GridLayout(8,5,3,3));
        gameBoardPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        gameBoardPanel.setBackground(Color.black); 
        
        gameCenterPanel.add(gameBoardPanel, gbc);

        // Create 8x5 buttons
        pieceButtons = new JButton[8][5];
        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 5; col++) 
            {
                // Button view configuration
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(60, 70));    
                button.setBackground(Color.white);
                button.setFocusPainted(false);
                gameBoardPanel.add(button); // Add button to panel

                pieceButtons[row][col] = button;    // Add button to array
            }
        }

        gbc.insets = new Insets(0, 10, 50, 0); // Padding between panels
        gbc.gridx = 2;  // Position at third column
        gbc.weightx = 1;  // Share equal horizontal space
        gbc.gridwidth = 1;
        JPanel gameRightPanel = new JPanel();
        gameRightPanel.setPreferredSize(new Dimension(300, 0));
        gameLeftPanel.setBorder(BorderFactory.createEmptyBorder(0, -10, 0, 0));
        gameCenterPanel.add(gameRightPanel, gbc);
    }

    // Method to resize image
    // Done by: Teh Yu Qian
    public ImageIcon loadImage(String path, int width, int height) 
    {
        Image image = new ImageIcon(this.getClass().getResource(path)).getImage();
        Image scaledImage = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    // Method to set button images by their respective chess pieces
    // Done by: Aaron Lim , Teh Yu Qian,  Lee Hong Yi
    public void renderBoard(ChessPiece[][] board) 
    {
        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 5; col++) 
            {
                ChessPiece piece = board[row][col];
                if (piece != null) 
                {
                    pieceButtons[row][col].setIcon(loadImage(piece.getImagePath(), 60, 65));    // Add chesspiece image to button
                    // Check if get piece = Ram, and if true then check if it needs to change direction
                    if (piece.getType().equals("Ram"))
                    {
                        // if Ram piece reached end, direction will different from others, so image will be different (terbalik)
                        if (piece.getChange() == true)
                        {
                            if (piece.isTurn() == true)
                            {
                                pieceButtons[row][col].setIcon(new ImageIcon("Invert_" + piece.getColour() + "_" + piece.getType() + ".png"));
                            }
                            else
                            {
                                pieceButtons[row][col].setIcon(new ImageIcon(piece.getColour() + "_" + piece.getType() + ".png"));
                            }
                        }
                    }
                } 
                else    // Empty space
                {
                    pieceButtons[row][col].setIcon(null); // Empty space
                }
            }
        }
    }

    // Method to reset all possible moves to white (if player clicks onnself again or other piece)
    // Done by: Aaron Lim , Teh Yu Qian,  Lee Hong Yi
    public void resetHighlight()
    {
        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 5; col++) 
            {
                pieceButtons[row][col].setBackground(Color.white);
            }
        }
    }

    // Method to turn board View by 180 degrees each time piece move
    // Done by: Aaron Lim , Teh Yu Qian,  Lee Hong Yi
    public void flipBoard(ChessPiece[][] board) 
    {
        // Temporary storage for flipped buttons
        tempButtons = new JButton[8][5];
    
        // Change to reverse order
        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 5; col++) 
            {
                // Reverse the row and col
                tempButtons[row][col] = pieceButtons[7 - row][4 - col];
            }
        }
        pieceButtons = tempButtons;     // Update pieceButtons array

        // Update View
        gameBoardPanel.removeAll(); // Clear all existing buttons to re-add new ones
    
        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 5; col++) 
            {
                gameBoardPanel.add(pieceButtons[row][col]); // Add buttons in new flipped order
            }
        }
    
        gameBoardPanel.revalidate(); // Refresh the layout
        gameBoardPanel.repaint();   // Repaint the panel
    }

    // Getters

    public JButton[][] getPieceButtons() { return pieceButtons; }
    public JLabel getTurnCountLabel() { return turnCountLabel; }
    public JLabel getColourLabel() { return colourLabel; }

    public JButton getOptionButton() { return optionButton; }
    
}

// Class to handle view for pause panel
// Done by: Lee Hong Yi
class PausePanelView extends JPanel
{
    private JButton resumeButton;
    private JButton saveButton;
    private JButton exitButton;

    private JButton confirmSaveButton; 
    private JButton cancelSaveButton;

    private JDialog saveDialog;     // Dialog to confirm save game

    // Constructor to initialise PausePanelView object
    public PausePanelView()
    {

        // Configure Pause Panel view
        setLayout(new GridBagLayout());

        JPanel pauseLabelPanel = new JPanel();
        JPanel pauseButtonPanel = new JPanel();

        pauseButtonPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 10, 100, 10);

        JLabel pauseGameLabel = new JLabel("Game Paused");
        pauseGameLabel.setFont(new Font("Arial Black", Font.BOLD, 50));

        pauseLabelPanel.add(pauseGameLabel);
        gbc.insets = new Insets(10, 10, 10, 10);

        resumeButton = new JButton("Resume Game");
        saveButton = new JButton("Save Game");
        exitButton = new JButton("Back to Home");

        resumeButton.setFont(new Font("Arial", Font.BOLD, 20));
        saveButton.setFont(new Font("Arial", Font.BOLD, 20));
        exitButton.setFont(new Font("Arial", Font.BOLD, 20));
        
        resumeButton.setPreferredSize(new Dimension(200, 50));
        saveButton.setPreferredSize(new Dimension(200, 50));
        exitButton.setPreferredSize(new Dimension(200, 50));

        resumeButton.setFocusable(false);
        saveButton.setFocusable(false);
        exitButton.setFocusable(false);

        resumeButton.setBackground(Color.WHITE);
        saveButton.setBackground(Color.WHITE);
        exitButton.setBackground(Color.WHITE);

        pauseButtonPanel.add(resumeButton, gbc);
        pauseButtonPanel.add(saveButton, gbc);
        pauseButtonPanel.add(exitButton, gbc);

        add(pauseLabelPanel, gbc);
        gbc.insets = new Insets(100, 10, 10, 10);
        add(pauseButtonPanel, gbc);

        // Confirm Save Dialog configuration
        saveDialog = new JDialog();
        saveDialog.setTitle("Save Game");
        saveDialog.setLayout(new GridBagLayout());
        saveDialog.setSize(300, 150);
        saveDialog.setLocationRelativeTo(null);
        saveDialog.setModal(true);

        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 20, 5); // Margins for components

        JLabel idLabel = new JLabel("Do you want to save the game?");
        idLabel.setFont(new Font("Arial", Font.BOLD, 15));
        idLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text
        
        confirmSaveButton = new JButton("Save");
        confirmSaveButton.setFont(new Font("Arial", Font.BOLD, 13));
        confirmSaveButton.setPreferredSize(new Dimension(100, 40));
        confirmSaveButton.setFocusable(false);

        cancelSaveButton = new JButton("Cancel");
        cancelSaveButton.setFont(new Font("Arial", Font.BOLD, 13));
        cancelSaveButton.setPreferredSize(new Dimension(100, 40));
        cancelSaveButton.setFocusable(false);

        // Set GridBagConstraints for label to span the full width
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Make the label span across both columns
        saveDialog.add(idLabel, gbc);

        // Adjust GridBagConstraints for button placement
        gbc.weightx = 0.5; // Equal weight for both buttons to share horizontal space equally
        gbc.gridwidth = 1; // Reset gridwidth for buttons

        // Add components to the dialog
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 5, 20);
        saveDialog.add(confirmSaveButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 5, 20);
        saveDialog.add(cancelSaveButton, gbc);
    }

    // Getters
    public JButton getResumeButton() { return resumeButton; }
    public JButton getSaveButton() { return saveButton; }
    public JButton getExitButton() { return exitButton; }

    public JButton getConfirmSaveButton() { return confirmSaveButton; }

    public JDialog getSaveDialog() { return saveDialog; }
    public JButton getCancelSaveButton() { return cancelSaveButton; }
}

// Class to handle view for win panel
// Done by: Aaron Lim
class WinPanelView extends JPanel
{
    private JLabel winColourLabel;
    private JButton newGameButton;
    private JButton backToHomeButton;
    private JButton exitGameButton;

    // Constructor to initialise WinPanelView object
    public WinPanelView()
    {
        // Configure Win Panel view
        setLayout(new GridBagLayout());

        JPanel winLabelPanel = new JPanel();
        JPanel winButtonPanel = new JPanel();

        winButtonPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 10, 100, 10);

        winColourLabel = new JLabel("");
        winColourLabel.setFont(new Font("Arial Black", Font.BOLD, 50));
        winColourLabel.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 0));

        JLabel winLabel = new JLabel(" Wins ! ! ! ! !");
        winLabel.setFont(new Font("Arial Black", Font.BOLD, 50));

        winLabelPanel.add(winColourLabel);
        winLabelPanel.add(winLabel);
        gbc.insets = new Insets(10, 10, 10, 10);

        newGameButton = new JButton("New Game");
        backToHomeButton = new JButton("Back to Home");
        exitGameButton = new JButton("Exit Game");

        newGameButton.setFont(new Font("Arial", Font.BOLD, 20));
        backToHomeButton.setFont(new Font("Arial", Font.BOLD, 20));
        exitGameButton.setFont(new Font("Arial", Font.BOLD, 20));
        
        newGameButton.setPreferredSize(new Dimension(200, 50));
        backToHomeButton.setPreferredSize(new Dimension(200, 50));
        exitGameButton.setPreferredSize(new Dimension(200, 50));

        newGameButton.setFocusable(false);
        backToHomeButton.setFocusable(false);
        exitGameButton.setFocusable(false);

        winButtonPanel.add(newGameButton, gbc);
        winButtonPanel.add(backToHomeButton, gbc);
        winButtonPanel.add(exitGameButton, gbc);

        add(winLabelPanel, gbc);
        gbc.insets = new Insets(100, 10, 10, 10);
        add(winButtonPanel, gbc);
    }

    // Getters
    public JLabel getWinColourLabel() { return winColourLabel; }
    public JButton getNewGameButton() { return newGameButton; }
    public JButton getBackToHomeButton() { return backToHomeButton; }
    public JButton getExitGameButton() { return exitGameButton; }
    
}


//-------------------------------------------------------------------------------------------------------------------------------------
// -------------------------------------- Controller ----------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------------------


// Class to handle controller for game frame and create all base panel controllers
// Done by: Teh Yu Qian,  Lee Hong Yi
class GameFrameController
{
    // self MVC
    private ChessBoard chessboard;
    private TurnManager turnManager;
    private GameStatus gameStatus;
    private PieceMovement pieceMovement;

    private GameFrameView frame;

    //child MVC
    private MainPanelView mainPanel;
    private GamePanelView gamePanel;
    private PausePanelView pausePanel;
    private WinPanelView winPanel;

    private MainPanelController mainPanelController;
    private GamePanelController gamePanelController;
    private PausePanelController pausePanelController;
    private WinPanelController winPanelController;

    // Constructor to initialise GameFrameController object
    public GameFrameController(ChessBoard chessboard, TurnManager turnManager, GameStatus gameStatus, PieceMovement pieceMovement, GameFrameView frame)
    {
        this.chessboard = chessboard;
        this.turnManager = turnManager;
        this.gameStatus = gameStatus;
        this.pieceMovement = pieceMovement;

        this.frame = frame;

        // Get all base panels
        mainPanel = frame.getMainPanel();
        gamePanel = frame.getGamePanel();
        pausePanel = frame.getPausepanel();
        winPanel = frame.getWinPanel();

        // Create new instances of each panel controller
        mainPanelController = new MainPanelController(chessboard, turnManager, gameStatus, pieceMovement, mainPanel, frame);
        gamePanelController = new GamePanelController(chessboard, turnManager, gameStatus, pieceMovement, frame, gamePanel);
        pausePanelController = new PausePanelController(chessboard, turnManager, gameStatus, pieceMovement, frame, pausePanel);
        winPanelController = new WinPanelController(chessboard, turnManager, gameStatus, pieceMovement, frame, winPanel);
    }
}

// Class to handle controller for main menu panel
// Done by: Aaron Lim
class MainPanelController
{
    // parent MVC
    private ChessBoard chessboard;
    private TurnManager turnManager;
    private GameStatus gameStatus;
    private PieceMovement pieceMovement;
    private LoadSaveGame loadSaveGame;

    private GameFrameView frame;

    // self MVC
    private MainPanelView mainPanel;

    // Constructor to initialise MainPanelController object
    public MainPanelController(ChessBoard chessboard, TurnManager turnManager, GameStatus gameStatus, PieceMovement pieceMovement, MainPanelView mainPanel, GameFrameView frame)
    {
        this.chessboard = chessboard;
        this.turnManager = turnManager;
        this.gameStatus = gameStatus;
        this.pieceMovement = pieceMovement;

        this.mainPanel = mainPanel;
        this.frame = frame;

        // Create new instance of LoadSaveGame
        loadSaveGame = new LoadSaveGame(chessboard, turnManager, gameStatus);

        // Observer DP: Add action listeners to buttons and link them to respective methods
        mainPanel.getNewGameButton().addActionListener(e -> newGame());    // Switch to game panel
        mainPanel.getContinueButton().addActionListener(e -> continueGame());   // Show load save dialog
        mainPanel.getExitGameButton().addActionListener(e -> System.exit(0));   // Exit game

        mainPanel.getConfirmLoadButton().addActionListener(e -> loadSavedGame());    
        mainPanel.getCancelLoadButton().addActionListener(e -> mainPanel.getLoadSaveDialog().setVisible(false));
    }

    // Method to check whether there's a previously save game, and get confirmation from player to either continue game or start a new one
    // Done by: Aaron Lim
    public void newGame()
    {
        if (loadSaveGame.checkSaveEmpty())      //  if save is empty
        {
            // Reset board
            chessboard.initialiseBoard();
            turnManager.setTurnCount(1);    // reset turn
            turnManager.setOneCompleteTurn(0);
            // New gamepanelcontroller to initialise game panel
            GamePanelController gamePanelController = new GamePanelController(chessboard, turnManager, gameStatus, pieceMovement, frame, frame.getGamePanel());
            frame.showCard(frame.getFrame(), "Start Game");    // Show game page
        }
        else
        {   
            Object[] options = {"Yes, continue game", "No, start a new game"};
            int result = JOptionPane.showOptionDialog(
                    mainPanel,
                    "There's a previously saved game. Do you want to continue the game?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,      // Custom button text
                    options[0]    // Default button
            );
            // Handle user's response
            if (result == JOptionPane.YES_OPTION) 
            {
                loadSavedGame();     // if player wants to continue game, call method to load from txt file
            } 
            else if (result == JOptionPane.NO_OPTION)
            {
                // Reset board
                chessboard.initialiseBoard();
                turnManager.setTurnCount(1);    // reset turn
                turnManager.setOneCompleteTurn(0);
                // New gamepanelcontroller to initialise game panel
                GamePanelController gamePanelController = new GamePanelController(chessboard, turnManager, gameStatus, pieceMovement, frame, frame.getGamePanel());
                frame.showCard(frame.getFrame(), "Start Game");    // Show game page
            }
        }
    }

    // Method to load saved game
    // Done by: Aaron Lim
    public void continueGame()
    {
        if(loadSaveGame.checkSaveEmpty())
        {
            Object[] options = {"Ok"};
            int result = JOptionPane.showOptionDialog(
            null,
            "There's currently no saved game. Please start a new one.",
            "No Save",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[0]
        );
        }
        else
        {
            mainPanel.getLoadSaveDialog().setVisible(true);
        }
    }

    public void loadSavedGame()
    {
        loadSaveGame.loadGame("save.txt");  // Load game from save file
        GamePanelController gamePanelController = new GamePanelController(chessboard, turnManager, gameStatus, pieceMovement, frame, frame.getGamePanel());   // Create new game panel controller to update game panel after loaded
        frame.showCard(frame.getFrame(), "Start Game");   // Show game panel
        mainPanel.getLoadSaveDialog().setVisible(false);    // Hide load save dialog
    }
}   

// Class to handle controller for game panel's view and game models(logic)
// Done by: Aaron Lim , Teh Yu Qian,  Lee Hong Yi
class GamePanelController
{
    // parent MVC
    private ChessBoard chessboard;
    private TurnManager turnManager;
    private GameStatus gameStatus;
    private PieceMovement pieceMovement;
    private ChessPiece piece;

    private GameFrameView frame;

    // Same lvl MVC
    private PausePanelView pausePanel;
    private WinPanelView winPanel;

    private WinPanelController winPanelController;

    // self MVC
    private GamePanelView gamePanel;

    private boolean repeatClick = true;

    // Constructor to initialise GamePanelController object
    // Done by: Aaron Lim , Teh Yu Qian,  Lee Hong Yi
    public GamePanelController(ChessBoard chessboard, TurnManager turnManager, GameStatus gameStatus, PieceMovement pieceMovement, GameFrameView frame, GamePanelView gamePanel)
    {
        this.chessboard = chessboard;
        this.turnManager = turnManager;
        this.gameStatus = gameStatus;
        this.pieceMovement = pieceMovement;

        this.frame = frame;
        this.gamePanel = gamePanel;

        // Observer DP: Add action listeners to buttons and link them to respective methods
        gamePanel.getOptionButton().addActionListener(e -> frame.showCard(frame.getFrame(), "Pause Game"));

        gamePanel.resetHighlight();     // Reset all previous highlighted positions
        gamePanel.renderBoard(chessboard.getBoard());   // Render board view (link board array to buttons)
        updateTurnView();   // Method to update turn view (on top panel in game panel view)
        setButtonListener();    //  Method to set button listener based by piece turn
    }

    // Method to update turn count and colour's turn each turn
    // Done by: Lee Hong Yi
    public void updateTurnView()
    {
        // gamePanel is the view class
        gamePanel.getTurnCountLabel().setText("Turn: " + turnManager.getTurnCount());

        // Check if turn is even or odd to determine which player's turn
        gamePanel.getColourLabel().setText(turnManager.getOneCompleteTurn() % 2 == 0 ? "Blue's" : "Red's");
        // Set colour of text based on player's turn
        if (gamePanel.getColourLabel().getText().equals("Blue's"))
        {
            gamePanel.getColourLabel().setForeground(Color.BLUE);
        }
        else
        {
            gamePanel.getColourLabel().setForeground(Color.RED);
        }
        //gamePanel.renderBoard(chessboard.getBoard());
    }

    // Method to set button listener based on piece turn
    // Done by: Teh Yu Qian,  Lee Hong Yi
    public void setButtonListener()
    {
        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 5; col++) 
            {
                // Remove all action listeners from buttons (Initialise)
                for (ActionListener al : gamePanel.getPieceButtons()[row][col].getActionListeners()) 
                {
                    gamePanel.getPieceButtons()[row][col].removeActionListener(al);
                }
            }
        }

        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 5; col++) 
            {
                int currentRow = row;
                int currentCol = col;
                // If pos is empty, then set notTurn
                if (chessboard.getPiece(row, col) == null)
                {
                    gamePanel.getPieceButtons()[row][col].addActionListener(e -> notTurn(currentRow, currentCol));
                }
                // If current turn is not pos pieces', then set notTurn
                else if (chessboard.getPiece(row, col).isTurn() == false)
                {
                    gamePanel.getPieceButtons()[row][col].addActionListener(e -> notTurn(currentRow, currentCol));
                }
                // Set to handlClick if current turn is pos pieces'
                else
                {
                    gamePanel.getPieceButtons()[row][col].addActionListener(e -> handleClick(currentRow, currentCol));
                }
            }
        }
    }
    
    // Method to let button do nothing when clicked (not their turn / empty)
    public void notTurn(int currentRow, int currentCol)
    {
        ;   // not their turn / empty space so do nothing when clicked
    }

    // Method that handles operation when current turn piece button is clicked
    // Done by: Aaron Lim , Teh Yu Qian,  Lee Hong Yi
    public void handleClick(int currentRow, int currentCol)
    {
        repeatClick = !repeatClick;     // current click different with prev click
        
        // Check if its the first time the same button is clicked
        if (!repeatClick)
        {
            // Reset possible moves (reset highlight view)
            gamePanel.resetHighlight();     // reset all possiblemoves to white (after that set bg colour for possible move / enemy)
            // Get current piece and its respective possible moves
            chessboard.getPiece(currentRow, currentCol).getPossibleMoves(currentRow, currentCol);   // Get piece button and get possible moves
            
            //  To show all possiblemoves and set bg colour based on pos situation
            for (Position position : ChessPiece.possibleMoves) 
            {
                // Check if in board bound
                if (position.getRow() >= 0 && position.getRow() < 8 && 
                    position.getCol() >= 0 && position.getCol() < 5) 
                {
                    
                    ChessPiece targetPiece = chessboard.getPiece(position.getRow(), position.getCol());
                    
                    // Check if target move is empty or enemy piece
                    if (targetPiece == null || !(targetPiece.getColour().equals(chessboard.getPiece(currentRow, currentCol).getColour()))) 
                    {    
                        // If target move is empty
                        if (targetPiece == null) 
                        {
                            gamePanel.getPieceButtons()[position.getRow()][position.getCol()].setBackground(Color.LIGHT_GRAY);
                        } 
                        // If target move is enemy piece
                        else 
                        {
                            // Red background if enemy in target move/position
                            gamePanel.getPieceButtons()[position.getRow()][position.getCol()].setBackground(new Color(255, 150, 150));
                        }
                        
                        // If confirmed move, set Action listener that links to handleMove for button at target move/position
                        final int moveRow = position.getRow();
                        final int moveCol = position.getCol();
                        gamePanel.getPieceButtons()[moveRow][moveCol].addActionListener(e -> 
                        {
                            handleMove(currentRow, currentCol, moveRow, moveCol);
                        });
                    }
                }
            }
        }
        else    // if same piece is clicked again, cancel move by resetting
        {
            gamePanel.resetHighlight();     // View set bg to white
            setButtonListener();
        }
    }

    // Method to handle piece movement after clicking on target move
    // Done by: Aaron Lim , Teh Yu Qian,  Lee Hong Yi
    public void handleMove(int currentRow, int currentCol, int chosedRow, int chosedCol)
    {
        // Get the piece
        ChessPiece piece = chessboard.getPiece(currentRow, currentCol);

        gameStatus.checkGameOver(chosedRow, chosedCol, chessboard);     // Check if piece eaten (if any) is Sau, and if yes set gameOver to true
        pieceMovement.movePiece(currentRow, currentCol, chosedRow, chosedCol);  // Move the piece (change board array position)
        turnManager.nextTurn(chessboard);   // Ready for next turn (turn board array upside down)
        pieceMovement.checkTransform(chessboard, turnManager, chosedRow, chosedCol);    // Check if Ram transform movement
        chessboard.changeTorXor(turnManager.getOneCompleteTurn(), turnManager.getTurnCount());  // Check if its turn to change Tor Xor

        // Check if Sau is eaten and game over if true, then show win panel
        if (gameStatus.isGameOver() == true)
        {
            gameStatus.setGameOver(false);  //reset for next game
            winPanelController = new WinPanelController(chessboard, turnManager, gameStatus, pieceMovement, frame, frame.getWinPanel());
            frame.showCard(frame.getFrame(), "Win Game");
        }

        // If not gameOver
        updateTurnView();   // controller handle turn view in game panel
        gamePanel.resetHighlight();     // clear all highlights moves to white in board view
        gamePanel.flipBoard(chessboard.getBoard());     // flip the board view (button positions) 180 degrees 
        gamePanel.renderBoard(chessboard.getBoard());   // Update board view
        setButtonListener();    // reset actionlistener for all buttons
        repeatClick = true;     
    }
}

// Class to handle controller for pause panel's view and game models(logic)
// Done by: Lee Hong Yi
class PausePanelController
{
    private ChessBoard chessboard;
    private TurnManager turnManager;
    private GameStatus gameStatus;
    private PieceMovement pieceMovement;
    private LoadSaveGame loadSaveGame;

    private PausePanelView pausePanel;
    private GameFrameView frame;

    // Constructor to initialise and add actionlistener to buttons
    public PausePanelController(ChessBoard chessboard, TurnManager turnManager, GameStatus gameStatus, PieceMovement pieceMovement, GameFrameView frame, PausePanelView pausePanel)
    {
        this.chessboard = chessboard;
        this.turnManager = turnManager;
        this.gameStatus = gameStatus;
        this.pieceMovement = pieceMovement;

        this.pausePanel = pausePanel;
        this.frame = frame;

        // Observer DP: Add Actionlistener to buttons to handle their action when clicked
        loadSaveGame = new LoadSaveGame(chessboard, turnManager, gameStatus);

        pausePanel.getResumeButton().addActionListener(e -> frame.showCard(frame.getFrame(), "Start Game"));    //Show game panel again
        pausePanel.getSaveButton().addActionListener(e -> pausePanel.getSaveDialog().setVisible(true));     // Show confirm dialog for save game
        pausePanel.getExitButton().addActionListener(e -> backToHome());    // back to home

        pausePanel.getConfirmSaveButton().addActionListener(e -> confirmSave());    // button in dialog to confirm save
        pausePanel.getCancelSaveButton().addActionListener(e -> pausePanel.getSaveDialog().setVisible(false));  // cancel save
    }

    // Method to save game into text file
    // Done by: Lee Hong Yi
    public void confirmSave()
    {
        // loadSaveGame's method to handle save game
        loadSaveGame.saveGame(chessboard, turnManager, gameStatus);

        // Go back to main menu page after finishing save
        // Reset board
        chessboard.initialiseBoard();
        turnManager.setTurnCount(1);
        turnManager.setOneCompleteTurn(0);
        // New gamepanelcontroller to initialise game panel
        GamePanelController gamePanelController = new GamePanelController(chessboard, turnManager, gameStatus, pieceMovement, frame, frame.getGamePanel());
        frame.showCard(frame.getFrame(), "Main Menu");    // Go back to main menu page
        pausePanel.getSaveDialog().setVisible(false);   // Hide dialog window
    }

    // Method to cancel game, reset game, and return to main menu page
    public void backToHome()
    {
        // Confirm cancel game and back to main menu page
        int result = JOptionPane.showConfirmDialog(
                pausePanel,
                "Are you sure to cancel game and go back to Main Menu?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        // Handle user's response
        if (result == JOptionPane.YES_OPTION) 
        {
            // Reset board
            chessboard.initialiseBoard();
            turnManager.setTurnCount(1);
            turnManager.setOneCompleteTurn(0);
            // New gamepanelcontroller to initialise game panel
            GamePanelController gamePanelController = new GamePanelController(chessboard, turnManager, gameStatus, pieceMovement, frame, frame.getGamePanel());
            frame.showCard(frame.getFrame(), "Main Menu");    // Go back to main menu page
        } 
    }
}

// Class to handle controller for win panel's view and game models(logic)
// Done by: Aaron Lim
class WinPanelController
{
    private ChessBoard chessboard;
    private TurnManager turnManager;
    private GameStatus gameStatus;
    private PieceMovement pieceMovement;

    private WinPanelView winPanel;
    private GameFrameView frame;

    // Constructor to initialise and add actionlistener
    public WinPanelController(ChessBoard chessboard, TurnManager turnManager, GameStatus gameStatus, PieceMovement pieceMovement, GameFrameView frame, WinPanelView winPanel)
    {
        this.chessboard = chessboard;
        this.turnManager = turnManager;
        this.gameStatus = gameStatus;
        this.pieceMovement = pieceMovement;

        this.frame = frame;
        this.winPanel = winPanel;

        // Observer DP: Add Actionlistener to handle buttons' clicks
        winPanel.getNewGameButton().addActionListener(e -> newGame());  // reset whole game 
        winPanel.getBackToHomeButton().addActionListener(e -> backToHome());    // back to main menu page
        winPanel.getExitGameButton().addActionListener(e -> System.exit(0));    // exit game

        whoWin();   // Check who wins and set colour text every time panel shows
    }

    // Method to check which colour wins, then set the text
    public void whoWin()
    {
        String winColour = "";
        // Get the turn's cokour when the move ends the game 
        if (frame.getGamePanel().getColourLabel().getText().equals("Blue's"))
        {
            winColour = "Blue";
            winPanel.getWinColourLabel().setForeground(Color.BLUE); // set foreground colour to respective colour
        }
        else
        {
            winColour = "Red";
            winPanel.getWinColourLabel().setForeground(Color.RED);
        }
        winPanel.getWinColourLabel().setText(winColour);
        
        frame.getFrame().revalidate();
        frame.getFrame().repaint();
    }

    // Method to reset whole game and start a new one
    // Done by: Aaron Lim
    public void newGame()
    {
        chessboard.initialiseBoard(); // Initialise board array and pieces
        turnManager.setTurnCount(1);    // Reset turn to 0 for new game
        turnManager.setOneCompleteTurn(0);

        // Create new gamepanelcontroller to update game panel view based on model(logic)
        GamePanelController gamePanelController = new GamePanelController(chessboard, turnManager, gameStatus, pieceMovement, frame, frame.getGamePanel());
        frame.showCard(frame.getFrame(), "Start Game");     // Show game panel
    }

    // Method to reset game and go back tomain menu page
    public void backToHome()
    {
        chessboard.initialiseBoard();   // Reset board status
        // Reset game panel
        GamePanelController gamePanelController = new GamePanelController(chessboard, turnManager, gameStatus, pieceMovement, frame, frame.getGamePanel());
        frame.showCard(frame.getFrame(), "Main Menu");  //  go back to main menu page
    }
}

// Main Application Entry Point
// Done by: Teh Yu Qian
public class KwazamChess
{
    public static void main(String[] args)
    {
        // Instantiate MVC components

        // Models (Game Logic)
        ChessBoard chessBoard = new ChessBoard();
        TurnManager turnManager = new TurnManager();
        GameStatus gameStatus = new GameStatus();
        PieceMovement pieceMovement = new PieceMovement(chessBoard);

        // Main View
        GameFrameView frame = new GameFrameView();

        // Main controller
        new GameFrameController(chessBoard, turnManager, gameStatus, pieceMovement, frame);
    }
}