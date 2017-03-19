public class CarOnRoundabout
{
	public Car carOnRoundabout;
	public int indexOnRoundabout;
	public int exitIndex;
	public int maxIndex;
	public Road exitRoad;
	public int failedAttempts = 0;
	public boolean hasReconsidered = false;
	public double currentSpeed = 0;
	public Road enterRoad;
	public CarOnRoundabout (Car a, int indexOnRoundabout, int exitIndex, Road exitRoad,int maxIndexOfRoundabout, Road enterRoad)
	{
		carOnRoundabout = a;
		this.indexOnRoundabout = indexOnRoundabout;
		this.exitIndex = exitIndex;
		this.exitRoad = exitRoad;
		this.maxIndex = maxIndexOfRoundabout;
		this.enterRoad = enterRoad;
	}
	
	public int workOutDistanceTillEnd(int currentIndex, int maxIndex, int exitIndex)
	{
		if(currentIndex<exitIndex)
		{
			return exitIndex-currentIndex;
		}
		else
		{
			return (maxIndex-currentIndex)+exitIndex;
		}
	}
	public void increaseIndex()
	{
		double oldSpeed = currentSpeed;
		indexOnRoundabout = (indexOnRoundabout+1)%(maxIndex+1);
		if(currentSpeed + (workOutDistanceTillEnd(indexOnRoundabout,maxIndex,exitIndex)*carOnRoundabout.getDeceleration()*Config.SIMACCELERATION)>0)
		{
			currentSpeed=currentSpeed+carOnRoundabout.getDeceleration()*Config.SIMACCELERATION;
		}
		else
		{
			if(currentSpeed<13.4111944444)
			{
				currentSpeed=currentSpeed+carOnRoundabout.getAcceleration()*Config.SIMACCELERATION;
			}
			if(currentSpeed>13.4111944444)
			{
				currentSpeed=13.4111944444;
			}
		}
		carOnRoundabout.addToFuel(oldSpeed, currentSpeed);
	}
	
	public boolean isWithinExitIndex()
	{
		if(indexOnRoundabout==exitIndex)
		{
				return true;
		}
		return false;
	}
	
	public boolean addToFailedAttempts()
	{
		failedAttempts++;
		if(failedAttempts>Config.ROUNDABOUTRECHOOSEAMOUNT)
		{
			failedAttempts=0;
			return true;
		}
		return false;
	}
}

