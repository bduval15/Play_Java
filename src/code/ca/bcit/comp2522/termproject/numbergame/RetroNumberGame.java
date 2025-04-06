package ca.bcit.comp2522.termproject.numbergame;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code RetroNumberGame} class implements a retro-styled number placement game.
 * <p>
 * This class extends {@code AbstractSynthwaveUI} to inherit common UI styling and behavior and
 * implements the {@code GameLogic} interface to provide the core game functionality. The game
 * features a board represented as a 4x5 grid of buttons, where each button corresponds to a placement
 * position for numbers. Players are tasked with placing randomly generated numbers in a valid order –
 * ensuring that numbers to the left are less than or equal to the number being placed and numbers to the
 * right are greater than or equal to it.
 * </p>
 * <p>
 * Key responsibilities of this class include:
 * <ul>
 *   <li>
 *     Building and configuring the user interface via the {@link #buildUI()} method, which sets up the game board,
 *     informational labels, and control buttons.
 *   </li>
 *   <li>
 *     Managing game state transitions through methods like {@link #startNewGame()},
 *     {@link #placeNumber(int)}, {@link #checkIfGameOver()}, and {@link #checkIfWin()}.
 *   </li>
 *   <li>
 *     Updating score statistics using {@link #updateScoreOnGameEnd(boolean)} and providing a detailed score summary via
 *     {@link #getScoreMessage()}.
 *   </li>
 *   <li>
 *     Handling user input for number placement and game control via event handlers associated with the grid buttons
 *     and control buttons.
 *   </li>
 *   <li>
 *     Displaying game outcomes through dialogs (using {@link #showGameOverDialog(String)}) and updating UI components
 *     (using methods such as {@link #updateInfoLabel(String)} and {@link #updateScoreDisplay(String)}).
 * </ul>
 * </p>
 * <p>
 * The class maintains internal state including the array of generated numbers, the current index in the sequence,
 * and overall game statistics such as the number of games played, wins, losses, and total placements. It also
 * provides utility methods for resetting the game board, enabling/disabling game controls, and computing the score.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class RetroNumberGame
             extends AbstractSynthwaveUI
             implements Gameplay
{

    private static final int noNumPlacement     = -1;
    private static final int GRID_ROWS          = 4;
    private static final int GRID_COLS          = 5;
    private static final int TOTAL_NUMBERS      = 20;
    private static final int LOWER_BOUND_RAND   = 1;
    private static final int UPPER_BOUND_RAND   = 1000;
    private static final double DEFAULT_AVG     = 0.0;

    private static final int boardHGap          = 5;
    private static final int boardVGap          = 5;
    private static final int bracketBtnWidth    = 80;
    private static final int bracketBtnHeight   = 40;
    private static final int bottomBoxGap       = 20;

    private static final String synthButtonCss              = "synth-button";
    private static final String NUM_PLACEHOLDER             = "[]";
    private static final String FINAL_SCORE_TITLE           = "Final Score";
    private static final String FINAL_SCORE_STATUS_HEADER   = "Final Score Status";
    private static final String TRY_AGAIN_TEXT              = "Try Again";
    private static final String QUIT_TEXT                   = "Quit";

    private static int currentIndex                 = 0;
    private static int successfulPlacementsThisGame = 0;
    private static int totalGames                   = 0;
    private static int totalWins                    = 0;
    private static int totalLosses                  = 0;
    private static int totalPlacementsAcrossGames   = 0;

    private final int[]     numbers                  = new int[TOTAL_NUMBERS];
    private final Button[]  gridButtons              = new Button[GRID_ROWS * GRID_COLS];

    private Label      infoLabel;
    private BorderPane root;

    /*
     * Populates the numbers array with a set of unique random numbers.
     *
     * <p>This method creates a list of numbers from 1 to 1000, shuffles the list to randomize the order,
     * and then selects the first TOTAL_NUMBERS values from the shuffled list.
     * This approach ensures that all the numbers in the numbers array are unique.
     */
    private void generateAllNumbers()
    {
        List<Integer>   numberList;
        final int       startingNum;
        final int       endNum;

        numberList      = new ArrayList<>();
        startingNum     = LOWER_BOUND_RAND;
        endNum          = UPPER_BOUND_RAND;

        for (int i = startingNum; i <= endNum; i++)
        {
            numberList.add(i);
        }

        Collections.shuffle(numberList);

        for (int i = 0; i < TOTAL_NUMBERS; i++)
        {
            numbers[i] = numberList.get(i);
        }
    }

    /*
     * Determines if the given number can be legally placed at the specified index on the board.
     * <p>
     * The number is considered legal if:
     * <ul>
     *     <li>All numbers placed in positions prior to index are less than or equal to the number.</li>
     *     <li>All numbers placed in positions after index are greater than or equal to the number.</li>
     * </ul>
     * </p>
     *
     * @param index the board position to check.
     * @param num   the number that is intended to be placed.
     * @return true if the number can be legally placed; false otherwise.
     */
    private boolean canPlaceHere(final int index,
                                 final int num)
    {
        for (int i = 0; i < index; i++)
        {
            final String txt;
            txt = gridButtons[i].getText();

            if (!txt.equals(NUM_PLACEHOLDER ))
            {
                final int placedNum;
                placedNum = Integer.parseInt(txt);

                if (placedNum > num)
                {
                    return false;
                }
            }
        }
        for (int i = index + 1; i < gridButtons.length; i++)
        {
            final String txt;
            txt = gridButtons[i].getText();

            if (!txt.equals(NUM_PLACEHOLDER))
            {
                final int placedNum;
                placedNum = Integer.parseInt(txt);

                if (placedNum < num)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * Handles the end-of-game procedures.
     * <p>
     * This method disables further game interaction, updates the overall score, and displays a game-over
     * dialog informing the user whether they won or lost. The dialog message includes a summary of the score.
     * </p>
     *
     * @param didIWin true if the game was won; false if lost.
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
        }
        else
        {
            final int nextNumber;

            if (currentIndex < TOTAL_NUMBERS)
            {
                nextNumber = numbers[currentIndex];
            }
            else
            {
                nextNumber = noNumPlacement;
            }

            final String detail;

            if (nextNumber == noNumPlacement)
            {
                detail = "No more numbers left.";
            }
            else
            {
                detail = "Impossible to place the next number: " + nextNumber + ".";
            }

            msg = "Game Over!\n" + detail + "\n" + getScoreMessage();
            updateInfoLabel("Game Over! " + detail);
        }
        showGameOverDialog(msg);
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
        final double noGamesPlayed;
        noGamesPlayed = DEFAULT_AVG;

        if (totalGames == noGamesPlayed)
        {
            avg = DEFAULT_AVG;
        }
        else
        {
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
        boardGrid.setHgap(boardHGap);
        boardGrid.setVgap(boardVGap);
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.getStyleClass().add("board-grid");

        for (int i = 0; i < gridButtons.length; i++)
        {
            final Button bracketBtn;
            bracketBtn = new Button(NUM_PLACEHOLDER);
            bracketBtn.setPrefSize(bracketBtnWidth, bracketBtnHeight);
            bracketBtn.getStyleClass().add(synthButtonCss);

            final int index = i;
            bracketBtn.setOnAction(e -> placeNumber(index));

            gridButtons[i] = bracketBtn;
            boardGrid.add(bracketBtn, i % GRID_COLS, i / GRID_COLS);
        }
        root.setCenter(boardGrid);

        tryAgainBtn = new Button(TRY_AGAIN_TEXT);
        tryAgainBtn.getStyleClass().add(synthButtonCss);
        tryAgainBtn.setOnAction(e -> startNewGame());

        quitBtn = new Button(QUIT_TEXT);
        quitBtn.getStyleClass().add(synthButtonCss);
        quitBtn.setOnAction(e -> {
            final Alert alert;
            alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(FINAL_SCORE_TITLE );
            alert.setHeaderText(FINAL_SCORE_STATUS_HEADER);
            alert.setContentText(getScoreMessage());
            alert.showAndWait();
            ((javafx.stage.Stage) quitBtn.getScene().getWindow()).close();
        });

        bottomBox = new HBox(bottomBoxGap, tryAgainBtn, quitBtn);
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
        currentIndex                 = 0;
        successfulPlacementsThisGame = 0;
        generateAllNumbers();
        updateInfoLabel("Game started! Place the number: " + numbers[currentIndex]);
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
        return noNumPlacement;
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
     */
    @Override
    public void placeNumber(final int index)
    {
        final String text;
        text = gridButtons[index].getText();

        if (!text.equals(NUM_PLACEHOLDER))
        {
            return;
        }

        final int numberToPlace;
        numberToPlace = generateNextNumber();

        if (numberToPlace == noNumPlacement)
        {
            return;
        }

        if (!canPlaceHere(index, numberToPlace))
        {
            gameOver(false);
            return;
        }

        gridButtons[index].setText(String.valueOf(numberToPlace));
        successfulPlacementsThisGame++;
        currentIndex++;

        if (checkIfWin())
        {
            gameOver(true);
        }
        else if (checkIfGameOver())
        {
            gameOver(false);
        }
        else
        {
            updateInfoLabel("Next Number: " + numbers[currentIndex]);
        }
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

        final int nextNumber;
        nextNumber = numbers[currentIndex];

        for (int i = 0; i < gridButtons.length; i++)
        {
            if (gridButtons[i].getText().equals(NUM_PLACEHOLDER))
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
        }
        else
        {
            totalLosses++;
        }
        totalPlacementsAcrossGames += successfulPlacementsThisGame;
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
        for (final Button btn : gridButtons)
        {
            btn.setText(NUM_PLACEHOLDER);
            btn.setDisable(false);
        }
    }

    /**
     * Disables all interactive game controls.
     */
    @Override
    protected void disableGameControls()
    {
        for (final Button btn : gridButtons)
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
        for (final Button btn : gridButtons)
        {
            btn.setDisable(false);
        }
    }

    /**
     * Displays a game-over dialog with the specified title and message.
     *
     * @param message the message to display.
     */
    @Override
    protected void showGameOverDialog(final String message)
    {
        final Alert alert;
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Score Status");
        alert.setContentText(message);

        final ButtonType tryAgainBtn;
        final ButtonType quitBtn;

        tryAgainBtn = new ButtonType(TRY_AGAIN_TEXT);
        quitBtn = new ButtonType(QUIT_TEXT);

        alert.getButtonTypes().setAll(tryAgainBtn, quitBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == tryAgainBtn)
            {
                startNewGame();
            }
            else if (response == quitBtn)
            {
                final Alert finalScoreAlert;
                finalScoreAlert = new Alert(Alert.AlertType.INFORMATION);
                finalScoreAlert.setTitle(FINAL_SCORE_TITLE );
                finalScoreAlert.setHeaderText(FINAL_SCORE_STATUS_HEADER);
                finalScoreAlert.setContentText(getScoreMessage());
                finalScoreAlert.showAndWait();

                final Stage stage;
                stage = (Stage) getRootPane().getScene().getWindow();

                stage.close();
            }
        });
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
}
