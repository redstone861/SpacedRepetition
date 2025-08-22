/*
 By Samuel Cousins,
 For SmartWithIt pilot, August 2025
 All rights to SmartWithIt, Inc.
 */

package me.cousinss;

import me.cousinss.graphic.GraphicDemo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class App {

    //Helper method to count the number of days late per question to be asked on the given date, per the correct first-ask-date bases map.
    private static void countLate(AtomicInteger qCount, AtomicInteger daysLate, ReviewCalendar calendar, Date date, Map<Question, Date> bases) {
        for(SpacedQuestion q : calendar.getForDate(date)) {
            qCount.getAndIncrement();
            daysLate.getAndAdd(me.cousinss.ReviewCalendar.daysLate(q, bases, date));
        }
    }

    private static void drawAnalysis() {
        //The number of iterations to run the entire simulation for.
        final int NUM_ITERS = 30;
        float[][][] lateVec = new float[11][8][NUM_ITERS];
        float[][][] abVec = new float[11][8][NUM_ITERS];
        Random random = new Random(0);
        //The expected fraction (questions/lesson)/(maxDayFeed)
        float feedProp = 0.4f;
        //The maximum questions that can be asked per day.
        int maxDayFeed = 5;
        //The end date of the simulation. Be aware that questions can be pushed out after this date.
        int endDate = 80;
        //The SpacedRepetition algorithm to use. Here, we use a static model, but a dynamic (procedural) model is simple as well, using the abstract method.
        SpacedRepetition ssp = new StaticSpacedRepetition(Arrays.stream(new int[] {0, 1, 2, 5, 8, 14}).mapToObj(Date::new).toArray(Date[]::new));
        for(int iter = 0; iter < NUM_ITERS; iter++) {
            //Maps a question to its canonical first-ask date.
            Map<Question, Date> bases = new HashMap<>();
            //The chance that a student will skip a given question.
            float skipChance;
            //The chance that a lesson will be posted on a particular day.
            float feedChance;
            int row;
            int col = 1;
            for(feedChance = 0.1f; feedChance <= 0.8; feedChance += 0.1f, col++) {
                row = 1;
                //FeedChance takes the top header, which is only present at z=0.
                lateVec[0][col][0] = feedChance;
                for (skipChance = 0; skipChance <= 0.5; skipChance += 0.05f, row++) {
                    //SkipChance takes the side header, which is only present at z=0.
                    lateVec[row][0][0] = skipChance;
                    ReviewCalendar calendar = new ReviewCalendar(maxDayFeed, new Date(endDate)) {
                        @Override
                        public boolean abandonRepair(SpacedQuestion question, Date date, int daysLate) {
                            //The abandonRepair model used, which skips questions if they're about 14 days late (with smoothing).
                            return random.nextFloat()*(float)(daysLate - 14)/14 > 0.5;
                        }
                    };
                    for (int day = 0; day < endDate; day++) {
                        Date date = new Date(day);
                        //Post a lesson depending on feedChance.
                        if(random.nextFloat() < feedChance) {
                            for (int q = 0; q < maxDayFeed; q++) {
                                //Post a set of questions (for the lesson) with an expected size of (maxDayFeed * feedProp).
                                if(random.nextFloat() < feedProp) {
                                    Question question = new Question("Q" + day + "." + q, day);
                                    calendar.addWithSpacing(question, date, ssp);
                                    bases.put(question, date);
                                }
                            }
                        }
                        //Skip a number of questions with an expected value equal to (# questions asked today) * (skipChance)
                        int count = calendar.count(date);
                        int skips = (int) (random.nextFloat() * skipChance * count);
                        if(skips > 0) {
                            calendar.skip(date, skips);
                        }
                    }
                    //Count the days late and the proportion of questions abandoned, and save it to the collector array
                    //at an index relative to [skipChance][feedChance][iteration].
                    AtomicInteger qCount = new AtomicInteger(0);
                    AtomicInteger daysLate = new AtomicInteger(0);
                    for(int i = 0; i < endDate; i++) {
                        countLate(qCount, daysLate, calendar, new Date(i), bases);
                    }
                    Date after = new Date(endDate);
                    while(calendar.count(after) > 0) {
                        countLate(qCount, daysLate, calendar, after, bases);
                        after = after.add(new Date(1));
                    }
                    float avgLate = (float)daysLate.get()/qCount.get();
                    float abandonedPortion = (float) calendar.getNumAbandoned() / calendar.getNumAdded();
                    lateVec[row][col][iter] = Float.isNaN(avgLate) ? 0 : avgLate;
                    abVec[row][col][iter] = Float.isNaN(abandonedPortion) ? 0 : abandonedPortion;
                }
            }
        }
        //Averaging the many trials.
        float[][] avgLateVec = new float[lateVec.length][lateVec[0].length];
        float[][] avgAbVec = new float[lateVec.length][lateVec[0].length];
        for(int r = 0; r < lateVec.length; r++) {
            for(int c = 0; c < lateVec[0].length; c++) {
                float sumL = 0;
                int countL = lateVec[0][0].length;
                for(int z = 0; z < countL; z++) {
                    sumL+=lateVec[r][c][z];
                }
                avgLateVec[r][c] = ((r == 0 || c == 0) ? lateVec[r][c][0] : sumL/countL);
                float sumA = 0;
                int countA = abVec[0][0].length;
                for(int z = 0; z < countA; z++) {
                    sumA+=abVec[r][c][z];
                }
                avgAbVec[r][c] = ((r == 0 || c == 0) ? abVec[r][c][0] : sumA/countA);
            }
        }
        //We can do whatever we want with the late and abandoned csv-type arrays here. I put them into a little graphical display.
        new GraphicDemo(avgLateVec, avgAbVec, feedProp, maxDayFeed, endDate);
    }

    public static void main(String[] args) {
        drawAnalysis();
    }
}