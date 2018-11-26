
import java.util.LinkedList;
import java.util.TreeSet;

public class MinSoFar extends BFSearch
{
	@Override
	public void selectWeightStrategyInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road endRoad)
	{
		searchStrategy(callingRoad, a, b, c, endRoad);
	}
	
	@Override
	public void selectWeightStrategyAfterInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road firstChosen, double weight, Road endRoad, int amountOfCarsSoFar)
	{
		searchStrategy(callingRoad, a, b, c, endRoad);
	}
	
	private void searchStrategy(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road endRoad)
	{
		while(0 < a.size( ))
		{
			Road temp = a.removeFirst( );
			if(! temp.hasBeenChecked)
			{
				RoadAndWeight connections;
				double weightOfEdge;
				double weightOfNode;
				if(callingRoad.minArrayForUser[callingRoad.getGoingTo( ).getIndex(callingRoad, temp)] < Double.MAX_VALUE / 2)
					weightOfNode = callingRoad.minArrayForUser[callingRoad.getGoingTo( ).getIndex(callingRoad, temp)];
				else
					weightOfNode = callingRoad.getWeightOfRoad( )[callingRoad.getGoingTo( ).getIndex(callingRoad, temp)];
				if(temp.minArrayForUser[0] < Double.MAX_VALUE / 2)
					weightOfEdge = temp.minArrayForUser[0];
				else
					weightOfEdge = temp.getWeightOfRoad( )[0];
				connections = new RoadAndWeight(temp, weightOfNode + weightOfEdge).addFirstChosen(temp);
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
}
