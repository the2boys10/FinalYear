
public class CarOnRoundabout implements Comparable<CarOnRoundabout>
{
	public final Car carOnRoundabout;
	public int indexOnRoundabout;
	private final int exitIndex;
	public final int maxIndex;
	public final Road exitRoad;
	public int failedAttempts = 0;
	private double currentSpeed = 0;
	
	public CarOnRoundabout(Car a, int indexOnRoundabout, int exitIndex, Road exitRoad, int maxIndexOfRoundabout)
	{
		carOnRoundabout = a;
		this.indexOnRoundabout = indexOnRoundabout;
		this.exitIndex = exitIndex;
		this.exitRoad = exitRoad;
		this.maxIndex = maxIndexOfRoundabout;
	}
	
	private int workOutDistanceTillEnd(int currentIndex, int maxIndex, int exitIndex)
	{
		if(currentIndex < exitIndex)
		{
			return exitIndex - currentIndex;
		}
		else
		{
			return (maxIndex - currentIndex) + exitIndex;
		}
	}
	
	public void increaseIndex()
	{
		double oldSpeed = currentSpeed;
		indexOnRoundabout = (indexOnRoundabout + 1) % (maxIndex + 1);
		if(currentSpeed + (workOutDistanceTillEnd(indexOnRoundabout, maxIndex, exitIndex) * - Config.CARACCELERATIONANDDECELERATION * Config.SIMACCELERATION) > 0)
		{
			currentSpeed = currentSpeed - Config.CARACCELERATIONANDDECELERATION * Config.SIMACCELERATION;
		}
		else
		{
			if(currentSpeed < 13.4111944444)
			{
				currentSpeed = currentSpeed - Config.CARACCELERATIONANDDECELERATION * Config.SIMACCELERATION;
			}
			if(currentSpeed > 13.4111944444)
			{
				currentSpeed = 13.4111944444;
			}
		}
		carOnRoundabout.endOfTurnActivities(oldSpeed, currentSpeed);
	}
	
	public boolean isWithinExitIndex()
	{
		return indexOnRoundabout == exitIndex;
	}
	
	public boolean addToFailedAttempts()
	{
		failedAttempts++;
		if(failedAttempts > Config.ROUNDABOUTRECHOOSEAMOUNT)
		{
			failedAttempts = 0;
			return true;
		}
		return false;
	}
	
	@Override
	public int compareTo(CarOnRoundabout o)
	{
		if(indexOnRoundabout > o.indexOnRoundabout)
		{
			return 1;
		}
		else if(indexOnRoundabout < o.indexOnRoundabout)
		{
			return - 1;
		}
		return 0;
	}
}