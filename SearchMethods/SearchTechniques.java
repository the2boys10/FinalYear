
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Class which deals with planning out of search techniques
 */
public interface SearchTechniques
{
	/**
	 * The method that will initialise a road weights for example in a simple bfs search all roads will be set a value of
	 * 1
	 * @param passWeights The initialise weights
	 * @param road The road we are setting the weights for
	 */
	public void determineWeightOfRoadInitial(double[] passWeights, Road road);
	
	/**
	 * The search method to use if a road has not been previously traversed
	 * @param callingRoad The road that the search is originating from
	 * @param a The linkedList of the search procedure the method should be called with a containing the callingroad
	 * @param b The linkedList of The Road and weights starting from only A
	 * @param c The linkedList of the roads that have been searched so far.
	 * @param endRoad The road that we would like to reach.
	 */
	public void selectWeightStrategyInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road endRoad);
	
	/**
	 * The search method to use if a has been previously traversed
	 * @param callingRoad The road that the search is originating from
	 * @param a The linkedList of the search procedure the method should be called with a containing the callingroad
	 * @param b The linkedList of The Road and weights starting from only A
	 * @param c The linkedList of the roads that have been searched so far.
	 * @param firstChosen The initial road taken in the search procedure.
	 * @param weight The weight of the current path.
	 * @param endRoad The road that we would like to reach.
	 * @param amountOfCarsSoFar The amount of cars that have travelled on the path so far.
	 */
	public void selectWeightStrategyAfterInit(Road callingRoad, LinkedList<Road> a, TreeSet<RoadAndWeight> b, LinkedList<Road> c, Road firstChosen, double weight, Road endRoad, int amountOfCarsSoFar);
	
	/**
	 * Method to update the cost of edges after initialisation
	 * @param car The car updating the weight
	 * @param road The road being updated
	 * @param reason The reason for the update.
	 */
	public void weightsOfEdgesAfterInitialization(Car car, Road road, String reason);
}
