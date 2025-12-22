package org.ccrosie.spline;

import com.ginsberg.gatherers4j.Gatherers4j;
import javafx.geometry.Point2D;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.List;
import java.util.stream.IntStream;

public record Bezier(List<Point2D> points)
{
    public Point2D at(double u)
    {
        final int n = points.size()-1;
        var bezierCoefficients = IntStream.range(0, n+1)
                .mapToDouble(i -> CombinatoricsUtils.binomialCoefficient(n, i)*Math.pow(u, i)*Math.pow(1-u, n-i))
                .boxed().toList();
        return points.stream().gather(Gatherers4j.zipWith(bezierCoefficients))
                .map(p -> p.first().multiply(p.second()))
                .reduce((point2D, point2D2) -> point2D.add(point2D2)).orElse(null);
    }
}
