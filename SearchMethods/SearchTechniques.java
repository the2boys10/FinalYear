
import java.util.LinkedList;
import java.util.TreeSet;

public abstract class SearchTechniques
{
	
	public void determineWeightOfRoadInitial(double[] passWeights, Road road)
	{
		for(int i = 0; i < passWeights.length; i++)
		{
			passWeights[i] = 1;
		}
		passWeights[0] = 0;
	}
	
	public void selectWeightStrategyInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road endRoad)
	{
		while(0 < a.size( ))
		{
			Road temp = a.removeFirst( );
			if(! temp.hasBeenChecked)
			{
				RoadAndWeight connections = new RoadAndWeight(temp, callingRoad.getWeightOfRoad( )[callingRoad.getGoingTo( ).getIndex(callingRoad, temp)] + temp.getWeightOfRoad( )[0]).addFirstChosen(temp);
				b.add(connections);
				connections.amountOfCarsOnJourney = connections.amountOfCarsOnJourney + temp.getCarsOnRoad( ).size( );
				c.add(temp);
				if(endRoad != temp)
				{
					temp.hasBeenChecked = true;
				}
			}
		}
	}
	
	public void selectWeightStrategyAfterInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road firstChosen, double weight, Road endRoad, int amountOfCarsSoFar)
	{
		while(0 < a.size( ))
		{
			Road temp = a.removeFirst( );
			if(! temp.hasBeenChecked)
			{
				RoadAndWeight connections = new RoadAndWeight(temp, callingRoad.getWeightOfRoad( )[callingRoad.getGoingTo( ).getIndex(callingRoad, temp)] + temp.getWeightOfRoad( )[0] + weight).addFirstChosen(firstChosen);
				b.add(connections);
				connections.amountOfCarsOnJourney = amountOfCarsSoFar + temp.getCarsOnRoad( ).size( );
				c.add(temp);
				if(endRoad != temp)
				{
					temp.hasBeenChecked = true;
				}
			}
		}
	}
	
	public void weightsOfEdgesAfterInitialization(Car car, Road road, String reason)
	{
	
	}
}
