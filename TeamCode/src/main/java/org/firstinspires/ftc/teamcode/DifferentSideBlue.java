package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

import java.util.Locale;

/**
 * Created by Athira on 1/7/2018.
 */

@Autonomous(name = "DifferentSideBlue", group = "Qualifier")
public class DifferentSideBlue extends LinearOpMode{
    //Hardware
    RobotHardware robot = new RobotHardware();
    RobotControl robotControl;
    SensorControl sensorControl;

    // State used for updating telemetry
    Orientation angles;
    Acceleration gravity;

    private ElapsedTime runtime = new ElapsedTime();
    final double DRIVE_SPEED = 0.6;
    final double ALIGN_SPEED = 0.3;
    final double TURN_SPEED = 0.3;
    static final double UP_LIFT_SPEED = 0.5;
    static final double DOWN_LIFT_SPEED = 0.2;
    final double COUNTS_PER_MOTOR_REV = 1120;    // eg: TETRIX Motor Encoder
    final double DRIVE_GEAR_REDUCTION = 1.0;     // This is < 1.0 if geared UP
    final double WHEEL_DIAMETER_INCHES = 4.0;     // For figuring circumference
    final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415);

    final String myColor = "BLUE";

    //Other Variables
    double forwardDistance = 24;
    double distanceToLeftWall;

    String vuMark = "CENTER";

    public void runOpMode() {

        /*
         * Initialize the drive system variables.
         * The init() method of the hardware class does all the work here
         */
        robot.init(hardwareMap);

        //Ready to run
        //telemetry.log().clear();
        telemetry.addData("Status", "Ready to run");
        telemetry.update();

        //Wait for start
        waitForStart();

        //Adding the robotControls
        robotControl = new RobotControl(hardwareMap, robot);
        sensorControl = new SensorControl(hardwareMap, robot);

        runtime.reset();

        smacker();

        vuMarkAndSetParameters();


        //Grab the glyph
        robotControl.closeHand2();
        sleep(100);
        runtime.reset();

        while (runtime.seconds() < 1)
        {
            robotControl.liftUp(UP_LIFT_SPEED);
        }
        robotControl.liftStop();


        runtime.reset();
        robotControl.changeToEncoderMode();
        encoderDrive(DRIVE_SPEED, forwardDistance, forwardDistance, 5.0);  // S1: Forward 12 Inches with 5 Sec timeout
        sleep(500);


        sideWaysAlign();

        placeGlyphAndPark();


    }


    /**
     * ENCODER METHOD!
     *
     * @param speed
     * @param leftInches
     * @param rightInches
     * @param timeoutS
     */
    public void encoderDrive(double speed,
                             double leftInches, double rightInches,
                             double timeoutS) {
        int newBackLeftTarget;
        int newBackRightTarget;
        int newFrontLeftTarget;
        int newFrontRightTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            newBackLeftTarget = robotControl.getBackLeftWheelCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH);
            newBackRightTarget = robotControl.getBackRightWheelCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH);
            newFrontLeftTarget = robotControl.getFrontLeftWheelCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH);
            newFrontRightTarget = robotControl.getFrontRightWheelCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH);


            robotControl.setBackLeftWheelTargetPosition(newBackLeftTarget);
            robotControl.setBackRightWheelTargetPosition(newBackRightTarget);
            robotControl.setFrontLeftWheelPosition(newFrontLeftTarget);
            robotControl.setFrontRightWeelPosition(newFrontRightTarget);

            // Turn On RUN_TO_POSITION
            robotControl.setMode(DcMotor.RunMode.RUN_TO_POSITION);


            // reset the timeout time and start motion.
            runtime.reset();
            robotControl.setBackLeftWheelPower(Math.abs(speed));
            robotControl.setBackRightWheelPower(Math.abs(speed));
            robotControl.setFrontLeftWheelPower(Math.abs(speed));
            robotControl.setFrontRightWheelPower(Math.abs(speed));


            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (robotControl.isBackLeftWheelBusy() && robotControl.isBackRightWheelBusy() &&
                            robotControl.isFrontLeftWheelBusy() && robotControl.isFrontRightWheelBusy())) {

                // Display it for the driver.
                telemetry.addData("Path1", "Running to %7d :%7d : %7d :%7d",
                        newBackLeftTarget, newBackRightTarget, newFrontLeftTarget, newFrontRightTarget);
                telemetry.addData("Path2", "Running at %7d :%7d : %7d : %7d",
                        robotControl.getBackLeftWheelCurrentPosition(),
                        robotControl.getBackRightWheelCurrentPosition(),
                        robotControl.getFrontLeftWheelCurrentPosition(),
                        robotControl.getFrontRightWheelCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            robotControl.stopMoving();

            // Turn off RUN_TO_POSITION
            robotControl.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            //  sleep(250);   // optional pause after each move
        }
    }


    /**
     * GYRO METHOD!!
     */
    public void composeTelemetry() {

        // At the beginning of each telemetry update, grab a bunch of data
        // from the IMU that we will then display in separate lines.
        telemetry.addAction(new Runnable() {
            @Override
            public void run() {
                // Acquiring the angles is relatively expensive; we don't want
                // to do that in each of the three items that need that info, as that's
                // three times the necessary expense.
                angles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
                gravity = robot.imu.getGravity();
            }
        });

        telemetry.addLine()
                .addData("status", new Func<String>() {
                    @Override
                    public String value() {
                        return robot.imu.getSystemStatus().toShortString();
                    }
                })
                .addData("calib", new Func<String>() {
                    @Override
                    public String value() {
                        return robot.imu.getCalibrationStatus().toString();
                    }
                });

        telemetry.addLine()
                .addData("heading", new Func<String>() {
                    @Override
                    public String value() {
                        return formatAngle(angles.angleUnit, angles.firstAngle);
                    }
                })
                .addData("roll", new Func<String>() {
                    @Override
                    public String value() {
                        return formatAngle(angles.angleUnit, angles.secondAngle);
                    }
                })
                .addData("pitch", new Func<String>() {
                    @Override
                    public String value() {
                        return formatAngle(angles.angleUnit, angles.thirdAngle);
                    }
                });

        telemetry.addLine()
                .addData("grvty", new Func<String>() {
                    @Override
                    public String value() {
                        return gravity.toString();
                    }
                })
                .addData("mag", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format(Locale.getDefault(), "%.3f",
                                Math.sqrt(gravity.xAccel * gravity.xAccel
                                        + gravity.yAccel * gravity.yAccel
                                        + gravity.zAccel * gravity.zAccel));
                    }
                });
    }

    // Formatting
    String formatAngle(AngleUnit angleUnit, double angle) {
        return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
    }

    String formatDegrees(double degrees) {
        return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
    }


    public void smacker()
    {
        //Bringing the smacker down
        robotControl.moveSmacker(0.4);

        robotControl.moveStickShoulder(0.4);
        sleep(750);
        robotControl.moveStickElbow(0.4);
        sleep(750);

        robotControl.moveStickShoulder(0.6);
        sleep(750);
        robotControl.moveStickElbow(0.2);

        sleep(750);

        robotControl.moveStickShoulder(1);

        sleep(750);

        robotControl.moveStickElbow(0);

        telemetry.addData("Moved Stick Down: " ,"");
        sleep(500);
        telemetry.update();

        //Getting the distance
        double distance = sensorControl.getSmackerDistance();
        telemetry.addData("Distance is: " , distance);
        telemetry.update();
        sleep(750);

        //Testing if the color sensed is red
        if(sensorControl.isRed() && distance < 20)
        {
            telemetry.addData("I see: " , "Red");
            telemetry.update();
            sleep(500);
            robotControl.smackRight();
            telemetry.addData("Smacked: " , "Right");
            sleep(750);
        }
        //Testing if the color sensed is blue
        else if(sensorControl.isBlue() && distance < 20)
        {
            telemetry.addData("I see: " , "Blue");
            telemetry.update();
            sleep(500);
            robotControl.smackLeft();
            telemetry.addData("Smacked: " , "Left");
            sleep(750);
        }
        //If not able to sense the color OR the distance is "NaN'
        else
        {
            telemetry.addData("I can't sense the color", "");
            telemetry.update();
        }

        telemetry.update();

        //Bring the smacker back up
        robotControl.stickUp();
        sleep(750);
        robotControl.moveSmacker(0.4);

        telemetry.addData("I have returned to the starting position: " , "");
        telemetry.update();
    }


    public void vuMarkAndSetParameters()
    {
        //Reading VuMark
        vuMark = sensorControl.readVuMark();

        if (vuMark.equalsIgnoreCase("CENTER"))
        {
            distanceToLeftWall = 66;
        }
        else if (vuMark.equalsIgnoreCase("LEFT")) {
            distanceToLeftWall = 48;
        }
        else {
            distanceToLeftWall = 76;
        }
    }

    public void sideWaysAlign()
    {
        //Side ways align with the correct column of the cryptobox
        boolean alignedWithCryptobox = false;
        double distanceMarginOfError = 3;

        runtime.reset();
        telemetry.addData("Robot Status: ", "About to align with cyrptobox");
        telemetry.update();
        Log.d("DifferentSideBlue", "Robot Status: " + "About to align with cyrptobox");
        Log.d("DifferentSideBlue", "distanceToLeftWall is  " + distanceToLeftWall);
        Log.d("DifferentSideBlue", "distanceMarginOfError is  " + distanceMarginOfError);

        while (alignedWithCryptobox == false && runtime.seconds() < 4)
        {
            ElapsedTime sideWaysTime = new ElapsedTime();

            double distanceSum = 0;
            for (int i = 0; i < 10; i++)
            {
                double testDistance = sensorControl.getDistance(myColor);
                distanceSum = distanceSum + testDistance;
            }
            double distance = distanceSum / 10;
            telemetry.addData("cm", "%.2f cm", distance);
            Log.d("DifferentSideBlue", "distance is " + distance);

            if (distance < distanceToLeftWall + distanceMarginOfError && distance > distanceToLeftWall - distanceMarginOfError) {
                telemetry.addData("Status: ", "Alligned!");
                telemetry.update();
                Log.d("DifferentSideBlue", "Status:  Alligned");
                alignedWithCryptobox = true;
            }
            else
            {
                telemetry.addData("Status: ", "Not Alligned :(");
                telemetry.update();
                Log.d("DifferentSideBlue", "Status:  Not Alligned :(");


                sideWaysTime.reset();
                if (distance > distanceToLeftWall)
                {
                    while (sideWaysTime.seconds() < 0.1)
                    {
                        robotControl.sideWaysLeft(ALIGN_SPEED);
                        telemetry.addData("Status: ", "Moving left");
                        telemetry.update();
                        Log.d("DifferentSideBlue", "Status:  Moving left");
                    }

                }
                else
                {
                    while (sideWaysTime.seconds() < 0.1)
                    {
                        robotControl.sideWaysRight(ALIGN_SPEED);
                        telemetry.addData("Status: ", "Moving right");
                        telemetry.update();
                        Log.d("DifferentSideBlue", "Status:  Moving right");
                    }
                }
            }
        }

        robotControl.stopMoving();
    }


    public void placeGlyphAndPark()
    {
        //move forward to place glyph in cryptobox
        runtime.reset();
        robotControl.changeToEncoderMode();
        encoderDrive(DRIVE_SPEED, 15, 15, 5);
        sleep(250);

        //release glyph
        robotControl.openHand2();

        runtime.reset();
        robotControl.changeToEncoderMode();
        encoderDrive(DRIVE_SPEED, -8, -8, 5);
        sleep(250);

        runtime.reset();

        robotControl.changeToEncoderMode();
        encoderDrive(DRIVE_SPEED, 12 , 12, 5);
        sleep(250);


        runtime.reset();

        robotControl.changeToEncoderMode();
        encoderDrive(DRIVE_SPEED, -8 , -8, 5);
        sleep(250);
    }


}
