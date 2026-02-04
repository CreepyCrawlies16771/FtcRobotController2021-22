package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="Delete Me!")
public class Deleteme extends OpMode {
    DcMotor motor;


    @Override
    public void init() {
        motor = hardwareMap.get(DcMotor.class, "frontLeft");
    }

    @Override
    public void loop() {
        motor.setPower(gamepad1.left_stick_y);
    }
}
