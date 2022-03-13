package commons;

import java.util.List;

public class Question {

    private List<Activity> activities;
    private String question;
    private int correctAnswer;
    private String questionType;

    /**
     * Empty constructor need to create an instance from JSON file
     */
    public Question() {
    }

    public Question(List<Activity> activities, String question, int correctAnswer, String questionType) {
        this.activities = activities;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.questionType = questionType;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }
}
