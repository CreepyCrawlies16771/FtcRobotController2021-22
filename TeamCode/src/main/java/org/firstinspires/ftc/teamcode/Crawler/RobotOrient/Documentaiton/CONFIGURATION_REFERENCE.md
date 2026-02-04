# Crawler - Configuration Reference

## File Organization

```
AutoEninge/
├── README.md                    # Library overview & features
├── QUICK_START.md              # 5-minute getting started
├── API_DOCUMENTATION.md        # Complete API reference
├── CONFIGURATION_REFERENCE.md  # This file
├── MovementEngine.java         # Core movement engine
├── Robot.java                  # Hardware abstraction
├── AprilTagWebcam.java        # Vision system (Vision/)
├── HeadingTimeline.java        # Path animation
├── AnimationBuilder.java       # Lambda interface
├── Team.java                   # Team enum
├── Tuner.java                  # Configuration tool
├── Sorter.java                 # Ball sorting
├── IndexerRotation.java        # Indexer states
└── BALLCOLOR.java              # Color enum
```

---

## Hardware Configuration (robot_config.xml)

Your `robot_config.xml` must define these devices:

### Motors (DcMotor)

```xml
<!-- Drive Train -->
<Motor name="frontLeft" port="0" />
<Motor name="frontRight" port="1" />
<Motor name="backLeft" port="2" />
<Motor name="backRight" port="3" />

<!-- Shooter -->
<Motor name="leftShoot" port="0" />
<Motor name="rightShoot" port="1" />

<!-- Intake/Indexer -->
<Motor name="gobbler" port="2" />
<Motor name="indexer" port="3" />
```

### Sensors (IMU, Color, etc.)

```xml
<!-- Integrated IMU (Rev Hub) -->
<IMU name="imu" port="0" />

<!-- Color Sensor -->
<ColorSensor name="ballCSensor" port="0" />
```

### Servos

```xml
<!-- Actuator Servo -->
<Servo name="lifter" port="0" />
```

---

## Movement Engine Configuration

### AutoEngineConfig (Inner Class)

Located in MovementEngine.java:

```java
static class AutoEngineConfig {
    // Odometry Wheel Constants
    public static final double ODO_WHEEL_DIAMETER_METERS = 0.048;
    public static final double ENCODER_TICKS_PER_REV = 2000;
    public static final double ODO_WHEEL_CIRCUMFERENCE = ODO_WHEEL_DIAMETER_METERS * Math.PI;
    public static final double TICKS_PER_METER = (ENCODER_TICKS_PER_REV / ODO_WHEEL_CIRCUMFERENCE);
    
    // PID Tuning Constants
    public static double Kp = 0.6;      // Proportional gain
    public static double Kd = 0;        // Derivative gain
    public static double Ki = 0.0015;   // Integral gain
    
    // Strafe-Specific PID
    public static double strafe_Kp = 0;
    public static double strafe_Ki = 0;
    public static double strafe_Kd = 0;
    
    // Steering & Power Constants
    public static final double STEER_P = 0.02;      // Steering proportional gain
    public static final double MIN_POWER = 0.2;     // Minimum motor power threshold
}
```

### Parameter Meanings

| Parameter | Range | Effect | Default |
|-----------|-------|--------|---------|
| `Kp` | 0.1-1.0 | Movement responsiveness | 0.6 |
| `Ki` | 0.0-0.01 | Error accumulation | 0.0015 |
| `Kd` | 0.0-0.5 | Oscillation damping | 0.0 |
| `STEER_P` | 0.01-0.05 | Heading correction | 0.02 |
| `MIN_POWER` | 0.1-0.4 | Minimum motor output | 0.2 |

---

## PID Tuning Guide

### Basic Tuning Process

**Step 1: Start Conservative**
```java
Kp = 0.3;
Ki = 0;
Kd = 0;
```

**Step 2: Increase Kp Until Response**
- Increase by 0.1 each iteration
- Stop when robot moves smoothly
- Watch for oscillation

**Step 3: Add Damping if Oscillating**
```java
Kd = 0.05;  // Start small
```

**Step 4: Add Integral if Error Persists**
```java
Ki = 0.001;  // Very small to start
```

