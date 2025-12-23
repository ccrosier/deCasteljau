package org.openjfx;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import com.ginsberg.gatherers4j.Gatherers4j;
import org.ccrosie.function.LinSpace;
import org.ccrosie.spline.Bezier;
import org.ccrosie.spline.LineParametric;

import java.util.*;

/**
 * Visualization of de Casteljau's algorithm for Bezier curve point interpolation.
 */
public class App extends Application
{
    static final int SCREEN_WIDTH = 800;
    static final int SCREEN_HEIGHT = 600;

    // determines curve smoothness
    static double SAMPLE_RATE = 0.025;

    // UI Components
    static Slider slider = new Slider(0, 1, 0.5);
    static ChoiceBox<Integer> degreeDropDown = new ChoiceBox<>();
    static CheckBox showLines = new CheckBox("Show Interpolation");
    static HBox controlPane = new HBox(new Label("U-Value"), slider, new Label("Degree"), degreeDropDown);
    static Pane gridPlane = new Pane();
    static SceneState sceneState;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static Optional<Circle> selectedCircle = Optional.empty();

    /**
     * Create an interactive circle at the given point.
     *
     * @param point where to create
     */
    public static void createCircle(Point2D point)
    {
        var c = new Circle(10);

        c.setTranslateX(point.getX());
        c.setTranslateY(point.getY());

        c.setFill(Color.WHITE);
        c.setOnMouseClicked(e ->
        {
            if (e.getButton() == MouseButton.PRIMARY)
            {
                selectedCircle = Optional.of(c);
            }
        });
        c.setOnMouseEntered(_ ->
        {
            c.setFill(Color.DARKGREEN);
        });
        c.setOnMouseExited(_ ->
        {
            c.setFill(Color.WHITE);
        });

        sceneState.controlCircles().put(c, point);
        gridPlane.getChildren().add(c);
    }

