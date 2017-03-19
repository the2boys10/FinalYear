import java.util.Arrays;
import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.ui.spriteManager.Sprite;

public class Road 
{

	private IntersectionAbstract goingFrom;
	private IntersectionAbstract goingTo;
	private double angleOfRoad;
	private double distanceOfNode;
	private double MaxSpeedOfCarsOnRoad;
	private double[] weightOfRoad;
	private double[] averageTimesOnIntersection;
	private int[] amountOfAvg,emptyArrayForUserAmountAdded;
	private char typeOfRoad = 'N';
	private double spacingX = 0;
	private double spacingY = 0;
	@SuppressWarnings("unused")
	private Edge roadRepresentation;
	private LinkedList<Car> carsOnRoad = new LinkedList<Car>();
	private double differenceX;
	private double differenceY;
	private char colorOfCarsOnRoad;
	public boolean hasBeenChecked;
	public double[] emptyArrayForUser;
	public double[] maxArrayForUser;
	public double[] minArrayForUser;
	public Road(IntersectionAbstract intersectionAbstract, IntersectionAbstract intersectionAbstract2, double anglesBetweenNodes, double distances, double speedOfRoads, Edge edge, char colorOfCarsOnRoad) 
	{
		hasBeenChecked = false;
		if(intersectionAbstract2.getClass().equals(Roundabout.class)||intersectionAbstract2.getClass().equals(ClosedRoad.class))
		{
			if(Config.UPDATEAVERAGETIME)
			{
				averageTimesOnIntersection = new double[intersectionAbstract2.getConnectionsIn().length+1];
				amountOfAvg = new int[intersectionAbstract2.getConnectionsIn().length+1];
			}
			if(Config.UPDATEAVERAGETIME)
			{
				emptyArrayForUser = new double[intersectionAbstract2.getConnectionsIn().length+1];
				emptyArrayForUserAmountAdded = new int[intersectionAbstract2.getConnectionsIn().length+1];
			}
			if(Config.UPDATEMAXTIME)
			{
				maxArrayForUser = new double[intersectionAbstract2.getConnectionsIn().length+1];
				Arrays.fill(maxArrayForUser,-1);
			}
			if(Config.UPDATEMINTIME)
			{
				minArrayForUser = new double[intersectionAbstract2.getConnectionsIn().length+1];
				Arrays.fill(minArrayForUser,Double.MAX_VALUE/2);
			}
			weightOfRoad = new double[intersectionAbstract2.getConnectionsIn().length+1];
		}
		else
		{
			if(Config.UPDATEAVERAGETIME)
			{
				averageTimesOnIntersection = new double[intersectionAbstract2.getConnectionsIn().length];
				amountOfAvg = new int[intersectionAbstract2.getConnectionsIn().length];
			}
			if(Config.createEmptyArrayForRoad)
			{
				emptyArrayForUser = new double[intersectionAbstract2.getConnectionsIn().length];
				emptyArrayForUserAmountAdded = new int[intersectionAbstract2.getConnectionsIn().length];
			}
			if(Config.UPDATEMAXTIME)
			{
				maxArrayForUser = new double[intersectionAbstract2.getConnectionsIn().length];
				Arrays.fill(maxArrayForUser,-1);
			}
			if(Config.UPDATEMINTIME)
			{
				minArrayForUser = new double[intersectionAbstract2.getConnectionsIn().length];
				Arrays.fill(minArrayForUser,Double.MAX_VALUE/2);
			}
			weightOfRoad = new double[intersectionAbstract2.getConnectionsIn().length];
		}
		this.setColorOfCarsOnRoad(colorOfCarsOnRoad);
		differenceX = Math.abs(intersectionAbstract.getxCord() - intersectionAbstract2.getxCord());
		differenceY = Math.abs(intersectionAbstract.getyCord() - intersectionAbstract2.getyCord());
		roadRepresentation = edge;
		goingFrom = intersectionAbstract;
		goingTo = intersectionAbstract2;
		this.angleOfRoad = anglesBetweenNodes;
		this.distanceOfNode = distances;
		this.MaxSpeedOfCarsOnRoad = speedOfRoads;
		if (getAngleOfRoad()==-Math.PI)
		{
			spacingX = -Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'R';
		}
		else if (getAngleOfRoad()>-Math.PI&&getAngleOfRoad()<-Math.PI/2)
		{
			spacingY = -Math.sin(Math.PI+getAngleOfRoad())*Config.SPACINGBETWEENVEHICLES;
			spacingX = -Math.cos(Math.PI+getAngleOfRoad())*Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'D';
		}
		else if (getAngleOfRoad()==-Math.PI/2)
		{
			spacingY = -Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'D';
		}
		else if (getAngleOfRoad()>-Math.PI/2&&getAngleOfRoad()<0)
		{
			spacingY = -Math.sin(-getAngleOfRoad())*Config.SPACINGBETWEENVEHICLES;
			spacingX = Math.cos(-getAngleOfRoad())*Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'D';
		}
		else if (getAngleOfRoad()==0)
		{
			spacingX = Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'L';
		}
		else if (getAngleOfRoad()>0&&getAngleOfRoad()<Math.PI/2)
		{
			spacingY = Math.sin(getAngleOfRoad())*Config.SPACINGBETWEENVEHICLES;
			spacingX = Math.cos(getAngleOfRoad())*Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'U';
		}
		else if (getAngleOfRoad()==Math.PI/2)
		{
			spacingY = Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'U';
		}
		else if (getAngleOfRoad()>Math.PI/2&&getAngleOfRoad()<Math.PI)
		{
			spacingY = Math.sin(Math.PI-getAngleOfRoad())*Config.SPACINGBETWEENVEHICLES;
			spacingX = -Math.cos(Math.PI-getAngleOfRoad())*Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'U';
		}
		else if (getAngleOfRoad()==Math.PI)
		{
			spacingX = -Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'R';
		}
	}
	
	
	public double hasSpace()
	{
		if(getCarsOnRoad().size()>0)
		{
			if(Util.isOnLineCars(
					getCarsOnRoad().get(getCarsOnRoad().size()-1).getCurrentX()+getSpacingX(), 
					getCarsOnRoad().get(getCarsOnRoad().size()-1).getCurrentY()+getSpacingY(),
					getGoingFrom().getxCord(),getGoingFrom().getyCord(),
					getGoingTo().getxCord(),getGoingTo().getyCord()))
			{
				double finalCarsValueX = getCarsOnRoad().get(getCarsOnRoad().size()-1).getCurrentX()+getSpacingX();
				double finalCarsValueY = getCarsOnRoad().get(getCarsOnRoad().size()-1).getCurrentY()+getSpacingY();
				double spaceOnNextRoad = Math.sqrt(((finalCarsValueX-this.getGoingFrom().getxCord())*(finalCarsValueX-this.getGoingFrom().getxCord()))+((finalCarsValueY-this.getGoingFrom().getyCord())*(finalCarsValueY-this.getGoingFrom().getyCord())));
				return spaceOnNextRoad;
			}
			else
			{
				return 0;
			}
		}
		return distanceOfNode;
	}
	
