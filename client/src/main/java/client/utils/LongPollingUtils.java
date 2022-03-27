package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import client.scenes.LobbyCtrl;
import client.scenes.MainCtrl;
import client.scenes.QuestionFrameCtrl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Game;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import javax.inject.Inject;
import org.glassfish.jersey.client.ClientConfig;

/**
 * Controls client-side functionality related to long-polling
 */
public class LongPollingUtils {

    private final ServerUtils serverUtils;
    private final MainCtrl mainCtrl;
    private final LobbyCtrl lobbyCtrl;
    public final QuestionFrameCtrl questionFrameCtrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean active;

    /**
     * Constructor
     *
     * @param serverUtils       ServerUtils object
     * @param questionFrameCtrl QuestionFrameCtrl object
     */
    @Inject
    public LongPollingUtils(ServerUtils serverUtils, MainCtrl mainCtrl, LobbyCtrl lobbyCtrl,
                            QuestionFrameCtrl questionFrameCtrl) {
        this.serverUtils = serverUtils;
        this.mainCtrl = mainCtrl;
        this.lobbyCtrl = lobbyCtrl;
        this.questionFrameCtrl = questionFrameCtrl;

        this.active = false;
    }

    /**
     * Changes whether long polling is active on front-end
     *
     * @param pollingActive Whether polling must be active on front-end
     */
    public void setActive(boolean pollingActive) {
        if (!active && pollingActive) {
            active = true;
            new Thread(() -> {
                while (active) {
                    sendPoll();
                }
            }).start();
        }
        active = pollingActive;
    }

    /**
     * Executes long-polling loop
     */
    private void sendPoll() {
        String json = ClientBuilder.newClient(new ClientConfig())
            .target(serverUtils.getServerIP())
            .path("poll/" + (mainCtrl.getGame() == null ? lobbyCtrl.getLobby().getId() :
                mainCtrl.getGame().getId()))
            .request(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .get(new GenericType<>() {
            });

        try {
            if (active) {
                performAction(mapper.readTree(json));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.err.println("Error while parsing JSON from long polling on client-side");
        }
    }

    /**
     * Calls appropriate method based on received JSON string
     *
     * @param response An object generated from the parsed JSON string that allows for value retrieval
     */
    public void performAction(JsonNode response) {
        switch (response.get("type").asText()) {
            case "START_MP_GAME":
                try {
                    Game game = new ObjectMapper().readValue(response.get("game").asText(), Game.class);
                    mainCtrl.setGame(game);
                    mainCtrl.startMultiplayerGame();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                break;
            case "EMOJI":
                String name = response.get("name").asText();
                String reaction = response.get("reaction").asText();
                questionFrameCtrl.displayNewEmoji(name, reaction);
                break;
            case "HALVE_TIME":
                mainCtrl.halveTime();
                break;
            case "DISCONNECT":
                String nameDisconnect = response.get("name").asText();
                break;
            case "JOIN":
                String nameJoin = response.get("name").asText();
                break;
            default:
                System.err.println("Unknown long polling response type");
                break;
        }
    }
}
