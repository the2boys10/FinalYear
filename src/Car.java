
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.graphstream.ui.spriteManager.Sprite;

public class Car {

	public static boolean anyChange = false;
	public int fuelApplied = 0;
	private double currentX;
	private double currentY;
	private boolean hasMoved;
	private Road currentRoad;
	private Road nextRoad;
	@SuppressWarnings("unused")
	private int fuelChoice;
	private PointOnRoad endGoal;
	private boolean valid;
	private boolean activated;
	private Sprite carsSprite;
	private double currentSpeed;
	private double acceleration;
	private double deceleration;
	private boolean atIntersection=false;
	public BigDecimal fuelUsedSoFar = new BigDecimal(0);
	public BigDecimal distanceTravelledSoFar = new BigDecimal(0);
	public int timeTakenAccelerating = 0;
	public int timeTakenDecelerating = 0;
	public int timeTakenConstant = 0;
	public static BigDecimal fuelUsedSoFarInTimeStep = new BigDecimal(0);
	public BigDecimal fuelUsedSoFarConstant = new BigDecimal(0);
	public BigDecimal fuelUsedSoFarAccelerating = new BigDecimal(0);
	public BigDecimal fuelUsedSoFarIdle = new BigDecimal(0);
	public BigDecimal fuelUsedSoFarDecelerating = new BigDecimal(0);
	public BigDecimal fuelUsedInTimestep = new BigDecimal(0);
	public int timeIdleSoFar = 0;
	public int timeTakenToGetToDestination = 0;
	public double timeIdle = 0;
	public static int averageOfHowMany = 0;
	public static int moveAmount = 0;
	public static BigDecimal averageSpeedOfVehiclesInTimeStep = new BigDecimal(0);
	public boolean getReadingOfCurrentRoad = false;
	
	
	
	private int failedAmount = 0;
	private boolean hasAddedToRoadAverage = false;
	private boolean hasAddedToRoadPass = false;
	public int timeTakenToTravelDownRoad = 0;
	public int timeTakenToPassIntersection = 0;
	public int timeWaitingAtIntersection = 0;
	public double timeTakenToTravelDownRoadPrevious = 0;
	public double timeTakenToPassIntersectionPrevious = 0;
	
	
	public Car(double xCord, double yCord, Road road, PointOnRoad endRoad, boolean activation,
			int fuelChoice, Sprite carSprite) 
	{
		if(road.getColorOfCarsOnRoad()=='R')
			carSprite.addAttribute("ui.style", "fill-color: rgba(255,0,0,125);");
		else
			carSprite.addAttribute("ui.style", "fill-color: rgba(0,0,0,125);");
		setCurrentSpeed(0);
		setCarsSprite(carSprite);
		this.fuelChoice = fuelChoice;
		setCurrentX(xCord);
		setCurrentY(yCord);
		this.setCurrentRoad(road);
		this.setEndGoal(endRoad);
		setAcceleration(Config.CARACCELERATIONANDDECELERATION);
		setDeceleration(-Config.CARACCELERATIONANDDECELERATION);
		if (getCurrentRoad().getTypeOfRoad()=='L')
		{
			if (getCurrentX() < getEndGoal().xCord)
			{
				setValid(false);
			}
		}
		else if (getCurrentRoad().getTypeOfRoad()=='R')
		{
			if (getCurrentX() > getEndGoal().xCord)
			{
				setValid(false);
			}
		}
		else if (getCurrentRoad().getTypeOfRoad()=='U')
		{
			if (getCurrentY() < getEndGoal().yCord)
			{
				setValid(false);
			}
		}
		else if (getCurrentRoad().getTypeOfRoad()=='D')
		{
			if (getCurrentX() > getEndGoal().yCord)
			{
				setValid(false);
			}
		}
		this.activated = activation;
	}
	
