package ca.bcit.comp25222.termproject.NumberGame;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>RetroNumberGame implements the GameLogic interface and provides a retro-styled
 * number game using a grid of buttons. Players must place randomly generated numbers
 * in the correct order to win. This class encapsulates all game logic and UI behavior,
 * and it extends AbstractSynthwaveUI to leverage common UI functionality and styling.</p>
 *
 * <p>The game board is structured as a 4x5 grid (20 positions) where each button
 * represents a placement position. The player is given a series of random numbers and must
 * place them such that the numbers to the left are always less than or equal to the number being
 * placed, and the numbers to the right are greater than or equal to it.</p>
 *
 * @author Braeden
 */
public class RetroNumberGame extends AbstractSynthwaveUI implements GameLogic
{

    private static final int GRID_ROWS = 4;
    private static final int GRID_COLS = 5;
    private static final int TOTAL_NUMBERS = 20;

    private final int[] numbers = new int[TOTAL_NUMBERS];
    private final Button[] gridButtons = new Button[GRID_ROWS * GRID_COLS];

    private Label infoLabel;
    private BorderPane root;

    private int currentIndex = 0;
    private int successfulPlacementsThisGame = 0;
    private static int totalGames = 0;
    private static int totalWins = 0;
    private static int totalLosses = 0;
    private static int totalPlacementsAcrossGames = 0;

    /**
     * Constructs a new RetroNumberGame instance and builds its user interface.
     */
    public RetroNumberGame()
    {
        buildUI();
    }

    /**
     * Returns the root pane of the game UI.
     *
     * @return the BorderPane that serves as the root of the game UI.
     */
    public Pane getRootPane()
    {
        return root;
    }

