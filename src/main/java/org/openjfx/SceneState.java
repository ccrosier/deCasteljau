package org.openjfx;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.Map;

/**
 * State container for the on-screen controls.
 * @param controlPoints bezier curve points
 * @param controlCircles mapping from on-screen circles to control points
 * @param u parameter for curve
 * @param drawLines whether to draw de Casteljau lines
 */
public record SceneState(List<Point2D> controlPoints, Map<Circle, Point2D> controlCircles, double u, boolean drawLines)
{
}
