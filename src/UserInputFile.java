import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

public class UserInputFile
{
	static Random rand = new Random();
	static boolean beenHere = false;
	static boolean switchBetween = false;
	static boolean printedoutonce = false;
	@SuppressWarnings("unused")
	public static void determineWeightOfRoadInitial(double[] passWeights, Road road)
	{
		if(Config.ALGORITHM==0)//algorithm 0 = BFS
		{
			BFSWeights(passWeights);
		}
		else if (Config.ALGORITHM==1)//algorithm 1 = Dijstra's
		{
			DijstraWeights(passWeights, road);
		}
		else if (Config.ALGORITHM==2)//algorithm 2 = timeBasedWeighting on edges and Intersections
		{
			weightsOnEdgesAndIntersections(passWeights, road);
		}
		else if (Config.ALGORITHM==3)//algorithm 3 = Min So far
		{
			BFSWeights(passWeights);
		}
		else if (Config.ALGORITHM==4)//algorithm 4 = Max So far
		{
			BFSWeights(passWeights);
		}
		else if (Config.ALGORITHM==5)//algorithm 5 = Avg
		{
			BFSWeights(passWeights);
		}
		else if (Config.ALGORITHM==6)//algorithm 6 = Congestion
		{
			congestionGame(passWeights, road);
		}
		else if (Config.ALGORITHM==7)//algorithm 7 = AddingAndTakingAway
		{
			weightsOnEdgesAndIntersections(passWeights, road);
		}
		else if (Config.ALGORITHM==8)//algorithm 8 = actualWaitingTime
		{
			BFSWeights(passWeights);
		}
	}
	
	public static void congestionGame(double[] passWeights, Road road)
	{
		passWeights[0]=road.getCarsOnRoad().size()/road.getDistance();
		for (int i = 1; i < passWeights.length; i++) 
		{
			passWeights[i]=0;
		}
	}
	
	public static void addingOrTakingAwayDelaysAtIntersection(double[] passWeights, Road road)
	{
		for (int i = 0; i < passWeights.length; i++) 
		{
			passWeights[i]=0;
		}
	}
	
