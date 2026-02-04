# Crawler - Quick Start Guide

## Getting Started in 5 Minutes

### Step 1: Create Your First Autonomous OpMode

```java
package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.MovementEngine;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "My First Auto", group = "Crawler")
public class MyFirstAuto extends MovementEngine {

   @Override
   public void runPath() {
      // Your autonomous code goes here
      telemetry.addLine("Starting autonomous!");
      telemetry.update();

      // Drive 1 meter forward
      drivePID(1.0, 0);

      // Turn 90 degrees
      turnPID(90);

      telemetry.addLine("Complete!");
      telemetry.update();
   }
}
```

### Step 2: Test Individual Movements

**Test drive forward:**
```java
drivePID(0.5, 0);  // 50cm forward
```

**Test turning:**
```java
turnPID(45);       // Turn 45 degrees right
```

**Test strafing:**
```java
strafePID(0.25, 0); // Slide 25cm right
```

### Step 3: Use April Tag Targeting

```java
@Override
public void runPath() {
    // Align with blue team shooting target
    moveToShoot(Team.BLUE);
    
    // Shooter will now be aimed
    // Call shooter activation here
}
```

---

## Configuration Guide

### Robot Configuration (First Time Setup)

Your robot must have these hardware devices configured:

**Motors:**
```
frontLeft    - Front left drive motor
frontRight   - Front right drive motor
backLeft     - Back left drive motor
backRight    - Back right drive motor
leftShoot    - Left shooter motor
rightShoot   - Right shooter motor
gobbler      - Intake motor
indexer      - Ball indexer motor
frontLeft    - Left odometry encoder (reuses front left motor)
backRight    - Right odometry encoder (reuses back right motor)
backLeft     - Center odometry encoder (reuses back left motor)
```

**Sensors:**
```
imu          - IMU (Rev Hub integrated or external)
ballCSensor  - Color sensor for ball detection
```

**Servos:**
```
lifter       - Servo for lift mechanism
```

### Odometry Calibration

#### Step 1: Measure Your Robot's Dimensions

```java
// Width between parallel encoders (inches)
public static double TRACKWIDTH_INCHES = 13.7;

// Distance from center of parallel encoders to perpendicular encoder
public static double CENTER_WHEEL_OFFSET = 2.4;
```

#### Step 2: Calibrate Encoder Ticks

Run this test:

```java
@Autonomous(name="Calibration - Drive Straight", group="Calibration")
public class CalibrateOdo extends MovementEngine {
    @Override
    public void runPath() {
        resetOdometry();
        
        // Drive exactly 10 feet (3.048 meters)
        // Measure how far robot actually travels
        drivePID(3.048, 0);
        
        // Check telemetry for ticks traveled
        telemetry.addData("Done. Check encoder values in log.", "");
        telemetry.update();
        sleep(5000);
    }
}
```

**Then adjust TICKS_PER_METER:**
```
TICKS_PER_METER = (actual_ticks / desired_distance_in_meters)
```

#### Step 3: Test Circular Path

```java
@Autonomous(name="Calibration - Circle", group="Calibration")
public class CalibrateCircle extends MovementEngine {
    @Override
    public void runPath() {
        // Drive in a square to test rotation accuracy
        for (int i = 0; i < 4; i++) {
            drivePID(1.0, 0);   // Drive 1 meter forward
            turnPID(90);         // Turn 90 degrees
        }
        // Robot should return to starting position with 0° heading
    }
}
```

### PID Tuning

**Default values (usually work well):**
```java
public static double Kp = 0.6;
public static double Ki = 0.0015;
public static double Kd = 0;
public static double STEER_P = 0.02;
```

**If robot overshoots targets:**
```java
Kp = 0.4;  // Reduce proportional gain
```

**If robot undershoots:**
```java
Ki = 0.003;  // Increase integral gain
MIN_POWER = 0.3;  // Increase minimum power
```

**If movement oscillates:**
```java
Kd = 0.1;  // Add derivative damping
```

### Camera/Vision Calibration

1. Place robot in front of April Tag
2. Run detection test OpMode
3. Check telemetry for tag detection
4. If tags aren't detected:
   - Verify camera focus
   - Check lighting conditions
   - Ensure tags are within ~3 meters
   - Check camera calibration in FTC dashboard

---

## Common Tasks

### Task 1: Navigate to Specific Location

```java
@Override
public void runPath() {
    // Move to position (24 inches right, 36 inches forward)
    drivePID(0.9144, 0);      // ~3 feet forward
    turnPID(0);               // Face original direction
    strafePID(0.6096, 0);     // ~2 feet right
}
```

