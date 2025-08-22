/*
 By Samuel Cousins,
 For SmartWithIt pilot, August 2025
 All rights to SmartWithIt, Inc.
 */

package me.cousinss;

public class SpacedQuestion extends Question {
    private final int spaceID;
    private final SpacedRepetition repetition;

    public SpacedQuestion(Question question, int spaceID, SpacedRepetition repetition) {
        super(question.getQuestion(), question.getLessonID(), question.getId());
        this.spaceID = spaceID;
        this.repetition = repetition;
    }

    public int getSpaceID() {
        return spaceID;
    }

    public SpacedRepetition getRepetition() {
        return repetition;
    }

    @Override
    public String toString() {
        return "#" + repetition.hashCode()%100 + "'s " + spaceID + ": Q" + this.getId();
    }

    //adding a specific equals override breaks some existing code.
    // look into cases where SpacedQuestion.equals(Question q) (and replace with explicit cast to Question)
    // in ReviewCalendar.java if fix desired
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof SpacedQuestion sq)) return false;
//        return spaceID == sq.spaceID && repetition.equals(sq.repetition) && super.equals(o);
//    }
}