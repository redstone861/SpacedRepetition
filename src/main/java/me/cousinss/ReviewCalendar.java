/*
 By Samuel Cousins,
 For SmartWithIt pilot, August 2025
 All rights to SmartWithIt, Inc.
 */

package me.cousinss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

public abstract class ReviewCalendar  {

    private static class QuestionComparator implements Comparator<SpacedQuestion> {
        @Override
        public int compare(SpacedQuestion q1, SpacedQuestion q2) {
            return -(q1.getSpaceID() - q2.getSpaceID());
        }
    }

    static int daysLate(SpacedQuestion q, Map<Question, Date> bases, Date date) {
        Date shouldBe = bases.get(q).add(q.getRepetition().getDateVector(q.getSpaceID()));
        return date.datePoint() - shouldBe.datePoint();
    }

    private static final Date UNIT_DATE_VECTOR = new Date(1);

    private final Map<Date, PriorityQueue<SpacedQuestion>> calendar;
    private final Map<Question, Date> basisIndex;
    private final Date cutoff;
    private final int maxPerDate;
    private int numAdded;
    private int numAbandoned;

    public ReviewCalendar(int maxPerDate, Date cutoff) {
        calendar = new HashMap<>();
        this.maxPerDate = maxPerDate;
        this.cutoff = cutoff;
        this.basisIndex = new HashMap<>();
    }

    private void delayFirst(PriorityQueue<SpacedQuestion> pq, Date toDate) {
        addToMap(toDate, pq.remove());
    }

    private void delay(PriorityQueue<SpacedQuestion> pq, SpacedQuestion question, Date toDate) {
        pq.remove(question);
        addToMap(toDate, question);
    }

    protected int getNumAdded() {
        return numAdded;
    }

    protected int getNumAbandoned() {
        return numAbandoned;
    }

    //Returns true if we should abandon the given question, rather than repairing its position (to a later date).
    public abstract boolean abandonRepair(SpacedQuestion question, Date date, int daysLate);

    private void repair() {
        List<Date> toCheck = new ArrayList<>( calendar.keySet().stream().toList());
        toCheck.sort(Date::compareTo);
        for(int i = 0; i < toCheck.size(); i++) {
            Date date = toCheck.get(i);
            //Tomorrow.
            Date nextDate = date.add(UNIT_DATE_VECTOR);
            PriorityQueue<SpacedQuestion> pq = this.calendar.get(date);
            //Maps a question to the smallest spacing iteration of this exact question that has been seen in the queue for this day.
            Map<Question, Integer> minSpaceIDs = new HashMap<>();
            boolean kicked;
            boolean ran = false;
            do {
                kicked = false;
                for(SpacedQuestion q : pq) {
                    //If the queue contains this question, but with a smaller iteration value, this is a duplicate -- delay it.
                    if(minSpaceIDs.containsKey(q) && minSpaceIDs.get(q) < q.getSpaceID()) {
                        if(!ran) {
                            toCheck.add(nextDate);
                            ran = true;
                        }
                        kicked = true;
                        delay(pq, q, nextDate);
                        break;
                    }
                    //Update the mapping.
                    minSpaceIDs.put(q, q.getSpaceID());
                }
            } while(kicked);
            if(pq.size() <= maxPerDate) {
                continue;
            }
            boolean anyAdded = false;
            //Delay (or abandon) the lowest-priority (ordered first for efficiency) question in the queue until we have reached our size goals.
            while (pq.size() > maxPerDate) {
                SpacedQuestion sq = pq.peek();
                if(abandonRepair(sq, date, daysLate(sq, basisIndex, date))) { // just remove the element, we give up
                    pq.remove();
                    numAbandoned++;
                } else {
                    anyAdded = true;
                    delayFirst(pq, nextDate);
                }
            }
            //If we added any questions to tomorrow's queue, we need to check that date as well (it may not originally have been a key in the mapping).
            if(anyAdded) {
                toCheck.add(nextDate);
            }
        }
    }

    //Skip the questions after the given question number for a given day. I.e., skipping fromQuestion=2 for a day with questions
    //[Q0, Q1, Q2, Q3, Q4] leaves the day with [Q0, Q1] and delays [Q2, Q3, Q4].
    public void skip(Date date, int fromQuestion) {
        if(!calendar.containsKey(date)) throw new NoSuchElementException();
        PriorityQueue<SpacedQuestion> pq = calendar.get(date);
        if(fromQuestion < 0 || fromQuestion >= pq.size()) throw new IllegalArgumentException();
        int toSkip = pq.size() - fromQuestion;
        Date nextDate = date.add(UNIT_DATE_VECTOR);
        for(int i = 0; i < toSkip; i++) {
            delayFirst(pq, nextDate);
        }
        repair();
    }

    //The set of questions to be asked on the date, in the order they are to be asked.
    public Collection<SpacedQuestion> getForDate(Date date) {
        List<SpacedQuestion> questions = new ArrayList<>(calendar.getOrDefault(date, new PriorityQueue<>()).stream().toList());
        Collections.reverse(questions);
        return questions;
    }

    public Date indexBasis(Question question) {
        return basisIndex.get(question);
    }

    //The number of questions to be asked on the date.
    public int count(Date date) {
        return calendar.getOrDefault(date, new PriorityQueue<>()).size();
    }

    private void addToMap(Date date, SpacedQuestion question) {
//        System.out.println("Adding " + question + " to " + date);
        if(!calendar.containsKey(date)) {
            calendar.put(date, new PriorityQueue<>(new QuestionComparator()));
        }
        calendar.get(date).add(question);
    }

    //Add the question at the given starting date and with the given spacing.
    public void addWithSpacing(Question question, Date dateBasis, SpacedRepetition spacing) {
        Date dateVector = spacing.getDateVector(0);
        int spaceID = 0;
        Date date;
        basisIndex.put(question, dateBasis);
        while(dateVector != null && (date = dateBasis.add(dateVector)).compareTo(cutoff) < 0) {
            SpacedQuestion sdq = new SpacedQuestion(question, spaceID, spacing);
            this.addToMap(date, sdq);
            numAdded++;
            dateVector = spacing.getDateVector(++spaceID);
        }
        repair();
    }
}