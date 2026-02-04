package org.firstinspires.ftc.teamcode.Crawler.RobotOrient;

import static org.firstinspires.ftc.teamcode.Crawler.RobotConfig.MIN_POWER;
import static org.firstinspires.ftc.teamcode.Crawler.RobotConfig.STEER_P;
import static org.firstinspires.ftc.teamcode.Crawler.RobotConfig.TICKS_PER_METER;
import static org.firstinspires.ftc.teamcode.Crawler.RobotConfig.strafe_Kd;
import static org.firstinspires.ftc.teamcode.Crawler.RobotConfig.strafe_Ki;


import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Crawler.Robot;
import org.firstinspires.ftc.teamcode.Crawler.RobotConfig;
import org.firstinspires.ftc.teamcode.Vision.AprilTagWebcam;
import org.firstinspires.ftc.teamcode.Vision.Rotation;
import org.firstinspires.ftc.teamcode.annotations.Experimental;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

public abstract class ROMovementEngine extends LinearOpMode {
    protected DcMotor backLeft, backRight, frontLeft, frontRight;
    protected DcMotor leftOdo, rightOdo, centerOdo;
    protected IMU imu;
    protected AprilTagWebcam aprilTagWebcam = new AprilTagWebcam();

    private double alpha = 0;
    private double beta = 0;
    private double gamma = 0;
    private double theta = 0;
    private double aSide = 0;
    private double bSide = 0;
    private final double cSide = 150;

    public Robot robot;

    public abstract void runPath() throws InterruptedException;

