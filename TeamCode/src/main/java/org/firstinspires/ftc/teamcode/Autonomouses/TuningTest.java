package org.firstinspires.ftc.teamcode.Autonomouses;

import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.ROMovementEngine;

public class TuningTest extends ROMovementEngine {
    @Override
    public void runPath() {
        drivePID(1, 0);
    }
}