	public static void weightsOnEdgesAndIntersections(double[] passWeights, Road road)
	{
		double roadDistance = road.getDistance();
		double finalSpeed = Math.sqrt(2*Config.CARACCELERATIONANDDECELERATION*(roadDistance/2));
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
	
	public static void DijstraWeights(double[] passWeights, Road road)
	{

		passWeights[0] = road.getDistance();
		for (int i = 1; i < passWeights.length; i++) 
		{
			passWeights[i]=0;
		}
	}
	
	public static void timeBasedWeightsOnEdges(double[] passWeights, Road road)
	{
		double roadDistance = road.getDistance();
		double finalSpeed = Math.sqrt(2*Config.CARACCELERATIONANDDECELERATION*(roadDistance/2));
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
		for (int i = 1; i < passWeights.length; i++) 
		{
			passWeights[i]=0;
		}
	}
	
	public static void BFSWeights(double[] passWeights)
	{
		for(int i = 0; i < passWeights.length; i++)
		{
			passWeights[i]=1;
		}
		passWeights[0] = 0;
	}

	
	
	
	public static Road getNextRoad(Car car)
	{
		Road currentRoad = car.getCurrentRoad();
		Road endRoad =  car.getEndGoal().specRoad;
		LinkedList<RoadAndWeight> chosenRoads = new LinkedList<RoadAndWeight>();
		double lowestWeight = Double.MAX_VALUE/2;
		LinkedList<Road> listOfRoadsToReset = new LinkedList<Road>();
		LinkedList<Road> connectionsToCurrentRoad = currentRoad.getGoingTo().validRoads(currentRoad);
		TreeSet<RoadAndWeight> ListOfRoadsInOrder = new TreeSet<RoadAndWeight>(new SortOutNextWeight());
		selectWeightStrategyInit(currentRoad, connectionsToCurrentRoad, ListOfRoadsInOrder, listOfRoadsToReset, endRoad);
		if (ListOfRoadsInOrder.size() == 1)
		{
			chosenRoads.add(ListOfRoadsInOrder.pollFirst());
		} 
		else
		{
			while (ListOfRoadsInOrder.size() > 0)
			{
				RoadAndWeight topRoad = ListOfRoadsInOrder.pollFirst();
				if(topRoad.goingTo.equals(endRoad))
				{
					chosenRoads.add(topRoad);
					lowestWeight = topRoad.weightOfRoad;
					break;
				}
				LinkedList<Road> temp = topRoad.goingTo.getGoingTo().validRoads(topRoad.goingTo);
				selectWeightStrategyAfterInit(topRoad.goingTo, temp, ListOfRoadsInOrder, listOfRoadsToReset, topRoad.firstChosenRoad, topRoad.weightOfRoad, endRoad, topRoad.amountOfCarsOnJourney);
			}
		}
		while(ListOfRoadsInOrder.size()>0&&ListOfRoadsInOrder.first().weightOfRoad<=lowestWeight)
		{
			RoadAndWeight topRoad = ListOfRoadsInOrder.pollFirst();
			if(topRoad.goingTo.equals(endRoad))
			{
				if(lowestWeight==topRoad.weightOfRoad)
				{
					chosenRoads.add(topRoad);
				}
			}
			LinkedList<Road> temp = topRoad.goingTo.getGoingTo().validRoads(topRoad.goingTo);
			selectWeightStrategyAfterInit(topRoad.goingTo, temp, ListOfRoadsInOrder, listOfRoadsToReset, topRoad.firstChosenRoad, topRoad.weightOfRoad, endRoad, topRoad.amountOfCarsOnJourney);
		}
		Road chosenRoad = useTieBreaking(chosenRoads);
		while(listOfRoadsToReset.size()>0)
		{
			listOfRoadsToReset.remove().hasBeenChecked = false;
		}
		return chosenRoad;
	}





	
	@SuppressWarnings("unused")
	public static void selectWeightStrategyInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road endRoad)
	{
		if(Config.ALGORITHM==0||Config.ALGORITHM==1||Config.ALGORITHM==2)//algorithm 0 = BFS
		{
			genericWeightInit(callingRoad, a, b, c, endRoad);
		}
		else if (Config.ALGORITHM==3)//algorithm 3 = Min So far
		{
			MinWeightInit(callingRoad, a, b, c, endRoad);
		}
		else if (Config.ALGORITHM==4)//algorithm 4 = Max So far
		{
			MaxWeightInit(callingRoad, a, b, c, endRoad);
		}
		else if (Config.ALGORITHM==5)//algorithm 5 = Avg
		{
			AvgWeightInit(callingRoad, a, b, c, endRoad);
		}
		else if (Config.ALGORITHM==6)//algorithm 6 = Congestion
		{
			genericWeightInit(callingRoad, a, b, c, endRoad);
		}
		else if (Config.ALGORITHM==7)//algorithm 7 = AddingAndTakingAway
		{
			genericWeightInit(callingRoad, a, b, c, endRoad);
		}
		else if (Config.ALGORITHM==8)//algorithm 8 = actualWaitingTime
		{
			actualWaitingTimeInit(callingRoad, a, b, c, endRoad);
		}
	}
	
