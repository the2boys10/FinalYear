
public class RoadAndWeight implements Comparable<RoadAndWeight>
{
	public final Road goingTo;
	public final double weightOfRoad;
	public Road firstChosenRoad;
	public int amountOfCarsOnJourney = 0;
	
	public RoadAndWeight(Road goingTo, double weightOfRoad)
	{
		this.weightOfRoad = weightOfRoad;
		this.goingTo = goingTo;
	}
	
	public RoadAndWeight addFirstChosen(Road currentRoad)
	{
		firstChosenRoad = currentRoad;
		return this;
	}
	
	@Override
	public int compareTo(RoadAndWeight o)
	{
		if(this.equals(o))
		{
			return 0;
		}
		if(weightOfRoad > o.weightOfRoad)
		{
			return 1;
		}
		else if(weightOfRoad <= weightOfRoad)
		{
			return - 1;
		}
		return 0;
	}
}
