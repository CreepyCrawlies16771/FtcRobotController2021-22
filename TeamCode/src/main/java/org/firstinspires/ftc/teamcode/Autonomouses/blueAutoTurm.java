package org.firstinspires.ftc.teamcode.Autonomouses;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.ROMovementEngine;

@Autonomous(name="BlueAuto")
public class blueAutoTurm extends ROMovementEngine {
    @Override
    public void runPath() throws InterruptedException {
// Generated Path (robot-oriented)
// Start: x=-1.76 y=-0.01 h=0.0deg

        // Generated Path (robot-oriented)
// Start: x=-1.47 y=-1.38 h=0.0deg

        turnPID(0); // face start heading

        drivePID(1, 0);
        //robot.shootSequence();
        strafePID(1,0);



    }
}
