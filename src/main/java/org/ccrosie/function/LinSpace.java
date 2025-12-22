package org.ccrosie.function;

import java.util.ArrayList;
import java.util.List;

/**
 * Analogous to NumPy's "linspace" generator.
 */
public class LinSpace
{
    /**
     * Points between start and end, in increments of inc.
     * @param start starting value
     * @param end ending value
     * @param increment increment to use
     * @return list of the values
     */
    public static List<Double> ofDoubles(double start, double end, double increment)
    {
        List<Double> result = new ArrayList<>();
        while (start <= end)
        {
            result.add(start);
            start += increment;
        }
        if (result.getLast() != end)
            result.add(end);
        return result;
    }
}
