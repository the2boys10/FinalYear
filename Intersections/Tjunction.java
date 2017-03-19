

import java.util.LinkedList;

import org.graphstream.graph.Node;


public class Tjunction extends IntersectionAbstract
{
	public LinkedList<DelayAndCooldown> delays = new LinkedList<DelayAndCooldown>();
	private int nonROWroad;
	private final int COOLDOWNPERIOD = 3; 
	private final int CARWITHINHOWMANYSECONDS = 3;
	public Tjunction(double xCord, double yCord, double interval, double name, Node nodeRepresentation, int degree)
	{
		super(xCord,yCord,name,nodeRepresentation,degree);
	}
	
	public void continueInterval() 
	{
		for (int i = 0; i < delays.size(); i++)
		{
			delays.get(i).cooldown--;
			if(delays.get(i).cooldown==0)
			{
				delays.remove(i);
				i--;
			}
		}
	}
	
	public void sortOutOutgoingConnections()
	{
		super.sortOutOutgoingConnections();
		double tempHighestAngle = 0;
		double tempAngleDifference = difference(getConnectionsOut()[1].getAngleOfRoad(),getConnectionsOut()[0].getAngleOfRoad());
		if (tempAngleDifference>tempHighestAngle)
		{
			tempHighestAngle = tempAngleDifference;
			nonROWroad = 2;
		}
		tempAngleDifference = difference(getConnectionsOut()[2].getAngleOfRoad(),getConnectionsOut()[1].getAngleOfRoad());
		if (tempAngleDifference>tempHighestAngle)
		{
			tempHighestAngle = tempAngleDifference;
			nonROWroad = 0;
		}
		tempAngleDifference = difference(getConnectionsOut()[0].getAngleOfRoad(),getConnectionsOut()[2].getAngleOfRoad());
		if (tempAngleDifference>tempHighestAngle)
		{
			tempHighestAngle = tempAngleDifference;
			nonROWroad = 1;
		}
	}
	
