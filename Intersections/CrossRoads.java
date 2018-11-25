
import java.util.LinkedList;

public class CrossRoads extends IntersectionAbstract
{
	private int interval;
	private int currentInterval;
	private final int COOLDOWNAMOUNT = 30;
	private String lastPast = "ROW";
	private int cooldown = 0;
	private final int[] howManyPassedSoFar = new int[2];
	private final int[] howManyAcceptedDuringIntervalMax = new int[4];
	
	public CrossRoads(double xCord, double yCord, double interval, double name, int degree)
	{
		super(xCord, yCord, name, degree);
		this.setInterval((int) interval);
		currentInterval = 0;
	}
	
	@Override
	public void continueInterval()
	{
		int previousInterval = currentInterval;
		currentInterval = ((currentInterval + getInterval( ) + 1) % (getInterval( ) + getInterval( ))) - getInterval( );
		if(currentInterval > 0 && previousInterval < 0)
		{
			if(howManyPassedSoFar[0] > getHowManyAcceptedDuringIntervalMax( )[0])
			{
				getHowManyAcceptedDuringIntervalMax( )[0] = howManyPassedSoFar[0];
			}
			else if(howManyPassedSoFar[1] > getHowManyAcceptedDuringIntervalMax( )[2])
			{
				getHowManyAcceptedDuringIntervalMax( )[2] = howManyPassedSoFar[1];
			}
			howManyPassedSoFar[0] = howManyPassedSoFar[1] = 0;
		}
		if(previousInterval > 0 && currentInterval < 0)
		{
			if(howManyPassedSoFar[0] > getHowManyAcceptedDuringIntervalMax( )[1])
			{
				getHowManyAcceptedDuringIntervalMax( )[1] = howManyPassedSoFar[0];
			}
			else if(howManyPassedSoFar[1] > getHowManyAcceptedDuringIntervalMax( )[3])
			{
				getHowManyAcceptedDuringIntervalMax( )[3] = howManyPassedSoFar[1];
			}
			howManyPassedSoFar[0] = howManyPassedSoFar[1] = 0;
		}
		if(cooldown != 0)
		{
			cooldown--;
			if(cooldown == 0)
			{
				lastPast = "ROW";
			}
		}
	}
	