    @Override
    public void runOpMode() throws InterruptedException {
        aprilTagWebcam.init(hardwareMap, telemetry);

        robot = new Robot(hardwareMap);

        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");

        leftOdo = hardwareMap.get(DcMotor.class, "frontLeft");
        rightOdo = hardwareMap.get(DcMotor.class, "backRight");
        centerOdo = hardwareMap.get(DcMotor.class, "backLeft");

        setMotorBehavior();

        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                RevHubOrientationOnRobot.UsbFacingDirection.UP)));

        resetOdometry();
        imu.resetYaw();
        waitForStart();
        imu.resetYaw();

        if (opModeIsActive()) {
            aprilTagWebcam.update();
            runPath();
        }
    }

    /**
     * THis function allows the robot to move to the remade shooting position
     * @param team the team to move to*/
    public void moveToShoot(Team team) {
        aprilTagWebcam.update();
        if(aprilTagWebcam.getDetectedTags() == null) return;
        if (team == Team.BLUE) {
            findData(team);

            // --- FIXED ORDER: Calculate side 'b' BEFORE angles gamma/alpha ---
            calculateBSide();   // Calculate distance to drive first
            calculateGamma();   // Calculate turn angle
            calculateAlpha();   // Calculate final alignment

            // First turn towards the target position
            double firstTurnAngle = (-theta + gamma);

            // Drive the distance (converted to meters)
            turnPID((int) -firstTurnAngle);


            drivePID(-(bSide / 100)/*convert to meters */, ((int) -firstTurnAngle));

            // Turn to the final shooting orientation
            double secondAngleTurn = 180 - alpha;
            turnPID((int) secondAngleTurn);

            telemetry.addData("turning Status", "completed");
            telemetry.addData("firstTurnAngle", firstTurnAngle);
            telemetry.addData("second angle", secondAngleTurn);
            telemetry.addData("Bearing", theta);
            telemetry.addData("Gamma", gamma);
            telemetry.addData("Alpha", alpha);
            telemetry.addData("Beta", beta);
            telemetry.addData("B side", bSide);
            telemetry.addData("A side", aSide);
            telemetry.addData("C side", cSide);

            telemetry.update();
        }

        aprilTagWebcam.close();
    }


    private void findData(Team team) {
        aprilTagWebcam.update();
        AprilTagDetection detection = aprilTagWebcam.getTagBySpecificId(team.getTeamAprilTagID());
        if (detection != null) {
            aSide = aprilTagWebcam.getAngle(detection, Rotation.RANGE);
            beta  = aprilTagWebcam.getAngle(detection, Rotation.YAW);
            theta = aprilTagWebcam.getAngle(detection, Rotation.BEARING);
        }
    }

    private void calculateBSide() {
        double thetaRad = Math.toRadians(theta);
        bSide = Math.sqrt(Math.pow(aSide, 2) + Math.pow(cSide, 2) - (2 * aSide * cSide * Math.cos(thetaRad)));
    }

    private void calculateGamma() {
        double cosGamma = (Math.pow(aSide, 2) + Math.pow(bSide, 2) - Math.pow(cSide, 2)) / (2 * aSide * bSide);
        cosGamma = Math.max(-1, Math.min(1, cosGamma));
        gamma = Math.toDegrees(Math.acos(cosGamma));
    }

    private void calculateAlpha() { alpha = 180 - (theta + gamma); }


    /**
     * Move the robot forward while using a pid/gyro correction
     * @param targetMeters distance to travel in meters
     * @param targetAngle angle to turn to in degrees
     */
    public void drivePID(double targetMeters, int targetAngle) {
        double targetTicks = targetMeters * TICKS_PER_METER;

        // 1. SAFE START: Calculate start position instead of resetting hardware
        // resetting hardware encoders can be slow/laggy in loops
        double startPos = ((leftOdo.getCurrentPosition() + rightOdo.getCurrentPosition()) / 2.0);

        double error = targetTicks;
        double lastError = 0;
        double integral = 0;

        // 2. TIMEOUT: Prevent infinite loops if sensors fail
        ElapsedTime timer = new ElapsedTime();
        timer.reset();

        while (opModeIsActive() && (timer.seconds() < RobotConfig.timeoutSecs) && Math.abs(error) > 50) {

            // Calculate current distance traveled relative to start
            double rawCurrentPos = ((leftOdo.getCurrentPosition() + rightOdo.getCurrentPosition()) / 2.0) * -1;
            double currentPos = rawCurrentPos - startPos;

            // 3. DEBUGGING: If this number goes NEGATIVE when driving FORWARD,
            // you must reverse your encoder direction in the config or code.

            error = targetTicks - currentPos;

            double derivative = error - lastError;

            // Integral anti-windup (only accumulate when close to target)
            if (Math.abs(error) < (0.1 * TICKS_PER_METER)) {
                integral += error;
            } else {
                integral = 0;
            }

            double power = (RobotConfig.Kp * (error / TICKS_PER_METER))
                    + (RobotConfig.Ki * integral)
                    + (RobotConfig.Kd * derivative);

            // Steering logic
            double currentYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            double steer = angleWrap(targetAngle - currentYaw) * STEER_P;

            // Clamp power
            power = Math.max(-0.7, Math.min(0.7, power));

            // Feedforward / Minimum power to overcome friction
            if (Math.abs(power) < MIN_POWER && Math.abs(error) > 50) {
                power = Math.signum(power) * MIN_POWER;
            }

            applyDrivePower(power, -steer);
            lastError = error;

            // 4. TELEMETRY: Essential for seeing WHY it won't stop
            telemetry.addData("Target Ticks", targetTicks);
            telemetry.addData("Current Pos", currentPos);
            telemetry.addData("Error", error);
            telemetry.addData("Power", power);
            telemetry.update();
        }
        stopRobot();
    }


    /**
     * Move the robot sideways while using a pid/gyro correction
     * @param targetMeters distance to travel in meters
     * @param targetAngle angle to turn to in degrees*/

    public void strafePID(double targetMeters, int targetAngle) {
        double targetTicks = targetMeters * TICKS_PER_METER;
        double error = targetTicks;
        double lastError = 0;
        double integral = 0;

        final int maxError = 50;

        resetOdometry();

        while (opModeIsActive() && Math.abs(error) > maxError) {
            double currentPos =  centerOdo.getCurrentPosition();

            //currentPos = currentPos * -1; // if the odometry pods are mounted backwards

            error = targetTicks - currentPos;

            // PID Logic
            double derivative = error - lastError;
            integral += error;

            // Anti-windup cap
            if (Math.abs(error) < (0.1 * TICKS_PER_METER)) { // Only use Integral when close
                integral = Math.max(-20, Math.min(20, integral));
            } else {
                integral = 0;
            }

            double power = (RobotConfig.strafe_Kp * (error / TICKS_PER_METER)) + (strafe_Ki * integral) + (strafe_Kd * derivative);

            // Steering with Angle Wrap
            double currentYaw = -imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            double steer = angleWrap(currentYaw - targetAngle) * -STEER_P;

            power = Math.max(-0.7, Math.min(0.7, power));
            if (Math.abs(power) < MIN_POWER) power = Math.signum(power) * MIN_POWER;

            applyStrafePower(power, steer);
            lastError = error;


        }
        stopRobot();
    }

    /**
     * Turn the robot to desired angle
     * @param targetAngle angle to turn to in degrees*/

    public void turnPID(int targetAngle) {
        // 1. Calculate error
        double currentYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        double error = angleWrap(targetAngle - currentYaw);

        // 2. Loop until error is small (e.g., < 1 degree)
        while (opModeIsActive() && Math.abs(error) > 1.0) {
            currentYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            error = angleWrap(targetAngle - currentYaw);

            // Simple P-Control for turning
            // We use a higher P value here because turning needs more punch than steering correction
            double turnPower = error * 0.03;

            // Clamp power to avoid moving too fast or stalling
            turnPower = Math.max(-0.6, Math.min(0.6, turnPower));
            if (Math.abs(turnPower) < 0.15) turnPower = Math.signum(turnPower) * 0.15;

            // Apply power (Turn Right = Left Forward, Right Back)
            // Note: Check your motor directions!
            backLeft.setPower(-turnPower);
            frontLeft.setPower(-turnPower);
            backRight.setPower(turnPower);
            frontRight.setPower(turnPower);

            telemetry.addData("Target", targetAngle);
            telemetry.addData("Heading", currentYaw);
        }
        stopRobot();
    }

    @Experimental("This is only a prototype, may be removed/moved to new pure pursuit algorithm")
    public void arc(double meters, double maxPower, AnimationBuilder animator) {
        double targetTicks = meters * TICKS_PER_METER;
        double startHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);

        // 1. Setup the Timeline
        HeadingTimeline timeline = new HeadingTimeline();
        animator.build(timeline); // Execute the user's instructions

        resetOdometry();

        while (opModeIsActive()) {
            double currentPos = (leftOdo.getCurrentPosition() + rightOdo.getCurrentPosition()) / 2.0;

            // Calculate Progress (0.0 to 1.0)
            double progress = Math.abs(currentPos / targetTicks);
            if (progress >= 1.0) break;

            // 2. ASK THE TIMELINE FOR HEADING
            double targetHeading = timeline.getTarget(progress, startHeading);

            // Standard Drive Logic
            double error = Math.abs(targetTicks) - Math.abs(currentPos);
            double power = (RobotConfig.Kp * (error / TICKS_PER_METER));

            power = Math.max(-maxPower, Math.min(maxPower, power));
            if (Math.abs(power) < MIN_POWER) power = Math.signum(power) * MIN_POWER;

            double currentYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            double steer = angleWrap(currentYaw - targetHeading) * -STEER_P;

            applyDrivePower(power, steer);

            // Telemetry for debugging
            telemetry.addData("Progress", "%.2f", progress);
            telemetry.addData("Target Head", "%.1f", targetHeading);
            telemetry.update();
        }
        stopRobot();
    }

    // --- HELPERS ---

    private void applyDrivePower(double p, double s) {
        // p = forward power, s = steer (turning)
        frontLeft.setPower(p + s);
        backLeft.setPower(p + s);
        frontRight.setPower(p - s); // Right side must be opposite of Left
        backRight.setPower(p - s); // Right side must be opposite of Left
    }

    public void applyStrafePower(double strafe, double steer) {
        // Mecanum Strafe Pattern:
        // FrontLeft and BackRight go one way
        // FrontRight and BackLeft go the other way
        double fl =  strafe + steer;
        double fr = -strafe - steer;
        double bl = -strafe + steer;
        double br =  strafe - steer;

        // Normalize power so no motor exceeds 1.0
        double max = Math.max(Math.abs(fl), Math.max(Math.abs(fr),
                Math.max(Math.abs(bl), Math.abs(br))));
        if (max > 1.0) {
            fl /= max;
            fr /= max;
            bl /= max;
            br /= max;
        }

        frontLeft.setPower(fl);
        frontRight.setPower(fr);
        backLeft.setPower(bl);
        backRight.setPower(br);
    }

    private void stopRobot() {
        backLeft.setPower(0); backRight.setPower(0);
        frontLeft.setPower(0); frontRight.setPower(0);
        sleep(100);
    }

    private void resetOdometry() {
        leftOdo.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightOdo.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        centerOdo.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftOdo.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightOdo.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        centerOdo.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    private void setMotorBehavior() {
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.REVERSE);
    }

    private double angleWrap(double degrees) {
        while (degrees > 180) degrees -= 360;
        while (degrees < -180) degrees += 360;
        return degrees;
    }
}