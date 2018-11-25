
public class ClosedRoad extends IntersectionAbstract
{
	
	public ClosedRoad(double xCord, double yCord, double name, int degree)
	{
		super(xCord, yCord, name, degree);
	}
	
	@Override
	public Road getNextRoad(Car caller, Road nextRoad)
	{
		return caller.getCurrentRoad( ).getGoingTo( ).getConnectionsOut( )[0];
	}
	
	@Override
	public boolean isReconsideration()
	{
		return false;
	}
	
	@Override
	public int getInternalBuffer() { return 1; }
}