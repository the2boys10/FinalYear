
import org.graphstream.ui.spriteManager.Sprite;

public class Car
{
	private static boolean anyChange = false;
	private int fuelApplied = 0;
	private double currentX;
	private double currentY;
	private boolean hasMoved;
	private Road currentRoad;
	private Road nextRoad;
	private final PointOnRoad endGoal;
	private boolean valid;
	private boolean activated;
	private final Sprite carsSprite;
	private double currentSpeed = 0;
	private boolean atIntersection = false;
	private boolean readingOfCurrentRoad = false;
	private int failedAmount = 0;
	private boolean hasAddedToRoadAverage = false;
	private boolean hasAddedToRoadPass = false;
	private int timeTakenToTravelDownRoad = 0;
	private int timeWaitingAtIntersection = 0;
	private double timeTakenToTravelDownRoadPrevious = 0;
	private double timeTakenToPassIntersectionPrevious = 0;
	private final FuelDependancies fuelDependant;
	
	public Car(double xCord, double yCord, Road road, PointOnRoad endRoad, boolean activation, int fuelChoice, Sprite carSprite)
	{
		if(road.getColorOfCarsOnRoad( ) == 'R')
			carSprite.addAttribute("ui.style", "fill-color: rgba(255,0,0,125);");
		else
			carSprite.addAttribute("ui.style", "fill-color: rgba(0,0,0,125);");
		carsSprite = carSprite;
		currentX = xCord;
		currentY = yCord;
		currentRoad = road;
		endGoal = endRoad;
		this.fuelDependant = new FuelDependancies(fuelChoice);
		if((currentRoad.getTypeOfRoad( ) == 'L' && currentX < endGoal.xCord) || (currentRoad.getTypeOfRoad( ) == 'R' && currentX > endGoal.xCord) || (currentRoad.getTypeOfRoad( ) == 'U' && currentY < endGoal.xCord) || (currentRoad.getTypeOfRoad( ) == 'D' && currentX > endGoal.xCord))
		{
			valid = false;
		}
		this.activated = activation;
	}
	
	public boolean canCarStopInTime(double xCord, double yCord)
	{
		double timeTakenToSlowToStop = - currentSpeed / - Config.CARACCELERATIONANDDECELERATION;
		double distanceThatWillPassSlowingDown = 0.5 * (currentSpeed) * timeTakenToSlowToStop;
		return Util.getLengthOfLine(currentX, currentY, xCord, yCord) > distanceThatWillPassSlowingDown;
	}
	
	private GoalAndDistance workoutGoal()
	{
		GoalAndDistance currentGoal = new GoalAndDistance( );
		Car carInFront;
		if(currentRoad.getCarsOnRoad( ).indexOf(this) == 0)// if we are the front most car on the road.
		{
			if(endGoal.specRoad.equals(currentRoad) && isValid( )) // if we can reach our destination on our current road and we are the front most car on the road.
			{
				double valueOfBoth = Util.getLengthOfLine(currentX, currentY, endGoal.xCord, endGoal.yCord);
				currentGoal.setAllAtOnce("GOAL", valueOfBoth, valueOfBoth);
			}
			else // else we cannot get to our goal but are at the front of the road.
			{
				currentGoal.setAllAtOnce("EOCR", currentRoad.getGoingTo( ).getRemainingDistance(currentRoad, endGoal, currentX, currentY, currentRoad.getGoingTo( ).getxCord( ), currentRoad.getGoingTo( ).getyCord( )), Util.getLengthOfLine(currentX, currentY, currentRoad.getGoingTo( ).getxCord( ), currentRoad.getGoingTo( ).getyCord( )));
			}
		}
		else// if we are not the front most car
		{
			carInFront = currentRoad.getCarsOnRoad( ).get(currentRoad.getCarsOnRoad( ).indexOf(this) - 1);
			if(Util.isOnLineCars(currentX, currentY, carInFront.currentX + currentRoad.getSpacingX( ), carInFront.currentY + currentRoad.getSpacingY( ), carInFront.currentX, carInFront.currentY))
			{
				currentGoal.setGoal("CIF");
			}
			else
			{
				double distanceToCarInFront = Util.getLengthOfLine(currentX, currentY, carInFront.currentX + currentRoad.getSpacingX( ), carInFront.currentY + currentRoad.getSpacingY( ));
				double distanceToGoal;
				if(endGoal.specRoad.equals(currentRoad) && (distanceToGoal = Util.getLengthOfLine(currentX, currentY, endGoal.xCord, endGoal.yCord)) <= distanceToCarInFront && isValid( ))
				{
					currentGoal.setAllAtOnce("GOAL", distanceToGoal, distanceToGoal);
				}
				else
				{
					currentGoal.setAllAtOnce("CIF", distanceToCarInFront, distanceToCarInFront);
				}
			}
		}
		return currentGoal;
	}
	
