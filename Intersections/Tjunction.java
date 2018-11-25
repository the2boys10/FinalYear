
import java.util.LinkedList;


public class Tjunction extends IntersectionAbstract
{
	private final LinkedList<DelayAndCooldown> delays = new LinkedList<DelayAndCooldown>( );
	private int nonROWroad;
	
	public Tjunction(double xCord, double yCord, double name, int degree)
	{
		super(xCord, yCord, name, degree);
	}
	
	@Override
	public void continueInterval()
	{
		for(int i = 0; i < delays.size( ); i++)
		{
			delays.get(i).cooldown--;
			if(delays.get(i).cooldown == 0)
			{
				delays.remove(i);
				i--;
			}
		}
	}
	
	@Override
	public void sortOutOutgoingConnections()
	{
		super.sortOutOutgoingConnections( );
		double tempHighestAngle = 0;
		double tempAngleDifference = difference(getConnectionsOut( )[1].getAngleOfRoad( ), getConnectionsOut( )[0].getAngleOfRoad( ));
		if(tempAngleDifference > tempHighestAngle)
		{
			tempHighestAngle = tempAngleDifference;
			nonROWroad = 2;
		}
		tempAngleDifference = difference(getConnectionsOut( )[2].getAngleOfRoad( ), getConnectionsOut( )[1].getAngleOfRoad( ));
		if(tempAngleDifference > tempHighestAngle)
		{
			tempHighestAngle = tempAngleDifference;
			nonROWroad = 0;
		}
		tempAngleDifference = difference(getConnectionsOut( )[0].getAngleOfRoad( ), getConnectionsOut( )[2].getAngleOfRoad( ));
		if(tempAngleDifference > tempHighestAngle)
		{
			nonROWroad = 1;
		}
	}
	
	private double difference(double a, double b)
	{
		if(a > b)
		{
			return a - b;
		}
		else
		{
			return b - a;
		}
	}
	
