package org.ccrosie.spline;

import javafx.geometry.Point2D;
import javafx.scene.shape.Line;

/**
 * Parametric line equation.
 * @param l line
 */
public record LineParametric(Line l)
{
    /**
     * For d in [0, 1], return point at d.
     * @param d parameter
     * @return point
     */
    public Point2D at(double d)
    {
        return new Point2D(l.getStartX() + (d * (l.getEndX() - l.getStartX())),
                l.getStartY() + (d * (l.getEndY() - l.getStartY())));
    }
}
