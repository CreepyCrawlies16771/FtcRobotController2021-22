package org.firstinspires.ftc.teamcode.Autonomouses;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.ROMovementEngine;

@Autonomous(name="Red Second Auto")
public class redAutoTriangle extends ROMovementEngine {

    public void runPath() throws InterruptedException {

        // Generated Path (robot-oriented)
        // Start: x=1.73 y=-0.01 h=171.9deg


        drivePID(2.33, 0);
        turnPID(106); // waypoint heading
        robot.shootSequence();


    }
}
