
public class Dijstra extends BFSearch
{
	@Override
	public void determineWeightOfRoadInitial(double[] passWeights, Road road)
	{
		passWeights[0] = road.getDistance( );
		for(int i = 1; i < passWeights.length; i++)
		{
			passWeights[i] = 0;
		}
	}
}
