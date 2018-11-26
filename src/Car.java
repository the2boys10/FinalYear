import org.graphstream.ui.spriteManager.Sprite;


/**
 * This class consists information relating to the vehicles within the simulation
 * containing where their goal is within the simulation as well as their current location
 * and speed perameters.
 *
 * @author Robert Johnson
 * @see PointOnRoad
 * @see FuelDependancies
 */
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
	
	/**
	 * Constructor to create a car
	 * @param xCord The starting x Cord of the vehicle
	 * @param yCord The starting y Cord of the vehicle
	 * @param road The starting road of the vehicle
	 * @param endRoad The goal of the vehicle (Where it would like to end up)
	 * @param activation Whether the care has been activated upon placing it on a road
	 *                   this could be false if the vehicle behind the current vehicle is
	 *                   unable to slow down in time.
	 * @param fuelChoice The fuel choice of the vehicle, can take values of 0,1,2 increasing the fuel
	 *                   usage used by a factor of 10
	 * @param carSprite The physical representation of the car in the simulation.
	 */
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
	
	/**
	 * Method which takes the information of another car within the simulation and verifies that the current car
	 * is able to slow down to avoid a collision i.e such a method can be useful when inserting a car into the
	 * simulation.
	 * @param xCord The xCord of the vehicle we are comparing against
	 * @param yCord The yCord of the vehicle we are comparing against
	 * @return Whether the car in question is able to slow down quick enough such that a collision does not happen.
	 */
	public boolean canCarStopInTime(double xCord, double yCord)
	{
		double timeTakenToSlowToStop = - currentSpeed / - Config.CARACCELERATIONANDDECELERATION;
		double distanceThatWillPassSlowingDown = 0.5 * (currentSpeed) * timeTakenToSlowToStop;
		return Util.getLengthOfLine(currentX, currentY, xCord, yCord) > distanceThatWillPassSlowingDown;
	}
	
	/**
	 * Method to work out what the current goal is, the goal can take 3 perameters "Goal" in which case the vehicle we are looking
	 * at is on the road that contains its goal, "EOCR" in which care the goal is not on the current road and the car is
	 * the front most car in a road, "CIF" the car is following another car on the same road as itself. It also returns
	 * the distance left to the goal and the actual distance to the goal.
	 * @return A goal object discribing the current goal and the distance to it.
	 */
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
	
	/**
	 * Method to move a car within the simulation if possible, else it keeps the vehicle in the same place.
	 * @param forceFuel If the vehicle was unable to move throughout the current timestep however we would like to
	 *                  penalise it due to not moving then we can force the fuel usage for the timestep
	 * @return A string representing if the car was unable to move "Failed to Move", moved "UsedUpTurn" or reached
	 * its goal "Finished"
	 */
	public String moveCar(boolean forceFuel)
	{
		// if the car hasn't moved in the current timestep
		if(! hasMoved)
		{
			// if the car has been activated
			if(activated)
			{
				// work out the current goal and whether we can stop in time to make it to the goal
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
				// if slowing down in the current timestep would cause us to go into a negative speed i.e reverse set it to 0.
				if(currentSpeed - (Config.CARACCELERATIONANDDECELERATION * Config.SIMACCELERATION) < 0 && currentSpeed != 0)
				{
					currentSpeed = 0;
				}
				else
				{
					// if travelling the distance we would travel accelerating would be less than the distance we have left
					// then accelerate
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
					// else if if travelling at the same speed would be less than the distance we have left continue at
					// current speed
					else if(Double.compare(goal.getDistanceLeft( ), distance2) >= 0)
					{
						currentSpeed = oldspeed;
						distanceToMove = currentSpeed * Config.SIMACCELERATION;
					}
					// else slow down.
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
				// make sure the spacing to the car infront is maintained.
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
				// make the move.
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
			// else the vehicle did not move, check the reasoning for this and update fuel ect.
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
	
	/**
	 * Method used to update the cars location and fuel usage after moving.
	 * @param forceFuel If we would like to force fuel usage in the case that the car was unable to move previously.
	 * @param oldspeed The old speed of the vehicle
	 * @param distanceToGo The distance that the car needs to move.
	 * @return A string representing if the car was able to move "UsedUpTurn", reached its goal "Finished" or failed to move
	 * "Failed to Move"
	 */
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
	
	/**
	 * Method used to force fuel if the intersection in question allows multiple different decisions about exiting the
	 * intersection set a cap for reconsideration of route
	 * @param forceFuel If we would like to force fuel usage in the case that the car was unable to move previously.
	 * @param oldspeed The old speed of the vehicle.
	 * @return A string representing if the car was able to move "UsedUpTurn", reached its goal "Finished" or failed to move
	 * "Failed to Move"
	 */
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
	
	/**
	 * Method used to move a vehicle in the case that it is able to successfully.
	 * @param oldspeed The old speed of the vehicle
	 * @param distanceToGo The distance that the car needs to move.
	 * @return A string representing if the car was able to move "UsedUpTurn", reached its goal "Finished" or failed to move
	 * "Failed to Move"
	 */
	private String updateCarLocationAndFuel(double oldspeed, double distanceToGo)
	{
		endOfTurnActivities(oldspeed, currentSpeed);
		moveCarXandY(distanceToGo);
		failedAmount = 0;
		hasMoved = true;
		return "UsedUpTurn";
	}
	
	/**
	 * In the case where the vehicle was able to move, the car should reconsider their next road to take.
	 * @param oldspeed The old speed of the vehicle
	 * @param distanceToGo The distance that the car needs to move.
	 * @return A string representing if the car was able to move "UsedUpTurn", reached its goal "Finished" or failed to move
	 * "Failed to Move"
	 */
	private String updateCarsPositionAndFuel(double oldspeed, double distanceToGo)
	{
		nextRoad = null;
		return updateCarLocationAndFuel(oldspeed, distanceToGo);
	}
	
	
	/**
	 * Check how many failed attempts the current car has had at the current intersection and make it rechoose the road
	 * if it has been there past a threshold.
	 */
	private void verifyFailedAmount()
	{
		if((currentRoad.getGoingTo( ).getClass( ).equals(Tjunction.class) && failedAmount > Config.TJUNCTIONRECHOOSEAMOUNT) || (currentRoad.getGoingTo( ).getClass( ).equals(CrossRoads.class) && failedAmount > Config.CROSSROADSRECHOOSEAMOUNT))
		{
			nextRoad = null;
			failedAmount = 0;
		}
	}
	
	/**
	 * Move the car in the simulation the stated distance
	 * @param distanceToGo The distance to move the car.
	 */
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
	
	/**
	 * Method is used to update weights on roads in a dynamic system where weights are updated on the fly based upon
	 * previous time waiting at an intersection or travelling on a road.
	 */
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
	
	/**
	 * Update the time waiting on a road or at an intersection and calulates,updates the cars own FuelDependancies object
	 * @param oldSpeed The speed of the vehicle at the start of the timestep
	 * @param currentSpeed The speed of the vehicle at the end of the timestep
	 */
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
	
	public void setHasAddedToRoadAverage(boolean hasAddedToRoadAverage)
	{
		this.hasAddedToRoadAverage = hasAddedToRoadAverage;
	}
	
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
	
	public void setTimeTakenToTravelDownRoad(int timeTakenToTravelDownRoad)
	{
		this.timeTakenToTravelDownRoad = timeTakenToTravelDownRoad;
	}
	
	public int getTimeWaitingAtIntersection()
	{
		return timeWaitingAtIntersection;
	}
	
	public void setTimeWaitingAtIntersection(int timeWaitingAtIntersection)
	{
		this.timeWaitingAtIntersection = timeWaitingAtIntersection;
	}
	
	public void setTimeTakenToTravelDownRoadPrevious(double timeTakenToTravelDownRoadPrevious)
	{
		this.timeTakenToTravelDownRoadPrevious = timeTakenToTravelDownRoadPrevious;
	}
	
	public void setTimeTakenToPassIntersectionPrevious(double timeTakenToPassIntersectionPrevious)
	{
		this.timeTakenToPassIntersectionPrevious = timeTakenToPassIntersectionPrevious;
	}
	
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
