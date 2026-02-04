package org.firstinspires.ftc.teamcode.Autonomouses;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.ROMovementEngine;

@Autonomous(name="RedAutoTurm")
public class RedAutoTurm extends ROMovementEngine {

    @Override
    public void runPath() throws InterruptedException{

        // Generated Path (robot-oriented)
        // Start: x=-1.47 y=1.37 h=-49.0deg



        drivePID(1.54, 0);

        robot.shootSequence();
        strafePID(-1,0);


    }
}