### Common Tuning Scenarios

**Robot Jerky/Oscillating:**
```java
// Reduce responsiveness
Kp = 0.4;
// Add damping
Kd = 0.1;
// Increase MIN_POWER for cleaner stops
MIN_POWER = 0.25;
```

**Robot Undershoots (Stops Before Target):**
```java
// Increase responsiveness
Kp = 0.8;
// Increase minimum power
MIN_POWER = 0.3;
// Add integral term
Ki = 0.002;
```

**Robot Overshoots (Goes Past Target):**
```java
// Reduce responsiveness
Kp = 0.4;
// Add damping
Kd = 0.1;
```

**Movement Too Slow:**
```java
// Increase gains (higher power output)
Kp = 0.8;
// Increase minimum power
MIN_POWER = 0.3;
```

**Movement Stutters:**
```java
// Increase minimum power
MIN_POWER = 0.25;
```

---

## Odometry Calibration

### Dead Wheel Setup

Three wheel odometry uses:
- **Left encoder**: Parallel (Y-axis tracking)
- **Right encoder**: Parallel (Y-axis tracking)
- **Center encoder**: Perpendicular (X-axis strafe tracking)

### Calibration Values

```java
// Wheel diameter in meters (measure your wheels)
public static final double ODO_WHEEL_DIAMETER_METERS = 0.048;

// Encoder resolution (from motor datasheet)
public static final double ENCODER_TICKS_PER_REV = 2000;

// This is auto-calculated:
public static final double TICKS_PER_METER = 
    (ENCODER_TICKS_PER_REV / (ODO_WHEEL_DIAMETER_METERS * Math.PI));
```

### Step-by-Step Calibration

**Measuring Wheel Diameter:**
1. Mark a point on wheel
2. Roll wheel exactly one rotation
3. Measure distance traveled
4. That distance ÷ π = wheel diameter

**Encoder Ticks Per Revolution:**
- Check motor datasheet (typically 1440 or 2000 for Core Hex)
- Or: Rotate motor until it clicks 1 revolution, count encoder ticks

**Calibrating Movement:**
1. Reset encoders: `resetOdometry()`
2. Drive straight 10 feet (3.048m)
3. Measure actual distance traveled
4. Calculate new TICKS_PER_METER:
   ```
   new TICKS_PER_METER = (measured_ticks / 3.048)
   ```

### Example Calibration

Drive 10 feet test result: 
- Left encoder: 123,500 ticks
- Right encoder: 123,400 ticks
- Average: 123,450 ticks
- Calculation: 123,450 / 3.048 = 40,479 TICKS_PER_METER

---

## Team Configuration

### Defining Team

```java
public enum Team {
    RED(24),      // Red team April Tag ID
    BLUE(20)      // Blue team April Tag ID
}
```

Usage in code:
```java
moveToShoot(Team.RED);   // Use red team tag (24)
moveToShoot(Team.BLUE);  // Use blue team tag (20)
```

### April Tag IDs for DECODE

- **Team Indicators**: IDs 20 (Blue), 24 (Red)
- **Field Markers**: IDs 1-8 (various field locations)
- **Obelisk Tags**: Special markers (not for localization)

---

## Coordinate System

### Movement Conventions

```
        +Y (Forward)
         ↑
         |
-X ←-----+-----→ +X (Right Strafe)
         |
         ↓
        -Y (Backward)
```

### Heading Convention

```
0°   = Facing forward (+Y direction)
90°  = Facing right (+X direction)
180° = Facing backward (-Y direction)
-90° = Facing left (-X direction)
```

### Example Movements

```java
drivePID(1.0, 0);      // 1 meter forward, facing 0°
drivePID(-0.5, 90);    // 0.5 meter backward, facing 90°
strafePID(0.5, 0);     // Strafe 0.5 meter right, face 0°
strafePID(-0.25, 45);  // Strafe 0.25 meter left, face 45°
```

---

## Motor Configuration

### Direction Setup