	public Car addCar(PointOnRoad startRoad, PointOnRoad endRoad, int fuelChoice, Sprite carSprite)
	{
		int index = 0;
		boolean activation = true;
		boolean shouldNotBeActivated = false;
		if (carsOnRoad.size() >= 1)
		{
			if (typeOfRoad=='L')
			{
				for (int i = 0; i < carsOnRoad.size(); i++)
				{
					index = i;
					if (carsOnRoad.get(i).getCurrentX() > startRoad.xCord)
					{
						activation = false;
						if(carsOnRoad.get(i).canCarStopInTime(startRoad.xCord, startRoad.yCord,carSprite)==true)
						{
							if(shouldNotBeActivated==false)
								activation = true;
							break;
						}
						shouldNotBeActivated=true;
					}
					index = i + 1;
				}
			}
			else if (typeOfRoad=='R')
			{
				
				for (int i = 0; i < carsOnRoad.size(); i++)
				{
					index = i;
					if (carsOnRoad.get(i).getCurrentX() < startRoad.xCord)
					{
						activation = false;
						if(carsOnRoad.get(i).canCarStopInTime(startRoad.xCord, startRoad.yCord,carSprite)==true)
						{
							if(shouldNotBeActivated==false)
								activation = true;
							break;
						}
						shouldNotBeActivated=true;
					}
					index = i + 1;
				}
			}
			else if (typeOfRoad=='U')
			{
				for (int i = 0; i < carsOnRoad.size(); i++)
				{
					if(carSprite.getId().equals("191"))
					{
						//System.out.println("here");
					}
					index = i;
					if (carsOnRoad.get(i).getCurrentY() > startRoad.yCord)
					{
						if(carSprite.getId().equals("191"))
						{
							System.out.println("here");
						}
						activation = false;
						if(carsOnRoad.get(i).canCarStopInTime(startRoad.xCord, startRoad.yCord,carSprite)==true)
						{
							if(shouldNotBeActivated==false)
								activation = true;
							break;
						}
						shouldNotBeActivated=true;
					}
					index = i + 1;
				}
			}
			else if (typeOfRoad=='D')
			{
				for (int i = 0; i < carsOnRoad.size(); i++)
				{
					index = i;
					if (carsOnRoad.get(i).getCurrentY() < startRoad.yCord)
					{
						activation = false;
						if(carsOnRoad.get(i).canCarStopInTime(startRoad.xCord, startRoad.yCord,carSprite)==true)
						{
							if(shouldNotBeActivated==false)
								activation = true;
							break;
						}
						shouldNotBeActivated=true;
					}
					index = i + 1;
				}
			}
		}
		if(activation == false)
		{
			//System.out.println("gotone");
		}
		Car tempCar = new Car(startRoad.xCord, startRoad.yCord, this, endRoad, activation, fuelChoice, carSprite);
		carsOnRoad.add(index, tempCar);
		return tempCar;
	}
	
	
	public double[] percentLine(double firstX, double secondX, double firstY, double secondY, int percent)
	{
		double percentDoub = (double) (100-percent) / 100.0;
		return (new double[]
		{ (secondX + (percentDoub * (firstX - secondX))), (secondY + (percentDoub * (firstY - secondY))) });
	}
	
