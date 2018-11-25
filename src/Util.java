package com.company.Other;

import java.awt.geom.Line2D;

public class Util
{
	public static boolean isOnLineCars(double xCord1, double yCord1, double lineX1, double lineY1, double lineX2, double lineY2)
	{
		double test = Line2D.ptSegDist(lineX1, lineY1, lineX2, lineY2, xCord1, yCord1);
		return test < 1 && test > - 1;
	}
	
	public static double getLengthOfLine(double X1, double Y1, double X2, double Y2) { return Math.sqrt(((X1 - X2) * (X1 - X2)) + ((Y1 - Y2) * (Y1 - Y2))); }
}