	public String moveCar(boolean forceFuel)
	{
		if(! hasMoved)
		{
			if(activated)
			{
				GoalAndDistance goal = workoutGoal( );
				double v = currentSpeed + Config.CARACCELERATIONANDDECELERATION * Config.SIMACCELERATION;
				double time = (0 - v) / - Config.CARACCELERATIONANDDECELERATION;
				double distance;
				if(v > currentRoad.getMaxSpeedOfCarsOnRoad( ))
				{
					v = currentRoad.getMaxSpeedOfCarsOnRoad( );
					distance = (0.5 * (currentSpeed + currentRoad.getMaxSpeedOfCarsOnRoad( )) * Config.SIMACCELERATION) + v * time + 0.5 * - Config.CARACCELERATIONANDDECELERATION * ((time) * (time));
				}
				else
				{
					distance = (0.5 * (currentSpeed + v) * Config.SIMACCELERATION) + v * time + 0.5 * - Config.CARACCELERATIONANDDECELERATION * ((time) * (time));
				}
				double time2 = (0 - currentSpeed) / - Config.CARACCELERATIONANDDECELERATION;
				double distance2 = (currentSpeed * Config.SIMACCELERATION) + currentSpeed * time2 + 0.5 * - Config.CARACCELERATIONANDDECELERATION * time2 * time2;
				double distanceToMove = 0;
				double oldspeed = currentSpeed;
				if(currentSpeed - (Config.CARACCELERATIONANDDECELERATION * Config.SIMACCELERATION) < 0 && currentSpeed != 0)
				{
					currentSpeed = 0;
				}
				else
				{
					if(Double.compare(goal.getDistanceLeft( ), distance) >= 0)
					{
						currentSpeed = oldspeed + (Config.CARACCELERATIONANDDECELERATION * Config.SIMACCELERATION);
						if(currentSpeed > currentRoad.getMaxSpeedOfCarsOnRoad( ))
						{
							if(currentSpeed <= currentRoad.getMaxSpeedOfCarsOnRoad( ) + (Config.CARACCELERATIONANDDECELERATION * Config.SIMACCELERATION))
							{
								currentSpeed = currentRoad.getMaxSpeedOfCarsOnRoad( );
							}
							else
							{
								currentSpeed = oldspeed + (- Config.CARACCELERATIONANDDECELERATION) * Config.SIMACCELERATION;
							}
						}
						distanceToMove = 0.5 * (oldspeed + currentSpeed) * Config.SIMACCELERATION;
					}
					else if(Double.compare(goal.getDistanceLeft( ), distance2) >= 0)
					{
						currentSpeed = oldspeed;
						distanceToMove = currentSpeed * Config.SIMACCELERATION;
					}
					else
					{
						currentSpeed = oldspeed + (- Config.CARACCELERATIONANDDECELERATION) * Config.SIMACCELERATION;
						if(currentSpeed < 0)
						{
							currentSpeed = 0;
						}
						distanceToMove = 0.5 * (oldspeed + currentSpeed) * Config.SIMACCELERATION;
					}
				}
				if(goal.getDistanceLeft( ) < Config.CARACCELERATIONANDDECELERATION && currentSpeed == 0 && goal.getDistanceLeft( ) != 0)
				{
					switch(goal.getGoal( ))
					{
						case "GOAL":
							currentX = endGoal.xCord;
							currentY = endGoal.yCord;
							break;
						case "CIF":
							Car carInFront = currentRoad.getCarsOnRoad( ).get(currentRoad.getCarsOnRoad( ).indexOf(this) - 1);
							currentX = carInFront.currentX + currentRoad.getSpacingX( );
							currentY = carInFront.currentY + currentRoad.getSpacingY( );
							break;
						case "EOCR":
							currentX = currentRoad.getGoingTo( ).getxCord( );
							currentY = currentRoad.getGoingTo( ).getyCord( );
							break;
					}
					goal.setDistanceLeft(0);
					distanceToMove = 0;
				}
				if(goal.getActualDistanceLeft( ) <= distanceToMove)
				{
					switch(goal.getGoal( ))
					{
						case "GOAL":
							endOfTurnActivities(oldspeed, currentSpeed);
							return "Finished";
						case "CIF":
							endOfTurnActivities(oldspeed, currentSpeed);
							hasMoved = true;
							return "UsedUpTurn";
						case "EOCR":
							atIntersection = true;
							nextRoad = currentRoad.getGoingTo( ).getNextRoad(this, nextRoad);
							if(! currentRoad.getGoingTo( ).isReconsideration( ))
							{
								double distanceToGo = distanceToMove - (Util.getLengthOfLine(currentX, currentY, currentRoad.getGoingTo( ).getxCord( ), currentRoad.getGoingTo( ).getyCord( )));
								return updateCarsPositionAndFuelBothCases(forceFuel, oldspeed, distanceToGo);
							}
							else
							{
								return updateCarsPositionAndFuelBothCases(forceFuel, oldspeed, 0);
							}
					}
				}
				else
				{
					return updateCarLocationAndFuel(oldspeed, distanceToMove);
				}
			}
			else
			{
				hasMoved = true;
				fuelDependant.vehicleDidNotMove( );
				if(currentRoad.getCarsOnRoad( ).indexOf(this) == 0)
				{
					activated = true;
				}
				else
				{
					Car carInFront = currentRoad.getCarsOnRoad( ).get(currentRoad.getCarsOnRoad( ).indexOf(this) - 1);
					if(Util.isOnLineCars(carInFront.currentX, carInFront.currentY, currentX, currentY, currentRoad.getGoingTo( ).getxCord( ), currentRoad.getGoingTo( ).getyCord( )))
					{
						activated = true;
					}
				}
			}
		}
		return "UsedUpTurn";
	}
	
