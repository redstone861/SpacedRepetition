/*
 By Samuel Cousins,
 For SmartWithIt pilot, August 2025
 All rights to SmartWithIt, Inc.
 */

package me.cousinss;

public record Date(int datePoint) implements Comparable<Date> {

    @Override
    public int compareTo(Date date) {
        return this.datePoint - date.datePoint;
    }

    public Date add(Date date) {
        return new Date(this.datePoint + date.datePoint);
    }

    @Override
    public String toString() {
        return "T+" + this.datePoint;
    }
}
