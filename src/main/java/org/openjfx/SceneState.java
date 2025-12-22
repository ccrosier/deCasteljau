package org.openjfx;

import javafx.geometry.Point2D;

import java.util.List;

public record SceneState(List<Point2D> controlPoints, double u, boolean drawLines)
{
}
