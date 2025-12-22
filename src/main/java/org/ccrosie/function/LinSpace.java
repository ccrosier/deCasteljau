package org.ccrosie.function;

import java.util.ArrayList;
import java.util.List;

public class LinSpace
{
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
