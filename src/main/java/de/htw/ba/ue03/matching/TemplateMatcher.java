package de.htw.ba.ue03.matching;

import java.awt.Point;
import java.util.List;

public interface TemplateMatcher {

	public int getTemplateWidth();
	public int getTemplateHeight();
	
	/**
	 * Gibt die matching Distanz zwischen dem Template und dem Bild an jeder Position zurück.
	 * Die zurück gelieferte Map ist [srcWidth-templateWidth][srcHeight-templateHeight] groß.
	 * 
	 * @param srcPixels
	 * @param srcWidth
	 * @param srcHeight
	 * @return
	 */
	public double[][] getDistanceMap(int srcPixels[], int srcWidth, int srcHeight);
	
	/**
	 * Wandelt die DistanceMap zu einem Graustufenbild um
	 * 
	 * @param distanceMap
	 * @param dstPixels
	 * @return
	 */
	public void distanceMapToIntARGB(double[][] distanceMap, int dstPixels[], int dstWidth, int dstHeight);
	
	/**
	 * Gibt an Liste an Lokalen Maximas zurück.
	 * 
	 * @param distanceMap
	 * @return
	 */
	public List<Point> findMaximas(double[][] distanceMap);
}
