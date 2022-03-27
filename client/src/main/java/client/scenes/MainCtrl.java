/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package client.scenes;

import client.scenes.controllerrequirements.MainCtrlRequirements;
import client.scenes.controllerrequirements.QuestionRequirements;
import client.scenes.questioncontrollers.InsteadOfQuestionCtrl;
import client.scenes.questioncontrollers.OpenQuestionCtrl;
import client.scenes.questioncontrollers.QuestionOneImageCtrl;
import client.scenes.questioncontrollers.QuestionThreePicturesCtrl;
import client.scenes.questioncontrollers.QuestionTrueFalseCtrl;
import client.utils.GameUtils;
import client.utils.LobbyUtils;
import client.utils.ServerUtils;
import client.utils.TimeUtils;
import commons.Game;
import commons.LeaderboardEntry;
import commons.Lobby;
import commons.Question;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * Coordinates actions between different screens
 */
public class MainCtrl implements MainCtrlRequirements, WindowListener {

    public static final int ROUND_TIME = 10;
    public static final int OVERVIEW_TIME = 5;
    public static final int LEADERBOARD_TIME = 10;
    public static final int TOTAL_ROUNDS = 20;

    private LeaderboardEntry player;
    private Game game;
    private boolean isMultiplayerGame;
    private boolean intermediateLeaderboardShown;
    private int pointsGained;
    long questionStartTime;
    private boolean doublePoints;
    double questionEndTime;
    private int timeoutRoundCheck;
    private String currentQuestionType;
    boolean gameOngoing = false;

    private ServerUtils serverUtils;
    private GameUtils gameUtils;
    private LobbyUtils lobbyUtils;
    private TimeUtils timeUtils;

    private Stage primaryStage;

    private MainFrameCtrl mainFrameCtrl;
    private Scene mainFrame;

    private LobbyCtrl lobbyCtrl;
    private Scene lobbyFrame;

    private LeaderboardCtrl leaderboardCtrl;
    private Scene leaderboard;

    private AdminInterfaceCtrl adminInterfaceCtrl;
    private Scene adminInterfaceFrame;

    private QuestionFrameCtrl questionFrameCtrl;
    private Scene questionFrame;

    private QuestionTrueFalseCtrl questionTrueFalseCtrl;
    private Node questionTrueFalse;

    private OpenQuestionCtrl openQuestionCtrl;
    private Node openQuestion;

    private QuestionThreePicturesCtrl questionThreePicturesCtrl;
    private Node questionThreePictures;

    private QuestionOneImageCtrl questionOneImageCtrl;
    private Node questionOneImage;

    private InsteadOfQuestionCtrl insteadOfQuestionCtrl;
    private Node insteadOfQuestion;

    private FinalScreenCtrl finalScreenCtrl;
    private Node finalScreen;

    private ExitPopUpCtrl exitPopUpCtrl;
    private Scene exitPopUp;
    private Stage exitCheck;

    QuestionRequirements currentQuestionCtrl = null;

