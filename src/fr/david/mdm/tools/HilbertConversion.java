package fr.david.mdm.tools;


public class HilbertConversion {

	/* Hilbert Transformation 
	 * Reference: wikipedia
	 * Support 2 functions: 
	 * 	Hilber-index Convert Point (x, y) to Hilbert index and 
	 * 	Hilbert-invert convert Hilbert index into Point (x, y)
	 * */
	
	public int xy2d(int n, Point p)
	{
		int rx, ry, s, d = 0;
		for(s = n/2; s > 0; s /= 2)
		{
			rx = (p.x & s) > 0 ? 1 : 0;
			ry = (p.y & s) > 0 ? 1 : 0;
			d += s * s * ((3 * rx) ^ ry);
			rot(s, p, rx, ry);
		}
		return d;
	}
	
	public void d2xy(int n, int d, Point p)
	{
		int rx, ry, s, t = d;
		p.x = p.y = 0;
		for(s = 1; s < n; s *= 2)
		{
			rx = 1 & (t / 2);
			ry = 1 & (t ^ rx);
			rot(s, p, rx, ry);
			p.x += s * rx;
			p.y += s * ry;
			t /= 4;
		}
	}
	
	private void rot(int n, Point p, int rx, int ry)
	{
		if(ry == 0)
		{
			if(rx == 1)
			{
				p.x = n - 1 - p.x;
				p.y = n - 1 - p.y;
			}
			p.swapxy();
		}
	}
		
}
