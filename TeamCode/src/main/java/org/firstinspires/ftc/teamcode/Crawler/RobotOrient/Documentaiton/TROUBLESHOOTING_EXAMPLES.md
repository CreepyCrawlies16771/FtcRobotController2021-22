# Crawler - Troubleshooting & Examples

## Table of Contents

1. [Common Issues](#common-issues)
2. [Code Examples](#code-examples)
3. [Testing Procedures](#testing-procedures)
4. [Performance Optimization](#performance-optimization)

---

## Common Issues

### Issue 1: Robot Drives Too Far or Too Short

**Symptoms:**
- Move 1 meter forward, robot only moves 0.8 meters
- Move 2 meters backward, robot moves 2.3 meters

**Root Causes:**
1. Incorrect TICKS_PER_METER calibration
2. Encoder slipping or disconnected
3. Wheel wear affecting diameter
4. Uneven floor causing drag

**Diagnosis:**
```java
@Autonomous(name="Diagnostic - Measure Distance")
public class DiagnosticDistance extends MovementEngine {
    @Override
    public void runPath() {
        // Drive exactly 1 meter, measure actual distance
        resetOdometry();
        drivePID(1.0, 0);
        
        telemetry.addData("Left Encoder Ticks", leftOdo.getCurrentPosition());
        telemetry.addData("Right Encoder Ticks", rightOdo.getCurrentPosition());
        telemetry.addData("Measured Ticks", (leftOdo.getCurrentPosition() + rightOdo.getCurrentPosition()) / 2.0);
        telemetry.update();
        sleep(5000);
    }
}
```

**Solution:**
Recalibrate TICKS_PER_METER:
```java
// If you moved 1 meter and got 123,450 ticks average:
public static final double TICKS_PER_METER = 123450;  // Update this
```

### Issue 2: Robot Spins Out or Can't Turn

**Symptoms:**
- turnPID(90) causes jerky or incomplete rotation
- Robot turns too fast and overshoots
- Robot won't turn at all

**Root Causes:**
1. IMU not calibrated or drifting
2. PID gains too aggressive
3. Gyroscope interference from motors
4. Battery voltage too low

**Diagnosis:**
```java
@Autonomous(name="Diagnostic - IMU Heading")
public class DiagnosticIMU extends MovementEngine {
    @Override
    public void runPath() {
        for (int i = 0; i < 10; i++) {
            double heading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            telemetry.addData("IMU Heading", heading);
            telemetry.update();
            sleep(500);
        }
    }
}
```

**Solution:**

If heading is drifting:
```java
// Reduce turn speed
Kp = 0.02;  // Much lower for turns

// Or increase damping
Kd = 0.05;
```

Recalibrate IMU in robot settings.

### Issue 3: April Tag Not Detecting

**Symptoms:**
- moveToShoot() does nothing
- Telemetry shows "No tags detected"
- Tags are in view but not recognized

**Root Causes:**
1. Camera not focused at tag distance
2. Insufficient lighting
3. Camera not calibrated for phone
4. April Tag orientation wrong
5. Tag outside camera field of view

**Diagnosis:**
```java
@Autonomous(name="Diagnostic - Vision Debug")
public class DiagnosticVision extends MovementEngine {
    @Override
    public void runPath() {
        aprilTagWebcam.update();
        
        List<AprilTagDetection> detections = aprilTagWebcam.getDetectedTags();
        
        if (detections == null || detections.isEmpty()) {
            telemetry.addLine("NO TAGS DETECTED!");
        } else {
            for (AprilTagDetection tag : detections) {
                telemetry.addData("Tag ID", tag.id);
                telemetry.addData("Distance", aprilTagWebcam.getAngle(tag, Rotation.RANGE));
            }
        }
        telemetry.update();
    }
}
```

**Solutions:**

*Camera not focused:*
- Adjust camera focus manually
- Move closer/farther to find focus distance
- Mark focus distance with tape

*Poor lighting:*
- Add supplemental lighting
- Test in match lighting conditions
- Clean camera lens

*Camera calibration:*
- Recalibrate camera in FTC dashboard
- Use known resolution (640x480 or 320x240)

### Issue 4: Movement Jerky or Stuttering

**Symptoms:**
- Robot motion is jerky/stuttered, not smooth
- Speed constantly changes
- Movement seems to pulse

**Root Causes:**
1. MIN_POWER too high
2. PID gains causing oscillation
3. Battery running low
4. Motor thermal protection engaging

**Diagnosis:**
```java
@Autonomous(name="Diagnostic - Motor Power")
public class DiagnosticMotors extends MovementEngine {
    @Override
    public void runPath() {
        resetOdometry();
        
        while (opModeIsActive()) {
            double pos = (leftOdo.getCurrentPosition() + rightOdo.getCurrentPosition()) / 2.0;
            telemetry.addData("Position", pos);
            telemetry.addData("Error", (1.0 * TICKS_PER_METER) - pos);
            telemetry.update();
            
            // Give it 1 second then stop
            if (getRuntime() > 1.0) break;
        }
    }
}
```

**Solutions:**

*High MIN_POWER:*
```java
// Reduce minimum power
MIN_POWER = 0.15;  // Instead of 0.2
```

*PID oscillation:*
```java
// Reduce Kp
Kp = 0.4;

// Add damping
Kd = 0.05;
```

*Low battery:*
- Charge battery fully
- Check battery connector

### Issue 5: Odometry Drifts Over Time

**Symptoms:**
- After driving 10 feet, robot is off by 2+ feet
- Robot doesn't return to starting position after square path
- Heading keeps drifting

**Root Causes:**
1. Encoder wheel slippage
2. TICKS_PER_METER calibration error
3. Wheel misalignment
4. Encoder mounting loose

**Diagnosis:**
```java
@Autonomous(name="Diagnostic - Square Path")
public class DiagnosticDrift extends MovementEngine {
    @Override
    public void runPath() {
        // Drive a square and return to start
        for (int i = 0; i < 4; i++) {
            drivePID(1.0, 0);
            turnPID(90);
        }
        
        telemetry.addLine("Robot should be back at start, heading 0°");
        telemetry.addData("IMU Heading", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
        telemetry.update();
        sleep(5000);
    }
}
```

**Solutions:**

*Check wheel condition:*
- Look for flat spots on wheels
- Check for rubber deterioration
- Replace worn wheels

*Recalibrate encoders:*
- Measure 10-foot straight line
- Calculate new TICKS_PER_METER
- Test again

*Check encoder mounting:*
- Verify encoders aren't slipping on motor shafts
- Tighten set screws if loose
- Use blue threadlocker

---

## Code Examples

### Example 1: Simple Drive Forward

```java
@Autonomous(name="Example 1 - Drive Forward", group="Examples")
public class Example1Simple extends MovementEngine {
    @Override
    public void runPath() {
        telemetry.addLine("Starting: Drive 1 meter forward");
        telemetry.update();
        
        // Drive 1 meter (100cm) forward
        drivePID(1.0, 0);
        
        telemetry.addLine("Complete!");
        telemetry.update();
        sleep(2000);
    }
}
```

### Example 2: Navigate with Multiple Turns

```java
@Autonomous(name="Example 2 - Multi-Turn Path", group="Examples")
public class Example2MultiTurn extends MovementEngine {
    @Override
    public void runPath() {
        // Move in an "L" shape
        drivePID(1.0, 0);      // Forward 1 meter
        turnPID(90);            // Turn right
        drivePID(0.5, 90);     // Forward 0.5 meter facing right
        
        // Now make an S-shape back
        turnPID(-90);           // Turn left (back to forward)
        drivePID(-0.75, 0);    // Back up 0.75 meter
    }
}
```

### Example 3: Strafe and Turn

```java
@Autonomous(name="Example 3 - Strafe Path", group="Examples")
public class Example3Strafe extends MovementEngine {
    @Override
    public void runPath() {
        // Move left while turning
        turnPID(90);            // Face right direction
        strafePID(1.0, 90);    // Slide 1 meter right
        
        // Return to center
        strafePID(-1.0, 90);   // Slide 1 meter left
        turnPID(0);             // Face forward
    }
}
```

### Example 4: Curved Path with Animation

```java
@Autonomous(name="Example 4 - Curved S-Path", group="Examples")
public class Example4Curved extends MovementEngine {
    @Override
    public void runPath() {
        // Smooth S-curve: 
        // Start forward, curve right, end facing right
        arc(2.0, 0.8, timeline -> {
            timeline.at(0.0, 0)      // Start facing forward
                    .at(0.25, 22.5)  
                    .at(0.5, 45)     // Midpoint
                    .at(0.75, 67.5)  
                    .at(1.0, 90);    // End facing right
        });
    }
}
```

### Example 5: April Tag Targeting

```java
@Autonomous(name="Example 5 - Tag Targeting", group="Examples")
public class Example5Targeting extends MovementEngine {
    @Override
    public void runPath() {
        telemetry.addLine("Targeting April Tag (BLUE team)");
        telemetry.update();
        
        // Rough navigation
        drivePID(0.5, 0);
        
        // Fine alignment using vision
        moveToShoot(Team.BLUE);
        
        telemetry.addLine("Aligned! Ready to shoot.");
        telemetry.update();
        sleep(2000);
    }
}
```

### Example 6: Intake and Shoot Sequence

```java
@Autonomous(name="Example 6 - Intake & Shoot", group="Examples")
public class Example6IntakeShoot extends MovementEngine {
    
    private Robot robot;
    
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Robot(hardwareMap);
        super.runOpMode();
    }
    
    @Override
    public void runPath() {
        // Stage 1: Collect ball
        telemetry.addLine("Stage 1: Collecting ball");
        telemetry.update();
        
        drivePID(0.5, 0);                    // Drive to ball
        robot.activateGobbler(true);         // Start intake
        sleep(500);                          // Wait for intake
        robot.activateGobbler(false);        // Stop intake
        
        // Stage 2: Move to shooting area
        telemetry.addLine("Stage 2: Moving to shooting area");
        telemetry.update();
        
        turnPID(45);                         // Turn toward goal
        drivePID(1.0, 45);                  // Drive to goal area
        
        // Stage 3: Align and shoot
        telemetry.addLine("Stage 3: Aligning and shooting");
        telemetry.update();
        
        moveToShoot(Team.RED);               // Align with April Tag
        
        // Shoot the ball
        robot.activateShooters(false);       // Start shooters
        sleep(2000);                         // Let them spin up
        
        robot.activateGobbler(true);         // Feed ball
        sleep(100);                          // Quick pulse
        robot.activateGobbler(false);        // Stop feeder
        
        robot.activateShooters(true);        // Stop shooters
        
        telemetry.addLine("Complete!");
        telemetry.update();
    }
}
```

### Example 7: Multiple Balls / Multi-Stage

```java
@Autonomous(name="Example 7 - Multi-Ball", group="Examples")
public class Example7MultiBall extends MovementEngine {
    
    private Robot robot;
    
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Robot(hardwareMap);
        super.runOpMode();
    }
    
    @Override
    public void runPath() {
        // Collect 3 balls and shoot them
        for (int i = 0; i < 3; i++) {
            telemetry.addData("Ball", i + 1);
            telemetry.update();
            
            collectBall();
            shootBall();
        }
        
        telemetry.addLine("All balls shot!");
        telemetry.update();
    }
    
    private void collectBall() {
        drivePID(0.5, 0);
        robot.activateGobbler(true);
        sleep(500);
        robot.activateGobbler(false);
        drivePID(-0.5, 0);  // Back up
    }
    
    private void shootBall() {
        moveToShoot(Team.RED);
        robot.activateShooters(false);
        sleep(1000);
        robot.activateGobbler(true);
        sleep(100);
        robot.activateGobbler(false);
        robot.activateShooters(true);
        sleep(500);  // Cool down before next ball
    }
}
```

---

## Testing Procedures

### Procedure 1: Odometry Calibration (10 minutes)

1. **Reset and mark start:**
   ```java
   resetOdometry();
   // Mark robot position with tape
   ```

2. **Drive exactly 10 feet:**
   ```java
   drivePID(3.048, 0);  // 10 feet in meters
   ```

3. **Record encoder values:**
   - Check telemetry for encoder positions
   - Note left and right encoder values

4. **Calculate correction:**
   ```
   measured_ticks = (left_ticks + right_ticks) / 2
   new_TICKS_PER_METER = measured_ticks / 3.048
   ```

5. **Update code and re-test**

### Procedure 2: IMU Heading Calibration (5 minutes)

```java
@Autonomous(name="Test - IMU Calibration")
public class TestIMU extends MovementEngine {
    @Override
    public void runPath() {
        for (int angle = 0; angle <= 360; angle += 45) {
            turnPID(angle);
            
            double actualHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            telemetry.addData("Target", angle);
            telemetry.addData("Actual", actualHeading);
            telemetry.addData("Error", angle - actualHeading);
            telemetry.update();
            sleep(1000);
        }
    }
}
```

**Expected Results:** Error should be < ±3 degrees

### Procedure 3: Vision System Test (10 minutes)

1. **Place known April Tag in view**
2. **Run detection OpMode:**
   ```java
   @Autonomous(name="Test - Vision")
   public class TestVision extends MovementEngine {
       @Override
       public void runPath() {
           aprilTagWebcam.update();
           AprilTagDetection tag = aprilTagWebcam.getTagBySpecificId(20);
           
           if (tag != null) {
               double distance = aprilTagWebcam.getAngle(tag, Rotation.RANGE);
               double bearing = aprilTagWebcam.getAngle(tag, Rotation.BEARING);
               telemetry.addData("Tag Found!", true);
               telemetry.addData("Distance (m)", distance);
               telemetry.addData("Bearing (°)", bearing);
           } else {
               telemetry.addLine("NO TAG DETECTED");
           }
           telemetry.update();
           sleep(5000);
       }
   }
   ```

3. **Move tag closer/farther, verify distance changes**

### Procedure 4: Movement Accuracy Test (15 minutes)

```java
@Autonomous(name="Test - Movement Accuracy")
public class TestAccuracy extends MovementEngine {
    @Override
    public void runPath() {
        // Test drive accuracy
        testDrive("1 meter forward", 1.0, 0);
        testDrive("1 meter left", 0, -1.0);
        testDrive("1 meter backward", -1.0, 0);
        testDrive("1 meter right", 0, 1.0);
    }
    
    private void testDrive(String label, double drive, double strafe) {
        telemetry.addLine("Testing: " + label);
        telemetry.addLine("Mark position, then press START");
        telemetry.update();
        sleep(3000);
        
        if (drive != 0) {
            drivePID(drive, 0);
        } else {
            strafePID(strafe, 0);
        }
        
        telemetry.addLine("Movement complete. Measure distance.");
        telemetry.update();
        sleep(5000);
    }
}
```

---

## Performance Optimization

### For Speed

**Increase power output:**
```java
// In arc() or movement methods
double maxPower = 0.9;  // Instead of default
```

**Reduce accuracy requirements:**
```java
// In drivePID loop
if (Math.abs(error) > 150) continue;  // Stop at 150 ticks instead of 50
```

**Reduce number of waypoints:**
- Combine multiple small movements into one longer movement
- Use arc() instead of turn + drive sequences

### For Accuracy

**Increase error threshold:**
```java
// In drivePID
if (Math.abs(error) > 20) continue;  // Very strict
```

**Add extra PID tuning:**
```java
Kp = 0.8;  // Higher responsiveness
Kd = 0.1;  // Damping to prevent overshoot
Ki = 0.002; // Integral for final push
```

**Use vision for final alignment:**
- Rough movement with odometry
- Fine alignment with April Tags

### For Reliability

**Add timeouts:**
```java
ElapsedTime timeout = new ElapsedTime();
while (opModeIsActive() && Math.abs(error) > 50) {
    // ... movement code ...
    if (timeout.seconds() > 5) break;  // Prevent hanging
}
```

**Add fallback behaviors:**
```java
moveToShoot(Team.BLUE);
if (aprilTagWebcam.getDetectedTags() == null) {
    // Fallback if tag not found
    turnPID(45);
    telemetry.addLine("Tag not found, using fallback");
}
```

---

## Debugging Checklist

Before match/competition:

- [ ] Odometry calibrated (< 2% error)
- [ ] IMU heading stable (< 3° drift)
- [ ] Vision system detects tags reliably
- [ ] All movements tested 3+ times
- [ ] Motor directions verified
- [ ] PID gains optimized for conditions
- [ ] Battery fully charged
- [ ] Robot not physically damaged
- [ ] All cables connected and secured
- [ ] Backup plan if vision fails
- [ ] Telemetry configured for live viewing

---

**Last Updated:** January 28, 2026
