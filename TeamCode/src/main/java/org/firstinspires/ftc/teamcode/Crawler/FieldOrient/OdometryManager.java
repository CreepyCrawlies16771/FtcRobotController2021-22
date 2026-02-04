package org.firstinspires.ftc.teamcode.Crawler.FieldOrient;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Safe wrapper for Odometry initialization.
 * Use this to call Odometry without null/initialization errors.
 */
public class OdometryManager {
    private Odometry odometry;

    /**
     * Initialize odometry safely.
     * @param hwMap The HardwareMap from OpMode
     * @return true if successful, false otherwise
     */
    public boolean init(HardwareMap hwMap) {
        try {
            if (hwMap == null) {
                throw new IllegalArgumentException("HardwareMap cannot be null");
            }
            odometry = new Odometry(hwMap);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get the odometry instance (null if not initialized).
     */
    public Odometry getOdometry() {
        return odometry;
    }

    /**
     * Check if odometry is initialized.
     */
    public boolean isInitialized() {
        return odometry != null;
    }
}
