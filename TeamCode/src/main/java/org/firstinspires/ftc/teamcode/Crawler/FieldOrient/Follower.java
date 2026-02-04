package org.firstinspires.ftc.teamcode.Crawler.FieldOrient;

import static org.firstinspires.ftc.teamcode.Crawler.RobotConfig.FINISH_THRESHOLD_CM;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.geometry.Rotation2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Crawler.Robot;
import org.firstinspires.ftc.teamcode.Crawler.RobotConfig;

import java.util.ArrayList;
import java.util.List;

public abstract class Follower extends LinearOpMode {
    protected Robot robot;
    protected double currentLookahead = RobotConfig.defaultLookAheadDistance;


    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Robot(hardwareMap);
        initSystems();
        waitForStart();
        runPathInstructions();
    }

    /**
     * Define your paths and calls to runPath here in your child class.
     */
    protected abstract void runPathInstructions() throws InterruptedException;

    protected void initSystems() {}

    /**
     * The main path-following method.
     * @param path The list of waypoints (Pose2d)
     * @param markers List of actions to trigger at specific % of the path
     */
    public void runPath(List<Pose2d> path, List<PathMarker> markers) {
        if (path == null || path.isEmpty()) return;

        Pose2d startPose = getRobotPose();
        Pose2d finalPoint = path.get(path.size() - 1);
        double totalDistance = Math.hypot(finalPoint.getX() - startPose.getX(), finalPoint.getY() - startPose.getY());

        List<PathMarker> pendingMarkers = new ArrayList<>();
        if (markers != null) pendingMarkers.addAll(markers);

        while (opModeIsActive() && !isPathFinished(path)) {
            Pose2d currentPose = getRobotPose();

            // 1. Check Distance Markers
            double distCovered = Math.hypot(currentPose.getX() - startPose.getX(), currentPose.getY() - startPose.getY());
            double currentPercent = (distCovered / (totalDistance + 1e-6)) * 100.0;

            for (int i = 0; i < pendingMarkers.size(); i++) {
                if (currentPercent >= pendingMarkers.get(i).percentage) {
                    pendingMarkers.get(i).action.execute();
                    pendingMarkers.remove(i);
                    i--;
                }
            }

            // 2. Pure Pursuit Logic
            Pose2d lookaheadPoint = findLookaheadPoint(currentPose, path, currentLookahead);
            driveToPoint(currentPose, lookaheadPoint);
        }

        robot.drive(0, 0, 0); // Stop at end of path
    }

    // Standard runPath without markers
    public void runPath(List<Pose2d> path) {
        runPath(path, null);
    }

    private void driveToPoint(Pose2d currentPose, Pose2d targetPoint) {
        double deltaX = targetPoint.getX() - currentPose.getX();
        double deltaY = targetPoint.getY() - currentPose.getY();

        // Calculate heading to face the target
        double absoluteAngleToTarget = Math.atan2(deltaY, deltaX);
        double angleError = AngleUnit.normalizeRadians(absoluteAngleToTarget - currentPose.getHeading());
        double turnPower = angleError * RobotConfig.STEER_P;

        // Use the distance to the target to calculate speed
        double distance = Math.hypot(deltaX, deltaY);
        double driveSpeed = Math.max(RobotConfig.MIN_POWER, Math.min(0.7, distance * RobotConfig.Kp));

        // Normalize deltaX and deltaY to provide direction, then scale by driveSpeed
        double magnitude = Math.hypot(deltaX, deltaY);
        double forward = (deltaX / (magnitude + 1e-6)) * driveSpeed;
        double strafe = (deltaY / (magnitude + 1e-6)) * driveSpeed;

        // Robot class handles IMU rotation for Field Relative movement
        robot.driveFieldRelative(forward, strafe, turnPower);
    }

    private Pose2d getRobotPose() {
        double heading = robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
        // Simple 3-wheel localizer approximation
        double x = (robot.leftEncoder.getDistance() + robot.rightEncoder.getDistance()) / 2.0;
        double y = robot.centerEncoder.getDistance();
        return new Pose2d(x, y, new Rotation2d(heading));
    }

    private boolean isPathFinished(List<Pose2d> path) {
        if (path.isEmpty()) return true;
        Pose2d currentPose = getRobotPose();
        Pose2d finalPoint = path.get(path.size() - 1);

        return Math.hypot(finalPoint.getX() - currentPose.getX(),
                finalPoint.getY() - currentPose.getY()) < FINISH_THRESHOLD_CM;
    }

    // --- PURE PURSUIT MATH ---

    private Pose2d findLookaheadPoint(Pose2d robotPose, List<Pose2d> path, double radius) {
        Pose2d lookahead = path.get(path.size() - 1);
        for (int i = 0; i < path.size() - 1; i++) {
            List<Pose2d> intersections = lineCircleIntersection(robotPose, radius, path.get(i), path.get(i+1));
            if (!intersections.isEmpty()) {
                lookahead = intersections.get(intersections.size() - 1);
            }
        }
        return lookahead;
    }

    private List<Pose2d> lineCircleIntersection(Pose2d robotPos, double radius, Pose2d start, Pose2d end) {
        List<Pose2d> intersections = new ArrayList<>();
        double x1 = start.getX() - robotPos.getX();
        double y1 = start.getY() - robotPos.getY();
        double x2 = end.getX() - robotPos.getX();
        double y2 = end.getY() - robotPos.getY();

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dr = Math.sqrt(dx*dx + dy*dy);
        double D = x1 * y2 - x2 * y1;

        double disc = Math.pow(radius, 2) * Math.pow(dr, 2) - Math.pow(D, 2);
        if (disc >= 0) {
            double root = Math.sqrt(disc);
            double solX1 = (D * dy + (dy < 0 ? -1 : 1) * dx * root) / (dr*dr);
            double solY1 = (-D * dx + Math.abs(dy) * root) / (dr*dr);
            double solX2 = (D * dy - (dy < 0 ? -1 : 1) * dx * root) / (dr*dr);
            double solY2 = (-D * dx - Math.abs(dy) * root) / (dr*dr);

            Pose2d p1 = new Pose2d(solX1 + robotPos.getX(), solY1 + robotPos.getY(), start.getRotation());
            Pose2d p2 = new Pose2d(solX2 + robotPos.getX(), solY2 + robotPos.getY(), start.getRotation());

            if (isPointOnSegment(p1, start, end)) intersections.add(p1);
            if (isPointOnSegment(p2, start, end)) intersections.add(p2);
        }
        return intersections;
    }

    private static boolean isPointOnSegment(Pose2d p, Pose2d start, Pose2d end) {
        return p.getX() >= Math.min(start.getX(), end.getX()) - 0.1 &&
                p.getX() <= Math.max(start.getX(), end.getX()) + 0.1 &&
                p.getY() >= Math.min(start.getY(), end.getY()) - 0.1 &&
                p.getY() <= Math.max(start.getY(), end.getY()) + 0.1;
    }
}