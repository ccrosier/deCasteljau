package org.openjfx;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.Map;

public record SceneState(List<Point2D> controlPoints, Map<Circle, Point2D> controlCircles, double u, boolean drawLines)
{
}
