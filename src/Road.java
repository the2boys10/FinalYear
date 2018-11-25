
import org.graphstream.ui.spriteManager.Sprite;

import java.util.Arrays;

public class Road implements Comparable<Road>
{
	private final IntersectionAbstract goingFrom;
	private final IntersectionAbstract goingTo;
	private final double angleOfRoad;
	private final double distanceOfNode;
	private final double MaxSpeedOfCarsOnRoad;
	private final double[] weightOfRoad;
	private double[] averageTimesOnIntersection;
	private int[] amountOfAvg;
	private char typeOfRoad = 'N';
	private double spacingX = 0;
	private double spacingY = 0;
	private final LinkedList<Car> carsOnRoad = new LinkedList<Car>( );
	private final double differenceX;
	private final double differenceY;
	private char colorOfCarsOnRoad;
	public boolean hasBeenChecked;
	public double[] maxArrayForUser;
	public double[] minArrayForUser;
	
	public Road(IntersectionAbstract intersectionAbstract, IntersectionAbstract intersectionAbstract2, double anglesBetweenNodes, double distances, double speedOfRoads, char colorOfCarsOnRoad)
	{
		hasBeenChecked = false;
		int[] emptyArrayForUserAmountAdded;
		double[] emptyArrayForUser;
		int internalBuffer = intersectionAbstract2.getInternalBuffer( );
		if(Config.UPDATEAVERAGETIME)
		{
			averageTimesOnIntersection = new double[intersectionAbstract2.getConnectionsIn( ).length + internalBuffer];
			amountOfAvg = new int[intersectionAbstract2.getConnectionsIn( ).length + internalBuffer];
		}
		if(Config.UPDATEAVERAGETIME)
		{
			emptyArrayForUser = new double[intersectionAbstract2.getConnectionsIn( ).length + internalBuffer];
			emptyArrayForUserAmountAdded = new int[intersectionAbstract2.getConnectionsIn( ).length + internalBuffer];
		}
		if(Config.UPDATEMAXTIME)
		{
			maxArrayForUser = new double[intersectionAbstract2.getConnectionsIn( ).length + internalBuffer];
			Arrays.fill(maxArrayForUser, - 1);
		}
		if(Config.UPDATEMINTIME)
		{
			minArrayForUser = new double[intersectionAbstract2.getConnectionsIn( ).length + internalBuffer];
			Arrays.fill(minArrayForUser, Double.MAX_VALUE / 2);
		}
		weightOfRoad = new double[intersectionAbstract2.getConnectionsIn( ).length + internalBuffer];
		this.setColorOfCarsOnRoad(colorOfCarsOnRoad);
		differenceX = Math.abs(intersectionAbstract.getxCord( ) - intersectionAbstract2.getxCord( ));
		differenceY = Math.abs(intersectionAbstract.getyCord( ) - intersectionAbstract2.getyCord( ));
		goingFrom = intersectionAbstract;
		goingTo = intersectionAbstract2;
		this.angleOfRoad = anglesBetweenNodes;
		this.distanceOfNode = distances;
		this.MaxSpeedOfCarsOnRoad = speedOfRoads;
		if(getAngleOfRoad( ) == - Math.PI)
		{
			spacingX = - Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'R';
		}
		else if(getAngleOfRoad( ) > - Math.PI && getAngleOfRoad( ) < - Math.PI / 2)
		{
			spacingY = - Math.sin(Math.PI + getAngleOfRoad( )) * Config.SPACINGBETWEENVEHICLES;
			spacingX = - Math.cos(Math.PI + getAngleOfRoad( )) * Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'D';
		}
		else if(getAngleOfRoad( ) == - Math.PI / 2)
		{
			spacingY = - Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'D';
		}
		else if(getAngleOfRoad( ) > - Math.PI / 2 && getAngleOfRoad( ) < 0)
		{
			spacingY = - Math.sin(- getAngleOfRoad( )) * Config.SPACINGBETWEENVEHICLES;
			spacingX = Math.cos(- getAngleOfRoad( )) * Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'D';
		}
		else if(getAngleOfRoad( ) == 0)
		{
			spacingX = Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'L';
		}
		else if(getAngleOfRoad( ) > 0 && getAngleOfRoad( ) < Math.PI / 2)
		{
			spacingY = Math.sin(getAngleOfRoad( )) * Config.SPACINGBETWEENVEHICLES;
			spacingX = Math.cos(getAngleOfRoad( )) * Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'U';
		}
		else if(getAngleOfRoad( ) == Math.PI / 2)
		{
			spacingY = Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'U';
		}
		else if(getAngleOfRoad( ) > Math.PI / 2 && getAngleOfRoad( ) < Math.PI)
		{
			spacingY = Math.sin(Math.PI - getAngleOfRoad( )) * Config.SPACINGBETWEENVEHICLES;
			spacingX = - Math.cos(Math.PI - getAngleOfRoad( )) * Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'U';
		}
		else if(getAngleOfRoad( ) == Math.PI)
		{
			spacingX = - Config.SPACINGBETWEENVEHICLES;
			typeOfRoad = 'R';
		}
	}
	