    @Override
    public void start(Stage stage)
    {
        // initialize controls
        degreeDropDown.getItems().addAll(2, 3, 4, 5);
        degreeDropDown.setValue(3);

        // put points
        int deg = degreeDropDown.getValue();
        List<Point2D> points = new ArrayList<>(LinSpace.ofDoubles(0, 360, (double) 360 / (deg + 1))
                .stream().limit(deg + 1)
                .map(d -> new Point2D(100 + (Math.cos(Math.toRadians(d)) + 1) * 100, 100 + (Math.sin(Math.toRadians(d)) + 1) * 100))
                .toList());

        // initialize scene state
        sceneState = new SceneState(points, new HashMap<>(), slider.valueProperty().doubleValue(), showLines.isSelected());
        sceneState.controlPoints().forEach(App::createCircle);
        drawScene();

        slider.addEventHandler(MouseEvent.ANY, _ ->
        {
            sceneState = new SceneState(sceneState.controlPoints(), sceneState.controlCircles(), slider.getValue(), sceneState.drawLines());
            drawScene();
        });

        showLines.setOnAction(_ ->
        {
            sceneState = new SceneState(sceneState.controlPoints(), sceneState.controlCircles(), sceneState.u(), showLines.isSelected());
            drawScene();
        });

        degreeDropDown.setOnAction(_ ->
        {
            int de = degreeDropDown.getValue();
            sceneState.controlPoints().clear();
            sceneState.controlPoints().addAll(LinSpace.ofDoubles(0, 360, (double) 360 / (de + 1))
                    .stream().limit(de + 1)
                    .map(d -> new Point2D(100 + (Math.cos(Math.toRadians(d)) + 1) * 100, 100 + (Math.sin(Math.toRadians(d)) + 1) * 100))
                    .toList());
            sceneState.controlCircles().clear();
            sceneState.controlPoints().forEach(App::createCircle);
            drawScene();
        });

        gridPlane.setOnMouseClicked(e ->
        {
            if (e.getButton() == MouseButton.SECONDARY)
            {
                selectedCircle = Optional.empty();
            }
        });
        gridPlane.setOnMouseMoved(event ->
        {
            selectedCircle.ifPresent(circle ->
            {
                // move control point in scene state
                int idx = sceneState.controlPoints().indexOf(sceneState.controlCircles().get(circle));
                Point2D newPos = new Point2D(event.getX(), event.getY());
                sceneState.controlCircles().put(circle, newPos);
                sceneState.controlPoints().add(idx, newPos);
                sceneState.controlPoints().remove(idx + 1);

                // move circle's on screen position
                circle.setTranslateX(event.getX());
                circle.setTranslateY(event.getY());
            });
            drawScene();
        });

        // background color
        gridPlane.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(6), null)));

        // help menu
        var helpMenu = new Menu("Help");
        var helpMessage = new MenuItem("Left-Click a point to select and move it around. Right-Click to deselect.");
        helpMenu.getItems().add(helpMessage);

        var aboutMenu = new Menu("About");
        var aboutMessage = new MenuItem(
                """
                This program visualizes de Casteljau's algorithm for drawing points on Bezier curves.
                You can draw curves of degree 2-5 but dragging points. To draw the whole curve, set
                the U-slider to 1 (all the way to the right). To see de Casteljau's in action, make
                sure to select "Show interpolation".
                """);
        aboutMenu.getItems().add(aboutMessage);

        MenuBar menuBar = new MenuBar(aboutMenu, helpMenu);

        controlPane.setSpacing(10);

        // layout
        var vBox = new VBox(menuBar, controlPane, showLines, new Separator(Orientation.HORIZONTAL), gridPlane);
        VBox.setVgrow(gridPlane,  Priority.ALWAYS);

        // fill horizontal
        vBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        vBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        vBox.setPrefHeight(Region.USE_COMPUTED_SIZE);

        var scene = new Scene(vBox, SCREEN_WIDTH, SCREEN_HEIGHT, Color.BLACK);

        stage.setTitle("de Casteljau's Algorithm");
        stage.setScene(scene);
        stage.show();
    }

    static void drawScene()
    {
        // redraw everything except control points, which need to be persistent for interactions
        gridPlane.getChildren().removeIf(s -> !sceneState.controlCircles().containsKey(s));
        var curve = new Bezier(sceneState.controlPoints());
        gridPlane.getChildren()
                .addAll(connectPoints(LinSpace.ofDoubles(0, sceneState.u(), SAMPLE_RATE)
                        .stream().map(curve::at).toList())
                        .stream().map(LineParametric::l).toList());

        if (sceneState.drawLines())
        {
            gridPlane.getChildren().addAll(decasteljau(sceneState.controlPoints(), sceneState.u()));
        }
    }

    /**
     * de Casteljau's algorithm
     *
     * @param controlPoints the Bézier curve
     * @param u             parametric parameter in [0, 1]
     * @return shapes to add to scene that show the algorithm
     */
    static List<Shape> decasteljau(List<Point2D> controlPoints, double u)
    {
        if (controlPoints.isEmpty())
        {
            return List.of();
        } else if (controlPoints.size() == 1)
        {
            var c = new Circle(5);
            c.setTranslateX(controlPoints.getFirst().getX());
            c.setTranslateY(controlPoints.getFirst().getY());
            c.setFill(Color.WHITE);
            return List.of(c);
        }

        List<LineParametric> lines = connectPoints(controlPoints);

        assert lines != null;
        var newControl = lines.stream().map(l -> l.at(u)).toList();
        List<Shape> displayElements = new ArrayList<>(lines.stream().map(LineParametric::l).map(a -> (Shape) a).toList());
        displayElements.forEach(e -> e.setStroke(Color.RED)); // make the segments red

        newControl.forEach(p ->
        {
            var c = new Circle(5);
            c.setCenterX(p.getX());
            c.setCenterY(p.getY());
            c.setFill(Color.WHITE);
            displayElements.add(c);
        });
        displayElements.addAll(decasteljau(newControl, u));

        return displayElements;
    }

    /**
     * Connect the given points with lines.
     *
     * @param points to connect
     * @return lines connecting the points
     */
    static List<LineParametric> connectPoints(List<Point2D> points)
    {
        if (points.size() <= 1)
            return List.of();

        return points.stream().gather(Gatherers4j.zipWithNext())
                .map(p ->
                {
                    Point2D p1 = p.getFirst();
                    Point2D p2 = p.getLast();
                    Line l = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                    l.setStroke(Color.WHITE);
                    return new LineParametric(l);
                }).toList();
    }

    static void main(String[] args)
    {
        launch();
    }
}