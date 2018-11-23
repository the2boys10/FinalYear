public class AvgSoFar extends SearchTechniques
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
	
	public void searchStrategy(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road endRoad)
	{
		while (0 < a.size())
		{
			Road temp = a.removeFirst();
			if(temp.hasBeenChecked==false)
			{
				RoadAndWeight connections = null;
				double weightOfEdge = 0;
				double weightOfNode = 0;
				if(callingRoad.getAmountOfAvg()[callingRoad.getGoingTo().getIndex(callingRoad, temp)]>= Config.AMOUNTOUSEAVG)
					weightOfNode = callingRoad.getAverageTimesOnIntersection()[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
				else
					weightOfNode = callingRoad.getWeightOfRoad()[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
				if(temp.getAmountOfAvg()[0]>Config.AMOUNTOUSEAVG)
					weightOfEdge = temp.getAverageTimesOnIntersection()[0];
				else
					weightOfEdge = temp.getWeightOfRoad()[0];
				connections = new RoadAndWeight(temp,weightOfNode+weightOfEdge).addFirstChosen(temp);
				b.add(connections);
				connections.amountOfCarsOnJourney = connections.amountOfCarsOnJourney+temp.getCarsOnRoad().size();
				c.add(temp);
				if(endRoad!=temp)
				{
					temp.hasBeenChecked=true;
				}
			}
		}
	}
}
