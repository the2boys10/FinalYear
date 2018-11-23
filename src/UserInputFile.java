import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

public class UserInputFile
{
	static Random rand = new Random( );
	static SearchTechniques searchTechnique;
	
	public static void initialiseSearchStrategy()
	{
		if(Config.ALGORITHM == 0)//algorithm 0 = BFS
		{
			searchTechnique = new BFSearch( );
		}
		else if(Config.ALGORITHM == 1)//algorithm 1 = Dijstra's
		{
			searchTechnique = new Dijstra( );
		}
		else if(Config.ALGORITHM == 2)//algorithm 2 = timeBasedWeighting on edges and Intersections
		{
			searchTechnique = new WeightsOnEdgesAndIntersections( );
		}
		else if(Config.ALGORITHM == 3)//algorithm 3 = Min So far
		{
			searchTechnique = new MinSoFar( );
		}
		else if(Config.ALGORITHM == 4)//algorithm 4 = Max So far
		{
			searchTechnique = new MaxSoFar( );
		}
		else if(Config.ALGORITHM == 5)//algorithm 5 = Avg
		{
			searchTechnique = new AvgSoFar( );
		}
		else if(Config.ALGORITHM == 6)//algorithm 6 = Congestion
		{
			searchTechnique = new CongestionGame( );
		}
		else if(Config.ALGORITHM == 7)//algorithm 7 = AddingAndTakingAway
		{
			searchTechnique = new AddingAndTakingAway( );
		}
		else if(Config.ALGORITHM == 8)//algorithm 8 = actualWaitingTime
		{
			searchTechnique = new ActualWaitingTime( );
		}
	}
	
	
	public static void determineWeightOfRoadInitial(double[] passWeights, Road road)
	{
		searchTechnique.determineWeightOfRoadInitial(passWeights, road);
	}
	
	
	public static Road getNextRoad(Car car)
	{
		Road currentRoad = car.getCurrentRoad( );
		Road endRoad = car.getEndGoal( ).specRoad;
		LinkedList<RoadAndWeight> chosenRoads = new LinkedList<RoadAndWeight>( );
		double lowestWeight = Double.MAX_VALUE / 2;
		LinkedList<Road> listOfRoadsToReset = new LinkedList<Road>( );
		LinkedList<Road> connectionsToCurrentRoad = currentRoad.getGoingTo( ).validRoads(currentRoad);
		TreeSet<RoadAndWeight> ListOfRoadsInOrder = new TreeSet<RoadAndWeight>(new SortOutNextWeight( ));
		searchTechnique.selectWeightStrategyInit(currentRoad, connectionsToCurrentRoad, ListOfRoadsInOrder, listOfRoadsToReset, endRoad);
		if(ListOfRoadsInOrder.size( ) == 1)
		{
			chosenRoads.add(ListOfRoadsInOrder.pollFirst( ));
		}
		else
		{
			while(ListOfRoadsInOrder.size( ) > 0)
			{
				RoadAndWeight topRoad = ListOfRoadsInOrder.pollFirst( );
				if(topRoad.goingTo.equals(endRoad))
				{
					chosenRoads.add(topRoad);
					lowestWeight = topRoad.weightOfRoad;
					break;
				}
				LinkedList<Road> temp = topRoad.goingTo.getGoingTo( ).validRoads(topRoad.goingTo);
				searchTechnique.selectWeightStrategyAfterInit(topRoad.goingTo, temp, ListOfRoadsInOrder, listOfRoadsToReset, topRoad.firstChosenRoad, topRoad.weightOfRoad, endRoad, topRoad.amountOfCarsOnJourney);
			}
		}
		while(ListOfRoadsInOrder.size( ) > 0 && ListOfRoadsInOrder.first( ).weightOfRoad <= lowestWeight)
		{
			RoadAndWeight topRoad = ListOfRoadsInOrder.pollFirst( );
			if(topRoad.goingTo.equals(endRoad))
			{
				if(lowestWeight == topRoad.weightOfRoad)
				{
					chosenRoads.add(topRoad);
				}
			}
			LinkedList<Road> temp = topRoad.goingTo.getGoingTo( ).validRoads(topRoad.goingTo);
			searchTechnique.selectWeightStrategyAfterInit(topRoad.goingTo, temp, ListOfRoadsInOrder, listOfRoadsToReset, topRoad.firstChosenRoad, topRoad.weightOfRoad, endRoad, topRoad.amountOfCarsOnJourney);
		}
		Road chosenRoad = useTieBreaking(chosenRoads);
		while(listOfRoadsToReset.size( ) > 0)
		{
			listOfRoadsToReset.remove( ).hasBeenChecked = false;
		}
		return chosenRoad;
	}
	
	
	private static Road useTieBreaking(LinkedList<RoadAndWeight> chosenRoads)
	{
		LinkedList<Road> exitRoad = new LinkedList<Road>( );
		if(Config.TIEBREAKCARSONJOURNEY)
		{
			int lowestAmountOfCars = Integer.MAX_VALUE / 2;
			while(chosenRoads.size( ) > 0)
			{
				RoadAndWeight temp = chosenRoads.poll( );
				if(temp.amountOfCarsOnJourney == lowestAmountOfCars)
				{
					lowestAmountOfCars = temp.amountOfCarsOnJourney;
					exitRoad.add(temp.firstChosenRoad);
				}
				if(temp.amountOfCarsOnJourney < lowestAmountOfCars)
				{
					lowestAmountOfCars = temp.amountOfCarsOnJourney;
					exitRoad.clear( );
					exitRoad.add(temp.firstChosenRoad);
				}
			}
			return exitRoad.get(rand.nextInt(exitRoad.size( )));
		}
		else
		{
			return chosenRoads.get(rand.nextInt(chosenRoads.size( ))).firstChosenRoad;
		}
	}
	
	public static Road getNextRoadDeadlockPreventionRoundabout(Road road, Road exitRoad, Car carOnRoundabout)
	{
		return null;
	}
	
	public static Road getNextRoadDeadlockPreventionCrossroads(Road currentRoad, Road road, Car car)
	{
		return null;
	}
	
	public static Road getNextRoadDeadlockPreventionTjunction(Road currentRoad, Road road, Car car)
	{
		return null;
	}
	
	public static void weightsOfEdgesAfterInitialization(Car car, Road road, String reason)
	{
		searchTechnique.weightsOfEdgesAfterInitialization(car, road, reason);
	}
	
}

class SortOutNextWeight implements Comparator<RoadAndWeight>
{
	@Override
	public int compare(RoadAndWeight o1, RoadAndWeight o2)
	{
		if(o1.equals(o2))
		{
			return 0;
		}
		if (o1.weightOfRoad > o2.weightOfRoad)
		{
			return 1;
		}
		else if (o1.weightOfRoad <= o2.weightOfRoad)
		{
			return -1;
		}
		return 0;
	}
}
