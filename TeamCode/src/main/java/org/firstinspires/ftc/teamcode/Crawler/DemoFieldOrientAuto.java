package org.firstinspires.ftc.teamcode.Crawler.FieldOrient;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.geometry.Rotation2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo Autonomous OpMode using Field-Oriented Path Following.
 */
@Autonomous(name = "Demo Field Orient Path Follow", group = "Examples")
public class DemoFieldOrientAuto extends Follower {

    /**
     * Initialize any extra systems.
     * Note: The 'robot' instance is already initialized by the parent Follower class.
     */
    @Override
    protected void initSystems() {
        telemetry.addLine("✓ Systems Initialized");
        telemetry.update();
    }

    /**
     * Main auto routine. Called after waitForStart().
     */
    @Override
    protected void runPathInstructions() throws InterruptedException {
        telemetry.addLine("Starting path following...");
        telemetry.update();

        // 1. Create waypoints for the path (using CM as per previous requirement)
        List<Pose2d> path = createPath();

        // 2. Create markers (trigger actions at specific % of path completion)
        List<PathMarker> markers = createMarkers();

        // 3. Run the path using the inherited runPath method
        runPath(path, markers);

        telemetry.addLine("Path complete!");
        telemetry.update();

        // Example of an action after path completion
        robot.activateShooters(false);
        sleep(500);
        robot.activateShooters(true);
    }

    /**
     * Define your path as a series of waypoints in Centimeters.
     */
    private List<Pose2d> createPath() {
        List<Pose2d> waypoints = new ArrayList<>();

        // Start pose (origin, facing forward)
        waypoints.add(new Pose2d(0, 0, new Rotation2d(0)));

        // Move forward 60 cm
        waypoints.add(new Pose2d(60, 0, new Rotation2d(0)));

        // Curve to a point 60cm forward and 30cm right, facing 90 degrees
        waypoints.add(new Pose2d(60, 30, Rotation2d.fromDegrees(90)));

        // Return toward start line
        waypoints.add(new Pose2d(0, 30, Rotation2d.fromDegrees(180)));

        return waypoints;
    }

    /**
     * Define markers to trigger actions at specific points in the path.
     * Uses the PathMarker class and RobotAction interface from the Follower parent.
     */
    private List<PathMarker> createMarkers() {
        List<PathMarker> markers = new ArrayList<>();

        // At 25.5% of path: Start the gobbler
        markers.add(new PathMarker(25.5, () -> {
            robot.activateGobbler(true);
            telemetry.addData("Marker", "25.5% - Gobbler On");
            telemetry.update();
        }));

        // At 50% of path: Rotate the indexer
        markers.add(new PathMarker(50, () -> {
            try {
                robot.cycleIndexer();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            telemetry.addData("Marker", "50% - Indexer Cycled");
            telemetry.update();
        }));

        // At 85% of path: Stop the gobbler
        markers.add(new PathMarker(85, () -> {
            robot.activateGobbler(false);
            telemetry.addData("Marker", "85% - Gobbler Off");
            telemetry.update();
        }));

        return markers;
    }
}