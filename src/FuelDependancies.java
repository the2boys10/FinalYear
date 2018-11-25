package com.company.Other;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FuelDependancies
{
	private BigDecimal fuelUsedSoFar = new BigDecimal(0);
	private BigDecimal distanceTravelledSoFar = new BigDecimal(0);
	private int timeTakenAccelerating = 0;
	private int timeTakenDecelerating = 0;
	private int timeTakenConstant = 0;
	private static BigDecimal fuelUsedSoFarInTimeStep = new BigDecimal(0);
	private BigDecimal fuelUsedSoFarConstant = new BigDecimal(0);
	private BigDecimal fuelUsedSoFarAccelerating = new BigDecimal(0);
	private BigDecimal fuelUsedSoFarIdle = new BigDecimal(0);
	private BigDecimal fuelUsedSoFarDecelerating = new BigDecimal(0);
	private int timeTakenToGetToDestination = 0;
	private double timeIdle = 0;
	private static int averageOfHowMany = 0;
	private double timeIdleSoFar = 0;
	private static BigDecimal averageSpeedOfVehiclesInTimeStep = new BigDecimal(0);
	private double fuelMutiplier = 0;
	
	
	public FuelDependancies(int fuelChoice)
	{
		if(fuelChoice == 0)
		{
			this.fuelMutiplier = 0.00001862828;
		}
		else if(fuelChoice == 1)
		{
			this.fuelMutiplier = 0.0001862828;
		}
		else if(fuelChoice == 2)
		{
			this.fuelMutiplier = 0.001862828;
		}
	}
	
	public void calculateFuelUsed(double oldSpeed, double currentSpeed)
	{
		if(FinalYear.startTracking)
		{
			double speed = (oldSpeed + currentSpeed) / 2;
			if(oldSpeed == 0 && currentSpeed != 0)
			{
				timeIdleSoFar = 0;
			}
			averageSpeedOfVehiclesInTimeStep = (averageSpeedOfVehiclesInTimeStep.multiply(new BigDecimal(averageOfHowMany)).add(new BigDecimal(speed))).divide(new BigDecimal(averageOfHowMany + 1), 50, RoundingMode.HALF_EVEN);
			averageOfHowMany++;
			BigDecimal fuelUsedInTimestep;
			if(timeIdleSoFar <= Config.HOWLONGTILLTURNOFFENGINE)
			{
				fuelUsedInTimestep = (new BigDecimal(fuelMutiplier).add(new BigDecimal(0.000006573194).multiply(new BigDecimal(speed))).subtract(new BigDecimal(1.572406e-7).multiply(new BigDecimal(speed).pow(2))).subtract(new BigDecimal(2.600964e-9).multiply(new BigDecimal(speed).pow(3))).add(new BigDecimal(4.977537e-10).multiply(new BigDecimal(speed).pow(4))).subtract(new BigDecimal(8.453891e-12).multiply(new BigDecimal(speed).pow(5))));
				fuelUsedSoFar = fuelUsedSoFar.add(fuelUsedInTimestep);
				fuelUsedSoFarInTimeStep = fuelUsedSoFarInTimeStep.add(fuelUsedInTimestep);
			}
			else
			{
				fuelUsedInTimestep = new BigDecimal(0);
			}
			if(oldSpeed == 0 && currentSpeed == 0)
			{
				fuelUsedSoFarIdle = fuelUsedSoFarIdle.add(fuelUsedInTimestep);
				timeIdleSoFar++;
				timeIdle++;
			}
			else if(oldSpeed != 0 && currentSpeed == 0)
			{
				timeIdle = timeIdle + 0.1;
			}
			else if(oldSpeed == 0 && currentSpeed != 0)
			{
				timeIdleSoFar = 0;
				timeIdle = timeIdle + 0.1;
			}
			if(oldSpeed == currentSpeed && oldSpeed != 0)
			{
				fuelUsedSoFarConstant = fuelUsedSoFarConstant.add(fuelUsedInTimestep);
				timeTakenConstant++;
			}
			else if(oldSpeed < currentSpeed)
			{
				fuelUsedSoFarAccelerating = fuelUsedSoFarAccelerating.add(fuelUsedInTimestep);
				timeTakenAccelerating++;
			}
			else if(oldSpeed > currentSpeed)
			{
				fuelUsedSoFarDecelerating = fuelUsedSoFarDecelerating.add(fuelUsedInTimestep);
				timeTakenDecelerating++;
			}
			timeTakenToGetToDestination++;
			distanceTravelledSoFar = distanceTravelledSoFar.add(new BigDecimal(speed).divide(new BigDecimal(1 / Config.SIMACCELERATION), 50, RoundingMode.FLOOR));
		}
	}
	
	public void vehicleDidNotMove()
	{
		if(FinalYear.startTracking)
		{
			averageSpeedOfVehiclesInTimeStep = (averageSpeedOfVehiclesInTimeStep.multiply(new BigDecimal(averageOfHowMany)).add(new BigDecimal(0))).divide(new BigDecimal(averageOfHowMany + 1), 50, RoundingMode.HALF_EVEN);
			averageOfHowMany++;
			timeIdle++;
			timeTakenToGetToDestination++;
		}
	}
	
	public static BigDecimal getAverageSpeedOfVehiclesInTimeStep()
	{
		return averageSpeedOfVehiclesInTimeStep;
	}
	
	public static void setAverageSpeedOfVehiclesInTimeStep(BigDecimal averageSpeedOfVehiclesInTimeStep) { FuelDependancies.averageSpeedOfVehiclesInTimeStep = averageSpeedOfVehiclesInTimeStep; }
	
	public BigDecimal getDistanceTravelledSoFar()
	{
		return distanceTravelledSoFar;
	}
	
	public BigDecimal getFuelUsedSoFarConstant()
	{
		return fuelUsedSoFarConstant;
	}
	
	public BigDecimal getFuelUsedSoFarAccelerating()
	{
		return fuelUsedSoFarAccelerating;
	}
	
	public BigDecimal getFuelUsedSoFarIdle()
	{
		return fuelUsedSoFarIdle;
	}
	
	public BigDecimal getFuelUsedSoFarDecelerating()
	{
		return fuelUsedSoFarDecelerating;
	}
	
	public static void setAverageOfHowMany(int averageOfHowMany)
	{
		FuelDependancies.averageOfHowMany = averageOfHowMany;
	}
	
	public double getTimeIdle()
	{
		return timeIdle;
	}
	
	public double getTimeTakenConstant()
	{
		return timeTakenConstant;
	}
	
	public double getTimeTakenAccelerating()
	{
		return timeTakenAccelerating;
	}
	
	public double getTimeTakenDecelerating()
	{
		return timeTakenDecelerating;
	}
	
	public double getTimeTakenToGetToDestination()
	{
		return timeTakenToGetToDestination;
	}
}
