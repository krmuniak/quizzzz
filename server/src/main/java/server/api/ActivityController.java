package server.api;

import commons.Activity;
import commons.Question;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.database.ActivityRepository;

/**
 * This controller creates API endpoints for activities.
 */
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityRepository repo;

    public ActivityController(ActivityRepository repo) {
        this.repo = repo;
    }

    private static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    @GetMapping(path = {"", "/"})
    public List<Activity> getAll() {
        return repo.findAll();
    }

    /**
     * Uses the function defined {@link ActivityRepository} to fetch a number of random entries from the activity table.
     *
     * @param limit the number of entries
     * @return the specified amount of activities, randomly chosen from the DB;
     * returns fewer entries if limit is greater than the number of total activities.
     */
    @GetMapping("random/{limit}")
    public List<Activity> fetchRandom(@PathVariable("limit") int limit) {
        List<Activity> res = repo.fetchRandomActivities(limit);
        return res;
    }

    /**
     * This function will get a json-encoded Activity and will save it in the DB.
     * We can test this endpoint with Postman until we have a working UI.
     *
     * @param activity the activity to be inserted in the DB
     * @return a response containing the data of the inserted entry.
     */
    @PostMapping("add")
    public ResponseEntity<Activity> add(@RequestBody Activity activity) {
        if (nullOrEmpty(activity.source) || nullOrEmpty(activity.title) || nullOrEmpty(activity.id)
            || nullOrEmpty(activity.imagePath)) {
            return ResponseEntity.badRequest().build();
        }

        //return ResponseEntity.ok(activity);
        Activity saved = repo.save(activity);
        return ResponseEntity.ok(saved);
    }

    /**
     * generates 20 questions of random types
     * based on the random number generated between 0 and 4, a different specific method is called for
     * each type of question
     * 0 - true false
     * 1 - open question
     * 2 - three pictures question
     * 3 - one image question
     * 4 - instead of question
     *
     * @return a list of all the questions for one game
     */
    @GetMapping("generate")
    public List<Question> generateQuestions() {
        List<Question> questions = new ArrayList<>();
        Random rand = new Random();
        int upperBound = 5;
        for (int i = 0; i < 20; i++) {
            int typeOfQuestion = rand.nextInt(upperBound);
            switch (typeOfQuestion) {
                case 0:
                    generateTrueFalseQuestion(typeOfQuestion, questions);
                    break;
                case 1:
                    generateOpenQuestion(typeOfQuestion, questions);
                    break;
                case 2:
                    generateThreePicturesQuestion(typeOfQuestion, questions);
                    break;
                case 3:
                    generateOneImageQuestion(typeOfQuestion, questions);
                    break;
                case 4:
                    generateInsteadOfQuestion(typeOfQuestion, questions);
                    break;
                default:
                    break;
            }
        }
        return questions;
    }

    /**
     * used for the questionType/id attribute
     * associates a randomly generated question from the method generateQuestions to a descriptive string
     *
     * @param number - the randomly generated number
     * @return - a string with the question string identifier
     */
    public String associateQuestion(int number) {
        switch (number) {
            case 0:
                return "trueFalseQuestion";
            case 1:
                return "openQuestion";
            case 2:
                return "threePicturesQuestion";
            case 3:
                return "oneImageQuestion";
            case 4:
                return "insteadOfQuestion";
            default:
                break;
        }
        return null;
    }

    /**
     * randomizes the number of activities taken from the database
     * if there are two activities taken, a question with the string "x consumes more than y" is created
     * otherwise a question with the string "X consumes number" is created where the number is
     * "randomized" to be true or false
     * the question is created with all the according parameters and added to the list
     *
     * @param typeOfQuestion - the randomly generated number;
     * @param questions      - the list of all the questions;
     */
    public void generateTrueFalseQuestion(int typeOfQuestion, List<Question> questions) {
        String id = associateQuestion(typeOfQuestion);
        Random activitiesNumber = new Random();
        int numberOfActivities = activitiesNumber.nextInt(2) + 1;
        List<Activity> activities = fetchRandom(numberOfActivities);
        String question = "";
        int correctAnswer = 0;

        if (numberOfActivities == 1) {
            int correctNumber = activities.get(0).consumptionInWh;
            int wrongNumber = correctNumber * 110 / 100;
            if (wrongNumber % 2 == 0) {
                question = activities.get(0).title + " consumes " + wrongNumber + "per hour.";
                correctAnswer = 1;
            } else {
                question = activities.get(0).title + " consumes " + correctNumber + "per hour.";
            }
        } else {
            question = activities.get(0).title + " consumes more than " + activities.get(1) + ".";
            if (activities.get(0).consumptionInWh < activities.get(1).consumptionInWh) {
                correctAnswer = 1;
            }
        }

        Question trueFalseQuestion = new Question(activities, question, correctAnswer, id);
        questions.add(trueFalseQuestion);
    }

    public void generateOpenQuestion(int typeOfQuestion, List<Question> questions) {

    }

    /**
     * takes 3 randomized activities from the database
     * the string of the question will be "Which activity consumes more?"
     * the correct answer is the one with the highest wh consumption
     * the question is created with all the according parameters and added to the list
     *
     * @param typeOfQuestion - the randomly generated number
     * @param questions      - the list of all the questions
     */
    public void generateThreePicturesQuestion(int typeOfQuestion, List<Question> questions) {
        String id = associateQuestion(typeOfQuestion);

        List<Activity> activities = fetchRandom(3);
        String question = "Which consumes more?";
        int correctAnswer = 0;
        for (int i = 1; i < 3; i++) {
            if (activities.get(correctAnswer).consumptionInWh < activities.get(i).consumptionInWh) {
                correctAnswer = i;
            }
        }
        Question threePicturesQuestion = new Question(activities, question, correctAnswer, id);
        questions.add(threePicturesQuestion);
    }

    public void generateOneImageQuestion(int typeOfQuestion, List<Question> questions) {

    }

    public void generateInsteadOfQuestion(int typeOfQuestion, List<Question> question) {

    }
}
