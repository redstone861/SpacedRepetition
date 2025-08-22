/*
 By Samuel Cousins,
 For SmartWithIt pilot, August 2025
 All rights to SmartWithIt, Inc.
 */

package me.cousinss;

public class StaticSpacedRepetition implements SpacedRepetition {

    private final Date[] dateVectors;

    public StaticSpacedRepetition(Date... dateVectors) {
        this.dateVectors = dateVectors;
    }

    @Override
    public Date apply(Integer n) {
        return dateVectors.length > n ? dateVectors[n] : null;
    }

    @Override
    public Date[] asArray(int count) {
        return dateVectors;
    }

}
