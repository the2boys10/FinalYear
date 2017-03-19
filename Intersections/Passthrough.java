//import java.util.LinkedList;

import java.util.LinkedList;

import org.graphstream.graph.Node;

public class Passthrough extends IntersectionAbstract
{
	public Passthrough(double xCord, double yCord, double interval, double name, Node nodeRepresentation, int degree)
	{
		super(xCord,yCord,name,nodeRepresentation,degree);
	}
	
	public LinkedList<Road> validRoads(Road a)
	{
		LinkedList<Road> temp = new LinkedList<Road>();
		int in = 0;
		for (in = 0; in < getConnectionsIn().length; in++) 
		{
			if(getConnectionsIn()[in].equals(a))
			{
				break;
			}
		}
		for (int i = 0; i < getConnectionsOut().length-1; i++) 
		{
			if(getConnectionsOut()[(in+1+i)%getConnectionsOut().length].hasBeenChecked==false)
			{
				temp.add(getConnectionsOut()[(in+1+i)%getConnectionsOut().length]);	
			}
		}
		return temp;
	}
}