	public double difference (double a, double b)
	{
		if(a>b)
		{
			return a-b;
		}
		else
		{
			return b-a;
		}
	}
	public boolean isAccepting(Car car, Road a, Road b) 
	{
		if(b==null)
		{
			System.out.println(car.getCarsSprite().getId() + " broke" + " car was trying to get to " + car.getEndGoal().specRoad.getGoingFrom().getName() + " ," + car.getEndGoal().specRoad.getGoingTo().getName() + " is currently at " + car.getCurrentRoad().getGoingFrom().getName() + " , " + car.getCurrentRoad().getGoingTo().getName() + " in method tjunction");
		}
		if (b.hasSpace()>0)
		{
			if(a.equals(getConnectionsIn()[nonROWroad]))
			{
				if(b.equals(getConnectionsOut()[(nonROWroad+1)%3])&&(doesContainDelay("1+(2to1)")==false))
				{
					if(getConnectionsIn()[(nonROWroad+2)%3].hasCarWithinSeconds(CARWITHINHOWMANYSECONDS))
					{
						if(getConnectionsIn()[(nonROWroad+2)%3].getCarsOnRoad().get(0).getNextRoad()!=null)
						{
							if(getConnectionsIn()[(nonROWroad+2)%3].getCarsOnRoad().get(0).getNextRoad().equals(getConnectionsOut()[(nonROWroad)%3]))
							{
								updateDelays("3to2",COOLDOWNPERIOD);
								super.sortOutAdditionAndRemoval(car,b);
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
						updateDelays("3to2",COOLDOWNPERIOD);
						super.sortOutAdditionAndRemoval(car,b);
						return true;
					}
				}
				else if (b.equals(getConnectionsOut()[(nonROWroad+2)%3])&&(doesContainDelay("1+(2to1)")==false&&doesContainDelay("1to3")==false&&doesContainDelay("3+(1to3)")==false))
				{
					if(getConnectionsIn()[(nonROWroad+2)%3].hasCarWithinSeconds(CARWITHINHOWMANYSECONDS))
					{
						if(getConnectionsIn()[(nonROWroad+2)%3].getCarsOnRoad().get(0).getNextRoad()!=null)
						{
							if(getConnectionsIn()[(nonROWroad+2)%3].getCarsOnRoad().get(0).getNextRoad().equals(getConnectionsOut()[nonROWroad]))
							{
								if(getConnectionsIn()[(nonROWroad+1)%3].hasCarWithinSeconds(CARWITHINHOWMANYSECONDS))
								{
									if(getConnectionsIn()[(nonROWroad+1)%3].getCarsOnRoad().get(0).getNextRoad()!=null)
									{
										if(getConnectionsIn()[(nonROWroad+1)%3].getCarsOnRoad().get(0).getNextRoad().hasSpace()<=0)
										{
											updateDelays("2+(3to2)",COOLDOWNPERIOD);
											super.sortOutAdditionAndRemoval(car,b);
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
									updateDelays("2+(3to2)",COOLDOWNPERIOD);
									super.sortOutAdditionAndRemoval(car,b);
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
					else if(getConnectionsIn()[(nonROWroad+1)%3].hasCarWithinSeconds(CARWITHINHOWMANYSECONDS))
					{
						if(getConnectionsIn()[(nonROWroad+1)%3].getCarsOnRoad().get(0).getNextRoad()!=null)
						{
							if(getConnectionsIn()[(nonROWroad+1)%3].getCarsOnRoad().get(0).getNextRoad().hasSpace()<=0)
							{
								updateDelays("2+(3to2)",COOLDOWNPERIOD);
								super.sortOutAdditionAndRemoval(car,b);
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
						updateDelays("2+(3to2)",COOLDOWNPERIOD);
						super.sortOutAdditionAndRemoval(car,b);
						return true;
					}
				}
			}
			else if (a.equals(getConnectionsIn()[(nonROWroad+1)%3]))
			{
				if(b.equals(getConnectionsOut()[(nonROWroad)%3])&&(doesContainDelay("1+(2to1)")==false&&doesContainDelay("2to1")==false&&doesContainDelay("2+(3to2)")==false))
				{
					if(getConnectionsIn()[(nonROWroad+2)%3].hasCarWithinSeconds(CARWITHINHOWMANYSECONDS))
					{
						if(getConnectionsIn()[(nonROWroad+2)%3].getCarsOnRoad().get(0).getNextRoad()!=null)
						{
							if(getConnectionsIn()[(nonROWroad+2)%3].getCarsOnRoad().get(0).getNextRoad().hasSpace()<=0)
							{
								updateDelays("3+(1to3)",COOLDOWNPERIOD);
								super.sortOutAdditionAndRemoval(car,b);
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
						updateDelays("3+(1to3)",COOLDOWNPERIOD);
						super.sortOutAdditionAndRemoval(car,b);
						return true;
					}
				}
				else if (b.equals(getConnectionsOut()[(nonROWroad+2)%3])&&(doesContainDelay("2+(3to2)")==false))
				{
					updateDelays("1to3",COOLDOWNPERIOD);
					super.sortOutAdditionAndRemoval(car,b);
					return true;
				}
			}
			else if (a.equals(getConnectionsIn()[(nonROWroad+2)%3]))
			{
				if(b.equals(getConnectionsOut()[(nonROWroad)%3])&&(doesContainDelay("3+(1to3)")==false))
				{
					updateDelays("2to1",COOLDOWNPERIOD);
					super.sortOutAdditionAndRemoval(car,b);
					return true;
				}
				else if ((doesContainDelay("3+(1to3)")==false&&(doesContainDelay("2+(3to2)")==false)))
				{
					updateDelays("1+(2to1)",COOLDOWNPERIOD);
					super.sortOutAdditionAndRemoval(car,b);
					return true;	
				}
			}
		}
		return false;
	}
	
	private boolean doesContainDelay(String delay)
	{
		for (int i = 0; i < delays.size(); i++)
		{
			if(delays.get(i).delay.equals(delay))
			{
				return true;
			}
		}
		return false;
	}
	
	public LinkedList<Road> validRoads(Road a)
	{
		int roadGivenIndex = 0;
		for (roadGivenIndex=0;roadGivenIndex<getConnectionsIn().length;roadGivenIndex++)
		{
			if(getConnectionsIn()[roadGivenIndex].equals(a))
			{
				break;
			}
		}
		LinkedList<Road> validRoad = new LinkedList<Road>();
		int indexOfIncomingRoad = 0;
		for (int j = 0; j < getConnectionsIn().length; j++) {
			if (getConnectionsIn()[j].equals(a))
			{
				indexOfIncomingRoad = j;
				break;
			}
		}
		for (int i = 0; i < getConnectionsIn().length; i++)
		{
			if (!a.getGoingFrom().equals(getConnectionsOut()[(indexOfIncomingRoad+i)%getConnectionsIn().length].getGoingTo()) && getConnectionsOut()[(indexOfIncomingRoad+i)%getConnectionsIn().length].hasBeenChecked == false)
			{
				validRoad.add(getConnectionsOut()[(indexOfIncomingRoad+i)%getConnectionsIn().length]);
			}
		}
		if(a.getGoingFrom().getName().equals("9")&&a.getGoingTo().getName().equals("26"))
		{
			System.out.println(validRoad.size());
		}
		return validRoad;
	}
	
	private void updateDelays(String delay, int cooldown)
	{
		for (int i = 0; i < delays.size(); i++)
		{
			if(delays.get(i).delay.equals(delay))
			{
				delays.get(i).cooldown=cooldown;
				return;
			}
		}
		delays.add(new DelayAndCooldown(cooldown, delay));
	}
	
	public int getIndexOfRoadIncoming(Road a)
	{
		for(int i = 0; i<getConnectionsIn().length;i++)
		{
			if(a.equals(getConnectionsIn()[(nonROWroad+i)%getConnectionsIn().length]))
			{
				return i;
			}
		}
		return -1;
	}
}
