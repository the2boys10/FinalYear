
public class CongestionGame extends BFSearch
{
	@Override
	public void determineWeightOfRoadInitial(double[] passWeights, Road road)
	{
		passWeights[0] = road.getCarsOnRoad( ).size( ) / road.getDistance( );
		for(int i = 1; i < passWeights.length; i++)
		{
			passWeights[i] = 0;
		}
	}
	
	@Override
	public void weightsOfEdgesAfterInitialization(Car car, Road road, String reason)
	{
		road.getWeightOfRoad( )[0] = road.getCarsOnRoad( ).size( ) / road.getDistance( );
		System.out.println(road.getWeightOfRoad( )[0]);
	}
}