    /**
     * Initializes this class
     *
     * @param timeUtils        Only instance of TimeUtils class
     * @param serverUtils      Only instance of ServerUtils class
     * @param gameUtils        Only instance of GameUtils class
     * @param lobbyUtils       Only instance of LobbyUtils class
     * @param primaryStage     Only stage
     * @param mainFrame        Welcome screen FXML and controller
     * @param questionFrame    Question Frame screen FXML and controller
     * @param leaderboard      Leaderboard screen FXML and controller
     * @param adminInterface   Admin Interface screen FXML and controller
     * @param openQuestion     Open question node FXML and controller
     * @param questionOneImage Question with one image FXML and controller
     * @param finalScreen      Final screen FXML and controller
     */
    public void initialize(ServerUtils serverUtils, GameUtils gameUtils,
                           LobbyUtils lobbyUtils, TimeUtils timeUtils,
                           Stage primaryStage,
                           Pair<MainFrameCtrl, Parent> mainFrame,
                           Pair<LobbyCtrl, Parent> lobbyFrame,
                           Pair<LeaderboardCtrl, Parent> leaderboard,
                           Pair<AdminInterfaceCtrl, Parent> adminInterface,
                           Pair<QuestionFrameCtrl, Parent> questionFrame,
                           Pair<QuestionTrueFalseCtrl, Parent> questionTrueFalse,
                           Pair<OpenQuestionCtrl, Parent> openQuestion,
                           Pair<QuestionThreePicturesCtrl, Parent> questionThreePictures,
                           Pair<QuestionOneImageCtrl, Parent> questionOneImage,
                           Pair<InsteadOfQuestionCtrl, Parent> insteadOfQuestion,
                           Pair<FinalScreenCtrl, Parent> finalScreen,
                           Pair<ExitPopUpCtrl, Parent> exitPopUp) {

        this.serverUtils = serverUtils;
        this.gameUtils = gameUtils;
        this.lobbyUtils = lobbyUtils;
        this.timeUtils = timeUtils;

        this.primaryStage = primaryStage;

        primaryStage.widthProperty().addListener(
            (obs, oldVal, newVal) -> questionFrameCtrl.resizeTimerBar(newVal.intValue(),
                oldVal.intValue() - newVal.intValue()));
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            exitGameChecker(1);
        });

        this.mainFrameCtrl = mainFrame.getKey();
        this.mainFrame = new Scene(mainFrame.getValue());
        this.mainFrame.setOnKeyPressed(e -> mainFrameCtrl.keyPressed(e.getCode()));

        this.lobbyCtrl = lobbyFrame.getKey();
        this.lobbyFrame = new Scene(lobbyFrame.getValue());
        this.lobbyFrame.setOnKeyPressed(e -> lobbyCtrl.keyPressed(e.getCode()));

        this.leaderboardCtrl = leaderboard.getKey();
        this.leaderboard = new Scene(leaderboard.getValue());
        this.leaderboard.setOnKeyPressed(e -> leaderboardCtrl.keyPressed(e.getCode()));

        this.adminInterfaceCtrl = adminInterface.getKey();
        this.adminInterfaceFrame = new Scene(adminInterface.getValue());
        this.adminInterfaceFrame.setOnKeyPressed(e -> adminInterfaceCtrl.keyPressed(e.getCode()));

        this.questionFrameCtrl = questionFrame.getKey();
        this.questionFrame = new Scene(questionFrame.getValue());
        this.questionFrame.setOnKeyPressed(e -> questionFrameCtrl.keyPressed(e.getCode()));

        this.questionTrueFalseCtrl = questionTrueFalse.getKey();
        this.questionTrueFalse = questionTrueFalse.getValue();

        this.openQuestionCtrl = openQuestion.getKey();
        this.openQuestion = openQuestion.getValue();

        this.questionThreePicturesCtrl = questionThreePictures.getKey();
        this.questionThreePictures = questionThreePictures.getValue();

        this.questionOneImageCtrl = questionOneImage.getKey();
        this.questionOneImage = questionOneImage.getValue();

        this.insteadOfQuestionCtrl = insteadOfQuestion.getKey();
        this.insteadOfQuestion = insteadOfQuestion.getValue();

        this.exitPopUpCtrl = exitPopUp.getKey();
        this.exitPopUp = new Scene(exitPopUp.getValue());

        exitCheck = new Stage();
        exitCheck.setScene(exitPopUp.getValue().getScene());
        exitCheck.initOwner(primaryStage);
        exitCheck.initModality(Modality.WINDOW_MODAL);

        primaryStage.setTitle("Quizzzzz!");
        showMainFrame();

        primaryStage.show();
    }

    public ServerUtils getServerUtils() {
        return serverUtils;
    }

    public void showAdminInterface() {
        adminInterfaceCtrl.initialize(serverUtils.fetchActivities());
        primaryStage.setScene(adminInterfaceFrame);
    }

    public LeaderboardEntry getPlayer() {
        return this.player;
    }

    /**
     * Set player to new Player Object with given attributes
     *
     * @param username The name of the player
     * @param points   The number of points the player has
     */
    public void setPlayer(String username, int points) {
        this.player = new LeaderboardEntry(username, points);
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Lobby getLobby() {
        return lobbyCtrl.getLobby();
    }

    public void setLobby(Lobby lobby) {
        lobbyCtrl.setLobby(lobby);
    }

    /**
     * Starts a singleplayer game
     * <p>
     * To be run when a person chooses solo play in the welcome screen (main frame)
     */
    @Override
    public void startSingleplayerGame() {
        intermediateLeaderboardShown = false;
        isMultiplayerGame = false;
        timeoutRoundCheck = 1;
        gameOngoing = true;
        this.game = gameUtils.startSingleplayer();
        questionFrameCtrl.initializeSingleplayerGame();
        showQuestionFrame();
        nextEvent();
    }

    /**
     * Starts multiplayer game for all players in the lobby, switch to question frame
     */
    @Override
    public void startMultiplayerGame() {
        intermediateLeaderboardShown = true;
        isMultiplayerGame = true;
        timeoutRoundCheck = 1;
        gameOngoing = true;
        questionFrameCtrl.initializeMultiplayerGame(this.game.getPlayers());
        lobbyUtils.setActive(false);
        gameUtils.setActive("game", false);
        gameUtils.setActive("features", true);

        showQuestionFrame();
        nextEvent();
    }

    /**
     * Add player to the lobby, display it and start long polling for the lobby
     */
    public void joinLobby() {
        setLobby(lobbyUtils.joinLobby(this.player));
        lobbyUtils.setActive(true);
        gameUtils.setActive("game", true);
        showLobbyFrame();
    }

    /**
     * Remove player from the lobby and switch to main frame
     */
    public void playerLeavesLobby() {
        lobbyUtils.leaveLobby(player);
        showMainFrame();
    }

    /**
     * Executes the next event (question, leaderboard, game over)
     */
    private void nextEvent() {
        game.setPlayers(serverUtils.getUpdatedScores(game.getId()));

        if (isMultiplayerGame) {
            // The current event is the intermediate leaderboard
            if (game.getRound() == TOTAL_ROUNDS / 2 && !intermediateLeaderboardShown) {
                intermediateLeaderboardShown = true;
                showLeaderboard(game.getPlayers(), 10, "intermediate");

                timeUtils.runAfterDelay(() -> {
                    showQuestionFrame();
                    nextEvent();
                }, LEADERBOARD_TIME);
                return;
            }

            // The current event is the final leaderboard; the game is over
            if (game.getRound() == TOTAL_ROUNDS) {
                gameOngoing = false;
                showLeaderboard(game.getPlayers(), 10, "final");
                return;
            }
            questionFrameCtrl.setLeaderboardContents(game.getPlayers());
        } else if (game.getRound() == TOTAL_ROUNDS) {
            gameOngoing = false;
            showFinalScreen();
            return;
        }

        // The current event is a question
        game.incrementRound();
        Platform.runLater(() -> questionFrameCtrl.incrementQuestionNumber());
        questionFrameCtrl.setRemainingTime(ROUND_TIME);
        pointsGained = 0;
        doublePoints = false;
        Question currentQuestion = game.nextQuestion();
        currentQuestionType = currentQuestion.getQuestionType();

        Node questionNode = null;
        switch (currentQuestionType) {
            case "trueFalseQuestion":
                currentQuestionCtrl = questionTrueFalseCtrl;
                questionNode = questionTrueFalse;
                this.questionTrueFalse.setOnKeyPressed(e -> questionTrueFalseCtrl.keyPressed(e.getCode()));
                questionFrameCtrl.setWrongAnswerJoker(false);
                break;
            case "openQuestion":
                currentQuestionCtrl = openQuestionCtrl;
                questionNode = openQuestion;
                questionFrameCtrl.setWrongAnswerJoker(false);
                break;
            case "threePicturesQuestion":
                currentQuestionCtrl = questionThreePicturesCtrl;
                questionNode = questionThreePictures;
                break;
            case "oneImageQuestion":
                currentQuestionCtrl = questionOneImageCtrl;
                questionNode = questionOneImage;
                break;
            case "insteadOfQuestion":
                currentQuestionCtrl = insteadOfQuestionCtrl;
                questionNode = insteadOfQuestion;
                break;
            default:
                System.err.println("Unrecognized question type in MainCtrl");
                break;
        }
        currentQuestionCtrl.initialize(currentQuestion);
        questionFrameCtrl.setCenterContent(questionNode, true);
    }

    /**
     * Initializes timeouts until events that will happen after question and overview
     *
     * @param delay The time until the end of the question
     */
    void setQuestionTimeouts(double delay) {
        int expectedRound = game.getRound();
        UUID expectedGame = game.getId();
        timeUtils.runAfterDelay(() -> {
            if (expectedRound != timeoutRoundCheck || !expectedGame.equals(game.getId())) {
                return;
            }

            timeoutRoundCheck++;
            currentQuestionCtrl.revealCorrectAnswer();
            Platform.runLater(() -> questionFrameCtrl.addPoints(pointsGained));
            player.setScore(player.getScore() + pointsGained);
            questionFrameCtrl.tempDisableJokers(OVERVIEW_TIME);
            serverUtils.sendPointsGained(game.getId(), player, pointsGained);
            if (currentQuestionType.equals("trueFalseQuestion") || currentQuestionType.equals("openQuestion")) {
                questionFrameCtrl.setWrongAnswerJoker(true);
            }

            Platform.runLater(() -> {
                questionFrameCtrl.setRemainingTime(OVERVIEW_TIME);
                timeUtils.runAfterDelay(this::nextEvent, OVERVIEW_TIME);
            });
        }, delay);
    }

    /**
     * To be run when points are gained by the player
     *
     * @param baseScore the score in range [0; 100]
     */
    @Override
    public void addPoints(long baseScore) {
        if (baseScore != 0) {
            double progress = ((double) (timeUtils.now() - questionStartTime)) / (questionEndTime - questionStartTime);
            pointsGained = (int) (50.0 + 0.5 * (1.0 - progress) * (double) baseScore);
            if (doublePoints) {
                pointsGained *= 2;
            }
        }
        if (!isMultiplayerGame) {
            setQuestionTimeouts(0.0);
        }
    }

    /**
     * Doubles points gained on this question
     */
    @Override
    public void doublePoints() {
        pointsGained *= 2;
        doublePoints = true;
    }

    /**
     * Halves remaining time
     */
    @Override
    public void halveTime() {
        double timeUntilRoundEnd = (questionEndTime - timeUtils.now()) / 2.0;
        questionEndTime -= timeUntilRoundEnd;
        questionFrameCtrl.halveRemainingTime();
        setQuestionTimeouts(timeUntilRoundEnd / 1000.0);
    }

    public void displayNewEmoji(String name, String reaction) {
        questionFrameCtrl.displayNewEmoji(name, reaction);
    }

    /**
     * Eliminates incorrect answer
     */
    @Override
    public void eliminateWrongAnswer() {
        currentQuestionCtrl.removeIncorrectAnswer();
    }


    /**
     * Shows leaderboard
     *
     * @param players The players in the leaderboard
     * @param maxSize The maximum number of players to be displayed
     * @param type    The type of the leaderboard
     */
    private void showLeaderboard(List<LeaderboardEntry> players, int maxSize, String type) {
        leaderboardCtrl.initialize(players, maxSize, type);
        primaryStage.setScene(leaderboard);
    }

    public String getUsername() {
        return player.getName();
    }

    public void halveRemainingTime() {
        questionFrameCtrl.halveRemainingTime();
    }

    /**
     * Show global leaderboard frame
     */
    @Override
    public void showGlobalLeaderboardFrame() {
        int maxSize = 10;
        showLeaderboard(serverUtils.getSoloLeaderboard(maxSize), maxSize, "solo");
    }

    /**
     * Shows main frame (welcome/splash screen)
     */
    public void showMainFrame() {
        primaryStage.setScene(mainFrame);
    }

    /**
     * Shows lobby frame
     */
    public void showLobbyFrame() {
        primaryStage.setScene(lobbyFrame);
    }

    /**
     * Shows question frame
     */
    public void showQuestionFrame() {
        primaryStage.setScene(questionFrame);
    }

    public void toggleModalVisibility() {
        // TODO toggle visibility of modal ("do you really want to disconnect?")
        // TODO inform player that they are running game for others
    }

    /**
     * Shows final screen frame (after playing the game)
     */
    private void showFinalScreen() {
        finalScreenCtrl.setPoints(player.getScore());
        questionFrameCtrl.setCenterContent(finalScreen, false);
    }

    public void exitGameChecker(int type) {
        exitPopUpCtrl.setType(type);
        exitCheck.showAndWait();
    }

    /**
     * Disconnects the player from a game
     */
    public void disconnect(int type, String buttonID) {
        // TODO stop long polling
        if (type == 0 && buttonID.equals("yesButton")) {
            serverUtils.disconnect(game.getId(), player);
            exitCheck.close();
            showMainFrame();
        }
        if (type == 0 && buttonID.equals("noButton")) {
            exitCheck.close();
            showQuestionFrame();
        }
        if (type == 1 && buttonID.equals("yesButton")) {
            exitCheck.close();
            primaryStage.close();
        }
        if (type == 1 && buttonID.equals("noButton")) {
            exitCheck.close();
            showMainFrame();
        }

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        this.exitGameChecker(1);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}