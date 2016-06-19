package fr.david.mdm.traclus.clustering;

public class CMDLineSegment {

	private int m_nDimensions; 	// the number of dimensions of a point
	CMDPoint[] m_linesegment;	    // array of CMDPoint
	
	public CMDLineSegment(int nDimensions) {
		
		m_nDimensions = nDimensions;
		m_linesegment = new CMDPoint[m_nDimensions];
	}
	
	public CMDPoint getM_CMDPoint(int nth) {
		
		return m_linesegment[nth];
	}
	
	public int getM_nDimensions() {
		return m_nDimensions;
	}
	
	public void setM_CMDPoint(int nth, CMDPoint value) {
		this.m_linesegment[nth] = value;
	}
}