	@Override
	public boolean isAccepting(Car car, Road a, Road b, boolean hasCarMoved)
	{
		if(currentInterval >= 0 && currentInterval <= getInterval( ) - COOLDOWNAMOUNT)
		{
			if(a.equals(getConnectionsIn( )[1]))
			{
				if(b.equals(getConnectionsOut( )[2]) || b.equals(getConnectionsOut( )[3]))
				{
					if(wantToMoveIntoRightOfWay(b))
					{
						super.sortOutAdditionAndRemoval(car, b);
						howManyPassedSoFar[0]++;
						return true;
					}
				}
				else if(b.equals(getConnectionsOut( )[0]))
				{
					if(wantToMoveNROW(b, 3, 2, hasCarMoved))
					{
						super.sortOutAdditionAndRemoval(car, b);
						howManyPassedSoFar[0]++;
						return true;
					}
				}
			}
			else if(a.equals(getConnectionsIn( )[3]))
			{
				if(b.equals(getConnectionsOut( )[0]) || b.equals(getConnectionsOut( )[1]))
				{
					if(wantToMoveIntoRightOfWay(b))
					{
						super.sortOutAdditionAndRemoval(car, b);
						howManyPassedSoFar[1]++;
						return true;
					}
				}
				else if(b.equals(getConnectionsOut( )[2]))
				{
					if(wantToMoveNROW(b, 1, 0, hasCarMoved))
					{
						super.sortOutAdditionAndRemoval(car, b);
						howManyPassedSoFar[1]++;
						return true;
					}
				}
			}
		}
		else if(currentInterval < - COOLDOWNAMOUNT)
		{
			if(a.equals(getConnectionsIn( )[0]))
			{
				if(b.equals(getConnectionsOut( )[2]) || b.equals(getConnectionsOut( )[1]))
				{
					if(wantToMoveIntoRightOfWay(b))
					{
						super.sortOutAdditionAndRemoval(car, b);
						howManyPassedSoFar[0]++;
						return true;
					}
				}
				else if(b.equals(getConnectionsOut( )[3]))// entering at road 3
				{
					if(wantToMoveNROW(b, 2, 1, hasCarMoved))
					{
						super.sortOutAdditionAndRemoval(car, b);
						howManyPassedSoFar[0]++;
						return true;
					}
				}
			}
			else if(a.equals(getConnectionsIn( )[2]))
			{
				if(b.equals(getConnectionsOut( )[3]) || b.equals(getConnectionsOut( )[0]))
				{
					if(wantToMoveIntoRightOfWay(b))
					{
						super.sortOutAdditionAndRemoval(car, b);
						howManyPassedSoFar[1]++;
						return true;
					}
				}
				else if(b.equals(getConnectionsOut( )[1]))
				{
					if(wantToMoveNROW(b, 0, 3, hasCarMoved))
					{
						super.sortOutAdditionAndRemoval(car, b);
						howManyPassedSoFar[1]++;
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean wantToMoveIntoRightOfWay(Road b)
	{
		if(b.hasSpace( ) > 0)
		{
			if(! (lastPast.equals("NROW") && cooldown > 0))
			{
				lastPast = "ROW";
				cooldown = COOLDOWNAMOUNT;
				return true;
			}
		}
		return false;
	}
	
	
	private boolean wantToMoveNROW(Road b, int OppositeRoad, int otherRoadsNROW, boolean hasCarMoved)
	{
		if(b.hasSpace( ) > 0) // we have space on the road we want to go to
		{
			if(lastPast.equals("ROW") && cooldown > 0) // if there is currently a ROW car moving through
			{
				return false;
			}
			else if(lastPast.equals("NROW") && cooldown >= COOLDOWNAMOUNT - 1) // if it has been 1 second before last NROW car has passed then move.
			{
				lastPast = "NROW";
				cooldown = COOLDOWNAMOUNT;
				return true;
			}
			else// if its ROW but the cooldown is 0, or its NROW and the cooldown is less than 2
			{
				if(getConnectionsIn( )[OppositeRoad].getCarsOnRoad( ).size( ) > 0)
				{
					Car frontMostCar = getConnectionsIn( )[OppositeRoad].getCarsOnRoad( ).get(0);
					if(frontMostCar.getNextRoad( ) != null) // the car is waiting to move to an already existing choice
					{
						Road OtherCarsGoal = frontMostCar.getNextRoad( );
						if(OtherCarsGoal.equals(getConnectionsOut( )[otherRoadsNROW]) || OtherCarsGoal.hasSpace( ) <= 0)// the other cars aim is to move and is NROW so just move.
						{
							lastPast = "NROW";
							cooldown = COOLDOWNAMOUNT;
							return true;
						}
						else // you cant move
						{
							return false;
						}
					}
					else
					{
						if(frontMostCar.getCurrentRoad( ).hasCarWithinSeconds(COOLDOWNAMOUNT / 10.0, hasCarMoved))
						{
							return false;
						}
						else
						{
							lastPast = "NROW";
							cooldown = (COOLDOWNAMOUNT);
							return true;
						}
					}
				}
				else
				{
					lastPast = "NROW";
					cooldown = (COOLDOWNAMOUNT);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public LinkedList<Road> validRoads(Road a)
	{
		int roadGivenIndex;
		for(roadGivenIndex = 0; roadGivenIndex < getConnectionsIn( ).length; roadGivenIndex++)
		{
			if(getConnectionsIn( )[roadGivenIndex].equals(a))
			{
				break;
			}
		}
		LinkedList<Road> validRoad = new LinkedList<Road>( );
		for(int i = 0; i < getConnectionsOut( ).length; i++)
		{
			if(! getConnectionsOut( )[(roadGivenIndex + i) % getConnectionsOut( ).length].getGoingTo( ).equals(a.getGoingFrom( )) && ! getConnectionsOut( )[(roadGivenIndex + i) % getConnectionsOut( ).length].hasBeenChecked)
			{
				validRoad.add(getConnectionsOut( )[(roadGivenIndex + i) % getConnectionsOut( ).length]);
			}
		}
		return validRoad;
	}
	
	@Override
	public int howLongTillAcceptingState(Road goingFrom)
	{
		int indexOfRoad = super.getIndexOfRoadIncoming(goingFrom);
		if(indexOfRoad == 0 || indexOfRoad == 2)
		{
			if(currentInterval >= 0 && currentInterval <= getInterval( ) - COOLDOWNAMOUNT)
			{
				return 0;
			}
			else
			{
				if(currentInterval > 0)
				{
					return getInterval( ) - currentInterval + getInterval( );
				}
				else
				{
					return currentInterval;
				}
			}
		}
		else
		{
			if(currentInterval < - COOLDOWNAMOUNT)
			{
				return 0;
			}
			else
			{
				if(currentInterval < 0)
				{
					return - currentInterval + getInterval( );
				}
				else
				{
					return getInterval( ) - currentInterval;
				}
			}
		}
	}
	
	public int howLongTillStopAccepting(Road goingFrom)
	{
		int indexOfRoad = super.getIndexOfRoadIncoming(goingFrom);
		if(indexOfRoad == 0 || indexOfRoad == 2)
		{
			if(currentInterval >= 0 && currentInterval <= getInterval( ) - COOLDOWNAMOUNT)
			{
				return (getInterval( ) - COOLDOWNAMOUNT) - currentInterval;
			}
		}
		else
		{
			if(currentInterval < - COOLDOWNAMOUNT)
			{
				return - COOLDOWNAMOUNT - currentInterval;
			}
		}
		return - 1;
	}
	
	public int[] getHowManyAcceptedDuringIntervalMax()
	{
		return howManyAcceptedDuringIntervalMax;
	}
	
	public int getInterval()
	{
		return interval;
	}
	
	private void setInterval(int interval)
	{
		this.interval = interval;
	}
}