	private String updateCarsPositionAndFuelBothCases(boolean forceFuel, double oldspeed, double distanceToGo)
	{
		currentX = currentRoad.getGoingTo( ).getxCord( );
		currentY = currentRoad.getGoingTo( ).getyCord( );
		if(currentRoad.getGoingTo( ).isAccepting(this, currentRoad, nextRoad, hasMoved))
		{
			return updateCarsPositionAndFuel(oldspeed, distanceToGo);
		}
		else
		{
			return forceFuelGeneric(forceFuel, oldspeed);
		}
	}
	
	
	private String forceFuelGeneric(boolean forceFuel, double oldspeed)
	{
		if(forceFuel)
		{
			endOfTurnActivities(oldspeed, currentSpeed);
			hasMoved = true;
			if(currentRoad.getGoingTo( ).isReconsideration( ))
			{
				if(currentRoad.getGoingTo( ).howLongTillAcceptingState(currentRoad) == 0)
				{
					failedAmount = failedAmount + 1;
				}
				verifyFailedAmount( );
			}
		}
		return "Failed to Move";
	}
	
	
	private String updateCarLocationAndFuel(double oldspeed, double distanceToGo)
	{
		endOfTurnActivities(oldspeed, currentSpeed);
		moveCarXandY(distanceToGo);
		failedAmount = 0;
		hasMoved = true;
		return "UsedUpTurn";
	}
	
	private String updateCarsPositionAndFuel(double oldspeed, double distanceToGo)
	{
		nextRoad = null;
		return updateCarLocationAndFuel(oldspeed, distanceToGo);
	}
	
	
	private void verifyFailedAmount()
	{
		if((currentRoad.getGoingTo( ).getClass( ).equals(Tjunction.class) && failedAmount > Config.TJUNCTIONRECHOOSEAMOUNT) || (currentRoad.getGoingTo( ).getClass( ).equals(CrossRoads.class) && failedAmount > Config.CROSSROADSRECHOOSEAMOUNT))
		{
			nextRoad = null;
			failedAmount = 0;
		}
	}
	