	public boolean canCarStopInTime(double xCord, double yCord, Sprite carsSprites) 
	{
		double timeTakenToSlowToStop = -getCurrentSpeed()/getDeceleration();
		double distanceThatWillPassSlowingDown = 0.5*(getCurrentSpeed())*timeTakenToSlowToStop;
		if (Util.getLengthOfLine(getCurrentX(), getCurrentY(), xCord, yCord)>distanceThatWillPassSlowingDown)
		{
			return true;
		}
		return false;
	}

	public GoalAndDistance workoutGoal()
	{
		double distanceLeft = 0;
		double actualDistanceLeft = 0;
		String currentGoal = "None";
		if (getCurrentRoad().getCarsOnRoad().indexOf(this) == 0)// if we are the front most car on the road.
		{
			if (getEndGoal().specRoad.equals(getCurrentRoad()) && isValid() == true) // if we can reach our destination on our current road and we are the front most car on the road.
			{
				currentGoal  = "GOAL";
				distanceLeft = Util.getLengthOfLine(getCurrentX(), getCurrentY(), getEndGoal().xCord, getEndGoal().yCord);
				actualDistanceLeft = distanceLeft;
			}
			else // else we cannot get to our goal but are at the front of the road.
			{
				if(getCurrentRoad().getGoingTo().getClass().equals(Passthrough.class))
				{
					Road targetRoad = null;
					if(getCurrentRoad().getGoingTo().getConnectionsOut()[0].getGoingTo().equals(getCurrentRoad().getGoingFrom()))
					{
						targetRoad = getCurrentRoad().getGoingTo().getConnectionsOut()[1];
					}
					else
					{
						targetRoad = getCurrentRoad().getGoingTo().getConnectionsOut()[0];
					}
					if(targetRoad.equals(getEndGoal().specRoad))
					{
						double spaceOnNextRoad = targetRoad.hasSpace();
						double distanceToGoalOnNextRoad = Util.getLengthOfLine(getCurrentRoad().getGoingTo().getxCord(), getCurrentRoad().getGoingTo().getyCord(), getEndGoal().xCord, getEndGoal().yCord);
						if(distanceToGoalOnNextRoad<spaceOnNextRoad)
						{
							distanceLeft = Util.getLengthOfLine(getCurrentX(), getCurrentY(), getCurrentRoad().getGoingTo().getxCord(),getCurrentRoad().getGoingTo().getyCord())
									+ distanceToGoalOnNextRoad;							
						}
						else
						{
							distanceLeft = Util.getLengthOfLine(getCurrentX(), getCurrentY(), getCurrentRoad().getGoingTo().getxCord(),getCurrentRoad().getGoingTo().getyCord()) 
									+ targetRoad.hasSpace();
						}
					}
					else
					{
						distanceLeft = Util.getLengthOfLine(getCurrentX(), getCurrentY(), getCurrentRoad().getGoingTo().getxCord(),getCurrentRoad().getGoingTo().getyCord())
								+ targetRoad.hasSpace();
					}
					actualDistanceLeft = Util.getLengthOfLine(getCurrentX(), getCurrentY(), getCurrentRoad().getGoingTo().getxCord(),getCurrentRoad().getGoingTo().getyCord());
				}
				else
				{
					distanceLeft = Util.getLengthOfLine(getCurrentX(), getCurrentY(), getCurrentRoad().getGoingTo().getxCord(),getCurrentRoad().getGoingTo().getyCord());
					actualDistanceLeft = distanceLeft;
				}
				currentGoal = "EOCR";
			}
		}
		else// if we are not the front most car
		{
			Car carInFront = getCurrentRoad().getCarsOnRoad().get(getCurrentRoad().getCarsOnRoad().indexOf(this) - 1);
			if (Util.isOnLineCars(getCurrentX(), getCurrentY(), carInFront.getCurrentX() + getCurrentRoad().getSpacingX(),carInFront.getCurrentY() + getCurrentRoad().getSpacingY(), carInFront.getCurrentX(), carInFront.getCurrentY())) // if the car infront of us is causing an overlap then stop and wait for distance to be made.
			{
				distanceLeft = 0;
				actualDistanceLeft = distanceLeft;
				currentGoal = "CIF";
			}
			else
			{
				double distanceToCarInFront = Util.getLengthOfLine(getCurrentX(),getCurrentY(),carInFront.getCurrentX()+getCurrentRoad().getSpacingX(),carInFront.getCurrentY()+getCurrentRoad().getSpacingY());
				if (getEndGoal().specRoad.equals(getCurrentRoad()))
				{
					double distanceToGoal = Util.getLengthOfLine(getCurrentX(),getCurrentY(),getEndGoal().xCord,getEndGoal().yCord);
					if (distanceToGoal <= distanceToCarInFront && isValid() == true)
					{
						currentGoal = "GOAL";
						distanceLeft = distanceToGoal;
						actualDistanceLeft = distanceLeft;
					}
					else
					{
						currentGoal = "CIF";
						distanceLeft = distanceToCarInFront;
						actualDistanceLeft = distanceLeft;
					}
				}
				else
				{
					currentGoal = "CIF";
					distanceLeft = distanceToCarInFront;
					actualDistanceLeft = distanceLeft;
				}
			}
		}
		GoalAndDistance goal = new GoalAndDistance(currentGoal, distanceLeft, actualDistanceLeft);
		return goal;
	}
	