	public double hasSpace()
	{
		if(getCarsOnRoad( ).size( ) > 0)
		{
			if(Util.isOnLineCars(getCarsOnRoad( ).get(getCarsOnRoad( ).size( ) - 1).getCurrentX( ) + getSpacingX( ), getCarsOnRoad( ).get(getCarsOnRoad( ).size( ) - 1).getCurrentY( ) + getSpacingY( ), getGoingFrom( ).getxCord( ), getGoingFrom( ).getyCord( ), getGoingTo( ).getxCord( ), getGoingTo( ).getyCord( )))
			{
				double finalCarsValueX = getCarsOnRoad( ).get(getCarsOnRoad( ).size( ) - 1).getCurrentX( ) + getSpacingX( );
				double finalCarsValueY = getCarsOnRoad( ).get(getCarsOnRoad( ).size( ) - 1).getCurrentY( ) + getSpacingY( );
				return Math.sqrt(((finalCarsValueX - this.getGoingFrom( ).getxCord( )) * (finalCarsValueX - this.getGoingFrom( ).getxCord( ))) + ((finalCarsValueY - this.getGoingFrom( ).getyCord( )) * (finalCarsValueY - this.getGoingFrom( ).getyCord( ))));
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
		if(carsOnRoad.size( ) >= 1)
		{
			if(typeOfRoad == 'L')
			{
				for(int i = 0; i < carsOnRoad.size( ); i++)
				{
					index = i;
					if(carsOnRoad.get(i).getCurrentX( ) > startRoad.xCord)
					{
						activation = false;
						if(carsOnRoad.get(i).canCarStopInTime(startRoad.xCord, startRoad.yCord))
						{
							if(! shouldNotBeActivated)
								activation = true;
							break;
						}
						shouldNotBeActivated = true;
					}
					index = i + 1;
				}
			}
			else if(typeOfRoad == 'R')
			{
				
				for(int i = 0; i < carsOnRoad.size( ); i++)
				{
					index = i;
					if(carsOnRoad.get(i).getCurrentX( ) < startRoad.xCord)
					{
						activation = false;
						if(carsOnRoad.get(i).canCarStopInTime(startRoad.xCord, startRoad.yCord))
						{
							if(! shouldNotBeActivated)
								activation = true;
							break;
						}
						shouldNotBeActivated = true;
					}
					index = i + 1;
				}
			}
			else if(typeOfRoad == 'U')
			{
				for(int i = 0; i < carsOnRoad.size( ); i++)
				{
					index = i;
					if(carsOnRoad.get(i).getCurrentY( ) > startRoad.yCord)
					{
						activation = false;
						if(carsOnRoad.get(i).canCarStopInTime(startRoad.xCord, startRoad.yCord))
						{
							if(! shouldNotBeActivated)
								activation = true;
							break;
						}
						shouldNotBeActivated = true;
					}
					index = i + 1;
				}
			}
			else if(typeOfRoad == 'D')
			{
				for(int i = 0; i < carsOnRoad.size( ); i++)
				{
					index = i;
					if(carsOnRoad.get(i).getCurrentY( ) < startRoad.yCord)
					{
						activation = false;
						if(carsOnRoad.get(i).canCarStopInTime(startRoad.xCord, startRoad.yCord))
						{
							if(! shouldNotBeActivated)
								activation = true;
							break;
						}
						shouldNotBeActivated = true;
					}
					index = i + 1;
				}
			}
		}
		Car tempCar = new Car(startRoad.xCord, startRoad.yCord, this, endRoad, activation, fuelChoice, carSprite);
		carsOnRoad.add(index, tempCar);
		return tempCar;
	}
	
	private double[] percentLine(double firstX, double secondX, double firstY, double secondY, int percent)
	{
		double percentDoub = (double) (100 - percent) / 100.0;
		return (new double[]{(secondX + (percentDoub * (firstX - secondX))), (secondY + (percentDoub * (firstY - secondY)))});
	}
	
	public PointOnRoad getPointOnRoad(int percentOfRoad)
	{
		double[] point = percentLine(goingFrom.getxCord( ), goingTo.getxCord( ), goingFrom.getyCord( ), goingTo.getyCord( ), percentOfRoad);
		return (new PointOnRoad(this, point[0], point[1]));
	}
	
	public boolean hasCarWithinSeconds(double time, boolean hasCarMoved)
	{
		if(carsOnRoad.size( ) == 0)
		{
			return false;
		}
		else
		{
			Car frontMostCar = carsOnRoad.get(0);
			if(hasCarMoved)
			{
				time = time + 1;
			}
			double endSpeed = frontMostCar.getCurrentSpeed( ) + (Config.CARACCELERATIONANDDECELERATION * (time));
			double distanceTravelled;
			if(endSpeed > MaxSpeedOfCarsOnRoad)
			{
				double timeTakenToGetMaxSpeed = (MaxSpeedOfCarsOnRoad - frontMostCar.getCurrentSpeed( )) / Config.CARACCELERATIONANDDECELERATION;
				distanceTravelled = ((frontMostCar.getCurrentSpeed( ) + MaxSpeedOfCarsOnRoad) * 0.5 * timeTakenToGetMaxSpeed) + ((MaxSpeedOfCarsOnRoad) * ((time) - timeTakenToGetMaxSpeed));
			}
			else
			{
				distanceTravelled = ((frontMostCar.getCurrentSpeed( ) + endSpeed) * 0.5 * (time));
			}
			return distanceTravelled > Util.getLengthOfLine(frontMostCar.getCurrentX( ), frontMostCar.getCurrentY( ), goingTo.getxCord( ), goingTo.getyCord( ));
		}
	}
	
	public double getDistance()
	{
		return distanceOfNode;
	}
	
	public void removeFrom(Car car)
	{
		getCarsOnRoad( ).remove(car);
	}
	
	public void addTo(Car car)
	{
		getCarsOnRoad( ).add(car);
	}
	
	public char getTypeOfRoad()
	{
		return typeOfRoad;
	}
	
	public LinkedList<Car> getCarsOnRoad()
	{
		return carsOnRoad;
	}
	
	public double getSpacingX()
	{
		return spacingX;
	}
	
	public double getSpacingY()
	{
		return spacingY;
	}
	
	public IntersectionAbstract getGoingFrom()
	{
		return goingFrom;
	}
	
	public IntersectionAbstract getGoingTo()
	{
		return goingTo;
	}
	
	public double getAngleOfRoad()
	{
		return angleOfRoad;
	}
	
	public double getMaxSpeedOfCarsOnRoad()
	{
		return MaxSpeedOfCarsOnRoad;
	}
	
	public double getDiffX()
	{
		return differenceX;
	}
	
	public double getDiffY()
	{
		return differenceY;
	}
	
	public char getColorOfCarsOnRoad()
	{
		return colorOfCarsOnRoad;
	}
	
	private void setColorOfCarsOnRoad(char colorOfCarsOnRoad)
	{
		this.colorOfCarsOnRoad = colorOfCarsOnRoad;
	}
	
	public double[] getWeightOfRoad()
	{
		return weightOfRoad;
	}
	
	public double[] getAverageTimesOnIntersection()
	{
		return averageTimesOnIntersection;
	}
	
	public int[] getAmountOfAvg()
	{
		return amountOfAvg;
	}
	
	@Override
	public int compareTo(Road o)
	{
		if(getAngleOfRoad( ) < o.getAngleOfRoad( ))
		{
			return 1;
		}
		else
		{
			return - 1;
		}
	}
}
