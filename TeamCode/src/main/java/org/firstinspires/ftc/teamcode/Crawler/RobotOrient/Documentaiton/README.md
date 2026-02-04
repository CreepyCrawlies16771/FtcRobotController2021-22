# Crawler - Autonomous Engine Library

**Version:** 1.0.0  
**Team:** Creepy Crawlies FTC 16771  
**Season:** DECODE (2025-2026)

## Overview

Crawler is a comprehensive autonomous navigation and control library for FTC robots. It provides:

- **Advanced Movement Control**: PID-based driving with precise odometry integration
- **Vision-Guided Navigation**: April Tag detection and positioning for target alignment
- **Path Animation**: Dynamic heading control through motion trajectories
- **Robot Abstraction**: Clean hardware interface with support for drive trains and actuators
- **Modular Architecture**: Extensible design for custom autonomous routines

## Core Features

### 1. **MovementEngine** - Advanced Autonomous Movement
- PID-controlled driving (distance and angle)
- Strafe movement with gyroscope stabilization
- Turn-in-place rotation with heading control
- Arc movements with dynamic heading timeline
- April Tag-based targeting and alignment
- Dead-wheel odometry support

### 2. **Robot** - Hardware Abstraction Layer
- Unified drivetrain control (Mecanum wheels)
- Shooter system management (dual motors)
- Intake/Indexer mechanisms
- Color sensor integration for sorting
- IMU-based heading tracking
- Servo-based actuator support

### 3. **AprilTagWebcam** - Vision System
- April Tag detection and tracking
- Distance and bearing calculations
- Team-specific tag identification (Red/Blue)
- Calibrated camera rotation compensation

### 4. **HeadingTimeline** - Path Animation Engine
- Keyframe-based heading interpolation
- Progress-based trajectory control
- Smooth angle wrapping and transitions
- Lambda expression support for path definition

## Package Structure

```
org.firstinspires.ftc.teamcode.AutoEninge/
├── MovementEngine.java         # Core autonomous engine with PID control
├── Robot.java                  # Hardware abstraction layer
├── Team.java                   # Team enum (RED/BLUE with April Tag IDs)
├── HeadingTimeline.java        # Path animation and heading interpolation
├── AnimationBuilder.java       # Functional interface for path builders
├── Tuner.java                  # Configuration tuning utility
├── Sorter.java                 # Ball sorting mechanism controller
├── BALLCOLOR.java              # Ball color enumeration
├── IndexerRotation.java        # Indexer state management
└── [Legacy classes]
```

## Quick Start

### Basic Autonomous Routine

```java
public class BasicAuto extends MovementEngine {
    @Override
    public void runPath() {
        // Drive straight 24 inches forward
        drivePID(0.24, 0);
        
        // Turn 90 degrees right
        turnPID(90);
        
        // Drive forward with heading control
        drivePID(0.36, 90);
    }
}
```

### April Tag Targeting

```java
public class TargetAuto extends MovementEngine {
    @Override
    public void runPath() {
        // Navigate to and align with April Tag for shooting
        moveToShoot(Team.BLUE);
        
        // Fire shooter
        // (Implement shooter control here)
    }
}
```

### Arc Movement with Heading Animation

```java
public void executeArcShot() {
    // Drive 48 inches while smoothly rotating from 0° to 90°
    arc(0.48, 0.8, timeline -> {
        timeline.at(0.0, 0)      // Start heading: 0°
                .at(0.5, 45)     // Midpoint: 45°
                .at(1.0, 90);    // End heading: 90°
    });
}
```

## Configuration

### PID Tuning

Located in `AutoEngineConfig`:

```java
public static double Kp = 0.6;      // Proportional gain
public static double Kd = 0;        // Derivative gain
public static double Ki = 0.0015;   // Integral gain
public static double STEER_P = 0.02; // Steering proportional
public static final double MIN_POWER = 0.2;
```

### Hardware Constants

```java
public static final double ODO_WHEEL_DIAMETER_METERS = 0.048;
public static final double ENCODER_TICKS_PER_REV = 2000;
public static final double TICKS_PER_METER = 40691.65;
```

## API Reference

### MovementEngine

#### Movement Methods

```java
// Drive specified distance at target angle
void drivePID(double targetMeters, int targetAngle)

// Strafe (horizontal slide) with gyro stabilization
void strafePID(double targetMeters, int targetAngle)

// Turn to absolute heading
void turnPID(int targetAngle)

// Arc movement with heading timeline
void arc(double meters, double maxPower, AnimationBuilder animator)

// Navigate to and align with April Tag target
void moveToShoot(Team team)
```

#### Utility Methods