	public String moveCar(boolean forceFuel)
	{
		if (isHasMoved() == false && activated == true)
		{
			GoalAndDistance goal = workoutGoal();
			double v = getCurrentSpeed() + getAcceleration() * Config.SIMACCELERATION;
			double distance = 0;
			if (v > currentRoad.getMaxSpeedOfCarsOnRoad())
			{
				distance = 0.5*(getCurrentSpeed()+currentRoad.getMaxSpeedOfCarsOnRoad()) * Config.SIMACCELERATION;
				v = currentRoad.getMaxSpeedOfCarsOnRoad();
			}
			else
			{
				distance = 0.5*(getCurrentSpeed() + v) * Config.SIMACCELERATION;
			}
			double u = v;
			double time = (0-u)/getDeceleration();
			distance = distance + u*time + 0.5*getDeceleration()*((time)*(time));
			double distance2 = getCurrentSpeed()* Config.SIMACCELERATION;
			double time2 = (0-getCurrentSpeed())/getDeceleration();
			distance2 = distance2 + getCurrentSpeed()*time2 + 0.5*getDeceleration()*time2*time2;
			double distanceToMove = 0;
			double oldspeed = getCurrentSpeed();
			double check = getCurrentSpeed() - (getAcceleration() * Config.SIMACCELERATION);
			if (check<0&&getCurrentSpeed()!=0)
			{
				setCurrentSpeed(0);
			}
			else
			{
				if (Double.compare(goal.distanceLeft,distance)>=0)
				{
					setCurrentSpeed(oldspeed + (getAcceleration() * Config.SIMACCELERATION));
					if ( getCurrentSpeed() > currentRoad.getMaxSpeedOfCarsOnRoad())
					{
						if(getCurrentSpeed()<=currentRoad.getMaxSpeedOfCarsOnRoad()+(getAcceleration()*Config.SIMACCELERATION))
						{
							setCurrentSpeed(currentRoad.getMaxSpeedOfCarsOnRoad());
						}
						else
						{
							setCurrentSpeed(oldspeed + (getDeceleration()) * Config.SIMACCELERATION);
						}
					}
					distanceToMove = 0.5*(oldspeed+getCurrentSpeed()) * Config.SIMACCELERATION;
					
				}
				else if (Double.compare(goal.distanceLeft,distance2)>=0)
				{
					setCurrentSpeed(oldspeed);
					distanceToMove = getCurrentSpeed() * Config.SIMACCELERATION;
				}
				else
				{
					setCurrentSpeed(oldspeed + (getDeceleration()) * Config.SIMACCELERATION);
					if (getCurrentSpeed() < 0)
					{
						setCurrentSpeed(0);
					}
					distanceToMove = 0.5*(oldspeed+getCurrentSpeed()) * Config.SIMACCELERATION;
				}
			}
			if (goal.distanceLeft<getAcceleration()&&getCurrentSpeed()==0&&goal.distanceLeft!=0)
			{
				if(goal.goal.equals("GOAL"))
				{
					setCurrentX(getEndGoal().xCord);
					setCurrentY(getEndGoal().yCord);
				}
				else if(goal.goal.equals("CIF"))
				{
					Car carInFront = currentRoad.getCarsOnRoad().get(currentRoad.getCarsOnRoad().indexOf(this) - 1);
					setCurrentX(carInFront.getCurrentX()+currentRoad.getSpacingX());
					setCurrentY(carInFront.getCurrentY()+currentRoad.getSpacingY());
				}
				else if(goal.goal.equals("EOCR"))
				{
					setCurrentX(currentRoad.getGoingTo().getxCord());
					setCurrentY(currentRoad.getGoingTo().getyCord());
				}
				goal.distanceLeft=0;
				distanceToMove = 0;
				setCurrentSpeed(0);
			}
			if (goal.distanceLeft<getAcceleration()&&getCurrentSpeed()==0&&goal.distanceLeft!=0)
			{
				if(goal.goal.equals("GOAL"))
				{
					setCurrentX(getEndGoal().xCord);
					setCurrentY(getEndGoal().yCord);
				}
				else if(goal.goal.equals("CIF"))
				{
					Car carInFront = getCurrentRoad().getCarsOnRoad().get(getCurrentRoad().getCarsOnRoad().indexOf(this) - 1);
					setCurrentX(carInFront.getCurrentX()+getCurrentRoad().getSpacingX());
					setCurrentY(carInFront.getCurrentY()+getCurrentRoad().getSpacingY());
				}
				else if(goal.goal.equals("EOCR"))
				{
					setCurrentX(getCurrentRoad().getGoingTo().getxCord());
					setCurrentY(getCurrentRoad().getGoingTo().getyCord());
				}
				goal.distanceLeft=0;
				distanceToMove = 0;
				setCurrentSpeed(0);
			}
			if (goal.actualDistanceLeft <= distanceToMove)
			{
				if (goal.goal.equals("GOAL"))
				{
					addToFuel(oldspeed,currentSpeed);
					return "Finished";
				}
				else if (goal.goal.equals("CIF"))
				{
					addToFuel(oldspeed,currentSpeed);
					hasMoved = true;
					return "UsedUpTurn";
				}
				else if (goal.goal.equals("EOCR"))
				{
					setAtIntersection(true);
					if(getCurrentRoad().getGoingTo().getClass().equals(Passthrough.class))
					{
						if(getCurrentRoad().getGoingTo().getConnectionsOut()[0].getGoingTo().equals(getCurrentRoad().getGoingFrom()))
						{
							nextRoad = getCurrentRoad().getGoingTo().getConnectionsOut()[1];
						}
						else
						{
							nextRoad = getCurrentRoad().getGoingTo().getConnectionsOut()[0];
						}
						double distanceToGo = distanceToMove-(Util.getLengthOfLine(getCurrentX(), getCurrentY(), getCurrentRoad().getGoingTo().getxCord(), getCurrentRoad().getGoingTo().getyCord()));
						if(getCurrentRoad().getGoingTo().isAccepting(this, getCurrentRoad(),nextRoad))
						{
							setFailedAmount(0);
							nextRoad=null;
							addToFuel(oldspeed,currentSpeed);
							moveCarXandY(distanceToGo);
							hasMoved = true;
							return "UsedUpTurn";
						}
						else
						{
							if(forceFuel==true)
							{
								addToFuel(oldspeed,currentSpeed);
								hasMoved = true;
							}
							return "Failed to Move";
						}
					}
					else if(getCurrentRoad().getGoingTo().getClass().equals(ClosedRoad.class))
					{
						nextRoad = getCurrentRoad().getGoingTo().getConnectionsOut()[0];
						double distanceToGo = distanceToMove-(Util.getLengthOfLine(getCurrentX(), getCurrentY(), getCurrentRoad().getGoingTo().getxCord(), getCurrentRoad().getGoingTo().getyCord()));
						if(getCurrentRoad().getGoingTo().isAccepting(this, getCurrentRoad(),nextRoad))
						{
							setFailedAmount(0);
							nextRoad=null;
							moveCarXandY(distanceToGo);
							addToFuel(oldspeed,currentSpeed);
							hasMoved = true;
							return "UsedUpTurn";
						}
						else
						{
							if(forceFuel==true)
							{
								addToFuel(oldspeed,currentSpeed);
								hasMoved = true;
							}
							return "Failed to Move";
						}
					}
					else
					{
						if (getNextRoad() == null)
						{
							setNextRoad(UserInputFile.getNextRoad(this));
							if (getCurrentRoad().getGoingTo().isAccepting(this, getCurrentRoad(), getNextRoad()))
							{
								setFailedAmount(0);
								addToFuel(oldspeed,currentSpeed);
								hasMoved = true;
								return "UsedUpTurn";
							}
							else
							{
								if(forceFuel==true)
								{
									addToFuel(oldspeed,currentSpeed);
									hasMoved = true;
									if(currentRoad.getGoingTo().howLongTillAcceptingState(currentRoad)==0)
									{
										setFailedAmount(getFailedAmount() + 1);
									}
									verifyFailedAmount();
								}
								return "Failed to Move";
							}
						}
						else if (getCurrentRoad().getGoingTo().isAccepting(this, getCurrentRoad(), getNextRoad()))
						{
							setFailedAmount(0);
							addToFuel(oldspeed,currentSpeed);
							hasMoved = true;
							return "UsedUpTurn";
						}
						else
						{
							if(forceFuel==true)
							{
								addToFuel(oldspeed,currentSpeed);
								hasMoved = true;
								if(currentRoad.getGoingTo().howLongTillAcceptingState(currentRoad)==0)
								{
									setFailedAmount(getFailedAmount() + 1);
								}
								verifyFailedAmount();
							}
							return "Failed to Move";
						}
					}
				}
			}
			else
			{
				addToFuel(oldspeed,currentSpeed);
				hasMoved = true;
				moveCarXandY(distanceToMove);
				return "UsedUpTurn";
			}
		}
		else if (activated == false&&hasMoved==false)
		{
			setHasMoved(true);
			if(FinalYear.startTracking)
			{
				averageSpeedOfVehiclesInTimeStep = (averageSpeedOfVehiclesInTimeStep.multiply(new BigDecimal(averageOfHowMany)).add(new BigDecimal(0))).divide(new BigDecimal(averageOfHowMany+1),50, RoundingMode.HALF_EVEN);
				averageOfHowMany++;
				timeIdle++;
				timeTakenToGetToDestination++;
				
			}
			if (getCurrentRoad().getCarsOnRoad().indexOf(this) == 0)
			{
				activated = true;
				setHasMoved(true);
			}
			else
			{
				Car carInFront = getCurrentRoad().getCarsOnRoad().get(getCurrentRoad().getCarsOnRoad().indexOf(this) - 1);
				if(Util.isOnLineCars(carInFront.getCurrentX(), carInFront.getCurrentY(), getCurrentX(), getCurrentY(), getCurrentRoad().getGoingTo().getxCord(), getCurrentRoad().getGoingTo().getyCord()))
				{
					activated = true;
					setHasMoved(true);
				}
			}
		}
		return "UsedUpTurn";
	}
	
	
	public void verifyFailedAmount()
	{
		if(currentRoad.getGoingTo().getClass().equals(Tjunction.class))
		{
			if(getFailedAmount()>Config.TJUNCTIONRECHOOSEAMOUNT)
			{
				nextRoad = UserInputFile.getNextRoadDeadlockPreventionTjunction(currentRoad, getNextRoad(), this);
				setFailedAmount(0);
			}
		}
		if(currentRoad.getGoingTo().getClass().equals(CrossRoads.class))
		{
			if(getFailedAmount()>Config.CROSSROADSRECHOOSEAMOUNT)
			{
				nextRoad = UserInputFile.getNextRoadDeadlockPreventionCrossroads(currentRoad, getNextRoad(), this);
				setFailedAmount(0);
			}
		}
	}
	
