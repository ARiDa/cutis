package fr.david.mdm.tools;

public class Hilbert_SegmentPoint {

	public int ID;/* SegmentID */
	public double XCoordinate;
	public double YCoordinate;
	public int cellX;
	public int cellY;
	public int n;/* Grid n * n */
	public int HilbertIndex;	
	public int maxIndex;

	/* Tiger4: Min, Max values */
	private final double MIN_X = 0;
	private final double MAX_X = 127.12883;
	private final double MIN_Y = 0;
	private final double MAX_Y = 54.07559;
	/* Oldenburg: Min, Max values */
/*	private final int MIN_X = 413;
	private final int MAX_X = 22222;
	private final int MIN_Y = 4012;
	private final int MAX_Y = 30851;*/
	
	public Hilbert_SegmentPoint()
	{
		ID = -1;
		XCoordinate = YCoordinate = 0;
		n = 0;
		HilbertIndex = -1;
		maxIndex=Integer.MIN_VALUE;
		
	}
	public void set(int id, double x, double y, int gridSize)
	{
		ID = id;
		XCoordinate = x;
		YCoordinate = y;
		n = gridSize; //64
		
		/* Calculate Hilbert Index */
		cellX = (int) (n * (XCoordinate - MIN_X) / (MAX_X - MIN_X));
		cellY = (int) (n * (YCoordinate - MIN_Y) / (MAX_Y - MIN_Y));
		Point p = new Point(cellX, cellY);
		//Point p = new Point(XCoordinate, YCoordinate);
		HilbertConversion Hilbertconversion = new HilbertConversion();
		HilbertIndex = Hilbertconversion.xy2d(n, p);
	}
	
	public Hilbert_SegmentPoint(int id, int x, int y, int gridSize)
	{
		set(id, x, y, gridSize);
	}
	
	public Point set(int tid, double latitude, double longitude, long time, double eps) {
		//double l = 4*eps/Math.sqrt(2); //TODO em todos os experimentos venho utilizando esse
		double l = 4*eps/Math.sqrt(2);
		double centerY=0;
		double centerX = 0;

		double distLat = latitude - centerY;
		double distLon = longitude - centerX;

		if (latitude < centerY) {
			distLat = -distLat;
		}

		if (longitude < centerX) {
			distLon = -distLon;
		}

		int bm = (int)Math.ceil((distLat - (l/2))/(l));
		int bn = (int)Math.ceil((distLon - (l/2))/(l));
		
		if(bm>=bn && maxIndex<bm) maxIndex=bm;
		else if(maxIndex<bn) maxIndex=bn;

		return new Point(tid,bm, bn, time);
	}
	
}