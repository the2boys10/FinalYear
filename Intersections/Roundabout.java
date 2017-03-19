

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import org.graphstream.graph.Node;

public class Roundabout extends IntersectionAbstract
{
	private final int distanceBetweenExitsRound = 35;
	private TreeSet<CarOnRoundabout> carsCurrentlyOnRoundabout = new TreeSet<CarOnRoundabout>(new CompareRoundaboutPos());
	public Roundabout(double xCord, double yCord, double interval, double name, Node nodeRepresentation, int degree)
	{
		super(xCord,yCord,name, nodeRepresentation, degree);
	}
	
	public boolean isAccepting(Car car, Road a, Road b)
	{
		int indexOfIntersections = 0;
		int exitIntersection = 0;
		for(int i = 0 ; i < getConnectionsIn().length; i++)
		{
			if (getConnectionsIn()[i].equals(a))
			{
				indexOfIntersections=(i*distanceBetweenExitsRound);
			}
		}
		for(int i = 0 ; i < getConnectionsOut().length; i++)
		{
			if (getConnectionsOut()[i].equals(b))
			{
				exitIntersection=(i*distanceBetweenExitsRound)-1;
			}
		}
		CarOnRoundabout[] carsArray = carsCurrentlyOnRoundabout.toArray(new CarOnRoundabout[carsCurrentlyOnRoundabout.size()]);
		for (int i = 0; i < carsArray.length; i++)
		{
			if(indexOfIntersections-distanceBetweenExitsRound<0)
			{
				if(carsArray[i].indexOnRoundabout<=indexOfIntersections+distanceBetweenExitsRound-1||carsArray[i].indexOnRoundabout>=carsArray[i].maxIndex-(distanceBetweenExitsRound+1))
				{
					return false;
				}
			}
			else
			{
				if(carsArray[i].indexOnRoundabout>=indexOfIntersections-(distanceBetweenExitsRound-1)&&carsArray[i].indexOnRoundabout<=indexOfIntersections+(distanceBetweenExitsRound-1))
				{
					return false;
				}
			}
		}
		int exitIntersection2 = exitIntersection;
		if (exitIntersection<0)
		{
			exitIntersection2 = (getConnectionsIn().length*distanceBetweenExitsRound)-1;
		}
		carsCurrentlyOnRoundabout.add(new CarOnRoundabout(car,indexOfIntersections,exitIntersection2,b,getConnectionsIn().length*distanceBetweenExitsRound-1,a));
		car.getCurrentRoad().removeFrom(car);
		return true;
	}
	
	public void continueInterval()
	{
		Iterator<CarOnRoundabout> carsOnRoundabout = carsCurrentlyOnRoundabout.iterator();
		TreeSet<CarOnRoundabout> carsOnRoundaboutTemp = new TreeSet<CarOnRoundabout>(new CompareRoundaboutPos());
		while(carsOnRoundabout.hasNext())
		{
			CarOnRoundabout currentCar = carsOnRoundabout.next();
			currentCar.increaseIndex();
			if(currentCar.isWithinExitIndex())
			{
				if(currentCar.exitRoad.hasSpace()>0)
				{
					currentCar.carOnRoundabout.setHasMoved(true);
					currentCar.exitRoad.addTo(currentCar.carOnRoundabout);
					super.updateArraysOnCarFinish(currentCar.carOnRoundabout, currentCar.carOnRoundabout.getCurrentRoad(), currentCar.exitRoad);
					currentCar.carOnRoundabout.setCurrentRoad(currentCar.exitRoad);
					if(Config.REASKUSERTOCHANGEONADDITION)
					{
						UserInputFile.weightsOfEdgesAfterInitialization(currentCar.carOnRoundabout, currentCar.carOnRoundabout.getCurrentRoad(), "CarAdded");
					}
					currentCar.carOnRoundabout.setNextRoad(null);
					currentCar.carOnRoundabout.setAtIntersection(false);
					currentCar.carOnRoundabout.setValid(true);
					currentCar.carOnRoundabout.getReadingOfCurrentRoad=true;
					if(currentCar.exitRoad.getColorOfCarsOnRoad()=='R')
						currentCar.carOnRoundabout.getCarsSprite().addAttribute("ui.style", "fill-color: rgba(255,0,0,125);");
					else
						currentCar.carOnRoundabout.getCarsSprite().addAttribute("ui.style", "fill-color: rgba(0,0,0,125);");
				}
				else
				{
					if(currentCar.addToFailedAttempts())
					{
						currentCar.failedAttempts=0;
						for (int i = 0; i < getConnectionsOut().length; i++)
						{
							if(getConnectionsOut()[i].equals(currentCar.exitRoad))
							{
								Road secondChoice = UserInputFile.getNextRoadDeadlockPreventionRoundabout(currentCar.enterRoad,currentCar.exitRoad,currentCar.carOnRoundabout);
								if(secondChoice!=null)
								{
									int exitIntersection = 0;
									for(int j = 0 ; j < getConnectionsOut().length; j++)
									{
										if (getConnectionsOut()[j].equals(secondChoice))
										{
											exitIntersection=j*distanceBetweenExitsRound;
										}
									}
									int exitIntersection2 = exitIntersection;
									if (exitIntersection-1<0)
									{
										exitIntersection2 = getConnectionsIn().length*distanceBetweenExitsRound-1;
									}
									currentCar.exitRoad = getConnectionsOut()[exitIntersection/distanceBetweenExitsRound];
									currentCar.exitIndex = exitIntersection2;
									currentCar.carOnRoundabout.setNextRoad(secondChoice);
									currentCar.hasReconsidered = true;
								}
							}
						}
					}
					carsOnRoundaboutTemp.add(currentCar);
				}
			}
			else
			{
				carsOnRoundaboutTemp.add(currentCar);
			}
		}
		carsCurrentlyOnRoundabout = carsOnRoundaboutTemp;
	}
}

class CompareRoundaboutPos implements Comparator<CarOnRoundabout>
{
	@Override
	public int compare(CarOnRoundabout o1, CarOnRoundabout o2)
	{
		if (o1.indexOnRoundabout > o2.indexOnRoundabout)
		{
			return 1;
		}
		else if (o1.indexOnRoundabout < o2.indexOnRoundabout)
		{
			return -1;
		}
		return 0;
	}
}