    /**
     * Builds and configures the user interface for the RetroNumberGame.
     * <p>
     * This method sets up the main UI components including:
     * <ul>
     *     <li>The top information label displayed in an HBox.</li>
     *     <li>A central grid of buttons representing the game board.</li>
     *     <li>Control buttons ("Try Again" and "Quit") placed at the bottom.</li>
     * </ul>
     * Each button on the grid is styled and assigned an event handler to handle number placement.
     * </p>
     */
    @Override
    protected void buildUI()
    {
        root = new BorderPane();
        root.getStyleClass().add("root-pane");

        infoLabel = new Label("Waiting to start...");
        infoLabel.getStyleClass().add("info-label");

        final HBox topBox;
        final GridPane boardGrid;
        final Button tryAgainBtn;
        final Button quitBtn;
        final HBox bottomBox;

        topBox = new HBox(infoLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.getStyleClass().add("top-box");
        root.setTop(topBox);

        boardGrid = new GridPane();
        boardGrid.setHgap(5);
        boardGrid.setVgap(5);
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.getStyleClass().add("board-grid");

        for (int i = 0; i < gridButtons.length; i++)
        {
            Button btn = new Button("[]");
            btn.setPrefSize(80, 40);
            btn.getStyleClass().add("synth-button");

            final int index = i;
            btn.setOnAction(e -> placeNumber(index));

            gridButtons[i] = btn;
            boardGrid.add(btn, i % GRID_COLS, i / GRID_COLS);
        }
        root.setCenter(boardGrid);

        tryAgainBtn = new Button("Try Again");
        tryAgainBtn.getStyleClass().add("synth-button");
        tryAgainBtn.setOnAction(e -> startNewGame());

        quitBtn = new Button("Quit");
        quitBtn.getStyleClass().add("synth-button");
        quitBtn.setOnAction(e -> {
            final Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Final Score");
            alert.setHeaderText("Score Status");
            alert.setContentText(getScoreMessage());
            alert.showAndWait();
            ((javafx.stage.Stage) quitBtn.getScene().getWindow()).close();
        });


        bottomBox = new HBox(20, tryAgainBtn, quitBtn);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.getStyleClass().add("bottom-box");
        root.setBottom(bottomBox);
    }

    /**
     * Starts a new game by resetting the game board, initializing game state,
     * generating new numbers, and updating the information label.
     */
    @Override
    public void startNewGame()
    {
        resetGameBoard();
        currentIndex = 0;
        successfulPlacementsThisGame = 0;
        generateAllNumbers();
        updateInfoLabel("Game started! Place the number: " + numbers[currentIndex]);
    }

    /**
     * Populates the {@code numbers} array with a set of unique random numbers.
     *
     * <p>This method creates a list of numbers from 1 to 1000, shuffles the list to randomize the order,
     * and then selects the first {@code TOTAL_NUMBERS} values from the shuffled list.
     * This approach ensures that all the numbers in the {@code numbers} array are unique.
     *
     */
    private void generateAllNumbers() {
        List<Integer> numberList = new ArrayList<>();

        for (int i = 1; i <= 1000; i++)
        {
            numberList.add(i);
        }

        Collections.shuffle(numberList);

        for (int i = 0; i < TOTAL_NUMBERS; i++)
        {
            numbers[i] = numberList.get(i);
        }
    }

    /**
     * Returns the next number to be placed on the board.
     *
     * @return the next number, or -1 if there are no more numbers.
     */
    @Override
    public int generateNextNumber()
    {
        if (currentIndex < TOTAL_NUMBERS)
        {
            return numbers[currentIndex];
        }
        return -1;
    }

    /**
     * Attempts to place the next number onto the game board at the specified index.
     * <p>
     * The method checks whether the chosen board position is empty and whether the number can be legally
     * placed based on the game rules (i.e., maintaining a valid order with already placed numbers). If the move
     * is valid, the button's text is updated, and the game state advances. If the placement results in a win
     * or an unwinnable state, the game is ended.
     * </p>
     *
     * @param index the board position index where the number should be placed.
     * @return {@code true} if the number is successfully placed; {@code false} otherwise.
     */
    @Override
    public boolean placeNumber(final int index)
    {
        final String text;
        text = gridButtons[index].getText();
        if (!text.equals("[]"))
        {
            return false;
        }

        int numberToPlace = generateNextNumber();
        if (numberToPlace == -1)
        {
            return false;
        }

        if (!canPlaceHere(index, numberToPlace))
        {
            gameOver(false);
            return false;
        }

        gridButtons[index].setText(String.valueOf(numberToPlace));
        successfulPlacementsThisGame++;
        currentIndex++;

        if (checkIfWin())
        {
            gameOver(true);
        } else if (checkIfGameOver())
        {
            gameOver(false);
        } else {
            updateInfoLabel("Next Number: " + numbers[currentIndex]);
        }
        return true;
    }

    /*
     * Determines if the given number can be legally placed at the specified index on the board.
     * <p>
     * The number is considered legal if:
     * <ul>
     *     <li>All numbers placed in positions prior to {@code index} are less than or equal to the number.</li>
     *     <li>All numbers placed in positions after {@code index} are greater than or equal to the number.</li>
     * </ul>
     * </p>
     *
     * @param index the board position to check.
     * @param num   the number that is intended to be placed.
     * @return {@code true} if the number can be legally placed; {@code false} otherwise.
     */
    private boolean canPlaceHere(final int index, final int num)
    {
        for (int i = 0; i < index; i++)
        {
            final String txt;
            txt = gridButtons[i].getText();
            if (!txt.equals("[]"))
            {
                int placedNum = Integer.parseInt(txt);
                if (placedNum > num)
                {
                    return false;
                }
            }
        }
        for (int i = index + 1; i < gridButtons.length; i++)
        {
            String txt = gridButtons[i].getText();
            if (!txt.equals("[]"))
            {
                int placedNum = Integer.parseInt(txt);
                if (placedNum < num)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks whether the game is in an unwinnable state due to lack of valid moves.
     * <p>
     * The method iterates over all empty board positions to see if there exists at least one
     * position where the next number can be legally placed.
     * </p>
     *
     * @return {@code true} if no valid moves remain for the next number; {@code false} otherwise.
     */
    @Override
    public boolean checkIfGameOver()
    {
        if (currentIndex >= TOTAL_NUMBERS)
        {
            return false;
        }
        int nextNumber = numbers[currentIndex];
        for (int i = 0; i < gridButtons.length; i++)
        {
            if (gridButtons[i].getText().equals("[]"))
            {
                if (canPlaceHere(i, nextNumber))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if the win condition has been met by verifying that
     * all numbers have been successfully placed.
     *
     * @return {@code true} if all numbers have been placed on the board; {@code false} otherwise.
     */
    @Override
    public boolean checkIfWin()
    {
        final boolean win;
        win = currentIndex >= TOTAL_NUMBERS;
        return win;
    }


    /**
     * Handles the end-of-game procedures.
     * <p>
     * This method disables further game interaction, updates the overall score, and displays a game-over
     * dialog informing the user whether they won or lost. The dialog message includes a summary of the score.
     * </p>
     *
     * @param didIWin {@code true} if the game was won; {@code false} if lost.
     */
    private void gameOver(final boolean didIWin)
    {
        disableGameControls();
        updateScoreOnGameEnd(didIWin);

        final String msg;
        if (didIWin)
        {
            msg = "Congratulations! You placed all 20 numbers!\n" + getScoreMessage();
            updateInfoLabel("You Win! All 20 placed!");
        } else {
            final int nextNumber;
            if (currentIndex < TOTAL_NUMBERS)
            {
                nextNumber = numbers[currentIndex];
            } else {
                nextNumber = -1;
            }

            final String detail;
            if (nextNumber == -1) {
                detail = "No more numbers left.";
            } else {
                detail = "Impossible to place the next number: " + nextNumber + ".";
            }

            msg = "Game Over!\n" + detail + "\n" + getScoreMessage();
            updateInfoLabel("Game Over! " + detail);
        }
        showGameOverDialog("Game Over", msg);
    }

    /**
     * Updates the score statistics when a game ends.
     * <p>
     * This method increments the total game count, records a win or loss based on the game outcome,
     * and accumulates the total number of placements made during the game.
     * </p>
     *
     * @param didIWin {@code true} if the game was won; {@code false} if lost.
     */
    @Override
    public void updateScoreOnGameEnd(final boolean didIWin)
    {
        totalGames++;
        if (didIWin)
        {
            totalWins++;
        } else {
            totalLosses++;
        }
        totalPlacementsAcrossGames += successfulPlacementsThisGame;
    }

    /*
     * Generates a formatted summary of the current score statistics.
     * <p>
     * The summary includes the total number of games played, wins, losses, total placements,
     * and the average number of placements per game.
     * </p>
     *
     * @return a string representing the score summary.
     */
    private String getScoreMessage()
    {
        final double avg;
        if (totalGames == 0)
        {
            avg = 0.0;
        } else {
            avg = (double) totalPlacementsAcrossGames / totalGames;
        }

        final String scoreMessage;
        scoreMessage = String.format(
                "Games played: %d\nWins: %d\nLosses: %d\nTotal placements: %d\nAverage placements per game: %.2f",
                totalGames, totalWins, totalLosses, totalPlacementsAcrossGames, avg
        );
        return scoreMessage;
    }

    /**
     * Updates the information label with the given message.
     *
     * @param message the message to be displayed.
     */
    @Override
    protected void updateInfoLabel(final String message)
    {
        infoLabel.setText(message);
    }

    /**
     * Resets the game board by clearing all number placements and enabling every button.
     * <p>
     * This method sets each button's text back to the default placeholder ("[]")
     * and reactivates them for a new game.
     * </p>
     */
    @Override
    protected void resetGameBoard()
    {
        for (Button btn : gridButtons)
        {
            btn.setText("[]");
            btn.setDisable(false);
        }
    }

    /**
     * Disables all interactive game controls.
     */
    @Override
    protected void disableGameControls()
    {
        for (Button btn : gridButtons)
        {
            btn.setDisable(true);
        }
    }

    /**
     * Enables all interactive game controls.
     */
    @Override
    protected void enableGameControls()
    {
        for (Button btn : gridButtons)
        {
            btn.setDisable(false);
        }
    }

    /**
     * Displays a game-over dialog with the specified title and message.
     *
     * @param title   the title of the dialog.
     * @param message the message to display.
     */
    @Override
    protected void showGameOverDialog(final String title, final String message)
    {
        final Alert alert;
        alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Updates the score display on the UI.
     * In this implementation, the info label is used to display the score.
     *
     * @param scoreMessage the score message to be displayed.
     */
    @Override
    protected void updateScoreDisplay(final String scoreMessage)
    {
        infoLabel.setText(scoreMessage);
    }

    /**
     * Starts the game by invoking startNewGame.
     */
    @Override
    protected void startGame()
    {
        startNewGame();
    }
}
