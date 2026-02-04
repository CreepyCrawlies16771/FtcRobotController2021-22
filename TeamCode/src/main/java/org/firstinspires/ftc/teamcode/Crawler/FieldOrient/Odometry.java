package org.firstinspires.ftc.teamcode.Crawler.FieldOrient;

import com.arcrobotics.ftclib.command.OdometrySubsystem;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.kinematics.DifferentialOdometry;
import com.arcrobotics.ftclib.kinematics.HolonomicOdometry;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Crawler.Robot;
import org.firstinspires.ftc.teamcode.Crawler.RobotConfig;

public class Odometry {
    Robot robot;
    public Odometry(HardwareMap hwMap) {
        robot = new Robot(hwMap);
    }

    public HolonomicOdometry holonomicOdometry = new HolonomicOdometry(
            robot.leftEncoder::getDistance,
            robot.rightEncoder::getDistance,
            robot.centerEncoder::getDistance,
            RobotConfig.TRACK_WIDTH, RobotConfig.CENTER_WHEEL_OFFSET
    );

    OdometrySubsystem odometry = new OdometrySubsystem(holonomicOdometry);

}