	public void moveCarXandY(double distanceToGo)
	{
		if (getCurrentRoad().getDiffX() == 0)
		{
			if (getCurrentRoad().getGoingTo().getyCord() > getCurrentY())
			{
				setCurrentY(getCurrentY()+distanceToGo);
			}
			else
			{
				setCurrentY(getCurrentY()-distanceToGo);
			}
		}
		else if (getCurrentRoad().getDiffY() == 0)
		{
			if (getCurrentRoad().getGoingTo().getxCord() > getCurrentX())
			{
				setCurrentX(getCurrentX()+distanceToGo);
			}
			else
			{
				setCurrentX(getCurrentX()-distanceToGo);
			}
		}
		else
		{
			double distanceX = distanceToGo*Math.cos(getCurrentRoad().getAngleOfRoad());
			double distanceY = distanceToGo*Math.sin(getCurrentRoad().getAngleOfRoad());
			setCurrentX(getCurrentX()-distanceX);
			setCurrentY(getCurrentY()-distanceY);
		}
		anyChange = true;
		setHasMoved(true);
	}
	
	@SuppressWarnings("unused")
	public void addToFuel(double oldSpeed, double currentSpeed)
	{
		double speed = (oldSpeed + currentSpeed)/2;
		if(getReadingOfCurrentRoad==true&&atIntersection==false)
		{
			timeTakenToTravelDownRoad++;
			if(Config.UPDATEMAXTIME&&Config.UPDATEWEIGHTSAUTO)
			{
				int indexOfConnection = currentRoad.getGoingTo().getIndex(currentRoad, nextRoad);
				if(timeTakenToTravelDownRoad>currentRoad.maxArrayForUser[0])
				{
					currentRoad.maxArrayForUser[0] = timeTakenToTravelDownRoad;
				}
			}
			if(Config.UPDATEAVERAGETIME&&Config.UPDATEWEIGHTSAUTO)
			{
				if(timeTakenToTravelDownRoad>currentRoad.getAverageTimesOnIntersection()[0])
				{
					if(isHasAddedToRoadAverage()==false)
					{
						currentRoad.getAverageTimesOnIntersection()[0] = ((currentRoad.getAverageTimesOnIntersection()[0]*currentRoad.getAmountOfAvg()[0])+timeTakenToTravelDownRoad)/(currentRoad.getAmountOfAvg()[0]+1);
						setHasAddedToRoadAverage(true);
						currentRoad.getAmountOfAvg()[0]=currentRoad.getAmountOfAvg()[0]+1;
						timeTakenToTravelDownRoadPrevious = timeTakenToTravelDownRoad;
					}
					else
					{
						currentRoad.getAverageTimesOnIntersection()[0] = ((currentRoad.getAverageTimesOnIntersection()[0]*currentRoad.getAmountOfAvg()[0])-timeTakenToTravelDownRoadPrevious+timeTakenToTravelDownRoad)/(currentRoad.getAmountOfAvg()[0]);
						timeTakenToTravelDownRoadPrevious = timeTakenToTravelDownRoad;
					}
				}
			}
		}
		if(atIntersection==true)
		{
			timeWaitingAtIntersection++;
			if(Config.UPDATEMAXTIME&&Config.UPDATEWEIGHTSAUTO)
			{
				int indexOfConnection = currentRoad.getGoingTo().getIndex(currentRoad, nextRoad);
				if(timeWaitingAtIntersection>currentRoad.maxArrayForUser[indexOfConnection])
				{
					currentRoad.maxArrayForUser[indexOfConnection] = timeWaitingAtIntersection;
				}
			}
			if(Config.UPDATEAVERAGETIME&&Config.UPDATEWEIGHTSAUTO)
			{
				int indexOfConnection = currentRoad.getGoingTo().getIndex(currentRoad, nextRoad);
				if(timeWaitingAtIntersection>currentRoad.getAverageTimesOnIntersection()[indexOfConnection])
				{
					if(isHasAddedToRoadPass()==false)
					{
						currentRoad.getAverageTimesOnIntersection()[indexOfConnection] = ((currentRoad.getAverageTimesOnIntersection()[indexOfConnection]*currentRoad.getAmountOfAvg()[indexOfConnection])+timeWaitingAtIntersection)/(currentRoad.getAmountOfAvg()[indexOfConnection]+1);
						currentRoad.getAmountOfAvg()[indexOfConnection]=currentRoad.getAmountOfAvg()[indexOfConnection]+1;
						setHasAddedToRoadPass(true);
						timeTakenToPassIntersectionPrevious = timeWaitingAtIntersection;
					}
					else
					{
						currentRoad.getAverageTimesOnIntersection()[indexOfConnection] = ((currentRoad.getAverageTimesOnIntersection()[indexOfConnection]*currentRoad.getAmountOfAvg()[indexOfConnection])-timeTakenToPassIntersectionPrevious+timeWaitingAtIntersection)/(currentRoad.getAmountOfAvg()[indexOfConnection]);
						timeTakenToPassIntersectionPrevious = timeWaitingAtIntersection;
					}
				}
			}
		}
		if(FinalYear.startTracking==true)
		{
			if(oldSpeed == 0 && currentSpeed != 0)
			{
				timeIdleSoFar = 0;
			}
			averageSpeedOfVehiclesInTimeStep = (averageSpeedOfVehiclesInTimeStep.multiply(new BigDecimal(averageOfHowMany)).add(new BigDecimal(speed))).divide(new BigDecimal(averageOfHowMany+1),50, RoundingMode.HALF_EVEN);
			averageOfHowMany++;
			if(timeIdleSoFar<=Config.HOWLONGTILLTURNOFFENGINE)
			{
				if(fuelChoice==0)
				{
					fuelUsedInTimestep = (new BigDecimal(0.00001862828).add
							(new BigDecimal(0.000006573194).multiply(new BigDecimal(speed))).subtract
							(new BigDecimal(1.572406e-7).multiply(new BigDecimal(speed).pow(2))).subtract
							(new BigDecimal(2.600964e-9).multiply(new BigDecimal(speed).pow(3))).add
							(new BigDecimal(4.977537e-10).multiply(new BigDecimal(speed).pow(4))).subtract
							(new BigDecimal(8.453891e-12).multiply(new BigDecimal(speed).pow(5))));
				}
				else if (fuelChoice==1)
				{
					fuelUsedInTimestep = (new BigDecimal(0.0001862828).add
							(new BigDecimal(0.000006573194).multiply(new BigDecimal(speed))).subtract
							(new BigDecimal(1.572406e-7).multiply(new BigDecimal(speed).pow(2))).subtract
							(new BigDecimal(2.600964e-9).multiply(new BigDecimal(speed).pow(3))).add
							(new BigDecimal(4.977537e-10).multiply(new BigDecimal(speed).pow(4))).subtract
							(new BigDecimal(8.453891e-12).multiply(new BigDecimal(speed).pow(5))));
				}
				else if (fuelChoice==2)
				{
					fuelUsedInTimestep = (new BigDecimal(0.001862828).add
							(new BigDecimal(0.000006573194).multiply(new BigDecimal(speed))).subtract
							(new BigDecimal(1.572406e-7).multiply(new BigDecimal(speed).pow(2))).subtract
							(new BigDecimal(2.600964e-9).multiply(new BigDecimal(speed).pow(3))).add
							(new BigDecimal(4.977537e-10).multiply(new BigDecimal(speed).pow(4))).subtract
							(new BigDecimal(8.453891e-12).multiply(new BigDecimal(speed).pow(5))));
				}
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
				timeIdle = timeIdle+0.1;
			}
			else if(oldSpeed == 0 && currentSpeed != 0)
			{
				timeIdleSoFar = 0;
				timeIdle = timeIdle+0.1;
			}
			if(oldSpeed==currentSpeed&&oldSpeed!=0)
			{
				fuelUsedSoFarConstant = fuelUsedSoFarConstant.add(fuelUsedInTimestep);
				timeTakenConstant++;
			}
			else if (oldSpeed<currentSpeed)
			{
				fuelUsedSoFarAccelerating = fuelUsedSoFarAccelerating.add(fuelUsedInTimestep);
				timeTakenAccelerating++;
			}
			else if (oldSpeed>currentSpeed)
			{
				fuelUsedSoFarDecelerating = fuelUsedSoFarDecelerating.add(fuelUsedInTimestep);
				timeTakenDecelerating++;
			}
			timeTakenToGetToDestination++;
			distanceTravelledSoFar = distanceTravelledSoFar.add(new BigDecimal(speed).divide(new BigDecimal(1/Config.SIMACCELERATION),50,RoundingMode.FLOOR));
		}
	}
	
