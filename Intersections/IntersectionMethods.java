import java.util.LinkedList;

public interface IntersectionMethods
{
	String getName();
	void continueInterval();
	int howLongTillAcceptingState(Road goingFrom);
	int howLongTillStopAccepting(Road goingFrom);
	int getIndex(Road goingFrom, Road goingTo);
	double distanceOfSpaceOnNextRoad(Car c, Road a, Road b);
	int getIndexOfRoadIncoming(Road a);
	LinkedList<Road> validRoads(Road a);
	boolean isAccepting(Car car, Road currentRoad, Road nextRoad);
}
