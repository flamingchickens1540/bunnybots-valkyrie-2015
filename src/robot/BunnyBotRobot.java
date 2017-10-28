package robot;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.DriverImpls;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.instinct.InstinctModule;
import ccre.log.Logger;
import ccre.timers.PauseTimer;

/**
 * This is the core class of a CCRE project. The CCRE launching system will make
 * sure that this class is loaded, and will have set up everything else before
 * loading it. If you change the name, use Eclipse's rename functionality. If
 * you don't, you will have to change the name in Deployment.java.
 *
 * Make sure to set TEAM_NUMBER to your team number.
 */
public class BunnyBotRobot implements FRCApplication {

    public static final int TEAM_NUMBER = 1540;
    
    @Override
    public void setupRobot() throws ExtendedMotorFailureException {
    	

    	/**
    	 * POULTRY INSPECTOR
    	 */
    	
    	
    	//declare extendedmotors
        ExtendedMotor leftShooterExtended = FRC.talonCAN(9);
        ExtendedMotor rightShooterExtended = FRC.talonCAN(8);
        
    	
    	/**
    	 * TELEOPERATED
    	 */
    	
        //print "Started" to console
    	Logger.info("Started");
    	
    	
        /**
         * Declaring bind inputs from joysticks
         */
    	/** xbox controller */
    	//FloatInput xAxisLeftController = FRC.joystick1.axis(2).deadzone(0.1f);
       // FloatInput yAxisLeftController = FRC.joystick1.axis(3).deadzone(0.1f);
    	FloatInput yAxisLeftController = FRC.joystick1.axis(2).deadzone(0.1f).negated();
    	FloatInput xAxisRightController = FRC.joystick1.axis(5).deadzone(0.1f);
         I
        BooleanInput xButton = FRC.joystick1.button(3);
        BooleanInput aButton = FRC.joystick1.button(1);
        /** big joystick (lenny) **/
		FloatInput xAxisJoystick = FRC.joystick2.axis(1).deadzone(0.1f).multipliedBy(0.5f);
		FloatInput yAxisJoystick = FRC.joystick2.axis(2).deadzone(0.1f);
		FloatInput joystickTrigger = FRC.joystick2.button(1).toFloat(0,1);
		FloatInput centerButton = FRC.joystick2.button(3).toFloat(1,-1);
		BooleanInput buttonTwo = FRC.joystick2.button(2);
		BooleanInput button6 = FRC.joystick2.button(6);
        
        /**
         * Declaring motor binds
         */
        
        /** declare individual motors */
        
        //left side
        FloatOutput leftDriveFront = FRC.talon(4, FRC.MOTOR_FORWARD);
        FloatOutput leftDriveMiddle = FRC.talon(5, FRC.MOTOR_REVERSE);
        FloatOutput leftDriveBack = FRC.talon(6, FRC.MOTOR_FORWARD);
        //right side
        FloatOutput rightDriveFront = FRC.talon(1, FRC.MOTOR_REVERSE);
        FloatOutput rightDriveMiddle = FRC.talon(2, FRC.MOTOR_REVERSE);
        FloatOutput rightDriveBack = FRC.talon(3, FRC.MOTOR_REVERSE);
        //shooter 
        FloatOutput shooterIntake = FRC.talon(8, FRC.MOTOR_REVERSE);
        
        FloatOutput leftShooter = leftShooterExtended.simpleControl(FRC.MOTOR_REVERSE);
        FloatOutput rightShooter = rightShooterExtended.simpleControl(FRC.MOTOR_FORWARD);
        
        //pneumatics for speed
        BooleanOutput driveSpeed = FRC.solenoid(1);
        
        /** sensors **/
        BooleanInput ballSensor = FRC.digitalInputByInterrupt(1);
        
        /**declare motor groups */
        FloatOutput leftDrive = leftDriveFront.combine(leftDriveMiddle.combine(leftDriveBack));
        FloatOutput rightDrive = rightDriveFront.combine(rightDriveMiddle.combine(rightDriveBack));
        
        /** drive system **/
        
        //drivespeed
        BooleanCell driveSpeedStatus = new BooleanCell();
        driveSpeedStatus.set(true);
        driveSpeedStatus.send(driveSpeed);
    	//driveSpeedStatus.toggleWhen(xButton.onPress());
        driveSpeedStatus.setFalseWhen(aButton.onPress());
        driveSpeedStatus.setTrueWhen(xButton.onPress());
        
    	DriverImpls.tankDrive(yAxisLeftController,yAxisRightController,leftDrive,rightDrive);
    	
    	/** shooter **/
    	//intake
    	
    	/*
    	PauseTimer shooterTimer = new PauseTimer(5000);
    	EventInput intakeEvent = () -> {
    		joystickTrigger.send(shooterIntake);
    	};
    	intakeEvent.send(shooterTimer);
    	*/
    	
    	
    	/*
    	FloatOutput complexIntake = (value) -> {
    		shooterIntake.set(value);
    		// TIMER STUFF
    		EventOutput timerCallback = () -> {
    			// timer done
    		};
    	};
    	*/
    	
    	//the cell becomes false when a ball is seen; it then becomes true when you go to shoot or reverse the intake
    	BooleanCell sensorCell = new BooleanCell();
        sensorCell.set(false);
        sensorCell.setTrueWhen(ballSensor.onRelease());
    	sensorCell.setFalseWhen(FRC.joystick2.button(1).onPress());
    	sensorCell.setFalseWhen(FRC.joystick2.button(3).onPress());
    	
    	//if the second button and the sensorcell are true or the center button is pressed, then multiply it by centerbutton
    	((buttonTwo.and(sensorCell.not())).or((FRC.joystick2.button(3))).and(buttonTwo)).toFloat(0,1).multipliedBy(centerButton).send(shooterIntake);
    	//turn on the shooter intake regardless if it's running backwards
    	//buttonTwo.toFloat(0,1).multipliedBy(FRC.joystick2.button(3).toFloat(0,-1)).send(shooterIntake);
    	//aiming and firing
    	/* math:
    	 * right side: ((-yaxis)-(xaxis*-yaxis*aiming))*triggerbutton
    	 * left side:  ((-yaxis)+(xaxis*-yaxis*aiming))*triggerbutton
    	 */
    	
    	//the cell toggles based on button 6: false is no aiming
    	BooleanCell aiming = new BooleanCell();
    	aiming.set(false);
    	aiming.toggleWhen(button6.onPress());
    	
    	(yAxisJoystick.negated().minus(xAxisJoystick.multipliedBy(yAxisJoystick.negated()).multipliedBy(aiming.toFloat(0,1)))).multipliedBy(joystickTrigger).withRamping(0.5f, FRC.constantPeriodic).send(rightShooter);
    	(yAxisJoystick.negated().plus(xAxisJoystick.multipliedBy(yAxisJoystick.negated()).multipliedBy(aiming.toFloat(0,1)))).multipliedBy(joystickTrigger).withRamping(0.5f, FRC.constantPeriodic).send(leftShooter);
    	
    	
    	/**
    	 * POULTRY INSPECTOR
    	 */
    	//publish
        Cluck.publish("Left Shooter Current",leftShooterExtended.asStatus(ExtendedMotor.StatusType.OUTPUT_CURRENT));
        Cluck.publish("Right Shooter Current",rightShooterExtended.asStatus(ExtendedMotor.StatusType.OUTPUT_CURRENT));
        Cluck.publish("Gearbox", driveSpeedStatus);
        Cluck.publish("Ball Detection", ballSensor);
        Cluck.publish("Intake Stopping", sensorCell);
        Cluck.publish("Aiming Boolean", aiming);
    	
    	
    	/**
    	 * AUTONOMOUS
    	 */
    	FRC.registerAutonomous(new InstinctModule() {
			@Override
			protected void autonomousMain() throws Throwable {
				
				//pick up first ball and shoot it
				shooterIntake.set(1);
				turn(70);
				leftDrive.set(-0.25f);
				rightDrive.set(-0.25f);
				waitForTime(200);
				waitUntil(ballSensor);
				shooterIntake.set(0);
				leftDrive.set(1.0f);
				rightDrive.set(0.9f);
				waitForTime(1300);
				stop();
				shoot();
				//turn(180);
				leftDrive.set(1.0f);
				rightDrive.set(0.9f);
				shooterIntake.set(1);
				waitForTime(2050);
				stop();
				
				/*
				while (true) {	
					shooterIntake.set(1.0f);
					turn(180);
					driveAndIntake();
					//when the sensor sees a ball
					waitUntil(ballSensor);
					shooterIntake.set(0);
					turn(180);
					shoot();
				}
				*/
			}
			
			protected void turn(int degrees) throws Throwable {
				leftDrive.set(1.0f);
				rightDrive.set(-1.0f);
				waitForTime( (int) (3.70*degrees));
				leftDrive.set(0);
				rightDrive.set(0);
			}
			
			protected void shoot() throws Throwable {
				leftShooter.set(1.0f);
				rightShooter.set(1.0f);
				waitForTime(500);
				shooterIntake.set(1.0f);
				waitForTime(1500);
				leftShooter.set(0);
				rightShooter.set(0);
				shooterIntake.set(0.0f);
			}
			
			protected void driveAndIntake() throws Throwable {
				leftDrive.set(1.0f);
				rightDrive.set(1.0f);
				shooterIntake.set(1);
			}
			
			protected void stop() throws Throwable {
				leftDrive.set(0);
				rightDrive.set(0);
				shooterIntake.set(0);
				leftShooter.set(0);
				rightShooter.set(0);
			}
    	});
    	
    	//testing binds
    	/*
    	FRC.joystick1.button(3).toFloat(0,1).send(rightDriveMiddle);
    	FRC.joystick1.button(4).toFloat(0,1).send(rightDriveBack);
    	FRC.joystick1.button(1).toFloat(0,1).send(leftDriveMiddle);
    	FRC.joystick1.button(2).toFloat(0,1).send(leftDriveBack);
    	FRC.joystick1.button(4).toFloat(0,1).send(rightDriveFront);
    	FRC.joystick1.button(5).toFloat(0,1).send(leftDriveFront);
    	*/
    }
}
