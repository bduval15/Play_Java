package ca.bcit.comp2522.termproject.numbergame;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code NumberGame} class implements a retro-styled number placement game.
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
 *     (using methods such as {@link AbstractSynthwaveUI#updateInfoLabel(String)} and {@link #updateScoreDisplay(String)}).
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

final class NumberGame
             extends AbstractSynthwaveUI
             implements Gameplay
{

    private static final int noNumPlacement     = -1;
    private static final int GRID_ROWS          = 4;
    private static final int GRID_COLS          = 5;
    private static final int TOTAL_NUMBERS      = 20;
    private static final int LOWER_BOUND_RAND   = 1;
    private static final int UPPER_BOUND_RAND   = 1000;
    private static final int boardHGap          = 5;
    private static final int boardVGap          = 5;
    private static final int bracketBtnWidth    = 80;
    private static final int bracketBtnHeight   = 40;
    private static final int bottomBoxGap       = 20;

    private static final double DEFAULT_AVG     = 0.0;

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

    private final int[]     numbers;
    private final Button[]  gridButtons;

    private Label      infoLabel;
    private BorderPane root;

    {
        numbers     = new int[TOTAL_NUMBERS];
        gridButtons = new Button[GRID_ROWS * GRID_COLS];
    }

    /*
     * Populates the numbers array with unique random numbers.
     *
     * This method builds a list of integers starting at LOWER_BOUND_RAND
     * and ending at UPPER_BOUND_RAND (both inclusive). It then randomizes the order
     * of the list using Collections.shuffle(). The first TOTAL_NUMBERS values
     * from the shuffled list are then saved into the numbers array.
     *
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
     * Determines whether the provided number can be legally placed on the board at the specified index.
     *
     * A legal placement is determined by ensuring that:
     *
     *   All numbers in the positions preceding index (if any) are
     *   less than or equal to the provided number.
     *   All numbers in the positions following index (if any) are
     *   greater than or equal to the provided number.
     *
     * The method checks each occupied position on the board using the NUM_PLACEHOLDER
     * constant to decide whether it has a valid number.
     *
     *
     * @param index the board index at which the number is intended to be placed.
     * @param num   the candidate number to place.
     * @return true if the number can be legally placed at the specified index; false otherwise.
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
     * Handles the game-over procedures by disabling game controls,
     * updating scores, and displaying the end-of-game dialog.
     *
     * Depending on the value of the status parameter:
     *
     *   If status is true, a winning message is generated including the final score summary.
     *   If status is false, a losing message is generated that may include a note stating
     *   that the next number (if available) could not be placed.
     *
     * In both cases, the method calls #disableGameControls() and #updateScoreOnGameEnd(boolean)
     * to update the game state accordingly.
     *
     *
     * @param status true if the game was won; false if the game was lost.
     */
    private void gameOver(final boolean status)
    {
        disableGameControls();
        updateScoreOnGameEnd(status);

        final String msg;
        if (status)
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
     * Generates a formatted message summarizing the current game scores.
     *
     * The returned message includes:
     *
     *   The total number of games played.
     *   The number of wins and losses.
     *   The total number of placements across all games.
     *   The average placements per game (calculated as the ratio of total placements to total games),
     *   where the average uses DEFAULT_AVG if no games have been played.
     *
     * @return a formatted String representing the score summary.
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
                totalGames,
                totalWins,
                totalLosses,
                totalPlacementsAcrossGames,
                avg
        );
        return scoreMessage;
    }

    /**
     * Constructs a new {@code NumberGame} instance and initializes the user interface.
     * <p>
     * The constructor invokes {@link #buildUI()} to build the game components and set up the UI.
     * </p>
     */
    NumberGame()
    {
        buildUI();
    }

    /**
     * Returns the root pane of the game's user interface.
     * <p>
     * The root pane is a {@code BorderPane} which serves as the container for all other UI elements
     * in the game.
     * </p>
     *
     * @return the {@code Pane} that represents the root of the game UI.
     */
    Pane getRootPane()
    {
        return root;
    }

    /**
     * Builds and configures the user interface components for the RetroNumberGame.
     * <p>
     * This method carries out the following:
     * <ul>
     *   <li>Initializes the root pane and applies its style.</li>
     *   <li>Creates a top container (an {@code HBox}) that holds an information label.</li>
     *   <li>Constructs a central {@code GridPane} that functions as the game board using
     *       the constants {@code GRID_ROWS} and {@code GRID_COLS} for the layout. Each cell in the grid is
     *       represented by a {@code Button} styled with {@code synthButtonCss} and initially displays
     *       {@code NUM_PLACEHOLDER}.</li>
     *   <li>Adds an event handler to each grid button so that clicking invokes {@link #placeNumber(int)} with
     *       the appropriate index.</li>
     *   <li>Sets up a bottom container (an {@code HBox}) with control buttons to start a new game and to quit.
     *       The "Try Again" button calls {@link #startNewGame()}
     *       while the "Quit" button displays the final score and exits.</li>
     * </ul>
     * </p>
     */
    @Override
    void buildUI()
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
     * Updates the information label in the UI with the provided message.
     * <p>
     * This is used to give the player immediate feedback or instructions,
     * such as prompting the next move or displaying game results.
     * </p>
     *
     * @param message the message to set on the information label.
     */
    @Override
    void updateInfoLabel(final String message)
    {
        infoLabel.setText(message);
    }

    /**
     * Resets the game board to its initial state in preparation for a new game.
     * <p>
     * The method iterates over all buttons in the board and:
     * <ul>
     *   <li>Sets each button's text to {@code NUM_PLACEHOLDER}.</li>
     *   <li>Enables the button to allow user interaction.</li>
     * </ul>
     * </p>
     */
    @Override
    void resetGameBoard()
    {
        for (final Button btn : gridButtons)
        {
            btn.setText(NUM_PLACEHOLDER);
            btn.setDisable(false);
        }
    }

    /**
     * Disables all interactive controls on the game board.
     * <p>
     * This method iterates over each button in the grid and disables it,
     * ensuring the user cannot interact with the board after the game ends.
     * </p>
     */
    @Override
    void disableGameControls()
    {
        for (final Button btn : gridButtons)
        {
            btn.setDisable(true);
        }
    }

    /**
     * Displays a game-over dialog to inform the player of the current game outcome.
     * <p>
     * The dialog presents a message (typically including score details) and offers two options:
     * <ul>
     *   <li>A "Try Again" button which, if selected, restarts the game by calling {@link #startNewGame()}.</li>
     *   <li>A "Quit" button which displays the final score
     *   (using a secondary dialog) and then closes the application window.</li>
     * </ul>
     * </p>
     *
     * @param message the message to be displayed in the game-over dialog.
     */
    @Override
    void showGameOverDialog(final String message)
    {
        final Alert alert;
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Score Status");
        alert.setContentText(message);

        final ButtonType tryAgainBtn;
        final ButtonType quitBtn;

        tryAgainBtn = new ButtonType(TRY_AGAIN_TEXT);
        quitBtn     = new ButtonType(QUIT_TEXT);

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
     * Updates the UI display of the score.
     * <p>
     * In this implementation the same {@code infoLabel} is used to show the score.
     * This method sets its text to the provided {@code scoreMessage}.
     * </p>
     *
     * @param scoreMessage the final score message to be displayed.
     */
    @Override
    void updateScoreDisplay(final String scoreMessage)
    {
        infoLabel.setText(scoreMessage);
    }

    /**
     * Starts a new game by resetting the board, initializing game state variables, generating new random numbers,
     * and updating the user interface to prompt the next move.
     * <p>
     * The method performs the following steps:
     * <ul>
     *   <li>Resets the board by calling {@link #resetGameBoard()}.</li>
     *   <li>Resets the {@code currentIndex} and {@code successfulPlacementsThisGame} variables.</li>
     *   <li>Generates a new set of numbers by invoking {@link #generateAllNumbers()}.</li>
     *   <li>Updates the information label with a prompt to place the next number from the generated array.</li>
     * </ul>
     * </p>
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
     * Returns the next number to be placed on the game board.
     * <p>
     * If the {@code currentIndex} is less than {@code TOTAL_NUMBERS}, the number at the {@code currentIndex}
     * in the {@code numbers} array is returned. Otherwise, the method returns {@code noNumPlacement} to indicate
     * that no further numbers are available.
     * </p>
     *
     * @return the next number to place, or {@code noNumPlacement} if all numbers have been used.
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
     * Attempts to place the next number on the board at the specified index.
     * <p>
     * This method first verifies that the button at the provided index is empty (i.e. its text equals
     * {@code NUM_PLACEHOLDER}). It then retrieves the next number to be placed via {@link #generateNextNumber()}.
     * If a number is available and the move is legal (checked by {@link #canPlaceHere(int, int)}),
     * the number is placed on the button and the game state (e.g. {@code currentIndex} and
     * {@code successfulPlacementsThisGame}) is updated.
     * <br>
     * After placing the number, the game checks:
     * <ul>
     *   <li>If all numbers have been placed successfully by calling {@link #checkIfWin()} –
     *   if so, {@link #gameOver(boolean)}
     *       is called with {@code true}.</li>
     *   <li>If no valid moves remain (checked with {@link #checkIfGameOver()}) –
     *   if so, {@link #gameOver(boolean)} is called
     *       with {@code false}.</li>
     *   <li>If neither condition is met, the information label is updated with the next number prompt.</li>
     * </ul>
     * </p>
     *
     * @param index the board cell index where the next number is intended to be placed.
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
     * Checks whether the game is in a state where no legal moves remain.
     * <p>
     * This method operates by:
     * <ul>
     *   <li>Verifying that the {@code currentIndex} is less than {@code TOTAL_NUMBERS}
     *   (i.e. there is still a number to place).</li>
     *   <li>Iterating over each empty board cell (where the button text is {@code NUM_PLACEHOLDER}).</li>
     *   <li>For each empty cell, checking if the next number (from the {@code numbers}
     *       array at {@code currentIndex})
     *       can be legally placed by calling {@link #canPlaceHere(int, int)}.</li>
     * </ul>
     * If none of the empty cells can accept the number,
     * the method returns {@code true} indicating that the game is unwinnable.
     * </p>
     *
     * @return {@code true} if no valid moves exist for the next number; {@code false} otherwise.
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
     * Checks if the win condition has been met by verifying that all numbers have been successfully placed.
     * <p>
     * The game is won when the {@code currentIndex} is equal to or greater than {@code TOTAL_NUMBERS}.
     * </p>
     *
     * @return {@code true} if the game is won (all numbers placed); {@code false} otherwise.
     */
    @Override
    public boolean checkIfWin()
    {
        final boolean win;
        win = currentIndex >= TOTAL_NUMBERS;

        return win;
    }

    /**
     * Updates the overall score statistics when the game ends.
     * <p>
     * This method performs the following:
     * <ul>
     *   <li>Increments the total number of games played.</li>
     *   <li>Increments either the win count or loss count depending on whether the game was won
     *       (as indicated by the {@code didIWin} parameter).</li>
     *   <li>Adds the number of successful placements made in this game to
     *       the cumulative total placements count.</li>
     * </ul>
     * </p>
     *
     * @param didIWin {@code true} if the game was won; {@code false} if the game was lost.
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
}
