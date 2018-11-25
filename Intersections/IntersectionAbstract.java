
import java.util.LinkedList;

public abstract class IntersectionAbstract
{
	private final String name;
	private Road[] connectionsIn;
	private Road[] connectionsOut;
	private double xCord;
	private double yCord;
	private int amountOfRoadsInAdded = 0;
	
	IntersectionAbstract(double xCord, double yCord, double name, int degreeIn)
	{
		this.name = (int) name + "";
		this.setxCord(xCord);
		this.setyCord(yCord);
		setConnectionsIn(new Road[degreeIn]);
	}
	
	public int getIndex(Road goingFrom, Road goingTo)
	{
		if(goingTo == null)
		{
			return 0;
		}
		int i;
		for(i = 0; i < connectionsIn.length; i++)
		{
			if(goingFrom.equals(connectionsIn[i]))
			{
				break;
			}
		}
		for(int j = 0; j < connectionsOut.length; j++)
		{
			if(goingTo.equals(connectionsOut[(i + j) % connectionsOut.length]))
			{
				return j;
			}
		}
		return - 1;
	}
	
	public double getRemainingDistance(Road currentRoad, PointOnRoad endGoal, double X1, double Y1, double X2, double Y2)
	{
		return Util.getLengthOfLine(X1, Y1, X2, Y2);
	}
	
	public int getIndexOfRoadIncoming(Road a)
	{
		for(int i = 0; i < connectionsIn.length; i++)
		{
			if(a.equals(connectionsIn[i]))
			{
				return i;
			}
		}
		return - 1;
	}
	
	void sortOutAdditionAndRemoval(Car car, Road b)
	{
		car.getCurrentRoad( ).removeFrom(car);
		updateArraysOnCarFinish(car, car.getCurrentRoad( ), b);
		car.setCurrentRoad(b);
		car.getCurrentRoad( ).addTo(car);
		if(Config.REASKUSERTOCHANGEONADDITION)
		{
			UserInputFile.weightsOfEdgesAfterInitialization(car, car.getCurrentRoad( ), "CarAdded");
		}
		car.setNextRoad(null);
		car.setValid(true);
		car.setAtIntersection(false);
		car.setReadingOfCurrentRoad(true);
		if(b.getColorOfCarsOnRoad( ) == 'R')
			car.getCarsSprite( ).addAttribute("ui.style", "fill-color: rgba(255,0,0,125);");
		else
			car.getCarsSprite( ).addAttribute("ui.style", "fill-color: rgba(0,0,0,125);");
	}
	
	void updateArraysOnCarFinish(Car car, Road goingFrom, Road goingTo)
	{
		int indexOfConnection = goingFrom.getGoingTo( ).getIndex(goingFrom, goingTo);
		if(Config.UPDATEAVERAGETIME)
		{
			if(car.isNotAddedToRoadPass( ))
			{
				goingFrom.getAverageTimesOnIntersection( )[indexOfConnection] = ((goingFrom.getAverageTimesOnIntersection( )[indexOfConnection] * goingFrom.getAmountOfAvg( )[indexOfConnection]) + car.getTimeWaitingAtIntersection( )) / (goingFrom.getAmountOfAvg( )[indexOfConnection] + 1);
				goingFrom.getAmountOfAvg( )[indexOfConnection] = goingFrom.getAmountOfAvg( )[indexOfConnection] + 1;
				car.setHasAddedToRoadPass(false);
				car.setTimeTakenToPassIntersectionPrevious(0);
			}
			else
			{
				goingFrom.getAverageTimesOnIntersection( )[indexOfConnection] = ((goingFrom.getAverageTimesOnIntersection( )[indexOfConnection] * goingFrom.getAmountOfAvg( )[indexOfConnection]) - car.getTimeTakenToPassIntersectionPrevious( ) + car.getTimeWaitingAtIntersection( )) / (goingFrom.getAmountOfAvg( )[indexOfConnection]);
				car.setTimeTakenToPassIntersectionPrevious(0);
				car.setHasAddedToRoadPass(false);
			}
			if(car.isReadingOfCurrentRoad( ))
			{
				if(car.isNotAddedToRoadAverage( ))
				{
					goingFrom.getAverageTimesOnIntersection( )[0] = ((goingFrom.getAverageTimesOnIntersection( )[0] * goingFrom.getAmountOfAvg( )[0]) + car.getTimeTakenToTravelDownRoad( )) / (goingFrom.getAmountOfAvg( )[0] + 1);
					car.setHasAddedToRoadAverage(false);
					goingFrom.getAmountOfAvg( )[0] = goingFrom.getAmountOfAvg( )[0] + 1;
					car.setTimeTakenToTravelDownRoadPrevious(0);
				}
				else
				{
					goingFrom.getAverageTimesOnIntersection( )[0] = ((goingFrom.getAverageTimesOnIntersection( )[0] * car.getCurrentRoad( ).getAmountOfAvg( )[0]) - car.getTimeTakenToTravelDownRoadPrevious( ) + car.getTimeTakenToTravelDownRoad( )) / (goingFrom.getAmountOfAvg( )[0]);
					car.setTimeTakenToTravelDownRoadPrevious(0);
					car.setHasAddedToRoadAverage(false);
				}
			}
		}
		if(Config.UPDATEMAXTIME)
		{
			if(car.getTimeTakenToTravelDownRoad( ) > goingFrom.maxArrayForUser[0] && car.isReadingOfCurrentRoad( ))
			{
				goingFrom.maxArrayForUser[0] = car.getTimeTakenToTravelDownRoad( );
			}
			if(car.getTimeWaitingAtIntersection( ) > goingFrom.maxArrayForUser[indexOfConnection])
			{
				goingFrom.maxArrayForUser[indexOfConnection] = car.getTimeWaitingAtIntersection( );
			}
		}
		if(Config.UPDATEMINTIME)
		{
			if(car.getTimeTakenToTravelDownRoad( ) < goingFrom.minArrayForUser[0] && car.isReadingOfCurrentRoad( ))
			{
				goingFrom.minArrayForUser[0] = car.getTimeTakenToTravelDownRoad( );
			}
			if(car.getTimeWaitingAtIntersection( ) < goingFrom.minArrayForUser[indexOfConnection])
			{
				goingFrom.minArrayForUser[indexOfConnection] = car.getTimeWaitingAtIntersection( );
			}
		}
		if(Config.REASKUSERTOCHANGEONREMOVAL)
		{
			UserInputFile.weightsOfEdgesAfterInitialization(car, car.getCurrentRoad( ), "CarRemoved");
		}
		car.setTimeWaitingAtIntersection(0);
		car.setTimeTakenToTravelDownRoad(0);
	}
	
