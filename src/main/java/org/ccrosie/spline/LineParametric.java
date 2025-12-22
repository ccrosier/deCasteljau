package org.ccrosie.spline;

import javafx.geometry.Point2D;
import javafx.scene.shape.Line;

public record LineParametric(Line l)
{
    public Point2D at(double d)
    {
        return new Point2D(l.getStartX() + (d * (l.getEndX() - l.getStartX())),
                l.getStartY() + (d * (l.getEndY() - l.getStartY())));
    }
}