	private void moveCarXandY(double distanceToGo)
	{
		if(currentRoad.getDiffX( ) == 0)
		{
			if(currentRoad.getGoingTo( ).getyCord( ) > currentY)
			{
				currentY = currentY + distanceToGo;
			}
			else
			{
				currentY = currentY - distanceToGo;
			}
		}
		else if(currentRoad.getDiffY( ) == 0)
		{
			if(currentRoad.getGoingTo( ).getxCord( ) > currentX)
			{
				currentX = currentX + distanceToGo;
			}
			else
			{
				currentX = currentX - distanceToGo;
			}
		}
		else
		{
			double distanceX = distanceToGo * Math.cos(currentRoad.getAngleOfRoad( ));
			double distanceY = distanceToGo * Math.sin(currentRoad.getAngleOfRoad( ));
			currentX = currentX - distanceX;
			currentY = currentY - distanceY;
		}
		anyChange = true;
		hasMoved = true;
	}
	
	private void updateTimeOnRoads()
	{
		if(readingOfCurrentRoad && ! atIntersection)
		{
			timeTakenToTravelDownRoad++;
			if(Config.UPDATEMAXTIME && Config.UPDATEWEIGHTSAUTO && timeTakenToTravelDownRoad > currentRoad.maxArrayForUser[0])
			{
				currentRoad.maxArrayForUser[0] = timeTakenToTravelDownRoad;
			}
			if(Config.UPDATEAVERAGETIME && Config.UPDATEWEIGHTSAUTO && timeTakenToTravelDownRoad > currentRoad.getAverageTimesOnIntersection( )[0])
			{
				if(! hasAddedToRoadAverage)
				{
					currentRoad.getAverageTimesOnIntersection( )[0] = ((currentRoad.getAverageTimesOnIntersection( )[0] * currentRoad.getAmountOfAvg( )[0]) + timeTakenToTravelDownRoad) / (currentRoad.getAmountOfAvg( )[0] + 1);
					hasAddedToRoadAverage = true;
					currentRoad.getAmountOfAvg( )[0] = currentRoad.getAmountOfAvg( )[0] + 1;
				}
				else
				{
					currentRoad.getAverageTimesOnIntersection( )[0] = ((currentRoad.getAverageTimesOnIntersection( )[0] * currentRoad.getAmountOfAvg( )[0]) - timeTakenToTravelDownRoadPrevious + timeTakenToTravelDownRoad) / (currentRoad.getAmountOfAvg( )[0]);
				}
				timeTakenToTravelDownRoadPrevious = timeTakenToTravelDownRoad;
			}
		}
		if(atIntersection)
		{
			timeWaitingAtIntersection++;
			int indexOfConnection = currentRoad.getGoingTo( ).getIndex(currentRoad, nextRoad);
			if(Config.UPDATEMAXTIME && Config.UPDATEWEIGHTSAUTO && timeWaitingAtIntersection > currentRoad.maxArrayForUser[indexOfConnection])
			{
				currentRoad.maxArrayForUser[indexOfConnection] = timeWaitingAtIntersection;
			}
			if(Config.UPDATEAVERAGETIME && Config.UPDATEWEIGHTSAUTO && timeWaitingAtIntersection > currentRoad.getAverageTimesOnIntersection( )[indexOfConnection])
			{
				if(! hasAddedToRoadPass)
				{
					currentRoad.getAverageTimesOnIntersection( )[indexOfConnection] = ((currentRoad.getAverageTimesOnIntersection( )[indexOfConnection] * currentRoad.getAmountOfAvg( )[indexOfConnection]) + timeWaitingAtIntersection) / (currentRoad.getAmountOfAvg( )[indexOfConnection] + 1);
					hasAddedToRoadPass = true;
					currentRoad.getAmountOfAvg( )[indexOfConnection] = currentRoad.getAmountOfAvg( )[indexOfConnection] + 1;
				}
				else
				{
					currentRoad.getAverageTimesOnIntersection( )[indexOfConnection] = ((currentRoad.getAverageTimesOnIntersection( )[indexOfConnection] * currentRoad.getAmountOfAvg( )[indexOfConnection]) - timeTakenToPassIntersectionPrevious + timeWaitingAtIntersection) / (currentRoad.getAmountOfAvg( )[indexOfConnection]);
				}
				timeTakenToPassIntersectionPrevious = timeWaitingAtIntersection;
			}
		}
	}
	
