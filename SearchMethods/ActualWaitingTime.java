
import java.util.LinkedList;
import java.util.TreeSet;

public class ActualWaitingTime extends BFSearch
{
	
	@Override
	public void selectWeightStrategyInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road endRoad)
	{
		while(0 < a.size( ))
		{
			Road temp = a.removeFirst( );
			if(! temp.hasBeenChecked)
			{
				RoadAndWeight connections;
				double weightOfEdge;
				double weightOfNode;
				if(callingRoad.getAmountOfAvg( )[callingRoad.getGoingTo( ).getIndex(callingRoad, temp)] >= Config.AMOUNTOUSEAVG)
					weightOfNode = callingRoad.getAverageTimesOnIntersection( )[callingRoad.getGoingTo( ).getIndex(callingRoad, temp)];
				else
					weightOfNode = callingRoad.getWeightOfRoad( )[callingRoad.getGoingTo( ).getIndex(callingRoad, temp)];
				if(temp.getAmountOfAvg( )[0] > Config.AMOUNTOUSEAVG)
					weightOfEdge = temp.getAverageTimesOnIntersection( )[0];
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
	
	@Override
	public void selectWeightStrategyAfterInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road firstChosen, double weight, Road endRoad, int amountOfCarsSoFar)
	{
		while(0 < a.size( ))
		{
			double weightOfEdge = 0;
			double weightOfNode = 0;
			RoadAndWeight connections;
			Road temp = a.removeFirst( );
			if(! temp.hasBeenChecked)
			{
				boolean isCrossRoads = false;
				if(callingRoad.getGoingTo( ).getClass( ).equals(CrossRoads.class))
				{
					isCrossRoads = true;
					int indexOfCallingRoad = callingRoad.getGoingTo( ).getIndexOfRoadIncoming(callingRoad);
					CrossRoads temp2 = (CrossRoads) callingRoad.getGoingTo( );
					if(temp2.getHowManyAcceptedDuringIntervalMax( )[indexOfCallingRoad] > 0)
					{
						double weightOfEdgeCalling = callingRoad.getWeightOfRoad( )[0];
						int amountOfCarsOnRoadCurrently = callingRoad.getCarsOnRoad( ).size( );
						int howManyAccepted = temp2.getHowManyAcceptedDuringIntervalMax( )[indexOfCallingRoad];
						int howLongTillAccepting = temp2.howLongTillAcceptingState(callingRoad);
						int trafficLightInterval = temp2.getInterval( );
						double howLongForEachVehicle = (double) trafficLightInterval / (double) howManyAccepted;
						double howLongForVehicleWaitingIfTrafficLightAlwaysAccepting = (amountOfCarsOnRoadCurrently + 1) * howLongForEachVehicle;
						double overAllTimeToPassEdgeAndIntersection = howLongForVehicleWaitingIfTrafficLightAlwaysAccepting + weightOfEdgeCalling;
						int howLongAcceptingFor = temp2.howLongTillStopAccepting(callingRoad);
						if(howLongTillAccepting == 0)
						{
							if(howLongAcceptingFor < overAllTimeToPassEdgeAndIntersection)
							{
								overAllTimeToPassEdgeAndIntersection = overAllTimeToPassEdgeAndIntersection - howLongAcceptingFor;
								int amountOfSkippedIntervals = (int) Math.ceil(overAllTimeToPassEdgeAndIntersection % trafficLightInterval);
								weightOfNode = howLongAcceptingFor + (amountOfSkippedIntervals * trafficLightInterval);
							}
						}
						else
						{
							if(howLongTillAccepting < overAllTimeToPassEdgeAndIntersection)
							{
								overAllTimeToPassEdgeAndIntersection = overAllTimeToPassEdgeAndIntersection - howLongTillAccepting;
								int amountOfSkippedIntervals = (int) Math.ceil((overAllTimeToPassEdgeAndIntersection - trafficLightInterval) % trafficLightInterval);
								weightOfNode = howLongTillAccepting + (amountOfSkippedIntervals * trafficLightInterval);
							}
							else
							{
								weightOfNode = 0;
							}
						}
					}
					else
					{
						isCrossRoads = false;
					}
				}
				if(! isCrossRoads)
				{
					if(callingRoad.getAmountOfAvg( )[callingRoad.getGoingTo( ).getIndex(callingRoad, temp)] >= Config.AMOUNTOUSEAVG)
						weightOfNode = callingRoad.getAverageTimesOnIntersection( )[callingRoad.getGoingTo( ).getIndex(callingRoad, temp)];
					else
						weightOfNode = callingRoad.getWeightOfRoad( )[callingRoad.getGoingTo( ).getIndex(callingRoad, temp)];
					if(temp.getAmountOfAvg( )[0] > Config.AMOUNTOUSEAVG)
						weightOfEdge = temp.getAverageTimesOnIntersection( )[0];
					else
						weightOfEdge = temp.getWeightOfRoad( )[0];
				}
				connections = new RoadAndWeight(temp, weightOfNode + weightOfEdge + weight).addFirstChosen(firstChosen);
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
}