### Task 2: Align with April Tag and Shoot

```java
@Override
public void runPath() {
    // Drive to approximate area
    drivePID(1.5, 0);
    
    // Fine align with tag
    moveToShoot(Team.RED);
    
    // Activate shooter
    robot.activateShooters(false);  // Start shooting
    sleep(1000);                     // Let it spin up
    
    // Shoot ball (activate indexer)
    // TODO: Add your shooter firing logic
    
    sleep(100);
    robot.activateShooters(true);   // Stop shooter
}
```

### Task 3: Follow Curved Path

```java
@Override
public void runPath() {
    // Smooth S-curve: start at 0°, curve through 45°, end at 90°
    arc(2.0, 0.8, timeline -> {
        timeline.at(0.0, 0)      // Start heading
                .at(0.25, 22.5)  // First quarter
                .at(0.5, 45)     // Midpoint (steepest turn)
                .at(0.75, 67.5)  // Third quarter
                .at(1.0, 90);    // End heading
    });
}
```

### Task 4: Multiple Sequential Actions

```java
@Override
public void runPath() {
    // Sequence 1: Get first ball
    drivePID(1.0, 0);
    robot.activateGobbler(true);
    sleep(500);
    robot.activateGobbler(false);
    
    // Sequence 2: Move to shooting position
    turnPID(45);
    drivePID(1.5, 45);
    
    // Sequence 3: Shoot
    moveToShoot(Team.BLUE);
    robot.activateShooters(false);
    sleep(2000);
    robot.activateShooters(true);
}
```

---

## Debug Telemetry

Add this to see what's happening:

```java
@Override
public void runPath() {
    telemetry.addLine("=== CRAWLER AUTO DEBUG ===");
    telemetry.addData("Team", Team.BLUE.getTeamAprilTagID());
    telemetry.addData("Status", "Ready to start");
    telemetry.update();
    
    waitForStart();
    
    while (opModeIsActive()) {
        telemetry.addData("IMU Heading", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
        telemetry.addData("Encoder L", leftOdo.getCurrentPosition());
        telemetry.addData("Encoder R", rightOdo.getCurrentPosition());
        telemetry.addData("Encoder C", centerOdo.getCurrentPosition());
        telemetry.update();
        
        break;  // Exit after one loop to see values
    }
}
```

---

## Frequently Asked Questions

**Q: How do I integrate with existing robot code?**
A: Inherit from MovementEngine instead of LinearOpMode. You get all the movement functions automatically.

**Q: What if I need custom hardware control?**
A: Override methods or add new methods to your autonomous class:
```java
public void myCustomAction() {
    // Your code here
}
```

**Q: How accurate is the positioning?**
A: Typically ±5cm for distance, ±3° for heading. Odometry drifts over long movements.

**Q: Can I run multiple movements in parallel?**
A: Not directly. Use sequential commands. Create a helper method if needed:
```java
private void driveAndIntake(double distance) {
    robot.activateGobbler(true);
    drivePID(distance, 0);
    robot.activateGobbler(false);
}
```

**Q: What if April Tag detection fails?**
A: moveToShoot() returns immediately if no tags detected. Add fallback:
```java
moveToShoot(Team.BLUE);
if (aprilTagWebcam.getDetectedTags() == null) {
    // Fallback: turn and shoot anyway
    turnPID(45);
}
```

**Q: How do I save time during competition?**
A: Pre-compute waypoints, test paths multiple times, document coordinates. Keep telemetry output during matches.

---

## Next Steps

1. **Run calibration routines** - Verify odometry accuracy
2. **Test basic movements** - Drive, turn, strafe in safe area
3. **Test April Tag detection** - Verify vision system works
4. **Combine movements** - Create multi-step paths
5. **Fine-tune PID** - Adjust gains for smooth motion
6. **Full routine testing** - Run complete autonomous 5+ times

---

## Getting Help

**Movement not working?**
- Check that hardware is configured correctly
- Verify motor directions in setMotorBehavior()
- Check PID gains aren't too extreme
- Enable telemetry debug output

**Vision not detecting tags?**
- Ensure tags are in frame and properly oriented
- Check camera calibration
- Verify adequate lighting
- Test with known working tag

**Odometry drifting?**
- Recalibrate TICKS_PER_METER
- Check that encoders are clean and aligned
- Test on flat, level surface
- Verify wheel condition

---

**Need more help?** See README.md or API_DOCUMENTATION.md

**Last Updated:** January 28, 2026