	public void addInConnections(Road tempRoad)
	{
		getConnectionsIn( )[amountOfRoadsInAdded] = tempRoad;
		amountOfRoadsInAdded++;
		
	}
	
	public LinkedList<Road> validRoads(Road a)
	{
		LinkedList<Road> temp = new LinkedList<Road>( );
		int in;
		for(in = 0; in < connectionsIn.length; in++)
		{
			if(connectionsIn[in].equals(a))
			{
				break;
			}
		}
		for(int i = 0; i < getConnectionsOut( ).length; i++)
		{
			if(! getConnectionsOut( )[(in + 1 + i) % getConnectionsOut( ).length].hasBeenChecked)
			{
				temp.add(getConnectionsOut( )[(in + 1 + i) % getConnectionsOut( ).length]);
			}
		}
		return temp;
	}
	
	public boolean isAccepting(Car car, Road currentRoad, Road nextRoad, boolean hasCarMoved)
	{
		if(nextRoad.hasSpace( ) > 0)
		{
			sortOutAdditionAndRemoval(car, nextRoad);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public Road getNextRoad(Car caller, Road nextRoad)
	{
		if(nextRoad == null)
		{
			return UserInputFile.getNextRoad(caller);
		}
		return nextRoad;
	}
	
	public void sortOutOutgoingConnections()
	{
		Road[] arrayOfConnectionsIn = new Road[getConnectionsIn( ).length];
		for(int i = 0; i < getConnectionsIn( ).length; i++)
		{
			for(int j = 0; j < getConnectionsOut( ).length; j++)
			{
				if(getConnectionsIn( )[i].getGoingFrom( ).equals(getConnectionsOut( )[j].getGoingTo( )))
				{
					arrayOfConnectionsIn[j] = getConnectionsIn( )[i];
				}
			}
		}
		setConnectionsIn(arrayOfConnectionsIn);
	}
	
	public double getxCord()
	{
		return xCord;
	}
	
	private void setxCord(double xCord)
	{
		this.xCord = xCord;
	}
	
	public double getyCord()
	{
		return yCord;
	}
	
	private void setyCord(double yCord)
	{
		this.yCord = yCord;
	}
	
	Road[] getConnectionsOut()
	{
		return connectionsOut;
	}
	
	private void setConnectionsOut(Road[] connectionsOut)
	{
		this.connectionsOut = connectionsOut;
	}
	
	public Road[] getConnectionsIn()
	{
		return connectionsIn;
	}
	
	private void setConnectionsIn(Road[] connectionsIn)
	{
		this.connectionsIn = connectionsIn;
	}
	
	public void continueInterval() { }
	
	public int getInternalBuffer()
	{
		return 0;
	}
	
	public boolean isReconsideration()
	{
		return true;
	}
	
	public int howLongTillAcceptingState(Road goingFrom)
	{
		return 0;
	}
	
	public void addOutConnections(Road[] outConnections)
	{
		setConnectionsOut(outConnections);
	}
	
	public String getName()
	{
		return name;
	}
}