	public PointOnRoad getPointOnRoad(int percentOfRoad)
	{
		double[] point = percentLine(goingFrom.getxCord(), goingTo.getxCord(), goingFrom.getyCord(), goingTo.getyCord(), percentOfRoad);
		return (new PointOnRoad(this, point[0], point[1]));
	}
	
	public boolean hasCarWithinSeconds(double time)
	{
		if(carsOnRoad.size()==0)
		{
			return false;
		}
		else
		{
			Car frontMostCar = carsOnRoad.get(0);
			if(frontMostCar.isHasMoved())
			{
				time = time+1;
			}
			double endSpeed = frontMostCar.getCurrentSpeed()+(frontMostCar.getAcceleration()*(time));
			double distanceTravelled = 0;
			if(endSpeed>MaxSpeedOfCarsOnRoad)
			{
				double timeTakenToGetMaxSpeed = (MaxSpeedOfCarsOnRoad - frontMostCar.getCurrentSpeed())/frontMostCar.getAcceleration();
				distanceTravelled=((frontMostCar.getCurrentSpeed()+MaxSpeedOfCarsOnRoad)*0.5*timeTakenToGetMaxSpeed)+((MaxSpeedOfCarsOnRoad)*((time)-timeTakenToGetMaxSpeed));
			}
			else
			{
				distanceTravelled=((frontMostCar.getCurrentSpeed()+endSpeed)*0.5*(time));
			}
			if(distanceTravelled>Util.getLengthOfLine(frontMostCar.getCurrentX(), frontMostCar.getCurrentY(), goingTo.getxCord(), goingTo.getyCord()))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	public IntersectionAbstract getTo()
	{
		return getGoingTo();
	}

	public IntersectionAbstract getFrom()
	{
		return getGoingFrom();
	}

	public double getDistance()
	{
		return distanceOfNode;
	}
	
	public void removeFrom(Car car)
	{
		getCarsOnRoad().remove(car);
	}

	public Road addTo(Car car)
	{
		getCarsOnRoad().add(car);
		return this;
	}
	
	public char getTypeOfRoad() {
		return typeOfRoad;
	}

	public LinkedList<Car> getCarsOnRoad() {
		return carsOnRoad;
	}

	public double getSpacingX() {
		return spacingX;
	}

	public double getSpacingY() {
		return spacingY;
	}
	
	public IntersectionAbstract getGoingFrom() {
		return goingFrom;
	}
	
	public IntersectionAbstract getGoingTo() {
		return goingTo;
	}


	public double getAngleOfRoad() {
		return angleOfRoad;
	}


	public double getMaxSpeedOfCarsOnRoad() {
		return MaxSpeedOfCarsOnRoad;
	}
	
	public double getDiffX() {
		return differenceX;
	}
	
	public double getDiffY() {
		return differenceY;
	}


	public char getColorOfCarsOnRoad() {
		return colorOfCarsOnRoad;
	}


	public void setColorOfCarsOnRoad(char colorOfCarsOnRoad) {
		this.colorOfCarsOnRoad = colorOfCarsOnRoad;
	}


	public double[] getWeightOfRoad() {
		return weightOfRoad;
	}


	public void setWeightOfRoad(double[] weightOfRoad) {
		this.weightOfRoad = weightOfRoad;
	}
	

	public double[] getAverageTimesOnIntersection() {
		return averageTimesOnIntersection;
	}


	public void setAverageTimesOnIntersection(double[] averageTimesOnIntersection) {
		this.averageTimesOnIntersection = averageTimesOnIntersection;
	}


	public int[] getAmountOfAvg() {
		return amountOfAvg;
	}



}
