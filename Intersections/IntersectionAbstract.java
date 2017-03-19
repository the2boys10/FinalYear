import java.util.LinkedList;

import org.graphstream.graph.Node;

public abstract class IntersectionAbstract implements IntersectionMethods 
{
	private String name;
	private Road[] connectionsIn;
	private Road[] connectionsOut;
	@SuppressWarnings("unused")
	private Node nodeRepresentation;
	private double xCord;
	private double yCord;
	public int added = 0;
	private int amountOfRoadsInAdded = 0;
	public IntersectionAbstract(double xCord, double yCord, double name, Node nodeRepresentation, int degreeIn)
	{
		this.name = (int)name+"";
		this.setxCord(xCord);
		this.setyCord(yCord);
		this.nodeRepresentation = nodeRepresentation;
		setConnectionsIn(new Road[degreeIn]);
	}
	public String getName()
	{
		return name;
	}
	public int getIndex(Road goingFrom, Road goingTo)
	{
		if(goingTo==null)
		{
			return 0;
		} 
		int i = 0;;
		for (i = 0; i < connectionsIn.length; i++) {
			if(goingFrom.equals(connectionsIn[i]))
			{
				break;
			}
		}
		for (int j = 0; j < connectionsOut.length; j++)
		{
			if(goingTo.equals(connectionsOut[(i+j)%connectionsOut.length]))
			{
				return j;
			}
		}
		return -1;
	}
	public int getIndexOfRoadIncoming(Road a)
	{
		for(int i = 0; i<connectionsIn.length;i++)
		{
			if(a.equals(connectionsIn[i]))
			{
				return i;
			}
		}
		return -1;
	}
	
	public void sortOutAdditionAndRemoval(Car car, Road b)
	{
		car.getCurrentRoad().removeFrom(car);
		updateArraysOnCarFinish(car,car.getCurrentRoad(),car.getNextRoad());
		car.setCurrentRoad(b);
		car.getCurrentRoad().addTo(car);
		if(Config.REASKUSERTOCHANGEONADDITION)
		{
			UserInputFile.weightsOfEdgesAfterInitialization(car, car.getCurrentRoad(), "CarAdded");
		}
		car.setNextRoad(null);
		car.setCurrentX(car.getCurrentRoad().getGoingFrom().getxCord());
		car.setCurrentY(car.getCurrentRoad().getGoingFrom().getyCord());
		car.setValid(true);
		car.setAtIntersection(false);
		car.getReadingOfCurrentRoad=true;
		if(b.getColorOfCarsOnRoad()=='R')
			car.getCarsSprite().addAttribute("ui.style", "fill-color: rgba(255,0,0,125);");
		else
			car.getCarsSprite().addAttribute("ui.style", "fill-color: rgba(0,0,0,125);");
	}
	