	private static void actualWaitingTimeInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b,
			LinkedList<Road> c, Road endRoad) 
	{
		while (0 < a.size()) 
		{
			Road temp = a.removeFirst();
			if(temp.hasBeenChecked==false)
			{
				RoadAndWeight connections = null;
				double weightOfEdge = 0;
				double weightOfNode = 0;
				if(callingRoad.getAmountOfAvg()[callingRoad.getGoingTo().getIndex(callingRoad, temp)]>=Config.AMOUNTOUSEAVG)
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

	
	private static void actualWaitingTimeAfterInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b,
			LinkedList<Road> c, Road firstChosen, double weight, Road endRoad, int amountOfCarsSoFar) 
	{
		while (0 < a.size()) 
		{
			double weightOfEdge = 0;
			double weightOfNode = 0;
			RoadAndWeight connections = null;
			Road temp = a.removeFirst();
			if(temp.hasBeenChecked==false)
			{
				boolean isCrossRoads = false;
				if(callingRoad.getGoingTo().getClass().equals(CrossRoads.class))
				{
					isCrossRoads = true;
					int indexOfCallingRoad = callingRoad.getGoingTo().getIndexOfRoadIncoming(callingRoad);
					CrossRoads temp2 = (CrossRoads)callingRoad.getGoingTo();
					if(temp2.getHowManyAcceptedDuringIntervalMax()[indexOfCallingRoad]>0)
					{
						double weightOfEdgeCalling = callingRoad.getWeightOfRoad()[0];
						int amountOfCarsOnRoadCurrently = callingRoad.getCarsOnRoad().size();
						int howManyAccepted = temp2.getHowManyAcceptedDuringIntervalMax()[indexOfCallingRoad];
						int howLongTillAccepting = temp2.howLongTillAcceptingState(callingRoad);
						int trafficLightInterval = temp2.getInterval();
						double howLongForEachVehicle = trafficLightInterval/howManyAccepted;
						int howManyIntervalsWating = (int)Math.floor(amountOfCarsOnRoadCurrently/howManyAccepted);
						int howLongInAddition = howManyIntervalsWating*trafficLightInterval;
						double howLongForVehicleWaitingIfTrafficLightAlwaysAccepting = (amountOfCarsOnRoadCurrently + 1)*howLongForEachVehicle;
						double overAllTimeToPassEdgeAndIntersection = howLongForVehicleWaitingIfTrafficLightAlwaysAccepting + weightOfEdgeCalling;
						int howLongAcceptingFor = temp2.howLongTillStopAccepting(callingRoad);
						if(howLongTillAccepting==0)
						{
							if(howLongAcceptingFor<overAllTimeToPassEdgeAndIntersection)
							{
								overAllTimeToPassEdgeAndIntersection = overAllTimeToPassEdgeAndIntersection - howLongAcceptingFor;
								int amountOfSkippedIntervals = (int)Math.ceil(overAllTimeToPassEdgeAndIntersection%trafficLightInterval);
								weightOfNode = howLongAcceptingFor+(amountOfSkippedIntervals*trafficLightInterval);
							}
						}
						else
						{
							if(howLongTillAccepting<overAllTimeToPassEdgeAndIntersection)
							{
								overAllTimeToPassEdgeAndIntersection = overAllTimeToPassEdgeAndIntersection - howLongTillAccepting;
								int amountOfSkippedIntervals = (int)Math.ceil((overAllTimeToPassEdgeAndIntersection-trafficLightInterval)%trafficLightInterval);
								weightOfNode = howLongTillAccepting+(amountOfSkippedIntervals*trafficLightInterval);
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
				if(isCrossRoads==false)
				{
					if(callingRoad.getAmountOfAvg()[callingRoad.getGoingTo().getIndex(callingRoad, temp)]>=Config.AMOUNTOUSEAVG)
						weightOfNode = callingRoad.getAverageTimesOnIntersection()[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
					else
						weightOfNode = callingRoad.getWeightOfRoad()[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
					if(temp.getAmountOfAvg()[0]>Config.AMOUNTOUSEAVG)
						weightOfEdge = temp.getAverageTimesOnIntersection()[0];
					else
						weightOfEdge = temp.getWeightOfRoad()[0];
				}
				connections = new RoadAndWeight(temp,weightOfNode+weightOfEdge+weight).addFirstChosen(firstChosen);
				b.add(connections);
				connections.amountOfCarsOnJourney = amountOfCarsSoFar+temp.getCarsOnRoad().size();
				c.add(temp);
				if(endRoad!=temp)
				{
					temp.hasBeenChecked=true;
				}
			}
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@SuppressWarnings("unused")
	public static void selectWeightStrategyAfterInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road firstChosen, double weight, Road endRoad, int amountOfCarsSoFar)
	{
		if(Config.ALGORITHM==0||Config.ALGORITHM==1||Config.ALGORITHM==2)//algorithm 0 = BFS
		{
			genericWeightAfterInit(callingRoad, a, b, c, firstChosen, weight, endRoad, amountOfCarsSoFar);
		}
		else if (Config.ALGORITHM==3)//algorithm 3 = Min So far
		{
			MinWeightAfterInit(callingRoad, a, b, c, firstChosen, weight, endRoad, amountOfCarsSoFar);
		}
		else if (Config.ALGORITHM==4)//algorithm 4 = Max So far
		{
			MaxWeightAfterInit(callingRoad, a, b, c, firstChosen, weight, endRoad, amountOfCarsSoFar);
		}
		else if (Config.ALGORITHM==5)//algorithm 5 = Avg
		{
			AvgWeightAfterInit(callingRoad, a, b, c, firstChosen, weight, endRoad, amountOfCarsSoFar);
		}
		else if (Config.ALGORITHM==6)//algorithm 6 = Congestion
		{
			genericWeightAfterInit(callingRoad, a, b, c, firstChosen, weight, endRoad, amountOfCarsSoFar);
		}
		else if (Config.ALGORITHM==7)//algorithm 7 = AddingAndTakingAway
		{
			genericWeightAfterInit(callingRoad, a, b, c, firstChosen, weight, endRoad, amountOfCarsSoFar);
		}
		else if (Config.ALGORITHM==8)//algorithm 8 = actualWaitingTime
		{
			actualWaitingTimeAfterInit(callingRoad, a, b, c, firstChosen, weight, endRoad, amountOfCarsSoFar);
		}
	}
	
	private static Road useTieBreaking(LinkedList<RoadAndWeight> chosenRoads) 
	{
		LinkedList<Road> exitRoad = new LinkedList<Road>();
		if(Config.TIEBREAKCARSONJOURNEY)
		{
			int lowestAmountOfCars = Integer.MAX_VALUE/2;
			while(chosenRoads.size()>0)
			{
				RoadAndWeight temp = chosenRoads.poll();
				if(temp.amountOfCarsOnJourney==lowestAmountOfCars)
				{
					lowestAmountOfCars = temp.amountOfCarsOnJourney;
					exitRoad.add(temp.firstChosenRoad);
				}
				if(temp.amountOfCarsOnJourney<lowestAmountOfCars)
				{
					lowestAmountOfCars = temp.amountOfCarsOnJourney;
					exitRoad.clear();
					exitRoad.add(temp.firstChosenRoad);
				}
			}
			return exitRoad.get(rand.nextInt(exitRoad.size()));
		}
		else
		{
			return chosenRoads.get(rand.nextInt(chosenRoads.size())).firstChosenRoad;
		}
	}
	
	private static void AvgWeightAfterInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b,
			LinkedList<Road> c, Road firstChosen, double weight, Road endRoad, int amountOfCarsSoFar) 
	{
		while (0 < a.size()) 
		{
			Road temp = a.removeFirst();
			if(temp.hasBeenChecked==false)
			{
				RoadAndWeight connections = null;
				double weightOfEdge = 0;
				double weightOfNode = 0;
				if(callingRoad.getAmountOfAvg()[callingRoad.getGoingTo().getIndex(callingRoad, temp)]>=Config.AMOUNTOUSEAVG)
					weightOfNode = callingRoad.getAverageTimesOnIntersection()[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
				else
					weightOfNode = callingRoad.getWeightOfRoad()[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
				if(temp.getAmountOfAvg()[0]>Config.AMOUNTOUSEAVG)
					weightOfEdge = temp.getAverageTimesOnIntersection()[0];
				else
					weightOfEdge = temp.getWeightOfRoad()[0];
				connections = new RoadAndWeight(temp,weightOfNode+weightOfEdge+weight).addFirstChosen(firstChosen);
				b.add(connections);
				connections.amountOfCarsOnJourney = amountOfCarsSoFar+temp.getCarsOnRoad().size();
				c.add(temp);
				if(endRoad!=temp)
				{
					temp.hasBeenChecked=true;
				}
			}
		}
		
	}

	private static void MaxWeightAfterInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b,
			LinkedList<Road> c, Road firstChosen, double weight, Road endRoad, int amountOfCarsSoFar) 
	{
		while (0 < a.size()) 
		{
			Road temp = a.removeFirst();
			if(temp.hasBeenChecked==false)
			{
				RoadAndWeight connections = null;
				double weightOfEdge = 0;
				double weightOfNode = 0;
				if(callingRoad.maxArrayForUser[callingRoad.getGoingTo().getIndex(callingRoad, temp)]>-1)
					weightOfNode = callingRoad.maxArrayForUser[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
				else
					weightOfNode = callingRoad.getWeightOfRoad()[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
				if(temp.maxArrayForUser[0]>-1)
					weightOfEdge = temp.maxArrayForUser[0];
				else
					weightOfEdge = temp.getWeightOfRoad()[0];
				connections = new RoadAndWeight(temp,weightOfNode+weightOfEdge+weight).addFirstChosen(firstChosen);
				b.add(connections);
				connections.amountOfCarsOnJourney = amountOfCarsSoFar+temp.getCarsOnRoad().size();
				c.add(temp);
				if(endRoad!=temp)
				{
					temp.hasBeenChecked=true;
				}
			}
		}
		
	}

	public static void genericWeightAfterInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road firstChosen, double weight, Road endRoad, int amountOfCarsSoFar)
	{
		while (0 < a.size()) 
		{
			Road temp = a.removeFirst();
			if(temp.hasBeenChecked==false)
			{
				RoadAndWeight connections = new RoadAndWeight(temp,callingRoad.getWeightOfRoad()[callingRoad.getGoingTo().getIndex(callingRoad,temp)]+temp.getWeightOfRoad()[0]+weight).addFirstChosen(firstChosen);
				b.add(connections);
				connections.amountOfCarsOnJourney = amountOfCarsSoFar+temp.getCarsOnRoad().size();
				c.add(temp);
				if(endRoad!=temp)
				{
					temp.hasBeenChecked=true;
				}
			}
		}
	}
	
	private static void AvgWeightInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b,
			LinkedList<Road> c, Road endRoad) 
	{
		while (0 < a.size()) 
		{
			Road temp = a.removeFirst();
			if(temp.hasBeenChecked==false)
			{
				RoadAndWeight connections = null;
				double weightOfEdge = 0;
				double weightOfNode = 0;
				if(callingRoad.getAmountOfAvg()[callingRoad.getGoingTo().getIndex(callingRoad, temp)]>=Config.AMOUNTOUSEAVG)
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

	private static void MaxWeightInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b,
			LinkedList<Road> c, Road endRoad) 
	{
		while (0 < a.size()) 
		{
			Road temp = a.removeFirst();
			if(temp.hasBeenChecked==false)
			{
				RoadAndWeight connections = null;
				double weightOfEdge = 0;
				double weightOfNode = 0;
				if(callingRoad.maxArrayForUser[callingRoad.getGoingTo().getIndex(callingRoad, temp)]>-1)
					weightOfNode = callingRoad.maxArrayForUser[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
				else
					weightOfNode = callingRoad.getWeightOfRoad()[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
				if(temp.maxArrayForUser[0]>-1)
					weightOfEdge = temp.maxArrayForUser[0];
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

	private static void genericWeightInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road endRoad) 
	{
		while (0 < a.size()) 
		{
			Road temp = a.removeFirst();
			if(temp.hasBeenChecked==false)
			{
				RoadAndWeight connections = new RoadAndWeight(temp,callingRoad.getWeightOfRoad()[callingRoad.getGoingTo().getIndex(callingRoad, temp)]+temp.getWeightOfRoad()[0]).addFirstChosen(temp);
				b.add(connections);
				//System.out.println(callingRoad.getGoingFrom().getName() + " , " + callingRoad.getGoingTo().getName() + " , " + connections.goingTo.getGoingTo().getName() + " , edge "  + callingRoad.getWeightOfRoad()[callingRoad.getGoingTo().getIndex(callingRoad,temp)] + " width , " + temp.getWeightOfRoad()[0]);
				connections.amountOfCarsOnJourney = connections.amountOfCarsOnJourney+temp.getCarsOnRoad().size();
				c.add(temp);
				if(endRoad!=temp)
				{
					temp.hasBeenChecked=true;
				}
			}
		}
	}
	
	private static void MinWeightInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road endRoad) 
	{
		while (0 < a.size()) 
		{
			Road temp = a.removeFirst();
			if(temp.hasBeenChecked==false)
			{
				RoadAndWeight connections = null;
				double weightOfEdge = 0;
				double weightOfNode = 0;
				if(callingRoad.minArrayForUser[callingRoad.getGoingTo().getIndex(callingRoad, temp)]<Double.MAX_VALUE/2)
					weightOfNode = callingRoad.minArrayForUser[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
				else
					weightOfNode = callingRoad.getWeightOfRoad()[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
				if(temp.minArrayForUser[0]<Double.MAX_VALUE/2)
					weightOfEdge = temp.minArrayForUser[0];
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
	
	private static void MinWeightAfterInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road firstChosen, double weight, Road endRoad, int amountOfCarsSoFar) 
	{
		while (0 < a.size()) 
		{
			Road temp = a.removeFirst();
			if(temp.hasBeenChecked==false)
			{
				RoadAndWeight connections = null;
				double weightOfEdge = 0;
				double weightOfNode = 0;
				if(callingRoad.minArrayForUser[callingRoad.getGoingTo().getIndex(callingRoad, temp)]<Double.MAX_VALUE/2)
					weightOfNode = callingRoad.minArrayForUser[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
				else
					weightOfNode = callingRoad.getWeightOfRoad()[callingRoad.getGoingTo().getIndex(callingRoad, temp)];
				if(temp.minArrayForUser[0]<Double.MAX_VALUE/2)
					weightOfEdge = temp.minArrayForUser[0];
				else
					weightOfEdge = temp.getWeightOfRoad()[0];
				connections = new RoadAndWeight(temp,weightOfNode+weightOfEdge+weight).addFirstChosen(firstChosen);
				b.add(connections);
				connections.amountOfCarsOnJourney = amountOfCarsSoFar+temp.getCarsOnRoad().size();
				c.add(temp);
				if(endRoad!=temp)
				{
					temp.hasBeenChecked=true;
				}
			}
		}
	}
	
	
	public static Road getNextRoadDeadlockPreventionRoundabout(Road road, Road exitRoad, Car carOnRoundabout) 
	{
		System.out.println("gotherelolol3");
		return null;
	}

	public static Road getNextRoadDeadlockPreventionCrossroads(Road currentRoad, Road road, Car car) 
	{
		System.out.println("gotherelolol2");
		return null;
	}

	public static Road getNextRoadDeadlockPreventionTjunction(Road currentRoad, Road road, Car car) 
	{
		System.out.println("gotherelolol");
		return null;
	}

	@SuppressWarnings("unused")
	public static void weightsOfEdgesAfterInitialization(Car car, Road road, String reason) 
	{
		if (Config.ALGORITHM==6)
		{
			congestionGameAfterInit(car, road, reason);
		}
		else if (Config.ALGORITHM==7)
		{
			addingOrTakingAwayDelaysAtIntersectionAfterInit(car, road, reason);
		}
	}

	
	private static void congestionGameAfterInit(Car car, Road road, String reason)
	{
		road.getWeightOfRoad()[0]=road.getCarsOnRoad().size()/road.getDistance();
		System.out.println(road.getWeightOfRoad()[0]);
	}
	
	
	private static void addingOrTakingAwayDelaysAtIntersectionAfterInit(Car car, Road road, String reason)
	{
		if(reason.equals("CarRemoved"))
		{
			if(car.getReadingOfCurrentRoad==true)
			{
				if(car.getNextRoad().getWeightOfRoad()[0]>car.timeTakenToTravelDownRoad)
				{
					car.getNextRoad().getWeightOfRoad()[0]--;
				}
				else
				{
					car.getNextRoad().getWeightOfRoad()[0]++;
				}
			}
			if(car.getCurrentRoad().getWeightOfRoad()[car.getCurrentRoad().getGoingTo().getIndex(car.getCurrentRoad(), car.getNextRoad())]>car.timeTakenToPassIntersection)
			{
				car.getCurrentRoad().getWeightOfRoad()[car.getCurrentRoad().getGoingTo().getIndex(car.getCurrentRoad(), car.getNextRoad())]--;
			}
			else
			{
				car.getCurrentRoad().getWeightOfRoad()[car.getCurrentRoad().getGoingTo().getIndex(car.getCurrentRoad(), car.getNextRoad())]++;
			}
		}
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
