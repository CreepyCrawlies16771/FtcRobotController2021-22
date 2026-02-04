package org.firstinspires.ftc.teamcode.Autonomouses;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.ROMovementEngine;


@Autonomous(name="Just go straight")
public class EasyAuto extends ROMovementEngine {

    public void runPath() {
        drivePID(0.5,0);
    }
}
