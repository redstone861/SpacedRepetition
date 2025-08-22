/*
 By Samuel Cousins,
 For SmartWithIt pilot, August 2025
 All rights to SmartWithIt, Inc.
 */

package me.cousinss;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface SpacedRepetition extends Function<Integer, Date> {

    /**
     * Returns the date vector for the {@code n}th repetition, or {@code null} if no such value is accessible.
     * @param n the repetition id.
     * @return the date vector.
     */
    Date apply(Integer n);

    default Date getDateVector(int n) {
        return apply(n);
    }

    default Date[] asArray(int count) {
        Date[] dates = new Date[count];
        for (int i = 0; i < count; i++) {
            dates[i] = getDateVector(i);
        }
        return dates;
    }

    default List<Date> toList(int count) {
        return List.of(asArray(count));
    }

}