	public void updateArraysOnCarFinish(Car car, Road goingFrom, Road goingTo)
	{
		int indexOfConnection = goingFrom.getGoingTo().getIndex(goingFrom, goingTo);
		if(Config.UPDATEAVERAGETIME)
		{					
			if(car.isHasAddedToRoadPass()==false)
			{
				//System.out.println("upgrade edge " + goingFrom.getGoingFrom().getName() + " ," + goingFrom.getGoingTo().getName() + "    " + goingTo.getGoingFrom().getName() +" , " + goingTo.getGoingTo().getName() + " " + goingFrom.getAverageTimesOnIntersection()[indexOfConnection]);
				goingFrom.getAverageTimesOnIntersection()[indexOfConnection] = ((goingFrom.getAverageTimesOnIntersection()[indexOfConnection]*goingFrom.getAmountOfAvg()[indexOfConnection])+car.timeWaitingAtIntersection)/(goingFrom.getAmountOfAvg()[indexOfConnection]+1);
				goingFrom.getAmountOfAvg()[indexOfConnection]=goingFrom.getAmountOfAvg()[indexOfConnection]+1;
				car.setHasAddedToRoadPass(false);
				car.timeTakenToPassIntersectionPrevious = 0;
			}
			else
			{
				//System.out.println("upgrade edge " + goingFrom.getGoingFrom().getName() + " ," + goingFrom.getGoingTo().getName() + "    " + goingTo.getGoingFrom().getName() +" , " + goingTo.getGoingTo().getName() + " " + goingFrom.getAverageTimesOnIntersection()[indexOfConnection]);
				goingFrom.getAverageTimesOnIntersection()[indexOfConnection] = ((goingFrom.getAverageTimesOnIntersection()[indexOfConnection]*goingFrom.getAmountOfAvg()[indexOfConnection])-car.timeTakenToPassIntersectionPrevious+car.timeWaitingAtIntersection)/(goingFrom.getAmountOfAvg()[indexOfConnection]);
				car.timeTakenToPassIntersectionPrevious = 0;
				car.setHasAddedToRoadPass(false);
			}
			if(car.getReadingOfCurrentRoad==true)
			{
				if(car.isHasAddedToRoadAverage()==false)
				{
					//System.out.println("upgrade length here2" + goingFrom.getGoingFrom().getName() + " ," + goingFrom.getGoingTo().getName() + " " + goingFrom.getAverageTimesOnIntersection()[0]);
					goingFrom.getAverageTimesOnIntersection()[0] = ((goingFrom.getAverageTimesOnIntersection()[0]*goingFrom.getAmountOfAvg()[0])+car.timeTakenToTravelDownRoad)/(goingFrom.getAmountOfAvg()[0]+1);
					car.setHasAddedToRoadAverage(false);
					goingFrom.getAmountOfAvg()[0] = goingFrom.getAmountOfAvg()[0]+1;
					car.timeTakenToTravelDownRoadPrevious = 0;
				}
				else
				{
				//	System.out.println("upgrade length here" + goingFrom.getGoingFrom().getName() + " ," + goingFrom.getGoingTo().getName() + " " + goingFrom.getAverageTimesOnIntersection()[0]);
					goingFrom.getAverageTimesOnIntersection()[0] = ((goingFrom.getAverageTimesOnIntersection()[0]*car.getCurrentRoad().getAmountOfAvg()[0])-car.timeTakenToTravelDownRoadPrevious+car.timeTakenToTravelDownRoad)/(goingFrom.getAmountOfAvg()[0]);
					car.timeTakenToTravelDownRoadPrevious = 0;
					car.setHasAddedToRoadAverage(false);
				}
			}
		}
		if(Config.UPDATEMAXTIME)
		{
			if(car.timeTakenToTravelDownRoad>goingFrom.maxArrayForUser[0]&&car.getReadingOfCurrentRoad==true)
			{
				//System.out.println("upgrade length " + goingFrom.getGoingFrom().getName() + " ," + goingFrom.getGoingTo().getName() + " " + car.timeTakenToTravelDownRoad);
				goingFrom.maxArrayForUser[0] = car.timeTakenToTravelDownRoad;
			}
			if(car.timeWaitingAtIntersection>goingFrom.maxArrayForUser[indexOfConnection])
			{
				//System.out.println("upgrade edge " + goingFrom.getGoingFrom().getName() + " ," + goingFrom.getGoingTo().getName() + "    " + goingTo.getGoingFrom().getName() +" , " + goingTo.getGoingTo().getName() + " " + car.timeWaitingAtIntersection);
				goingFrom.maxArrayForUser[indexOfConnection] = car.timeWaitingAtIntersection;
			}
		}
		if(Config.UPDATEMINTIME)
		{
			if(car.timeTakenToTravelDownRoad<goingFrom.minArrayForUser[0]&&car.getReadingOfCurrentRoad==true)
			{
				//System.out.println("upgrade length " + goingFrom.getGoingFrom().getName() + " ," + goingFrom.getGoingTo().getName() + " " + car.timeTakenToTravelDownRoad);
				goingFrom.minArrayForUser[0] = car.timeTakenToTravelDownRoad;
			}
			if(car.timeWaitingAtIntersection<goingFrom.minArrayForUser[indexOfConnection])
			{
				//System.out.println("upgrade edge " + goingFrom.getGoingFrom().getName() + " ," + goingFrom.getGoingTo().getName() + "    " + goingTo.getGoingFrom().getName() +" , " + goingTo.getGoingTo().getName() + " " + car.timeWaitingAtIntersection);
				goingFrom.minArrayForUser[indexOfConnection] = car.timeWaitingAtIntersection;
			}
		}
		if(Config.REASKUSERTOCHANGEONREMOVAL)
		{
			UserInputFile.weightsOfEdgesAfterInitialization(car, car.getCurrentRoad(), "CarRemoved");
		}
		car.timeWaitingAtIntersection = 0;
		car.timeTakenToTravelDownRoad = 0;
	}
	public void addInConnections(Road tempRoad)
	{
		added++;
		getConnectionsIn()[amountOfRoadsInAdded] = tempRoad;
		amountOfRoadsInAdded++;
		
	}
	public void addOutConnections(Road[] outConnections)
	{
		setConnectionsOut(outConnections);
	}
	
	public LinkedList<Road> validRoads(Road a)
	{
		LinkedList<Road> temp = new LinkedList<Road>();
		int in = 0;
		for (in = 0; in < connectionsIn.length; in++) 
		{
			if(connectionsIn[in].equals(a))
			{
				break;
			}
		}
		for (int i = 0; i < getConnectionsOut().length; i++) 
		{
			if(getConnectionsOut()[(in+1+i)%getConnectionsOut().length].hasBeenChecked==false)
			{
				temp.add(getConnectionsOut()[(in+1+i)%getConnectionsOut().length]);	
			}
		}
		return temp;
	}
	public boolean isAccepting(Car car, Road currentRoad, Road nextRoad) 
	{
		if(nextRoad.hasSpace()>0)
		{
			sortOutAdditionAndRemoval(car,nextRoad);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void sortOutOutgoingConnections()
	{
		Road[] arrayOfConnectionsIn = new Road[getConnectionsIn().length];
		for (int i = 0; i < getConnectionsIn().length; i++) 
		{
			for (int j = 0; j < getConnectionsOut().length; j++) 
			{
				if(getConnectionsIn()[i].getGoingFrom().equals(getConnectionsOut()[j].getGoingTo()))
				{
					arrayOfConnectionsIn[j] = getConnectionsIn()[i];
				}
			}
		}
		setConnectionsIn(arrayOfConnectionsIn);
	}
	public double getxCord() {
		return xCord;
	}
	public void setxCord(double xCord) {
		this.xCord = xCord;
	}
	public double getyCord() {
		return yCord;
	}
	public void setyCord(double yCord) {
		this.yCord = yCord;
	}
	public Road[] getConnectionsOut() {
		return connectionsOut;
	}
	public void setConnectionsOut(Road[] connectionsOut) {
		this.connectionsOut = connectionsOut;
	}
	public Road[] getConnectionsIn() {
		return connectionsIn;
	}
	public void setConnectionsIn(Road[] connectionsIn) {
		this.connectionsIn = connectionsIn;
	}
	
	public void continueInterval() 
	{
		
	}
	
	public int howLongTillAcceptingState(Road goingFrom) 
	{
		return 0;
	}

	
	public int howLongTillStopAccepting(Road goingFrom) 
	{
		return 0;
	}

	public double distanceOfSpaceOnNextRoad(Car c, Road a, Road b) 
	{
		return b.hasSpace();
	}
}
