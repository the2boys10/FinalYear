
import java.util.LinkedList;

public class Passthrough extends IntersectionAbstract
{
	
	public Passthrough(double xCord, double yCord, double name, int degree)
	{
		super(xCord, yCord, name, degree);
	}
	
	@Override
	public double getRemainingDistance(Road currentRoad, PointOnRoad endGoal, double X1, double Y1, double X2, double Y2)
	{
		Road targetRoad;
		double distanceToGoalOnNextRoad;
		if(currentRoad.getGoingTo( ).getConnectionsOut( )[0].getGoingTo( ).equals(currentRoad.getGoingFrom( )))
		{
			targetRoad = currentRoad.getGoingTo( ).getConnectionsOut( )[1];
		}
		else
		{
			targetRoad = currentRoad.getGoingTo( ).getConnectionsOut( )[0];
		}
		if(targetRoad.equals(endGoal.specRoad) && (distanceToGoalOnNextRoad = Util.getLengthOfLine(X2, Y2, endGoal.xCord, endGoal.yCord)) < targetRoad.hasSpace( ))
		{
			return Util.getLengthOfLine(X1, Y1, X2, Y2) + distanceToGoalOnNextRoad;
		}
		else
		{
			return Util.getLengthOfLine(X1, Y1, X2, Y2) + targetRoad.hasSpace( );
		}
	}
	
	@Override
	public LinkedList<Road> validRoads(Road a)
	{
		LinkedList<Road> temp = new LinkedList<Road>( );
		int in;
		for(in = 0; in < getConnectionsIn( ).length; in++)
		{
			if(getConnectionsIn( )[in].equals(a))
			{
				break;
			}
		}
		for(int i = 0; i < getConnectionsOut( ).length - 1; i++)
		{
			if(! getConnectionsOut( )[(in + 1 + i) % getConnectionsOut( ).length].hasBeenChecked)
			{
				temp.add(getConnectionsOut( )[(in + 1 + i) % getConnectionsOut( ).length]);
			}
		}
		return temp;
	}
	
	@Override
	public Road getNextRoad(Car caller, Road nextRoad)
	{
		if(caller.getCurrentRoad( ).getGoingTo( ).getConnectionsOut( )[0].getGoingTo( ).equals(caller.getCurrentRoad( ).getGoingFrom( )))
		{
			return caller.getCurrentRoad( ).getGoingTo( ).getConnectionsOut( )[1];
		}
		else
		{
			return caller.getCurrentRoad( ).getGoingTo( ).getConnectionsOut( )[0];
		}
	}
	
	@Override
	public boolean isReconsideration()
	{
		return false;
	}
}