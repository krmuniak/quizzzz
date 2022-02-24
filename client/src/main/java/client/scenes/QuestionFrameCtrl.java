package client.scenes;

import client.scenes.frameComponents.EmoteCtrl;
import client.scenes.frameComponents.TimerBarCtrl;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for questionFrame scene
 */
public class QuestionFrameCtrl implements Initializable {

    private final MainCtrl mainCtrl;
    private final TimerBarCtrl timerBarCtrl;
    private final EmoteCtrl emoteCtrl;

    @FXML
    public Rectangle timerBar;
    @FXML
    private VBox sideLeaderboard;
    @FXML
    private Pane centerContent;
    @FXML
    private ImageView trophy;
    @FXML
    private Button emote;
    @FXML
    private VBox reactionContainer;
    @FXML
    private HBox emoticonSelectionField;

    private boolean isMultiplayerGame;

    /**
     * Injects necessary dependencies
     * @param mainCtrl - The main front-end controller
     */
    @Inject
    public QuestionFrameCtrl(MainCtrl mainCtrl, TimerBarCtrl timerBarCtrl, EmoteCtrl emoteCtrl) {
        this.mainCtrl = mainCtrl;
        this.timerBarCtrl = timerBarCtrl;
        this.emoteCtrl = emoteCtrl;
    }

    /**
     * Initializes this controller
     * @param location - Location of this controller
     * @param resources - (Potentially) useful resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setMultiplayerFeatures(true);

        timerBarCtrl.initialize(timerBar);
        emoteCtrl.initialize(reactionContainer);

        setRemainingTime(10);
    }

    /**
     * Changes whether leaderboard
     * @param isMultiplayerGame - Whether this is a multiplayer game
     */
    public void setMultiplayerFeatures(boolean isMultiplayerGame) {
        this.isMultiplayerGame = isMultiplayerGame;

        trophy.setManaged(isMultiplayerGame);
        trophy.setVisible(isMultiplayerGame);
        emote.setVisible(isMultiplayerGame);

        if(!isMultiplayerGame) {
            sideLeaderboard.setVisible(false);
        }
    }

    /**
     * Sets node (ordinarily) containing question screen at the center of the frame
     * @param questionNode - The node to be inserted in the center of the frame
     *
     * This should only be called by MainCtrl!
     */
    public void setCenterContent(Node questionNode) {
        centerContent.getChildren().clear();
        centerContent.getChildren().add(questionNode);
    }

    /**
     * Toggles visibility of the side leaderboard
     */
    @FXML
    private void toggleLeaderboardVisibility() {
        if(isMultiplayerGame) sideLeaderboard.setVisible(!sideLeaderboard.isVisible());
    }

    /**
     * Toggles emoticon field visibility
     */
    @FXML
    private void toggleEmoticonField() {
        setEmoticonField(!emoticonSelectionField.isVisible());
    }

    /**
     * Turns off emoticon field
     */
    @FXML
    private void toggleEmoticonFieldExit() {
        if(emoticonSelectionField.isVisible()) setEmoticonField(false);
    }

    /**
     * Turns emoticon field on or off
     * @param visible - Whether the emoticon field must become visible
     */
    private void setEmoticonField(boolean visible) {
        if(!isMultiplayerGame) return;

        if(!visible) {
            emoticonSelectionField.setVisible(false);
            emote.getStyleClass().remove("jagged");
        }
        else {
            emoticonSelectionField.setVisible(true);
            emote.getStyleClass().add("jagged");
        }
    }

    /**
     * Methods to be run when a user chooses to send an emoticon
     */
    @FXML
    private void addHappyReaction() {
        emoteCtrl.addReaction("Chris", "happy");
    }
    @FXML
    private void addSadReaction() {
        emoteCtrl.addReaction("Per", "sad");
    }
    @FXML
    private void addAngryReaction() {
        emoteCtrl.addReaction("Mirella", "angry");
    }
    @FXML
    private void addSurprisedReaction() {
        emoteCtrl.addReaction("Andrei", "surprised");
    }

    /**
     * Halves the time remaining as shown by the timer bar
     */
    @FXML
    void halveTime() {
        timerBarCtrl.halveRemainingTime();
    }

    /**
     * Makes timerBar slide from full to empty in a provided number of seconds
     * @param seconds - How long the bar should slide
     */
    void setRemainingTime(double seconds) {
        timerBarCtrl.setRemainingTime(seconds);
    }

    /**
     * Provides functionality for keybindings to accelerate certain actions
     * @param e - Information about a keypress performed by the user
     */
    public void keyPressed(KeyEvent e) {
        switch(e.getCode()) {
            case H:
                addHappyReaction();
                break;
            case A:
                addAngryReaction();
                break;
            case S:
                addSadReaction();
                break;
            case F: // F stands for flustered, obviously
                addSurprisedReaction();
                break;
            case L:
                toggleLeaderboardVisibility();
                break;
            default:
                break;
        }
    }
}
