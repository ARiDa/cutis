package fr.david.mdm.tools;

public class Point {

	public int x;
	public int y;
	public int tid; //trajectory id
	public long time; //timestamp
	public int cid; //for free movement: hilbertIndex, for road network:edge_id
	
	public Point(int tid, int x, int y, long time) {
		this.tid=tid;
		this.x=x;
		this.y=y;
		this.time=time;
	}
	
	public Point(int tid, int x, int y, long time, int cid) {
		this.tid=tid;
		this.x=x;
		this.y=y;
		this.time=time;
		this.cid=cid;
	}
	
	public Point(int x, int y) {
		this.x=x;
		this.y=y;
	}
	
	public Point() {
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}
	
	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}


	public void swapxy() {
		 int t = x;
	     x = y;
	     y = t;		
	}
	
	
}