In `setMotorBehavior()`:
```java
// Front motors face forward
frontLeft.setDirection(DcMotor.Direction.FORWARD);
frontRight.setDirection(DcMotor.Direction.FORWARD);

// Back motors reversed (coordinate system convention)
backLeft.setDirection(DcMotor.Direction.REVERSE);
backRight.setDirection(DcMotor.Direction.REVERSE);
```

### Shooter Motors

```java
shooterLeft.setDirection(DcMotorSimple.Direction.REVERSE);
shooterRight.setDirection(DcMotorSimple.Direction.FORWARD);
```

### Gobbler (Intake)

```java
gobbler.setDirection(DcMotorSimple.Direction.REVERSE);
```

### Brake Behavior

```java
// All motors brake when power set to 0
motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
```

---

## Vision Configuration

### AprilTagWebcam Setup

Camera must be:
- Focused for ~2 meter range
- Mounted perpendicular to floor
- Calibrated for your phone/camera model
- Oriented with known rotation

### Camera Mount Orientation

Specify in AprilTagWebcam initialization:
```java
CameraDevice.RelativeCameraDirection CAMERA_ORIENTATION = 
    CameraDevice.RelativeCameraDirection.FRONT;  // or BACK
```

### Rotation Compensation

```java
public enum Rotation {
    RANGE,       // Distance to tag (meters)
    YAW,         // Rotational offset from camera
    BEARING      // Direction/angle to tag
}
```

---

## Performance Tuning

### For Faster Movement

Increase power:
```java
// In arc() or other methods
double maxPower = 1.0;  // Use full motor power
```

Reduce accuracy requirements:
```java
// Increase error threshold for stopping
if (Math.abs(error) > 100) continue;  // Stop at 100 ticks
```

### For More Accurate Movement

Decrease MIN_POWER:
```java
MIN_POWER = 0.15;  // More precise low-power control
```

Increase PID gains:
```java
Kp = 0.8;  // Faster response
Kd = 0.1;  // More damping
```

### For Reliable Vision-Guided Movement

Test multiple times:
- Run moveToShoot() 3+ times to verify consistency
- Check if tag detection varies

Ensure adequate lighting:
- Test in competition lighting conditions
- Add supplemental lighting if needed

---

## Troubleshooting Configuration

### Motors Don't Move
- [ ] Verify motor names match robot_config.xml
- [ ] Check motor directions in setMotorBehavior()
- [ ] Verify power values aren't zero
- [ ] Check physical motor connections

### Odometry Inaccurate
- [ ] Recalibrate TICKS_PER_METER
- [ ] Verify wheel diameter measurement
- [ ] Check encoder connections
- [ ] Test on flat, level surface

### Vision Not Working
- [ ] Verify camera is in hardwareMap
- [ ] Check camera calibration
- [ ] Test April tag visibility in lighting
- [ ] Verify team tag IDs (20/24 for DECODE)

### Movement Jerky
- [ ] Reduce Kp (try 0.4)
- [ ] Increase Kd (try 0.1)
- [ ] Check battery voltage
- [ ] Verify MIN_POWER setting

---

## Performance Specifications

### Movement Speeds

| Operation | Typical Speed | Range |
|-----------|--------------|-------|
| drivePID | 0.6 m/s | 0.3-0.8 |
| strafePID | 0.4 m/s | 0.2-0.6 |
| turnPID | 90°/sec | 30-180° per second |
| arc | 0.8 m/s | 0.3-1.0 |

### Accuracy

| Measurement | Typical | Best | Notes |
|------------|---------|------|-------|
| Distance | ±5cm | ±2cm | Depends on odometry |
| Heading | ±3° | ±1° | Depends on IMU calibration |
| April Tag Align | ±2° | ±1° | Depends on camera FOV |

### Odometry Drift

- **Short paths** (< 3m): < 2% error
- **Long paths** (5-10m): 3-5% error
- **After rotation**: ±5° heading drift

---

## Safety Checklist

- [ ] Test movements in safe area first
- [ ] Verify all motor directions correct
- [ ] Check for cable/wire interference
- [ ] Ensure adequate space for testing
- [ ] Have E-stop button readily available
- [ ] Test with ball/game elements if used
- [ ] Verify shooter safety mechanisms
- [ ] Check for tip-over risks on high speeds

---

**Last Updated:** January 28, 2026
