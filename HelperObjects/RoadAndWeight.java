
public class RoadAndWeight {
	public Road goingTo;
	public double weightOfRoad;
	public Road firstChosenRoad;
	public int amountOfCarsOnJourney = 0;
	public RoadAndWeight(Road goingTo, double weightOfRoad)
	{
		this.weightOfRoad = weightOfRoad;
		this.goingTo=goingTo;
	}
	public RoadAndWeight addFirstChosen(Road currentRoad) {
		firstChosenRoad = currentRoad;
		return this;
	}
}
