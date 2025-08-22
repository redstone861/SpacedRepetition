/*
 By Samuel Cousins,
 For SmartWithIt pilot, August 2025
 All rights to SmartWithIt, Inc.
 */

package me.cousinss;

public class Question {

    private static int nextID = 0;

    private final String question;
    private final int id;
    private final int lessonID;

    protected Question(String question, int lessonID, int id) {
        this.question = question;
        this.lessonID = lessonID;
        this.id = id;
    }

    public Question(String question, int lessonID) {
        this(question, lessonID, nextID++);
    }

    public String getQuestion() {
        return question;
    }

    public int getLessonID() {
        return lessonID;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Q#" + this.id + ": " + question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question q)) return false;
        return question.equals(q.question) && lessonID == q.lessonID && id == q.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}
