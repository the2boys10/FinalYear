
import java.util.Iterator;
import java.util.TreeSet;


public class Roundabout extends IntersectionAbstract
{
	private TreeSet<CarOnRoundabout> carsCurrentlyOnRoundabout = new TreeSet<CarOnRoundabout>( );
	
	public Roundabout(double xCord, double yCord, double name, int degree)
	{
		super(xCord, yCord, name, degree);
	}
	
	@Override
	public boolean isAccepting(Car car, Road a, Road b, boolean hasCarMoved)
	{
		int indexOfIntersections = 0;
		int exitIntersection = 0;
		int distanceBetweenExitsRound = 35;
		for(int i = 0; i < getConnectionsIn( ).length; i++)
		{
			if(getConnectionsIn( )[i].equals(a))
			{
				indexOfIntersections = (i * distanceBetweenExitsRound);
			}
		}
		for(int i = 0; i < getConnectionsOut( ).length; i++)
		{
			if(getConnectionsOut( )[i].equals(b))
			{
				exitIntersection = (i * distanceBetweenExitsRound) - 1;
			}
		}
		CarOnRoundabout[] carsArray = carsCurrentlyOnRoundabout.toArray(new CarOnRoundabout[0]);
		for(CarOnRoundabout aCarsArray : carsArray)
		{
			if(indexOfIntersections - distanceBetweenExitsRound < 0)
			{
				if(aCarsArray.indexOnRoundabout <= indexOfIntersections + distanceBetweenExitsRound - 1 || aCarsArray.indexOnRoundabout >= aCarsArray.maxIndex - (distanceBetweenExitsRound + 1))
				{
					return false;
				}
			}
			else
			{
				if(aCarsArray.indexOnRoundabout >= indexOfIntersections - (distanceBetweenExitsRound - 1) && aCarsArray.indexOnRoundabout <= indexOfIntersections + (distanceBetweenExitsRound - 1))
				{
					return false;
				}
			}
		}
		int exitIntersection2 = exitIntersection;
		if(exitIntersection < 0)
		{
			exitIntersection2 = (getConnectionsIn( ).length * distanceBetweenExitsRound) - 1;
		}
		carsCurrentlyOnRoundabout.add(new CarOnRoundabout(car, indexOfIntersections, exitIntersection2, b, getConnectionsIn( ).length * distanceBetweenExitsRound - 1));
		car.getCurrentRoad( ).removeFrom(car);
		return true;
	}
	
	@Override
	public void continueInterval()
	{
		Iterator<CarOnRoundabout> carsOnRoundabout = carsCurrentlyOnRoundabout.iterator( );
		TreeSet<CarOnRoundabout> carsOnRoundaboutTemp = new TreeSet<CarOnRoundabout>( );
		while(carsOnRoundabout.hasNext( ))
		{
			CarOnRoundabout currentCar = carsOnRoundabout.next( );
			currentCar.increaseIndex( );
			if(currentCar.isWithinExitIndex( ))
			{
				if(currentCar.exitRoad.hasSpace( ) > 0)
				{
					currentCar.carOnRoundabout.setHasMoved(true);
					currentCar.exitRoad.addTo(currentCar.carOnRoundabout);
					super.updateArraysOnCarFinish(currentCar.carOnRoundabout, currentCar.carOnRoundabout.getCurrentRoad( ), currentCar.exitRoad);
					currentCar.carOnRoundabout.setCurrentRoad(currentCar.exitRoad);
					if(Config.REASKUSERTOCHANGEONADDITION)
					{
						UserInputFile.weightsOfEdgesAfterInitialization(currentCar.carOnRoundabout, currentCar.carOnRoundabout.getCurrentRoad( ), "CarAdded");
					}
					currentCar.carOnRoundabout.setAtIntersection(false);
					currentCar.carOnRoundabout.setValid(true);
					currentCar.carOnRoundabout.setReadingOfCurrentRoad(true);
					currentCar.carOnRoundabout.setNextRoad(null);
					if(currentCar.exitRoad.getColorOfCarsOnRoad( ) == 'R')
						currentCar.carOnRoundabout.getCarsSprite( ).addAttribute("ui.style", "fill-color: rgba(255,0,0,125);");
					else
						currentCar.carOnRoundabout.getCarsSprite( ).addAttribute("ui.style", "fill-color: rgba(0,0,0,125);");
				}
				else
				{
					if(currentCar.addToFailedAttempts( ))
					{
						currentCar.failedAttempts = 0;
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
	
	@Override
	public int getInternalBuffer()
	{
		return 1;
	}
}