	@Override
	public boolean isAccepting(Car car, Road a, Road b, boolean hasCarMoved)
	{
		if(b == null)
		{
			System.out.println(car.getCarsSprite( ).getId( ) + " broke" + " car was trying to get to " + car.getEndGoal( ).specRoad.getGoingFrom( ).getName( ) + " ," + car.getEndGoal( ).specRoad.getGoingTo( ).getName( ) + " is currently at " + car.getCurrentRoad( ).getGoingFrom( ).getName( ) + " , " + car.getCurrentRoad( ).getGoingTo( ).getName( ) + " in method tjunction");
		}
		if(b.hasSpace( ) > 0)
		{
			int CARWITHINHOWMANYSECONDS = 3;
			int COOLDOWNPERIOD = 3;
			if(a.equals(getConnectionsIn( )[nonROWroad]))
			{
				if(b.equals(getConnectionsOut( )[(nonROWroad + 1) % 3]) && (doesNotContainDelay("1+(2to1)")))
				{
					if(getConnectionsIn( )[(nonROWroad + 2) % 3].hasCarWithinSeconds(CARWITHINHOWMANYSECONDS, hasCarMoved))
					{
						if(getConnectionsIn( )[(nonROWroad + 2) % 3].getCarsOnRoad( ).get(0).getNextRoad( ) != null)
						{
							if(getConnectionsIn( )[(nonROWroad + 2) % 3].getCarsOnRoad( ).get(0).getNextRoad( ).equals(getConnectionsOut( )[(nonROWroad) % 3]))
							{
								updateDelays("3to2", COOLDOWNPERIOD);
								super.sortOutAdditionAndRemoval(car, b);
								return true;
							}
							else
							{
								return false;
							}
						}
						else
						{
							return false;
						}
					}
					else
					{
						updateDelays("3to2", COOLDOWNPERIOD);
						super.sortOutAdditionAndRemoval(car, b);
						return true;
					}
				}
				else if(b.equals(getConnectionsOut( )[(nonROWroad + 2) % 3]) && (doesNotContainDelay("1+(2to1)") && doesNotContainDelay("1to3") && doesNotContainDelay("3+(1to3)")))
				{
					if(getConnectionsIn( )[(nonROWroad + 2) % 3].hasCarWithinSeconds(CARWITHINHOWMANYSECONDS, hasCarMoved))
					{
						if(getConnectionsIn( )[(nonROWroad + 2) % 3].getCarsOnRoad( ).get(0).getNextRoad( ) != null)
						{
							if(getConnectionsIn( )[(nonROWroad + 2) % 3].getCarsOnRoad( ).get(0).getNextRoad( ).equals(getConnectionsOut( )[nonROWroad]))
							{
								if(getConnectionsIn( )[(nonROWroad + 1) % 3].hasCarWithinSeconds(CARWITHINHOWMANYSECONDS, hasCarMoved))
								{
									if(getConnectionsIn( )[(nonROWroad + 1) % 3].getCarsOnRoad( ).get(0).getNextRoad( ) != null)
									{
										if(getConnectionsIn( )[(nonROWroad + 1) % 3].getCarsOnRoad( ).get(0).getNextRoad( ).hasSpace( ) <= 0)
										{
											updateDelays("2+(3to2)", COOLDOWNPERIOD);
											super.sortOutAdditionAndRemoval(car, b);
											return true;
										}
										else
										{
											return false;
										}
									}
									else
									{
										return false;
									}
								}
								else
								{
									updateDelays("2+(3to2)", COOLDOWNPERIOD);
									super.sortOutAdditionAndRemoval(car, b);
									return true;
								}
							}
							else
							{
								return false;
							}
						}
						else
						{
							return false;
						}
					}
					else if(getConnectionsIn( )[(nonROWroad + 1) % 3].hasCarWithinSeconds(CARWITHINHOWMANYSECONDS, hasCarMoved))
					{
						if(getConnectionsIn( )[(nonROWroad + 1) % 3].getCarsOnRoad( ).get(0).getNextRoad( ) != null)
						{
							if(getConnectionsIn( )[(nonROWroad + 1) % 3].getCarsOnRoad( ).get(0).getNextRoad( ).hasSpace( ) <= 0)
							{
								updateDelays("2+(3to2)", COOLDOWNPERIOD);
								super.sortOutAdditionAndRemoval(car, b);
								return true;
							}
							else
							{
								return false;
							}
						}
						else
						{
							return false;
						}
					}
					else
					{
						updateDelays("2+(3to2)", COOLDOWNPERIOD);
						super.sortOutAdditionAndRemoval(car, b);
						return true;
					}
				}
			}
			else if(a.equals(getConnectionsIn( )[(nonROWroad + 1) % 3]))
			{
				if(b.equals(getConnectionsOut( )[(nonROWroad) % 3]) && (doesNotContainDelay("1+(2to1)") && doesNotContainDelay("2to1") && doesNotContainDelay("2+(3to2)")))
				{
					if(getConnectionsIn( )[(nonROWroad + 2) % 3].hasCarWithinSeconds(CARWITHINHOWMANYSECONDS, hasCarMoved))
					{
						if(getConnectionsIn( )[(nonROWroad + 2) % 3].getCarsOnRoad( ).get(0).getNextRoad( ) != null)
						{
							if(getConnectionsIn( )[(nonROWroad + 2) % 3].getCarsOnRoad( ).get(0).getNextRoad( ).hasSpace( ) <= 0)
							{
								updateDelays("3+(1to3)", COOLDOWNPERIOD);
								super.sortOutAdditionAndRemoval(car, b);
								return true;
							}
							else
							{
								return false;
							}
						}
						else
						{
							return false;
						}
					}
					else
					{
						updateDelays("3+(1to3)", COOLDOWNPERIOD);
						super.sortOutAdditionAndRemoval(car, b);
						return true;
					}
				}
				else if(b.equals(getConnectionsOut( )[(nonROWroad + 2) % 3]) && (doesNotContainDelay("2+(3to2)")))
				{
					updateDelays("1to3", COOLDOWNPERIOD);
					super.sortOutAdditionAndRemoval(car, b);
					return true;
				}
			}
			else if(a.equals(getConnectionsIn( )[(nonROWroad + 2) % 3]))
			{
				if(b.equals(getConnectionsOut( )[(nonROWroad) % 3]) && (doesNotContainDelay("3+(1to3)")))
				{
					updateDelays("2to1", COOLDOWNPERIOD);
					super.sortOutAdditionAndRemoval(car, b);
					return true;
				}
				else if((doesNotContainDelay("3+(1to3)") && (doesNotContainDelay("2+(3to2)"))))
				{
					updateDelays("1+(2to1)", COOLDOWNPERIOD);
					super.sortOutAdditionAndRemoval(car, b);
					return true;
				}
			}
		}
		return false;
	}
	
	
	private boolean doesNotContainDelay(String delay)
	{
		for(DelayAndCooldown delay1 : delays)
		{
			if(delay1.delay.equals(delay))
			{
				return false;
			}
		}
		return true;
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
		int indexOfIncomingRoad = 0;
		for(int j = 0; j < getConnectionsIn( ).length; j++)
		{
			if(getConnectionsIn( )[j].equals(a))
			{
				indexOfIncomingRoad = j;
				break;
			}
		}
		for(int i = 0; i < getConnectionsIn( ).length; i++)
		{
			if(! a.getGoingFrom( ).equals(getConnectionsOut( )[(indexOfIncomingRoad + i) % getConnectionsIn( ).length].getGoingTo( )) && ! getConnectionsOut( )[(indexOfIncomingRoad + i) % getConnectionsIn( ).length].hasBeenChecked)
			{
				validRoad.add(getConnectionsOut( )[(indexOfIncomingRoad + i) % getConnectionsIn( ).length]);
			}
		}
		if(a.getGoingFrom( ).getName( ).equals("9") && a.getGoingTo( ).getName( ).equals("26"))
		{
			System.out.println(validRoad.size( ));
		}
		return validRoad;
	}
	
	private void updateDelays(String delay, int cooldown)
	{
		for(DelayAndCooldown delay1 : delays)
		{
			if(delay1.delay.equals(delay))
			{
				delay1.cooldown = cooldown;
				return;
			}
		}
		delays.add(new DelayAndCooldown(cooldown, delay));
	}
	
	@Override
	public int getIndexOfRoadIncoming(Road a)
	{
		for(int i = 0; i < getConnectionsIn( ).length; i++)
		{
			if(a.equals(getConnectionsIn( )[(nonROWroad + i) % getConnectionsIn( ).length]))
			{
				return i;
			}
		}
		return - 1;
	}
}
