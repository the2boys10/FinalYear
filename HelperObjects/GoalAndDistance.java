
public class GoalAndDistance
{
	private String goal;
	private double distanceLeft;
	private double actualDistanceLeft;
	
	public GoalAndDistance()
	{
	
	}
	
	public void setGoal(String goal)
	{
		this.goal = goal;
	}
	
	public void setDistanceLeft(double distanceLeft)
	{
		this.distanceLeft = distanceLeft;
	}
	
	public void setAllAtOnce(String goal, double distanceLeft, double actualDistanceLeft)
	{
		this.goal = goal;
		this.distanceLeft = distanceLeft;
		this.actualDistanceLeft = actualDistanceLeft;
	}
	
	public String getGoal()
	{
		return goal;
	}
	
	public double getDistanceLeft()
	{
		return distanceLeft;
	}
	
	public double getActualDistanceLeft()
	{
		return actualDistanceLeft;
	}
}