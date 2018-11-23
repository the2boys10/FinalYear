
public class WeightsOnEdgesAndIntersections extends SearchTechniques
{
	@Override
	public void determineWeightOfRoadInitial(double[] passWeights, Road road)
	{
		double roadDistance = road.getDistance();
		double finalSpeed = Math.sqrt(2* Config.CARACCELERATIONANDDECELERATION*(roadDistance/2));
		double maxSpeed = road.getMaxSpeedOfCarsOnRoad();
		if(finalSpeed>maxSpeed)
		{
			double time = maxSpeed/Config.CARACCELERATIONANDDECELERATION;
			double distanceMade = 0.5*maxSpeed*time;
			double distanceLeft = (roadDistance/2)-distanceMade;
			double time2 = (2*distanceLeft)/(2*maxSpeed);
			passWeights[0] = (time+time2)*2*10;
		}
		else
		{
			passWeights[0] = (finalSpeed/Config.CARACCELERATIONANDDECELERATION)*2;
		}
		if(road.getGoingTo().getClass().equals(Roundabout.class))
		{
			passWeights[1]=25*(passWeights.length-1);
			for(int i = 2; i < passWeights.length;i++)
			{
				passWeights[i]=35*(i-1);
			}
		}
		else if (road.getGoingTo().getClass().equals(Tjunction.class))
		{
			int indexOfCurrentRoad = road.getGoingTo().getIndexOfRoadIncoming(road);
			if(indexOfCurrentRoad==0)
			{
				passWeights[1] = 30;
				passWeights[2] = 60;
			}
			else if (indexOfCurrentRoad==1)
			{
				passWeights[1] = 0;
				passWeights[2] = 30;
			}
			else if (indexOfCurrentRoad==2)
			{
				passWeights[1] = 0;
				passWeights[2] = 0;
			}
		}
		else if (road.getGoingTo().getClass().equals(CrossRoads.class))
		{
			passWeights[1] = 30;
			passWeights[2] = 30;
			passWeights[3] = 60;
		}
		else
		{
			passWeights[1] = 0;
		}
	}
}
