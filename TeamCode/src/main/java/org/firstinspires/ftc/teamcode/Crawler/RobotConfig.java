package org.firstinspires.ftc.teamcode.Crawler;

import com.acmerobotics.dashboard.config.Config;

/**
 * Internal configuration class, primarily to allow for simpler configuration
 */
@Config
public class RobotConfig {
    public static final double ODO_WHEEL_DIAMETER_METERS = 0.048;
    public static final double ENCODER_TICKS_PER_REV = 2000;
    public static final double ODO_WHEEL_CIRCUMFERENCE = ODO_WHEEL_DIAMETER_METERS * Math.PI;
    public static final double TICKS_PER_METER = (ENCODER_TICKS_PER_REV / ODO_WHEEL_CIRCUMFERENCE);
    public static final double TICKS_PER_CM = TICKS_PER_METER * 100;

    //Trackwidth is the distance between the center of the left parallel encoder wheel (or drive wheel) and the center of the right parallel encoder wheel.
    public static final double TRACK_WIDTH = 0; //MAKE SHURE THIS IS CM
    //The Center Wheel Offset (often called the "Perpendicular Offset") tells the odometry math where the tracking wheel is located relative to the robot's center of rotation.
    public static final double CENTER_WHEEL_OFFSET = 0; //IN centimeter

    public static String encodeLeftName = ""; //Replace this with the left encode name (motor name in the same port)
    public static String encoderRightName = "";
    public static String encoderCenterName = "";

    //Only tune this if you are using field Oriented
    public static double defaultLookAheadDistance = 5; //cm

    public static double FINISH_THRESHOLD_CM = 2.0; //cm


    // Only tune pid beneath if not using Robot Oriented
    public static double Kp = 0.6;
    public static double Kd = 0;
    public static double Ki = 0;

    // FIXED: Set strafe coefficients to non-zero values
    public static double strafe_Kp = 1.85;
    public static double strafe_Ki = 0.00015;
    public static double strafe_Kd = 0;

    public static final double STEER_P = 0.02;
    public static final double MIN_POWER = 0.1;

    public static double timeoutSecs = 4;
}