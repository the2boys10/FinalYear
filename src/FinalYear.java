
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Resolutions;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.layout.springbox.BarnesHutLayout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.Viewer;

import java.awt.geom.Line2D;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


class FinalYear
{
	private static int lastCarAdded = 0;
	private static boolean initialization = true;
	private static final String nodesConfig = System.getProperty("user.dir");
	private static final String roadsConfig = System.getProperty("user.dir");
	private static final String carsConfig = System.getProperty("user.dir");
	private static String prefixConfig = "/" + Config.ALGORITHM + "/1cp" + Config.TIMEBETWEENEACHADDITION;
	private static int currentRunthrough = 1;
	private static AppendToFile nodeFile;
	private static AppendToFile roadFile;
	private static AppendToFile carFile;
	private static double[][] nodeInfoStored;
	private static int[][] connectionInfo;
	private static final boolean debug = true;
	private static Graph graph;
	private static Viewer viewer;
	private static double[][] anglesBetweenNodes;
	private static double[][] distances;
	private static IntersectionAbstract[] intersectionList;
	private static Road[] roadsInSystem;
	private static double[][] speedOfRoads;
	private static final LinkedList<Car> cars = new LinkedList<Car>( );
	private static SpriteManager sman;
	private static int amountOfCarsAddedSoFar = 0;
	private static double sumOfAllRoadLengths;
	private static AppendToFile outputRatioIdleAndOverall;
	private static AppendToFile outputRatioTimeAndDistance;
	private static AppendToFile idleTimeOnFinish;
	private static AppendToFile timeTakenToReachDestination;
	private static AppendToFile timeAccelerating;
	private static AppendToFile timeDecelerating;
	private static AppendToFile timeConstant;
	private static AppendToFile fuelUsedWhileAccelerating;
	private static AppendToFile fuelUsedWhileDecelerating;
	private static AppendToFile fuelUsedWhileIdle;
	private static AppendToFile fuelUsedWhileConstant;
	private static AppendToFile errorFile;
	private static BigDecimal TotalFuelUsedByFinishedVehicles = new BigDecimal(0);
	private static BigDecimal AverageTimeToGetToDestination = new BigDecimal(0);
	private static double amountOfCarsFinished = 0;
	private static String[] carConfig;
	static boolean startTracking = false;
	private static AppendToFile totalFuelUsed;
	private static AppendToFile averageSpeedAtTimestep;
	private static AppendToFile overAllStats;
	private static int timestep = 0;
	private static final StringBuilder carOutput = new StringBuilder( );
	
	
	public static void main(String[] args)
	{
		//				while(Config.ALGORITHM<=8)
		//				{
		UserInputFile.initialiseSearchStrategy( );
		while(Config.TIMEBETWEENEACHADDITION >= 50)
		{
			for(currentRunthrough = 1; currentRunthrough <= Config.HOWMANYRUNS; currentRunthrough++)
			{
				startTracking = false;
				initialization = true;
				cleanData( );
				prepareOutputFiles( );
				createGraph( );
				createNodes( );
				createRoads( );
				workOutAngles( );
				createObjectMap( );
				createCars( );
				sortOutWeightsInitialization( );
				initialization = false;
				setTimestep(0);
				while(getTimestep( ) < Config.HOWLONGTORUNFOR * 10 + (1000))
				{
					moveCars( );
					setTimestep(getTimestep( ) + 1);
					if(getTimestep( ) % Config.TIMEBETWEENEACHADDITION * 10 == 0 && getTimestep( ) != 0)
					{
						createCars( );
					}
					if(getTimestep( ) % Config.REFRESHRATE == 0)
					{
						for(Car car : cars)
						{
							car.getCarsSprite( ).setPosition(car.getCurrentX( ), car.getCurrentY( ), 0);
						}
					}
					if(getTimestep( ) % 3000 == 0)
					{
						if(Config.RANDOMCARS || Config.RANDOMINTERSECTIONS || Config.RANDOMROADS)
						{
							if(carFile != null)
							{
								carFile.appendToFile(carOutput);
							}
							else
							{
								carFile.openWriterAppend( );
								carFile.appendToFile(carOutput);
							}
							carOutput.setLength(0);
						}
					}
					if(getTimestep( ) == Config.BURNINPERIOD)
					{
						startTracking = true;
					}
				}
				for(Car car : cars)
				{
					car.getCarsSprite( ).setPosition(car.getCurrentX( ), car.getCurrentY( ), 0);
				}
				FileSinkImages pic = new FileSinkImages(OutputType.JPG, Resolutions.HD1080);
				pic.setLayoutPolicy(LayoutPolicy.NO_LAYOUT);
				viewer.disableAutoLayout( );
				try
				{
					Thread.sleep(500);
				}
				catch(InterruptedException e1)
				{
					e1.printStackTrace( );
				}
				try
				{
					pic.writeAll(viewer.getGraphicGraph( ), System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/Test" + currentRunthrough + ".jpg");
				}
				catch(IOException e)
				{
					e.printStackTrace( );
				}
				if(Config.RANDOMCARS || Config.RANDOMINTERSECTIONS || Config.RANDOMROADS)
				{
					if(carFile != null)
					{
						carFile.appendToFile(carOutput);
					}
					else
					{
						carFile.openWriterAppend( );
						carFile.appendToFile(carOutput);
					}
					carOutput.setLength(0);
				}
				viewer.close( );
				pushData( );
			}
			new File(System.getProperty("user.dir") + prefixConfig + "/testMeans").mkdir( );
			new File(System.getProperty("user.dir") + prefixConfig).mkdir( );
			double[] meanData = new double[4];
			for(currentRunthrough = 1; currentRunthrough <= Config.HOWMANYRUNS; currentRunthrough++)
			{
				ReadFile temp = new ReadFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/overAllStats/overAllStats.tsv");
				String[] lines = temp.OpenFile( );
				meanData[0] = ((meanData[0] * (currentRunthrough - 1)) + (Double.parseDouble(lines[1].split("=")[1]))) / (currentRunthrough);
				meanData[1] = ((meanData[1] * (currentRunthrough - 1)) + (Double.parseDouble(lines[2].split("=")[1]))) / (currentRunthrough);
				meanData[2] = ((meanData[2] * (currentRunthrough - 1)) + (Double.parseDouble(lines[3].split("=")[1]))) / (currentRunthrough);
				meanData[3] = ((meanData[3] * (currentRunthrough - 1)) + (Double.parseDouble(lines[4].split("=")[1]))) / (currentRunthrough);
			}
			AppendToFile means = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/testMeans/meanDataOnAlgorithm.tsv");
			means.cleanFile( );
			means.openWriterAppend( );
			means.appendToFile(new StringBuilder( ).append("#MeanData\nMean of how many = ").append(currentRunthrough - 1).append("\nTotal fuel consumed by finished cars=").append(meanData[0]).append("\nAverage time taken to get to destination=").append(meanData[1]).append("\nGlobal Throughput =").append(meanData[2]).append("\nAverage fuel usage of cars =").append(meanData[3]));
			means.closeWriterAppend( );
			Config.TIMEBETWEENEACHADDITION--;
			Config.TIMEBETWEENEACHADDITION--;
			prefixConfig = "/1cp" + Config.TIMEBETWEENEACHADDITION;
		}
		//					Config.ALGORITHM++;
		//					Config.TIMEBETWEENEACHADDITION = 50;
		//					prefixConfig = "/"+Config.ALGORITHM+"/1cp"+Config.TIMEBETWEENEACHADDITION;
		//				}
	}
	
	private static void prepareOutputFiles()
	{
		new File(System.getProperty("user.dir") + "/" + Config.ALGORITHM).mkdir( );
		new File(System.getProperty("user.dir") + prefixConfig).mkdir( );
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough).mkdir( );
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs").mkdir( );
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/RatioIdleTimeAndOverallTime").mkdir( );
		outputRatioIdleAndOverall = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/RatioIdleTimeAndOverallTime/RatioIdleTimeAndOverallTime.tsv");
		outputRatioIdleAndOverall.cleanFile( );
		outputRatioIdleAndOverall.openWriterAppend( );
		outputRatioIdleAndOverall.appendToFile(new StringBuilder( ).append("#RatioIdleTimeAndOverallTime"));
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/RatioTimeTakenAndDistance").mkdir( );
		outputRatioTimeAndDistance = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/RatioTimeTakenAndDistance/RatioTimeTakenAndDistance.tsv");
		outputRatioTimeAndDistance.cleanFile( );
		outputRatioTimeAndDistance.openWriterAppend( );
		outputRatioTimeAndDistance.appendToFile(new StringBuilder( ).append("#RatioTimeTakenAndDistance"));
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/IdleTime").mkdir( );
		idleTimeOnFinish = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/IdleTime/IdleTime.tsv");
		idleTimeOnFinish.cleanFile( );
		idleTimeOnFinish.openWriterAppend( );
		idleTimeOnFinish.appendToFile(new StringBuilder( ).append("#IdleTime"));
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/timeTakenToReachDestination").mkdir( );
		timeTakenToReachDestination = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/timeTakenToReachDestination/timeTakenToReachDestination.tsv");
		timeTakenToReachDestination.cleanFile( );
		timeTakenToReachDestination.openWriterAppend( );
		timeTakenToReachDestination.appendToFile(new StringBuilder( ).append("timeTakenToReachDestination"));
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/timeAccelerating").mkdir( );
		timeAccelerating = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/timeAccelerating/timeAccelerating.tsv");
		timeAccelerating.cleanFile( );
		timeAccelerating.openWriterAppend( );
		timeAccelerating.appendToFile(new StringBuilder( ).append("#timeAccelerating"));
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/timeDecelerating").mkdir( );
		timeDecelerating = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/timeDecelerating/timeDecelerating.tsv");
		timeDecelerating.cleanFile( );
		timeDecelerating.openWriterAppend( );
		timeDecelerating.appendToFile(new StringBuilder( ).append("#timeDecelerating"));
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/timeConstant").mkdir( );
		timeConstant = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/timeConstant/timeConstant.tsv");
		timeConstant.cleanFile( );
		timeConstant.openWriterAppend( );
		timeConstant.appendToFile(new StringBuilder( ).append("#timeConstant"));
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/fuelUsedWhileAccelerating").mkdir( );
		fuelUsedWhileAccelerating = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/fuelUsedWhileAccelerating/fuelUsedWhileAccelerating.tsv");
		fuelUsedWhileAccelerating.cleanFile( );
		fuelUsedWhileAccelerating.openWriterAppend( );
		fuelUsedWhileAccelerating.appendToFile(new StringBuilder( ).append("#fuelUsedWhileAccelerating"));
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/fuelUsedWhileDecelerating").mkdir( );
		fuelUsedWhileDecelerating = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/fuelUsedWhileDecelerating/fuelUsedWhileDecelerating.tsv");
		fuelUsedWhileDecelerating.cleanFile( );
		fuelUsedWhileDecelerating.openWriterAppend( );
		fuelUsedWhileDecelerating.appendToFile(new StringBuilder( ).append("#fuelUsedWhileDecelerating"));
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/fuelUsedWhileIdle").mkdir( );
		fuelUsedWhileIdle = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/fuelUsedWhileIdle/fuelUsedWhileIdle.tsv");
		fuelUsedWhileIdle.cleanFile( );
		fuelUsedWhileIdle.openWriterAppend( );
		fuelUsedWhileIdle.appendToFile(new StringBuilder( ).append("#fuelUsedWhileIdle"));
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/fuelUsedWhileConstant").mkdir( );
		fuelUsedWhileConstant = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/fuelUsedWhileConstant/fuelUsedWhileConstant.tsv");
		fuelUsedWhileConstant.cleanFile( );
		fuelUsedWhileConstant.openWriterAppend( );
		fuelUsedWhileConstant.appendToFile(new StringBuilder( ).append("#fuelUsedWhileConstant"));
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/totalFuelUsed").mkdir( );
		totalFuelUsed = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/totalFuelUsed/totalFuelUsed.tsv");
		totalFuelUsed.cleanFile( );
		totalFuelUsed.openWriterAppend( );
		totalFuelUsed.appendToFile(new StringBuilder( ).append("#totalFuelUsed"));
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/averageSpeedAtTimestep").mkdir( );
		averageSpeedAtTimestep = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/averageSpeedAtTimestep/averageSpeedAtTimestep.tsv");
		averageSpeedAtTimestep.cleanFile( );
		averageSpeedAtTimestep.openWriterAppend( );
		averageSpeedAtTimestep.appendToFile(new StringBuilder( ).append("#averageSpeedAtTimestep"));
		errorFile = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/error.txt");
		errorFile.cleanFile( );
		errorFile.openWriterAppend( );
		new File(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/overAllStats").mkdir( );
		overAllStats = new AppendToFile(System.getProperty("user.dir") + prefixConfig + "/test" + currentRunthrough + "/graphs/overAllStats/overAllStats.tsv");
		overAllStats.cleanFile( );
		overAllStats.openWriterAppend( );
		overAllStats.appendToFile(new StringBuilder( ).append("#overAllStats"));
	}
	
	private static void pushData()
	{
		if(carFile != null)
		{
			carFile.closeWriterAppend( );
		}
		overAllStats.appendToFile(new StringBuilder( ).append("\nTotal Fuel Consumed By Finished Cars=").append(TotalFuelUsedByFinishedVehicles.doubleValue( )).append("\nAverage time taken to get to destination=").append(AverageTimeToGetToDestination.doubleValue( )).append("\nGlobal throughput=").append(amountOfCarsFinished / amountOfCarsAddedSoFar).append("\nAverage fuel used by finished vehicles =").append(TotalFuelUsedByFinishedVehicles.divide(new BigDecimal(amountOfCarsFinished), 50, RoundingMode.HALF_EVEN).doubleValue( )));
		overAllStats.closeWriterAppend( );
		outputRatioIdleAndOverall.closeWriterAppend( );
		outputRatioTimeAndDistance.closeWriterAppend( );
		idleTimeOnFinish.closeWriterAppend( );
		timeAccelerating.closeWriterAppend( );
		timeDecelerating.closeWriterAppend( );
		timeConstant.closeWriterAppend( );
		fuelUsedWhileAccelerating.closeWriterAppend( );
		fuelUsedWhileDecelerating.closeWriterAppend( );
		fuelUsedWhileIdle.closeWriterAppend( );
		fuelUsedWhileConstant.closeWriterAppend( );
		timeTakenToReachDestination.closeWriterAppend( );
		totalFuelUsed.closeWriterAppend( );
		averageSpeedAtTimestep.closeWriterAppend( );
		errorFile.closeWriterAppend( );
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set border 3;set xrange [0:1];set yzeroaxis;set boxwidth 0.005 absolute;set style fill solid 1.0 noborder;bin_width = 0.0005;bin_number(x) = floor(x/bin_width);rounded(x) = bin_width * ( bin_number(x) + 0.5 );set term png;set terminal png size 1000,300;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/RatioIdleTimeAndOverallTime/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/RatioIdleTimeAndOverallTime/RatioIdleTimeAndOverallTime.tsv' using (rounded($1)):(1) smooth frequency with boxes;"
		//           });
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set border 3;set yzeroaxis;set boxwidth 0.005 absolute;set style fill solid 1.0 noborder;bin_width = 0.005;bin_number(x) = floor(x/bin_width);rounded(x) = bin_width * ( bin_number(x) + 0.5 );set terminal png size 1000,300;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/RatioTimeTakenAndDistance/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/RatioTimeTakenAndDistance/RatioTimeTakenAndDistance.tsv' using (rounded($1)):(1) smooth frequency with boxes;"
		//           });
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set border 3;set yzeroaxis;set boxwidth 0.05 absolute;set style fill solid 1.0 noborder;bin_width = 10;bin_number(x) = floor(x/bin_width);rounded(x) = bin_width * ( bin_number(x) + 0.5 );set term png;set terminal png size 1000,300;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/IdleTime/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/IdleTime/IdleTime.tsv' using (rounded($1)):(1) smooth frequency with boxes;"
		//           });
		//
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set border 3;set yzeroaxis;set yrange [0:];set boxwidth 0.05 absolute;set style fill solid 1.0 noborder;bin_width = 50;bin_number(x) = floor(x/bin_width);rounded(x) = bin_width * ( bin_number(x) + 0.5 );set term png;set terminal png size 1000,300;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/timeTakenToReachDestination/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/timeTakenToReachDestination/timeTakenToReachDestination.tsv' using (rounded($1)):(1) smooth frequency with boxes;"
		//           });
		//
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set border 3;set yzeroaxis;set boxwidth 0.05 absolute;set style fill solid 1.0 noborder;bin_width = 20;bin_number(x) = floor(x/bin_width);rounded(x) = bin_width * ( bin_number(x) + 0.5 );set term png;set terminal png size 1000,300;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/timeAccelerating/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/timeAccelerating/timeAccelerating.tsv' using (rounded($1)):(1) smooth frequency with boxes;"
		//           });
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set border 3;set yzeroaxis;set boxwidth 0.05 absolute;set style fill solid 1.0 noborder;bin_width = 20;bin_number(x) = floor(x/bin_width);rounded(x) = bin_width * ( bin_number(x) + 0.5 );set term png;set terminal png size 1000,300;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/timeDecelerating/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/timeDecelerating/timeDecelerating.tsv' using (rounded($1)):(1) smooth frequency with boxes;"
		//           });
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set border 3;set yzeroaxis;set boxwidth 0.05 absolute;set style fill solid 1.0 noborder;bin_width = 20;bin_number(x) = floor(x/bin_width);rounded(x) = bin_width * ( bin_number(x) + 0.5 );set term png;set terminal png size 1000,300;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/timeConstant/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/timeConstant/timeConstant.tsv' using (rounded($1)):(1) smooth frequency with boxes;"
		//           });
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set border 3;set yzeroaxis;set yrange [0:];set xrange [0:];set boxwidth 0.00001 absolute;set style fill solid 1.0 noborder;bin_width = 0.001;bin_number(x) = floor(x/bin_width);rounded(x) = bin_width * ( bin_number(x) + 0.5 );set term png;set terminal png size 1000,300;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/fuelUsedWhileAccelerating/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/fuelUsedWhileAccelerating/fuelUsedWhileAccelerating.tsv' using (rounded($1)):(1) smooth frequency with boxes;"
		//           });
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set border 3;set yzeroaxis;set yrange [0:];set xrange [0:];set boxwidth 0.00001 absolute;set style fill solid 1.0 noborder;bin_width = 0.001;bin_number(x) = floor(x/bin_width);rounded(x) = bin_width * ( bin_number(x) + 0.5 );set term png;set terminal png size 1000,300;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/fuelUsedWhileDecelerating/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/fuelUsedWhileDecelerating/fuelUsedWhileDecelerating.tsv' using (rounded($1)):(1) smooth frequency with boxes;"
		//           });
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set border 3;set yzeroaxis;set yrange [0:];set xrange [0:];set boxwidth 0.00001 absolute;set style fill solid 1.0 noborder;bin_width = 0.0001;bin_number(x) = floor(x/bin_width);rounded(x) = bin_width * ( bin_number(x) + 0.5 );set term png;set terminal png size 1000,300;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/fuelUsedWhileIdle/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/fuelUsedWhileIdle/fuelUsedWhileIdle.tsv' using (rounded($1)):(1) smooth frequency with boxes;"
		//           });
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set border 3;set yzeroaxis;set yrange [0:];set xrange [0:];set boxwidth 0.001 absolute;set style fill solid 1.0 noborder;bin_width = 0.001;bin_number(x) = floor(x/bin_width);rounded(x) = bin_width * ( bin_number(x) + 0.5 );set term png;set terminal png size 1000,300;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/fuelUsedWhileConstant/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/fuelUsedWhileConstant/fuelUsedWhileConstant.tsv' using (rounded($1)):(1) smooth frequency with boxes;"
		//           });
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set border 3;set yzeroaxis;set yrange [0:];set xrange [0:];set boxwidth 0.001 absolute;set style fill solid 1.0 noborder;bin_width = 0.005;bin_number(x) = floor(x/bin_width);rounded(x) = bin_width * ( bin_number(x) + 0.5 );set term png;set terminal png size 1000,300;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/totalFuelUsed/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/totalFuelUsed/totalFuelUsed.tsv' using (rounded($1)):(1) smooth frequency with boxes;"
		//           });
		//		createGraph(new String[]{"C:/Program Files/gnuplot/bin/gnuplot",
		//            "-e",
		//            "reset;set key off;set terminal png size 1000,300;set yrange [0:];set xrange [1000:];set term png;set output '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/averageSpeedAtTimestep/xyz.png';plot '"+System.getProperty("user.dir")+prefixConfig+"/test"+currentRunthrough+"/graphs/averageSpeedAtTimestep/averageSpeedAtTimestep.tsv' with lines"
		//           });
	}
	
	private static void createGraph(String[] s)
	{
		try
		{
			Runtime rt = Runtime.getRuntime( );
			Process proc = rt.exec(s);
			InputStream stdin = proc.getErrorStream( );
			InputStreamReader isr = new InputStreamReader(stdin);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while((line = br.readLine( )) != null)
				System.err.println("gnuplot:" + line);
			proc.getInputStream( ).close( );
			proc.getOutputStream( ).close( );
			proc.getErrorStream( ).close( );
		}
		catch(Exception e)
		{
			System.err.println("Fail: " + e);
		}
	}
	
	private static void addToDataAtCarFinish(Car carFinished)
	{
		String carsId = carFinished.getCarsSprite( ).getId( );
		outputRatioIdleAndOverall.appendToFile(new StringBuilder( ).append("\n").append(carFinished.getFuelDepandant( ).getTimeIdle( ) / carFinished.getFuelDepandant( ).getTimeTakenToGetToDestination( )).append(" ").append(carsId));
		outputRatioTimeAndDistance.appendToFile(new StringBuilder( ).append("\n").append(carFinished.getFuelDepandant( ).getTimeTakenToGetToDestination( ) / carFinished.getFuelDepandant( ).getDistanceTravelledSoFar( ).doubleValue( )).append(" ").append(carsId));
		idleTimeOnFinish.appendToFile(new StringBuilder( ).append("\n").append(carFinished.getFuelDepandant( ).getTimeIdle( )).append(" ").append(carsId));
		timeTakenToReachDestination.appendToFile(new StringBuilder( ).append("\n").append(carFinished.getFuelDepandant( ).getTimeTakenToGetToDestination( )).append(" ").append(carsId));
		timeAccelerating.appendToFile(new StringBuilder( ).append("\n").append(carFinished.getFuelDepandant( ).getTimeTakenAccelerating( )).append(" ").append(carsId));
		timeDecelerating.appendToFile(new StringBuilder( ).append("\n").append(carFinished.getFuelDepandant( ).getTimeTakenDecelerating( )).append(" ").append(carsId));
		timeConstant.appendToFile(new StringBuilder( ).append("\n").append(carFinished.getFuelDepandant( ).getTimeTakenConstant( )).append(" ").append(carsId));
		fuelUsedWhileAccelerating.appendToFile(new StringBuilder( ).append("\n").append(carFinished.getFuelDepandant( ).getFuelUsedSoFarAccelerating( ).doubleValue( )).append(" ").append(carsId));
		fuelUsedWhileDecelerating.appendToFile(new StringBuilder( ).append("\n").append(carFinished.getFuelDepandant( ).getFuelUsedSoFarDecelerating( ).doubleValue( )).append(" ").append(carsId));
		fuelUsedWhileIdle.appendToFile(new StringBuilder( ).append("\n").append(carFinished.getFuelDepandant( ).getFuelUsedSoFarIdle( ).doubleValue( )).append(" ").append(carsId));
		fuelUsedWhileConstant.appendToFile(new StringBuilder( ).append("\n").append(carFinished.getFuelDepandant( ).getFuelUsedSoFarConstant( ).doubleValue( )).append(" ").append(carsId));
		BigDecimal totalFuelUsedCumalative = carFinished.getFuelDepandant( ).getFuelUsedSoFarAccelerating( ).add(carFinished.getFuelDepandant( ).getFuelUsedSoFarDecelerating( )).add(carFinished.getFuelDepandant( ).getFuelUsedSoFarIdle( )).add(carFinished.getFuelDepandant( ).getFuelUsedSoFarConstant( ));
		totalFuelUsed.appendToFile(new StringBuilder( ).append("\n ").append(totalFuelUsedCumalative.doubleValue( )).append(" ").append(carsId));
		TotalFuelUsedByFinishedVehicles = TotalFuelUsedByFinishedVehicles.add(totalFuelUsedCumalative);
		AverageTimeToGetToDestination = (AverageTimeToGetToDestination.multiply(new BigDecimal(amountOfCarsFinished)).add(new BigDecimal(carFinished.getFuelDepandant( ).getTimeTakenToGetToDestination( )))).divide(new BigDecimal(amountOfCarsFinished + 1), 50, RoundingMode.HALF_EVEN);
		amountOfCarsFinished++;
	}
	
	private static void cleanData()
	{
		anglesBetweenNodes = null;
		carConfig = null;
		distances = null;
		intersectionList = null;
		roadsInSystem = null;
		speedOfRoads = null;
		cars.clear( );
		connectionInfo = null;
		nodeInfoStored = null;
		sumOfAllRoadLengths = 0;
		nodeFile = null;
		roadFile = null;
		carFile = null;
		outputRatioIdleAndOverall = null;
		outputRatioTimeAndDistance = null;
		idleTimeOnFinish = null;
		timeTakenToReachDestination = null;
		timeAccelerating = null;
		timeDecelerating = null;
		timeConstant = null;
		fuelUsedWhileAccelerating = null;
		fuelUsedWhileDecelerating = null;
		fuelUsedWhileIdle = null;
		fuelUsedWhileConstant = null;
		totalFuelUsed = null;
		TotalFuelUsedByFinishedVehicles = new BigDecimal(0);
		AverageTimeToGetToDestination = new BigDecimal(0);
		amountOfCarsFinished = 0;
		amountOfCarsAddedSoFar = 0;
	}
	
	private static void createCars()
	{
		if(Config.RANDOMROADS || Config.RANDOMINTERSECTIONS || Config.RANDOMCARS)
		{
			if(carFile == null)
			{
				carFile = new AppendToFile(carsConfig + prefixConfig + "/test" + currentRunthrough + "/cars.txt");
			}
			if(initialization)
			{
				carFile.cleanFile( );
			}
			createCarsRandom( );
		}
		else
		{
			createCarsConfigUserInput( );
			createCarsConfigIndex( );
		}
	}
	
	private static void moveCars()
	{
		LinkedList<Road> carsWhichFailedToMove = new LinkedList<Road>( );
		for(IntersectionAbstract anIntersectionList : intersectionList)
		{
			anIntersectionList.continueInterval( );
		}
		for(Road temp : roadsInSystem)
		{
			for(int j = 0; j < temp.getCarsOnRoad( ).size( ); j++)
			{
				String returnValue = temp.getCarsOnRoad( ).get(j).moveCar(false);
				if(returnValue.equals("Finished"))
				{
					Car currentCar = temp.getCarsOnRoad( ).remove(j);
					if(Config.REASKUSERTOCHANGEONFINISH)
					{
						UserInputFile.weightsOfEdgesAfterInitialization(currentCar, currentCar.getCurrentRoad( ), "CarFinished");
					}
					currentCar.getCurrentRoad( ).removeFrom(currentCar);
					cars.remove(currentCar);
					sman.removeSprite(currentCar.getCarsSprite( ).getId( ));
					j--;
					if(FinalYear.startTracking)
					{
						addToDataAtCarFinish(currentCar);
					}
				}
				else if(returnValue.equals("Failed to Move"))
				{
					carsWhichFailedToMove.add(temp);
					break;
				}
			}
		}
		while(Car.isAnyChange( ))
		{
			Car.setAnyChange(false);
			for(int i = 0; i < carsWhichFailedToMove.size( ); i++)
			{
				Road temp = carsWhichFailedToMove.get(i);
				for(int j = 0; j < temp.getCarsOnRoad( ).size( ); j++)
				{
					String returnValue = temp.getCarsOnRoad( ).get(j).moveCar(false);
					if(returnValue.equals("Finished"))
					{
						Car currentCar = temp.getCarsOnRoad( ).remove(j);
						if(Config.REASKUSERTOCHANGEONFINISH)
						{
							UserInputFile.weightsOfEdgesAfterInitialization(currentCar, currentCar.getCurrentRoad( ), "CarFinished");
						}
						currentCar.getCurrentRoad( ).removeFrom(currentCar);
						cars.remove(currentCar);
						sman.removeSprite(currentCar.getCarsSprite( ).getId( ));
						j--;
						if(FinalYear.startTracking)
						{
							addToDataAtCarFinish(currentCar);
						}
					}
					else if(returnValue.equals("Failed to Move"))
					{
						break;
					}
					else if(temp.getCarsOnRoad( ).size( ) == j - 1)
					{
						carsWhichFailedToMove.remove(carsWhichFailedToMove.get(i));
					}
				}
			}
		}
		int amountOfCarsWhichFailedToMove = 0;
		for(Road temp : carsWhichFailedToMove)
		{
			for(int j = 0; j < temp.getCarsOnRoad( ).size( ); j++)
			{
				String returnValue = temp.getCarsOnRoad( ).get(j).moveCar(true);
				if(returnValue.equals("Finished"))
				{
					Car currentCar = temp.getCarsOnRoad( ).remove(j);
					if(Config.REASKUSERTOCHANGEONFINISH)
					{
						UserInputFile.weightsOfEdgesAfterInitialization(currentCar, currentCar.getCurrentRoad( ), "CarFinished");
					}
					currentCar.getCurrentRoad( ).removeFrom(currentCar);
					cars.remove(currentCar);
					sman.removeSprite(currentCar.getCarsSprite( ).getId( ));
					j--;
					if(FinalYear.startTracking)
					{
						addToDataAtCarFinish(currentCar);
					}
				}
				if(returnValue.equals("Failed to Move"))
				{
					amountOfCarsWhichFailedToMove++;
				}
			}
		}
		if(FinalYear.startTracking)
		{
			sortOutDataAtEndOfTimestep( );
		}
		for(Car car : cars)
		{
			car.setHasMoved(false);
			if(car.getFuelApplied( ) > 1)
			{
				System.out.println("We have Problems");
			}
			car.setFuelApplied(0);
		}
	}
	
	private static void sortOutDataAtEndOfTimestep()
	{
		averageSpeedAtTimestep.appendToFile(new StringBuilder( ).append("\n").append(getTimestep( )).append(" ").append(FuelDependancies.getAverageSpeedOfVehiclesInTimeStep( ).doubleValue( )));
		FuelDependancies.setAverageSpeedOfVehiclesInTimeStep(new BigDecimal(0));
		FuelDependancies.setAverageOfHowMany(0);
	}
	
	private static void sortOutWeightsInitialization()
	{
		for(Road aRoadsInSystem : roadsInSystem)
		{
			UserInputFile.determineWeightOfRoadInitial(aRoadsInSystem.getWeightOfRoad( ), aRoadsInSystem);
		}
	}
	
	private static void createCarsConfigIndex()
	{
		int k = 0;
		while((cars.size( ) < Config.AMOUNTOFCARSINSIMULATION && initialization) || k < Config.AMOUNTOFVEHICLESTOADDONEACHADDITION)
		{
			if(carConfig == null)
			{
				ReadFile file = new ReadFile(carsConfig + prefixConfig + "/test" + currentRunthrough + "/cars.txt");
				carConfig = file.OpenFile( );
			}
			for(String aCarConfig : carConfig)
			{
				String[] connections = carConfig[lastCarAdded].split(" ");
				lastCarAdded = (lastCarAdded + 1) % carConfig.length;
				Sprite carSprite = sman.addSprite(amountOfCarsAddedSoFar + "");
				amountOfCarsAddedSoFar++;
				Car carAdded = (roadsInSystem[Integer.parseInt(connections[0])].addCar(roadsInSystem[Integer.parseInt(connections[0])].getPointOnRoad(Integer.parseInt(connections[1])), roadsInSystem[Integer.parseInt(connections[2])].getPointOnRoad(Integer.parseInt(connections[3])), Integer.parseInt(connections[4]), carSprite));
				if(Config.REASKUSERTOCHANGEONADDITION)
				{
					UserInputFile.weightsOfEdgesAfterInitialization(carAdded, carAdded.getCurrentRoad( ), "CarAdded");
				}
				cars.add(carAdded);
				carSprite.setPosition(carAdded.getCurrentX( ), carAdded.getCurrentY( ), 0);
				k++;
				if(k >= Config.AMOUNTOFVEHICLESTOADDONEACHADDITION || (cars.size( ) >= Config.AMOUNTOFCARSINSIMULATION && initialization))
				{
					break;
				}
			}
		}
	}
	
	private static void createCarsConfigUserInput()
	{
		ReadFile file = new ReadFile(carsConfig + prefixConfig + "/test" + currentRunthrough + "/cars.txt");
		String[] aryLines = file.OpenFile( );
		int firstLine = 0;
		boolean wrongFormat = true;
		StringBuilder output = new StringBuilder( );
		for(String aryLine : aryLines)
		{
			String[] connections = aryLine.split(" ");
			if(connections.length == 5)
			{
				wrongFormat = false;
				break;
			}
			int goingFromIndex = - 1;
			int goingToIndex = - 1;
			for(int j = 0; j < roadsInSystem.length; j++)
			{
				if(roadsInSystem[j].getGoingFrom( ).getName( ).equals(connections[0]) && roadsInSystem[j].getGoingTo( ).getName( ).equals(connections[1]))
				{
					goingFromIndex = j;
				}
				if(roadsInSystem[j].getGoingFrom( ).getName( ).equals(connections[3]) && roadsInSystem[j].getGoingTo( ).getName( ).equals(connections[4]))
				{
					goingToIndex = j;
				}
				if(goingFromIndex != - 1 && goingToIndex != - 1)
				{
					break;
				}
			}
			if(firstLine == 0)
			{
				output.append(goingFromIndex).append(" ").append(connections[2]).append(" ").append(goingToIndex).append(" ").append(connections[5]).append(" ").append(connections[6]);
				firstLine = 1;
			}
			else
				output.append("\n").append(goingFromIndex).append(" ").append(connections[2]).append(" ").append(goingToIndex).append(" ").append(connections[5]).append(" ").append(connections[6]);
		}
		if(wrongFormat)
		{
			AppendToFile fileWrite = new AppendToFile(carsConfig + prefixConfig + "/test" + currentRunthrough + "/cars.txt");
			fileWrite.cleanFile( );
			fileWrite.appendToFile(output);
		}
	}
	
	
	private static void createCarsRandom()
	{
		int k = 0;
		boolean newLine = false;
		while((cars.size( ) < Config.AMOUNTOFCARSINSIMULATION && initialization) || k < Config.AMOUNTOFVEHICLESTOADDONEACHADDITION)
		{
			Random rand = new Random( );
			int roadToPick = rand.nextInt((int) sumOfAllRoadLengths);
			int roadChoice = 0;
			while(roadToPick >= 0)
			{
				roadToPick = roadToPick - (int) Math.ceil(roadsInSystem[roadChoice].getDistance( ));
				roadChoice++;
			}
			roadChoice--;
			roadToPick = rand.nextInt((int) sumOfAllRoadLengths);
			int roadChoice2 = 0;
			while(roadToPick >= 0)
			{
				roadToPick = roadToPick - (int) Math.ceil(roadsInSystem[roadChoice2].getDistance( ));
				roadChoice2++;
			}
			roadChoice2--;
			int percentIntoRoad = rand.nextInt(80) + 10;
			int percentIntoRoadEndPoint = rand.nextInt(80) + 10;
			Sprite carSprite = sman.addSprite(amountOfCarsAddedSoFar + "");
			amountOfCarsAddedSoFar++;
			if(newLine || ! initialization)
			{
				carOutput.append("\n");
			}
			int sumPercent = Config.LOWFUELPERCENT + Config.HIGHFUELPERCENT + Config.MEDIUMFUELPERCENT;
			int randomNumber = rand.nextInt(sumPercent);
			int fuel;
			if(randomNumber < Config.LOWFUELPERCENT)
			{
				fuel = 0;
			}
			else if(randomNumber < Config.MEDIUMFUELPERCENT + Config.LOWFUELPERCENT)
			{
				fuel = 1;
			}
			else
			{
				fuel = 2;
			}
			carOutput.append(roadChoice).append(" ").append(percentIntoRoad).append(" ").append(roadChoice2).append(" ").append(percentIntoRoadEndPoint).append(" ").append(fuel);
			newLine = true;
			Car carAdded = (roadsInSystem[roadChoice].addCar(roadsInSystem[roadChoice].getPointOnRoad(percentIntoRoad), roadsInSystem[roadChoice2].getPointOnRoad(percentIntoRoadEndPoint), fuel, carSprite));
			if(Config.REASKUSERTOCHANGEONADDITION)
			{
				UserInputFile.weightsOfEdgesAfterInitialization(carAdded, carAdded.getCurrentRoad( ), "CarAdded");
			}
			cars.add(carAdded);
			carSprite.setPosition(carAdded.getCurrentX( ), carAdded.getCurrentY( ), 0);
			k++;
		}
	}
	
	private static void createObjectMap()
	{
		intersectionList = new IntersectionAbstract[nodeInfoStored.length];
		for(int i = 0; i < nodeInfoStored.length; i++)
		{
			if(nodeInfoStored[i][4] > 4)
			{
				intersectionList[i] = new Roundabout(nodeInfoStored[i][0], nodeInfoStored[i][1], nodeInfoStored[i][3], (int) nodeInfoStored[i][4]);
			}
			else if(nodeInfoStored[i][4] == 4)
			{
				intersectionList[i] = new CrossRoads(nodeInfoStored[i][0], nodeInfoStored[i][1], nodeInfoStored[i][2], nodeInfoStored[i][3], (int) nodeInfoStored[i][4]);
			}
			else if(nodeInfoStored[i][4] == 3)
			{
				intersectionList[i] = new Tjunction(nodeInfoStored[i][0], nodeInfoStored[i][1], nodeInfoStored[i][3], (int) nodeInfoStored[i][4]);
			}
			else if(nodeInfoStored[i][4] == 2)
			{
				intersectionList[i] = new Passthrough(nodeInfoStored[i][0], nodeInfoStored[i][1], nodeInfoStored[i][3], (int) nodeInfoStored[i][4]);
			}
			else if(nodeInfoStored[i][4] == 1)
			{
				intersectionList[i] = new ClosedRoad(nodeInfoStored[i][0], nodeInfoStored[i][1], nodeInfoStored[i][3], (int) nodeInfoStored[i][4]);
			}
		}
		int amountOfRoadsOverall = 0;
		for(int[] aConnectionInfo : connectionInfo)
		{
			for(int j = 0; j < connectionInfo.length; j++)
			{
				if(aConnectionInfo[j] == 1)
				{
					amountOfRoadsOverall++;
				}
			}
		}
		roadsInSystem = new Road[amountOfRoadsOverall];
		int currentInsertIndexOverall = 0;
		for(int i = 0; i < connectionInfo.length; i++)
		{
			TreeSet<Road> listOfConnectedNodes = new TreeSet<Road>( );
			for(int j = 0; j < connectionInfo.length; j++)
			{
				if(connectionInfo[i][j] == 1)
				{
					sumOfAllRoadLengths = sumOfAllRoadLengths + distances[i][j];
					Road tempRoad;
					if(j < i)
					{
						tempRoad = new Road(intersectionList[i], intersectionList[j], anglesBetweenNodes[i][j], distances[i][j], speedOfRoads[i][j], 'R');
					}
					else
					{
						tempRoad = new Road(intersectionList[i], intersectionList[j], anglesBetweenNodes[i][j], distances[i][j], speedOfRoads[i][j], 'B');
					}
					
					roadsInSystem[currentInsertIndexOverall] = tempRoad;
					currentInsertIndexOverall++;
					listOfConnectedNodes.add(tempRoad);
					intersectionList[j].addInConnections(tempRoad);
				}
			}
			intersectionList[i].addOutConnections(listOfConnectedNodes.toArray(new Road[0]));
		}
		for(IntersectionAbstract anIntersectionList : intersectionList)
		{
			anIntersectionList.sortOutOutgoingConnections( );
		}
	}
	
	private static void createRoads()
	{
		if(Config.RANDOMROADS || Config.RANDOMINTERSECTIONS)
		{
			createRoadsRandom( );
		}
		else
		{
			createRoadsFromConfig( );
		}
	}
	
	private static void createNodes()
	{
		if(Config.RANDOMINTERSECTIONS)
		{
			createNodesRandom( );
			setLocationsOnGraphStream( );
		}
		else
		{
			createNodesFromConfig( );
		}
		updateNodeLocations( );
	}
	
	private static void workOutAngles()
	{
		anglesBetweenNodes = new double[connectionInfo.length][connectionInfo.length];
		distances = new double[connectionInfo.length][connectionInfo.length];
		for(int i = 0; i < connectionInfo.length; i++)
		{
			for(int j = i; j < connectionInfo.length; j++)
			{
				if(connectionInfo[i][j] == 1)
				{
					double xVal1 = nodeInfoStored[i][0];
					double xVal2 = nodeInfoStored[j][0];
					double yVal1 = nodeInfoStored[i][1];
					double yVal2 = nodeInfoStored[j][1];
					double differenceX = xVal1 - xVal2;
					double differenceY = yVal1 - yVal2;
					double angle;
					angle = Math.atan2(differenceY, differenceX);
					anglesBetweenNodes[i][j] = angle;
					if(angle < 0)
					{
						anglesBetweenNodes[j][i] = angle + (Math.PI);
					}
					else if(angle > 0)
					{
						anglesBetweenNodes[j][i] = angle - (Math.PI);
					}
					if(angle == 0)
					{
						if(xVal1 > xVal2)
						{
							anglesBetweenNodes[i][j] = 0;
							anglesBetweenNodes[j][i] = Math.PI;
						}
						else
						{
							anglesBetweenNodes[i][j] = Math.PI;
							anglesBetweenNodes[j][i] = 0;
						}
					}
					distances[i][j] = Math.sqrt(Math.pow(xVal1 - xVal2, 2) + Math.pow(yVal1 - yVal2, 2));
					distances[j][i] = distances[i][j];
					if(! (Config.RANDOMROADS || Config.RANDOMINTERSECTIONS))
					{
						nodeInfoStored[i][4]++;
						nodeInfoStored[j][4]++;
					}
				}
			}
		}
	}
	
	private static void setLocationsOnGraphStream()
	{
		for(int i = 0; i < nodeInfoStored.length; i++)
		{
			Node theNode = graph.addNode(i + "");
			theNode.setAttribute("xyz", nodeInfoStored[i][0], nodeInfoStored[i][1], 0);
			theNode.setAttribute("layout.weight", Integer.MAX_VALUE);
			theNode.setAttribute("ui.label", i);
		}
		try
		{
			Thread.sleep(1000);
		}
		catch(Exception e)
		{
			e.printStackTrace( );
		}
		viewer.disableAutoLayout( );
		//System.out.println("here");
		double smallestDoubleX = Double.MAX_VALUE - 1;
		double largestDoubleX = - Double.MAX_VALUE;
		double smallestDoubleY = Double.MAX_VALUE - 1;
		double largestDoubleY = - Double.MAX_VALUE;
		for(int i = 0; i < nodeInfoStored.length; i++)
		{
			GraphicNode theNodeGraphic = viewer.getGraphicGraph( ).getNode(i + "");
			if(smallestDoubleX > theNodeGraphic.getX( ))
			{
				smallestDoubleX = theNodeGraphic.getX( );
			}
			if(largestDoubleX < theNodeGraphic.getX( ))
			{
				largestDoubleX = theNodeGraphic.getX( );
			}
			if(smallestDoubleY > theNodeGraphic.getY( ))
			{
				smallestDoubleY = theNodeGraphic.getY( );
			}
			if(largestDoubleY < theNodeGraphic.getY( ))
			{
				largestDoubleY = theNodeGraphic.getY( );
			}
		}
		double largestY = largestDoubleY + Math.abs(smallestDoubleY);
		double largestX = largestDoubleX + Math.abs(smallestDoubleX);
		double largest;
		if(largestY < largestX)
		{
			largest = largestX;
		}
		else
		{
			largest = largestY;
		}
		double scale = Config.MAPSIZEY / (largest);
		for(int i = 0; i < nodeInfoStored.length; i++)
		{
			GraphicNode theNodeGraphic = viewer.getGraphicGraph( ).getNode(i + "");
			theNodeGraphic.setAttribute("xyz", (theNodeGraphic.getX( ) - smallestDoubleX) * scale, (theNodeGraphic.getY( ) - smallestDoubleY) * scale, 0);
			nodeInfoStored[i][0] = theNodeGraphic.getX( );
			nodeInfoStored[i][1] = theNodeGraphic.getY( );
		}
	}
	
	private static void createGraph()
	{
		graph = new SingleGraph("Graph" + currentRunthrough);
		graph.clear( );
		viewer = graph.display(false);
		sman = new SpriteManager(graph);
		BarnesHutLayout layout = new SpringBox( );
		layout.setForce(0.005);
		layout.setStabilizationLimit(0.5);
		layout.setQuality(1);
		viewer.enableAutoLayout(layout);
	}
	
	private static void updateNodeLocations()
	{
		StringBuilder output = new StringBuilder( );
		output.append(nodeInfoStored[0][0]).append(" ").append(nodeInfoStored[0][1]).append(" ").append(nodeInfoStored[0][2] * Config.SIMACCELERATION);
		for(int i = 1; i < nodeInfoStored.length; i++)
		{
			output.append("\n").append(nodeInfoStored[i][0]).append(" ").append(nodeInfoStored[i][1]).append(" ").append(nodeInfoStored[i][2] * Config.SIMACCELERATION);
		}
		nodeFile = new AppendToFile(nodesConfig + prefixConfig + "/test" + currentRunthrough + "/nodes.txt");
		nodeFile.cleanFile( );
		nodeFile.openWriterAppend( );
		nodeFile.appendToFile(output);
		nodeFile.closeWriterAppend( );
	}
	
	private static void createNodesFromConfig()
	{
		viewer.disableAutoLayout( );
		ReadFile file = new ReadFile(nodesConfig + prefixConfig + "/test" + currentRunthrough + "/nodes.txt");
		String[] aryLines = file.OpenFile( );
		nodeInfoStored = new double[aryLines.length][5];
		for(int i = 0; i < aryLines.length; i++)
		{
			String[] nodeInfo = aryLines[i].split(" ");
			double xCord = Double.parseDouble(nodeInfo[0]);
			double yCord = Double.parseDouble(nodeInfo[1]);
			double Interval = Double.parseDouble(nodeInfo[2]);
			for(int j = 0; j < i; j++)
			{
				if(xCord == nodeInfoStored[j][0] && yCord == nodeInfoStored[j][1])
				{
					System.out.println("Node " + i + "has the same x and y co'ordinates as another intersection please restart the program and correct the error");
					if(debug)
					{
						System.out.println("Error was found in the createNodesFromConfig method in the FinalYear class");
					}
					System.exit(1);
				}
			}
			nodeInfoStored[i][0] = xCord;
			nodeInfoStored[i][1] = yCord;
			nodeInfoStored[i][2] = Interval / Config.SIMACCELERATION;
			nodeInfoStored[i][3] = i;
			Node theNode = graph.addNode(i + "");
			theNode.setAttribute("xyz", nodeInfoStored[i][0], nodeInfoStored[i][1], 0);
			theNode.setAttribute("layout.weight", Integer.MAX_VALUE);
			theNode.setAttribute("ui.label", i);
		}
	}
	
	private static void createNodesRandom()
	{
		Random rand = new Random( );
		int failAmount = 0;
		double minDistance = 100;
		nodeInfoStored = new double[Config.AMOUNTOFINTERSECTIONS][5];
		int nodesAdded = 0;
		while(nodesAdded < Config.AMOUNTOFINTERSECTIONS)
		{
			double xCord = (rand.nextInt(Config.MAPSIZEX));
			double yCord = (rand.nextInt(Config.MAPSIZEY));
			double nodeInterval = (rand.nextInt(Config.MAXINTERSECTIONINTERVAL - Config.MININTERSECTIONINTERVAL + 1) + Config.MININTERSECTIONINTERVAL) / Config.SIMACCELERATION;
			boolean found = false;
			for(int i = 0; i < nodesAdded; i++)
			{
				double distance = Math.sqrt(Math.pow(xCord - nodeInfoStored[i][0], 2) + Math.pow(yCord - nodeInfoStored[i][1], 2));
				if(distance < minDistance)
				{
					found = true;
					break;
				}
			}
			if(! found)
			{
				failAmount = 0;
				nodeInfoStored[nodesAdded][0] = xCord;
				nodeInfoStored[nodesAdded][1] = yCord;
				nodeInfoStored[nodesAdded][2] = nodeInterval;
				nodeInfoStored[nodesAdded][3] = nodesAdded;
				nodesAdded++;
			}
			else
			{
				failAmount++;
				if(failAmount == 20 && minDistance > 1)
				{
					failAmount = 0;
					minDistance = minDistance - 1;
				}
			}
		}
	}
	
	private static void createRoadsFromConfig()
	{
		connectionInfo = new int[nodeInfoStored.length][nodeInfoStored.length];
		speedOfRoads = new double[nodeInfoStored.length][nodeInfoStored.length];
		ReadFile file = new ReadFile(roadsConfig + prefixConfig + "/test" + currentRunthrough + "/roads.txt");
		String[] aryLines = file.OpenFile( );
		for(String aryLine : aryLines)
		{
			String[] coOrdinates = aryLine.split(" ");
			int node1Index = Integer.parseInt(coOrdinates[0]);
			int node2Index = Integer.parseInt(coOrdinates[1]);
			double speedOfRoad = Double.parseDouble(coOrdinates[2]);
			if(node1Index < connectionInfo.length && node2Index < connectionInfo.length)
			{
				connectionInfo[node1Index][node2Index] = 1;
				connectionInfo[node2Index][node1Index] = 1;
				speedOfRoads[node1Index][node2Index] = speedOfRoad;
				speedOfRoads[node2Index][node1Index] = speedOfRoad;
				graph.addEdge(nodeInfoStored[node1Index][3] + " " + nodeInfoStored[node2Index][3], (int) nodeInfoStored[node1Index][3] + "", (int) nodeInfoStored[node2Index][3] + "", true);
				graph.addEdge(nodeInfoStored[node2Index][3] + " " + nodeInfoStored[node1Index][3], (int) nodeInfoStored[node2Index][3] + "", (int) nodeInfoStored[node1Index][3] + "", true);
			}
		}
	}
	
	private static void createRoadsRandom()
	{
		speedOfRoads = new double[nodeInfoStored.length][nodeInfoStored.length];
		viewer.disableAutoLayout( );
		Random rand = new Random( );
		StringBuilder output = new StringBuilder( );
		LinkedList<double[]> nodesWithConnections = new LinkedList<double[]>( );
		LinkedList<double[]> nodesWithoutConnections = new LinkedList<double[]>(Arrays.asList(nodeInfoStored));
		connectionInfo = new int[nodeInfoStored.length][nodeInfoStored.length];
		LinkedList<Line2D> roadsInSystem = new LinkedList<Line2D>( );
		int node1 = rand.nextInt(nodesWithoutConnections.size( ));
		double[] nodeWithoutConnection = nodesWithoutConnections.remove(node1);
		int node2 = rand.nextInt(nodesWithoutConnections.size( ));
		double[] nodeWithoutConnection2 = nodesWithoutConnections.remove(node2);
		int[] nextNode = blockedByFutureNode(200, nodeWithoutConnection, nodeWithoutConnection2, nodesWithoutConnections, nodesWithConnections);
		int failedAmount = 0;
		int spacingOfLines = 200;
		while(nextNode[1] != - 1)
		{
			failedAmount++;
			if(failedAmount > 20 && spacingOfLines > 1)
			{
				failedAmount = 0;
				spacingOfLines--;
			}
			nodesWithoutConnections.add(nodeWithoutConnection2);
			nodeWithoutConnection2 = nodesWithoutConnections.remove(nextNode[1]);
			nextNode = blockedByFutureNode(spacingOfLines, nodeWithoutConnection, nodeWithoutConnection2, nodesWithoutConnections, nodesWithConnections);
		}
		connectionInfo[(int) nodeWithoutConnection[3]][(int) nodeWithoutConnection2[3]] = 1;
		connectionInfo[(int) nodeWithoutConnection2[3]][(int) nodeWithoutConnection[3]] = 1;
		if(Config.HIGHESTSPEEDLIMIT == Config.LOWESTSPEEDLIMIT)
		{
			speedOfRoads[(int) nodeWithoutConnection2[3]][(int) nodeWithoutConnection[3]] = Config.HIGHESTSPEEDLIMIT;
		}
		else
		{
			speedOfRoads[(int) nodeWithoutConnection2[3]][(int) nodeWithoutConnection[3]] = rand.nextInt(Config.HIGHESTSPEEDLIMIT - Config.LOWESTSPEEDLIMIT) + Config.LOWESTSPEEDLIMIT;
		}
		speedOfRoads[(int) nodeWithoutConnection[3]][(int) nodeWithoutConnection2[3]] = speedOfRoads[(int) nodeWithoutConnection2[3]][(int) nodeWithoutConnection[3]];
		output.append((int) nodeWithoutConnection[3]).append(" ").append((int) nodeWithoutConnection2[3]).append(" ").append(speedOfRoads[(int) nodeWithoutConnection2[3]][(int) nodeWithoutConnection[3]]);
		graph.addEdge(nodeWithoutConnection[3] + " " + nodeWithoutConnection2[3], (int) nodeWithoutConnection[3] + "", (int) nodeWithoutConnection2[3] + "", true);
		graph.addEdge(nodeWithoutConnection2[3] + " " + nodeWithoutConnection[3], (int) nodeWithoutConnection2[3] + "", (int) nodeWithoutConnection[3] + "", true);
		roadsInSystem.add(new Line2D.Double(nodeWithoutConnection[0], nodeWithoutConnection[1], nodeWithoutConnection2[0], nodeWithoutConnection2[1]));
		nodesWithConnections.add(nodeWithoutConnection2);
		nodesWithConnections.add(nodeWithoutConnection);
		failedAmount = 0;
		spacingOfLines = 200;
		while(nodesWithoutConnections.size( ) > 0)
		{
			node1 = rand.nextInt(nodesWithConnections.size( ));
			double[] nodeWithConnection = nodesWithConnections.remove(node1);
			node2 = rand.nextInt(nodesWithoutConnections.size( ));
			nodeWithoutConnection = nodesWithoutConnections.remove(node2);
			if(notBlockedByRoad(nodeWithConnection, nodeWithoutConnection, roadsInSystem))
			{
				nextNode = blockedByFutureNode(200, nodeWithConnection, nodeWithoutConnection, nodesWithoutConnections, nodesWithConnections);
				while(nextNode[1] != - 1)
				{
					failedAmount++;
					if(failedAmount > 20 && spacingOfLines > 1)
					{
						failedAmount = 0;
						spacingOfLines--;
					}
					if(nextNode[0] == 0)
					{
						nodesWithoutConnections.add(nodeWithoutConnection);
						nodeWithoutConnection = nodesWithoutConnections.remove(nextNode[1]);
						nextNode = blockedByFutureNode(spacingOfLines, nodeWithConnection, nodeWithoutConnection, nodesWithoutConnections, nodesWithConnections);
					}
					else
					{
						nodesWithConnections.add(nodeWithConnection);
						nodeWithConnection = nodesWithConnections.remove(nextNode[1]);
						nextNode = blockedByFutureNode(spacingOfLines, nodeWithConnection, nodeWithoutConnection, nodesWithoutConnections, nodesWithConnections);
					}
				}
				failedAmount = 0;
				nodesWithConnections.add(nodeWithoutConnection);
				nodesWithConnections.add(nodeWithConnection);
				roadsInSystem.add(new Line2D.Double(nodeWithConnection[0], nodeWithConnection[1], nodeWithoutConnection[0], nodeWithoutConnection[1]));
				connectionInfo[(int) nodeWithConnection[3]][(int) nodeWithoutConnection[3]] = 1;
				connectionInfo[(int) nodeWithoutConnection[3]][(int) nodeWithConnection[3]] = 1;
				Edge newEdge = graph.addEdge(nodeWithConnection[3] + " " + nodeWithoutConnection[3], (int) nodeWithConnection[3] + "", (int) nodeWithoutConnection[3] + "", true);
				if(Config.HIGHESTSPEEDLIMIT == Config.LOWESTSPEEDLIMIT)
				{
					speedOfRoads[(int) nodeWithConnection[3]][(int) nodeWithoutConnection[3]] = Config.HIGHESTSPEEDLIMIT;
				}
				else
				{
					speedOfRoads[(int) nodeWithConnection[3]][(int) nodeWithoutConnection[3]] = rand.nextInt(Config.HIGHESTSPEEDLIMIT - Config.LOWESTSPEEDLIMIT) + Config.LOWESTSPEEDLIMIT;
				}
				speedOfRoads[(int) nodeWithoutConnection[3]][(int) nodeWithConnection[3]] = speedOfRoads[(int) nodeWithConnection[3]][(int) nodeWithoutConnection[3]];
				output.append("\n").append((int) nodeWithConnection[3]).append(" ").append((int) nodeWithoutConnection[3]).append(" ").append(speedOfRoads[(int) nodeWithConnection[3]][(int) nodeWithoutConnection[3]]);
				newEdge.setAttribute("layout.weight", 2);
			}
			else
			{
				nodesWithConnections.add(nodeWithConnection);
				nodesWithoutConnections.add(nodeWithoutConnection);
			}
		}
		if(Config.MAPTWEAK)
		{
			double smallestDoubleX = Double.MAX_VALUE - 1;
			double largestDoubleX = - Double.MAX_VALUE;
			double smallestDoubleY = Double.MAX_VALUE - 1;
			double largestDoubleY = - Double.MAX_VALUE;
			for(int i = 0; i < nodeInfoStored.length; i++)
			{
				GraphicNode theNodeGraphic = viewer.getGraphicGraph( ).getNode(i + "");
				if(smallestDoubleX > theNodeGraphic.getX( ))
				{
					smallestDoubleX = theNodeGraphic.getX( );
				}
				if(largestDoubleX < theNodeGraphic.getX( ))
				{
					largestDoubleX = theNodeGraphic.getX( );
				}
				if(smallestDoubleY > theNodeGraphic.getY( ))
				{
					smallestDoubleY = theNodeGraphic.getY( );
				}
				if(largestDoubleY < theNodeGraphic.getY( ))
				{
					largestDoubleY = theNodeGraphic.getY( );
				}
			}
			double scaleX = Config.MAPSIZEX / (largestDoubleX - smallestDoubleX);
			double scaleY = Config.MAPSIZEY / (largestDoubleY - smallestDoubleY);
			for(int i = 0; i < nodeInfoStored.length; i++)
			{
				GraphicNode theNodeGraphic = viewer.getGraphicGraph( ).getNode(i + "");
				theNodeGraphic.setAttribute("xyz", (theNodeGraphic.getX( ) - smallestDoubleX) * scaleX, (theNodeGraphic.getY( ) - smallestDoubleY) * scaleY, 0);
				nodeInfoStored[i][0] = theNodeGraphic.getX( );
				nodeInfoStored[i][1] = theNodeGraphic.getY( );
			}
		}
		roadsInSystem.clear( );
		int maxConnections = 0;
		for(int i = 0; i < connectionInfo.length; i++)
		{
			int amountOfConnectionsForNode = 0;
			for(int j = 0; j < connectionInfo.length; j++)
			{
				if(connectionInfo[i][j] == 1)
				{
					amountOfConnectionsForNode++;
				}
				if(connectionInfo[i][j] == 1 && j > i)
				{
					roadsInSystem.add(new Line2D.Double(nodeInfoStored[i][0], nodeInfoStored[i][1], nodeInfoStored[j][0], nodeInfoStored[j][1]));
				}
			}
			nodeInfoStored[i][4] = amountOfConnectionsForNode;
			if(amountOfConnectionsForNode > maxConnections)
			{
				maxConnections = amountOfConnectionsForNode;
			}
		}
		LinkedList<LinkedList<double[]>> listOfNodesInOrder = new LinkedList<LinkedList<double[]>>( );
		for(int i = 0; i < maxConnections; i++)
		{
			listOfNodesInOrder.add(new LinkedList<double[]>( ));
		}
		for(double[] currentNode : nodeInfoStored)
		{
			listOfNodesInOrder.get((int) currentNode[4] - 1).add(currentNode);
		}
		int index1 = 0;
		int index2 = 0;
		int amountToCheckInitial = 3;
		while(index1 < listOfNodesInOrder.size( ) && index2 < listOfNodesInOrder.size( ) && index1 < amountToCheckInitial && roadsInSystem.size( ) < Config.AMOUNTOFROADS)
		{
			LinkedList<double[]> currentLinkedList = listOfNodesInOrder.get(index1);
			LinkedList<double[]> currentLinkedList2 = listOfNodesInOrder.get(index2);
			int index3 = 0;
			int index4;
			while(index3 < currentLinkedList.size( ) && roadsInSystem.size( ) < Config.AMOUNTOFROADS)
			{
				double[] currentNode = currentLinkedList.get(index3);
				boolean added = false;
				for(index4 = 0; index4 < currentLinkedList2.size( ); index4++)
				{
					double[] currentNode2 = currentLinkedList2.get(index4);
					if(! currentNode2.equals(currentNode))
					{
						if(notBlockedByRoad(currentNode, currentNode2, roadsInSystem))
						{
							if(connectionInfo[(int) currentNode[3]][(int) currentNode2[3]] == 0 && blockedByFutureNode(Config.SPREADDISTANCE, currentNode, currentNode2, nodesWithoutConnections, nodesWithConnections)[1] == - 1)
							{
								if(connectionInfo[(int) currentNode[3]][(int) currentNode2[3]] != 1)
								{
									roadsInSystem.add(new Line2D.Double(currentNode[0], currentNode[1], currentNode2[0], currentNode2[1]));
									currentNode[4]++;
									currentNode2[4]++;
									connectionInfo[(int) currentNode[3]][(int) currentNode2[3]] = 1;
									connectionInfo[(int) currentNode2[3]][(int) currentNode[3]] = 1;
									graph.addEdge(currentNode[3] + " " + currentNode2[3], (int) currentNode[3] + "", (int) currentNode2[3] + "", true);
									graph.addEdge(currentNode2[3] + " " + currentNode[3], (int) currentNode2[3] + "", (int) currentNode[3] + "", true);
									if(Config.HIGHESTSPEEDLIMIT == Config.LOWESTSPEEDLIMIT)
									{
										speedOfRoads[(int) currentNode[3]][(int) currentNode2[3]] = Config.HIGHESTSPEEDLIMIT;
									}
									else
									{
										speedOfRoads[(int) currentNode[3]][(int) currentNode2[3]] = rand.nextInt(Config.HIGHESTSPEEDLIMIT - Config.LOWESTSPEEDLIMIT) + Config.LOWESTSPEEDLIMIT;
									}
									speedOfRoads[(int) currentNode2[3]][(int) currentNode[3]] = speedOfRoads[(int) currentNode[3]][(int) currentNode2[3]];
									output.append("\n").append((int) currentNode[3]).append(" ").append((int) currentNode2[3]).append(" ").append(speedOfRoads[(int) currentNode[3]][(int) currentNode2[3]]);
									listOfNodesInOrder.get(index1).remove(currentNode);
									listOfNodesInOrder.get(index2).remove(currentNode2);
									if(currentNode[4] > listOfNodesInOrder.size( ) - 1 || currentNode2[4] > listOfNodesInOrder.size( ) - 1)
									{
										listOfNodesInOrder.addLast(new LinkedList<double[]>( ));
									}
									listOfNodesInOrder.get(index1 + 1).add(currentNode);
									listOfNodesInOrder.get(index2 + 1).add(currentNode2);
									added = true;
									break;
								}
							}
						}
					}
				}
				if(! added)
				{
					index3++;
				}
			}
			if(index2 < amountToCheckInitial - 1)
			{
				index2++;
			}
			else
			{
				index1++;
				index2 = index1;
			}
		}
		index1 = 0;
		index2 = 3;
		while(index2 < listOfNodesInOrder.size( ) && roadsInSystem.size( ) < Config.AMOUNTOFROADS)
		{
			while(index1 <= index2 && roadsInSystem.size( ) < Config.AMOUNTOFROADS)
			{
				LinkedList<double[]> currentLinkedList = listOfNodesInOrder.get(index1);
				LinkedList<double[]> currentLinkedList2 = listOfNodesInOrder.get(index2);
				int index3 = 0;
				int index4;
				while(index3 < currentLinkedList.size( ) && roadsInSystem.size( ) < Config.AMOUNTOFROADS)
				{
					double[] currentNode = currentLinkedList.get(index3);
					boolean added = false;
					for(index4 = 0; index4 < currentLinkedList2.size( ); index4++)
					{
						double[] currentNode2 = currentLinkedList2.get(index4);
						if(! currentNode2.equals(currentNode))
						{
							if(notBlockedByRoad(currentNode, currentNode2, roadsInSystem))
							{
								if(connectionInfo[(int) currentNode[3]][(int) currentNode2[3]] == 0 && blockedByFutureNode(Config.SPREADDISTANCE, currentNode, currentNode2, nodesWithoutConnections, nodesWithConnections)[1] == - 1)
								{
									if(connectionInfo[(int) currentNode[3]][(int) currentNode2[3]] != 1)
									{
										roadsInSystem.add(new Line2D.Double(currentNode[0], currentNode[1], currentNode2[0], currentNode2[1]));
										currentNode[4]++;
										currentNode2[4]++;
										connectionInfo[(int) currentNode[3]][(int) currentNode2[3]] = 1;
										connectionInfo[(int) currentNode2[3]][(int) currentNode[3]] = 1;
										graph.addEdge(currentNode[3] + " " + currentNode2[3], (int) currentNode[3] + "", (int) currentNode2[3] + "", true);
										graph.addEdge(currentNode2[3] + " " + currentNode[3], (int) currentNode2[3] + "", (int) currentNode[3] + "", true);
										if(Config.HIGHESTSPEEDLIMIT == Config.LOWESTSPEEDLIMIT)
										{
											speedOfRoads[(int) currentNode[3]][(int) currentNode2[3]] = Config.HIGHESTSPEEDLIMIT;
										}
										else
										{
											speedOfRoads[(int) currentNode[3]][(int) currentNode2[3]] = rand.nextInt(Config.HIGHESTSPEEDLIMIT - Config.LOWESTSPEEDLIMIT) + Config.LOWESTSPEEDLIMIT;
										}
										speedOfRoads[(int) currentNode2[3]][(int) currentNode[3]] = speedOfRoads[(int) currentNode[3]][(int) currentNode2[3]];
										output.append("\n").append((int) currentNode[3]).append(" ").append((int) currentNode2[3]).append(" ").append(speedOfRoads[(int) currentNode[3]][(int) currentNode2[3]]);
										listOfNodesInOrder.get(index1).remove(currentNode);
										listOfNodesInOrder.get(index2).remove(currentNode2);
										if(currentNode[4] > listOfNodesInOrder.size( ) - 1 || currentNode2[4] > listOfNodesInOrder.size( ) - 1)
										{
											listOfNodesInOrder.addLast(new LinkedList<double[]>( ));
										}
										listOfNodesInOrder.get(index1 + 1).add(currentNode);
										listOfNodesInOrder.get(index2 + 1).add(currentNode2);
										added = true;
										break;
									}
								}
							}
						}
					}
					if(! added)
					{
						index3++;
					}
				}
				index1++;
			}
			if(index2 < listOfNodesInOrder.size( ))
			{
				index2++;
				index1 = 0;
			}
		}
		roadFile = new AppendToFile(roadsConfig + prefixConfig + "/test" + currentRunthrough + "/roads.txt");
		roadFile.cleanFile( );
		roadFile.openWriterAppend( );
		roadFile.appendToFile(output);
		roadFile.closeWriterAppend( );
	}
	
	private static boolean notBlockedByRoad(double[] nodeWithConnection, double[] nodeWithoutConnection, LinkedList<Line2D> linesInSystem)
	{
		Line2D possibleLine = new Line2D.Double(nodeWithConnection[0], nodeWithConnection[1], nodeWithoutConnection[0], nodeWithoutConnection[1]);
		for(Line2D aLinesInSystem : linesInSystem)
		{
			if(! ((aLinesInSystem.getX1( ) == possibleLine.getX1( ) && aLinesInSystem.getY1( ) == possibleLine.getY1( )) || (aLinesInSystem.getX2( ) == possibleLine.getX2( ) && aLinesInSystem.getY2( ) == possibleLine.getY2( )) || (aLinesInSystem.getX1( ) == possibleLine.getX2( ) && aLinesInSystem.getY1( ) == possibleLine.getY2( )) || (aLinesInSystem.getX2( ) == possibleLine.getX1( ) && aLinesInSystem.getY2( ) == possibleLine.getY1( ))))
			{
				if(aLinesInSystem.intersectsLine(possibleLine))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private static int[] blockedByFutureNode(int distance, double[] nodeWithoutConnection, double[] nodeWithoutConnection2, LinkedList<double[]> nodesWithoutConnections, LinkedList<double[]> nodesWithConnections)
	{
		Line2D possibleLine = new Line2D.Double(nodeWithoutConnection[0], nodeWithoutConnection[1], nodeWithoutConnection2[0], nodeWithoutConnection2[1]);
		for(int i = 0; i < nodesWithoutConnections.size( ); i++)
		{
			double[] currentCheck = nodesWithoutConnections.get(i);
			if(! ((currentCheck[0] == possibleLine.getX1( ) && currentCheck[1] == possibleLine.getY1( )) || (currentCheck[0] == possibleLine.getX2( ) && currentCheck[1] == possibleLine.getY2( )) || (currentCheck[0] == possibleLine.getX2( ) && currentCheck[1] == possibleLine.getY2( )) || (currentCheck[0] == possibleLine.getX1( ) && currentCheck[1] == possibleLine.getY1( ))) && possibleLine.ptSegDist(currentCheck[0], currentCheck[1]) < distance)
			{
				return new int[]{0, i};
			}
		}
		for(int i = 0; i < nodesWithConnections.size( ); i++)
		{
			double[] currentCheck = nodesWithConnections.get(i);
			if(! ((currentCheck[0] == possibleLine.getX1( ) && currentCheck[1] == possibleLine.getY1( )) || (currentCheck[0] == possibleLine.getX2( ) && currentCheck[1] == possibleLine.getY2( )) || (currentCheck[0] == possibleLine.getX2( ) && currentCheck[1] == possibleLine.getY2( )) || (currentCheck[0] == possibleLine.getX1( ) && currentCheck[1] == possibleLine.getY1( ))) && possibleLine.ptSegDist(currentCheck[0], currentCheck[1]) < distance)
			{
				return new int[]{1, i};
			}
		}
		return new int[]{0, - 1};
	}
	
	private static int getTimestep()
	{
		return timestep;
	}
	
	private static void setTimestep(int timestep)
	{
		FinalYear.timestep = timestep;
	}
}