	public boolean isHasMoved() {
		return hasMoved;
	}

	public void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}

	public Sprite getCarsSprite() {
		return carsSprite;
	}

	public void setCarsSprite(Sprite carsSprite) {
		this.carsSprite = carsSprite;
	}

	public Road getCurrentRoad() {
		return currentRoad;
	}

	public void setCurrentRoad(Road currentRoad) {
		this.currentRoad = currentRoad;
	}

	public Road getNextRoad() {
		return nextRoad;
	}

	public void setNextRoad(Road nextRoad) {
		this.nextRoad = nextRoad;
	}

	public double getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	public double getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(double currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public double getCurrentX() {
		return currentX;
	}

	public void setCurrentX(double currentX) {
		this.currentX = currentX;
	}

	public double getCurrentY() {
		return currentY;
	}

	public void setCurrentY(double currentY) {
		this.currentY = currentY;
	}

	public boolean isAtIntersection() {
		return atIntersection;
	}

	public void setAtIntersection(boolean atIntersection) {
		this.atIntersection = atIntersection;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public double getDeceleration() {
		return deceleration;
	}

	public void setDeceleration(double deceleration) {
		this.deceleration = deceleration;
	}

	public PointOnRoad getEndGoal() {
		return endGoal;
	}

	public void setEndGoal(PointOnRoad endGoal) {
		this.endGoal = endGoal;
	}

	public boolean isHasAddedToRoadPass() {
		return hasAddedToRoadPass;
	}

	public void setHasAddedToRoadPass(boolean hasAddedToRoadPass) {
		this.hasAddedToRoadPass = hasAddedToRoadPass;
	}

	public boolean isHasAddedToRoadAverage() {
		return hasAddedToRoadAverage;
	}

	public void setHasAddedToRoadAverage(boolean hasAddedToRoadAverage) {
		this.hasAddedToRoadAverage = hasAddedToRoadAverage;
	}

	public int getFailedAmount() {
		return failedAmount;
	}

	public void setFailedAmount(int failedAmount) {
		this.failedAmount = failedAmount;
	}
}
