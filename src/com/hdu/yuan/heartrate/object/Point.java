package com.hdu.yuan.heartrate.object;

public class Point implements Comparable{
	public int n;
	public int distance;
	
	public int compareTo(Object o)
	{
		return this.distance-((Point)o).distance;
	}
}
