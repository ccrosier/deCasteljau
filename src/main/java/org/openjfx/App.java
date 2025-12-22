package org.openjfx;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
 * JavaFX App
 */
public class App extends Application {

    static double SAMPLE_RATE = 0.01;

    // UI Components
    static Slider slider = new Slider(0, 1, 0.5);
    static ChoiceBox<Integer> degreeDropDown = new ChoiceBox<>();
    static CheckBox showLines = new CheckBox("Show Lines");
    static Pane controlPane = new VBox(new HBox(new Label("U-Value"), slider), new HBox(new Label("Degree"), degreeDropDown, showLines));
    static Pane gridPlane = new Pane();
    static final Circle[] selected = new Circle[1];
    static SceneState sceneState;

    public static void createCircle(Point2D point)
    {
        var c = new Circle(10);

        c.setTranslateX(point.getX());
        c.setTranslateY(point.getY());

        // mouse selector
        c.setOnMouseClicked(event ->
        {
            if (selected[0] != c)
            {
                selected[0] = c;
                event.consume();
            }
        });
        c.setOnMouseEntered(event -> {
            c.setFill(Color.RED);
        });
        c.setOnMouseExited(event -> {
            c.setFill(Color.BLACK);
        });
        gridPlane.getChildren().add(c);
    }

    @Override
    public void start(Stage stage) {
        // initialize controls
        degreeDropDown.getItems().addAll(2,3,4,5);
        degreeDropDown.setValue(3);

        // put points
        int deg = degreeDropDown.getValue();
        List<Point2D> points = LinSpace.ofDoubles(0, 360, (double) 360 /(deg+1))
                        .stream().limit(deg+1)
                        .map(d -> new Point2D(100+(Math.cos(Math.toRadians(d))+1)*100, 100+(Math.sin(Math.toRadians(d))+1)*100))
                        .toList();


        // initialize scene state
        sceneState = new SceneState(points, slider.valueProperty().doubleValue(), showLines.isSelected());
        drawScene();

        EventHandler<MouseEvent> moveHandler =  event -> {
            if (selected[0] != null)
            {
                selected[0].setTranslateX(event.getX());
                selected[0].setTranslateY(event.getY());
                drawScene();
            }
        };

        // handle selection and movement
        gridPlane.setOnMouseMoved(moveHandler);
        gridPlane.setOnMouseClicked(event -> {
            if (selected[0] != null)
            {
                selected[0] = null;
            }
        });

        slider.addEventHandler(MouseEvent.ANY, _ -> {
            sceneState = new SceneState(sceneState.controlPoints(), slider.getValue(), sceneState.drawLines());
            drawScene();
        });

        showLines.setOnAction(event -> {
            sceneState = new SceneState(sceneState.controlPoints(), sceneState.u(), showLines.isSelected());
            drawScene();
        });

        degreeDropDown.setOnAction(_ -> {
            int de = degreeDropDown.getValue();
            sceneState.controlPoints().clear();
            sceneState.controlPoints().addAll(LinSpace.ofDoubles(0, 360, (double) 360 /(de+1))
                    .stream().limit(de+1)
                    .map(d -> new Point2D(100+(Math.cos(Math.toRadians(d))+1)*100, 100+(Math.sin(Math.toRadians(d))+1)*100))
                    .toList());
            drawScene();
        });

        // move handler
        // layout
        var sep = new Separator(Orientation.HORIZONTAL);
        var vBox = new VBox(controlPane, sep, gridPlane);
        var grid = new Pane(vBox);

        var scene = new Scene(grid, 640, 480, Color.GRAY);

        stage.setTitle("Decasteljau's Algorithm");
        stage.setScene(scene);
        stage.show();
    }

    static void drawScene()
    {
        gridPlane.getChildren().clear();
        sceneState.controlPoints().forEach(App::createCircle);
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

    static List<Shape> decasteljau(List<Point2D> controlPoints, double u)
    {
        if (controlPoints.isEmpty())
        {
            return List.of();
        }
        else if (controlPoints.size() == 1)
        {
            var c = new Circle(5);
            c.setTranslateX(controlPoints.getFirst().getX());
            c.setTranslateY(controlPoints.getFirst().getY());
            return List.of(c);
        }

        List<LineParametric> lines = connectPoints(controlPoints);

        assert lines != null;
        var newControl = lines.stream().map(l -> l.at(u)).toList();
        List<Shape> displayElements = new ArrayList<>(lines.stream().map(LineParametric::l).map(a -> (Shape) a).toList());

        newControl.forEach(p -> {
            var c = new Circle(5);
            c.setCenterX(p.getX());
            c.setCenterY(p.getY());
            displayElements.add(c);
        });
        displayElements.addAll(decasteljau(newControl, u));

        return displayElements;
    }

    static List<LineParametric> connectPoints(List<Point2D> points)
    {
        if (points.size() <= 1)
            return List.of();

        return points.stream().gather(Gatherers4j.zipWithNext())
                .map(p -> {
                    Point2D p1 = p.getFirst();
                    Point2D p2 = p.getLast();
                    return  new LineParametric(new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY()));
                }).toList();
    }

    static void main(String[] args) {
        launch();
    }
}