	public void endOfTurnActivities(double oldSpeed, double currentSpeed)
	{
		updateTimeOnRoads( );
		fuelDependant.calculateFuelUsed(oldSpeed, currentSpeed);
	}
	
	public void setHasMoved(boolean hasMoved)
	{
		this.hasMoved = hasMoved;
	}
	
	public Sprite getCarsSprite()
	{
		return carsSprite;
	}
	
	public Road getNextRoad()
	{
		return nextRoad;
	}
	
	public Road getCurrentRoad()
	{
		return currentRoad;
	}
	
	public void setCurrentRoad(Road currentRoad)
	{
		this.currentRoad = currentRoad;
	}
	
	public void setNextRoad(Road nextRoad)
	{
		this.nextRoad = nextRoad;
	}
	
	public double getCurrentSpeed()
	{
		return currentSpeed;
	}
	
	public double getCurrentX()
	{
		return currentX;
	}
	
	public double getCurrentY()
	{
		return currentY;
	}
	
	public void setAtIntersection(boolean atIntersection)
	{
		this.atIntersection = atIntersection;
	}
	
	private boolean isValid()
	{
		return valid;
	}
	
	public void setValid(boolean valid)
	{
		this.valid = valid;
	}
	
	public PointOnRoad getEndGoal()
	{
		return endGoal;
	}
	
	public boolean isNotAddedToRoadPass()
	{
		return ! hasAddedToRoadPass;
	}
	
	public void setHasAddedToRoadPass(boolean hasAddedToRoadPass)
	{
		this.hasAddedToRoadPass = hasAddedToRoadPass;
	}
	
	public boolean isNotAddedToRoadAverage()
	{
		return ! hasAddedToRoadAverage;
	}
	
	public void setHasAddedToRoadAverage(boolean hasAddedToRoadAverage) { this.hasAddedToRoadAverage = hasAddedToRoadAverage; }
	
	public static void setAnyChange(boolean anyChange)
	{
		Car.anyChange = anyChange;
	}
	
	public static boolean isAnyChange()
	{
		return anyChange;
	}
	
	public int getFuelApplied()
	{
		return fuelApplied;
	}
	
	public void setFuelApplied(int fuelApplied)
	{
		this.fuelApplied = fuelApplied;
	}
	
	public int getTimeTakenToTravelDownRoad()
	{
		return timeTakenToTravelDownRoad;
	}
	
	public void setTimeTakenToTravelDownRoad(int timeTakenToTravelDownRoad) { this.timeTakenToTravelDownRoad = timeTakenToTravelDownRoad; }
	
	public int getTimeWaitingAtIntersection()
	{
		return timeWaitingAtIntersection;
	}
	
	public void setTimeWaitingAtIntersection(int timeWaitingAtIntersection) { this.timeWaitingAtIntersection = timeWaitingAtIntersection; }
	
	public void setTimeTakenToTravelDownRoadPrevious(double timeTakenToTravelDownRoadPrevious) { this.timeTakenToTravelDownRoadPrevious = timeTakenToTravelDownRoadPrevious; }
	
	public void setTimeTakenToPassIntersectionPrevious(double timeTakenToPassIntersectionPrevious) { this.timeTakenToPassIntersectionPrevious = timeTakenToPassIntersectionPrevious; }
	
	public boolean isReadingOfCurrentRoad()
	{
		return readingOfCurrentRoad;
	}
	
	public double getTimeTakenToTravelDownRoadPrevious()
	{
		return timeTakenToTravelDownRoadPrevious;
	}
	
	public double getTimeTakenToPassIntersectionPrevious()
	{
		return timeTakenToPassIntersectionPrevious;
	}
	
	public void setReadingOfCurrentRoad(boolean readingOfCurrentRoad)
	{
		this.readingOfCurrentRoad = readingOfCurrentRoad;
	}
	
	public FuelDependancies getFuelDepandant()
	{
		return fuelDependant;
	}
}