```java
// Stop all motors
void stopRobot()

// Reset odometry encoders to zero
void resetOdometry()

// Configure motor behavior (braking, direction)
void setMotorBehavior()

// Normalize angles to ±180°
double angleWrap(double degrees)

// Apply drive power with steering correction
void applyDrivePower(double p, double s)

// Apply strafe power (Mecanum pattern)
void applyStrafePower(double strafe, double steer)

// Update telemetry with current state
void findData(Team team)
```

### Robot

```java
// Drive with mecanum kinematics (forward, strafe, rotate)
void drive(double forward, double strafe, double rotate)

// Activate/deactivate shooter motors
void activateShooters(boolean stop)

// Control gobbler intake
void activateGobbler(boolean gooble)

// Direct motor power control
void powerDriveTrain(double fl, double fr, double bl, double br)

// Get current IMU heading
double getHeading()
```

### HeadingTimeline

```java
// Add keyframe at progress percentage (0.0-1.0)
HeadingTimeline at(double percentage, double heading)

// Get interpolated heading at current progress
double getTarget(double progress, double startHeading)
```

## Coordinate System

- **X-axis**: Forward/backward movement (positive = forward)
- **Y-axis**: Left/right strafe (positive = right/strafe)
- **Heading**: Degrees, where 0° = forward, 90° = right turn

## Odometry Tuning

The engine uses three-wheel dead-wheel odometry:

1. **Left encoder**: Parallel tracking, measures Y movement
2. **Right encoder**: Parallel tracking, measures Y movement
3. **Center encoder**: Perpendicular tracking, measures X strafe

### Calibration Procedure

1. Drive robot straight 10 feet
2. Measure actual distance traveled
3. Adjust `TICKS_PER_METER` ratio accordingly
4. Test circular path to verify rotation accuracy

## Common Issues and Solutions

### Issue: Robot overshoots targets
**Solution**: Reduce `Kp` value or increase `MIN_POWER` threshold

### Issue: Jerky movement
**Solution**: Enable integral control by increasing `Ki` value

### Issue: Odometry drift
**Solution**: Calibrate encoder ratios, check dead wheel alignment

### Issue: April Tag detection fails
**Solution**: Verify camera calibration, check lighting conditions, ensure tags are in view

## Migration to Pure Pursuit (Future)

The library is designed to be compatible with FTCLib's Pure Pursuit path following. See `PURE_PURSUIT_MIGRATION.md` for details.

## Best Practices

1. **Always test movements in safe area first**
   - Start with small distances (6-12 inches)
   - Verify angle calculations with gyroscope
   - Check odometry accuracy before main runs

2. **Use telemetry extensively**
   ```java
   telemetry.addData("Current Pos", currentPos);
   telemetry.addData("Error", error);
   telemetry.addData("Power", power);
   telemetry.update();
   ```

3. **Keep PID gains conservative**
   - Default Kp=0.6 works well for most drives
   - Increase Ki only if constant error persists
   - Kd rarely needed unless oscillation occurs

4. **Test vision-guided moves multiple times**
   - April Tag detection has environmental variability
   - Verify tag presence before starting moveToShoot()
   - Have fallback timeout to prevent hanging

5. **Document custom autonomous routines**
   - Record waypoint coordinates
   - Note any special considerations
   - Include team calibration data

## Testing Checklist

- [ ] Odometry calibration verified (straight line test)
- [ ] IMU heading tracking accurate
- [ ] PID gains tuned for smooth motion
- [ ] April Tags detected reliably
- [ ] Shooters activate at correct power
- [ ] All movements tested in isolation
- [ ] Full autonomous routine tested 3+ times
- [ ] Fallback behaviors implemented for edge cases

## Extending the Library

### Creating a Custom Movement

```java
public void customMovement() {
    // Use low-level building blocks
    resetOdometry();
    
    // Complex multi-step movement
    while (opModeIsActive() && !reachedTarget()) {
        double error = calculateError();
        double power = Kp * error;
        applyDrivePower(power, steer);
    }
    
    stopRobot();
}
```

### Custom Autonomous Routine

```java
public class MyAuto extends MovementEngine {
    @Override
    public void runPath() {
        // Your autonomous sequence here
        telemetry.addLine("Starting custom autonomous...");
        telemetry.update();
        
        // Implement your path logic
    }
}
```

## Support & Troubleshooting

For issues or questions:

1. Check telemetry output for error messages
2. Verify hardware connections in robot configuration
3. Review PID tuning parameters
4. Check odometry calibration
5. Test individual movements in isolation

## Version History

### v1.0.0 (January 2026)
- Initial library release
- PID-based movement system
- April Tag vision integration
- Odometry support
- HeadingTimeline animation engine
- Full documentation

## License

BSD 3-Clause License - See LICENSE file in repository

---

**Last Updated:** January 28, 2026  
**Maintainer:** Creepy Crawlies Robotics  
**Contact:** See team repository for details
