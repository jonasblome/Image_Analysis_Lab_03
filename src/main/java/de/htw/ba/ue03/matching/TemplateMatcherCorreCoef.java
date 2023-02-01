package de.htw.ba.ue03.matching;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Erstellt nur eine Kopie von dem Template Bild
 * 
 * @author Nico
 *
 */
public class TemplateMatcherCorreCoef extends TemplateMatcherBase {

	public TemplateMatcherCorreCoef(int[] templatePixel, int templateWidth, int templateHeight) {
		super(templatePixel, templateWidth, templateHeight);
	}
	
	@Override
	public double[][] getDistanceMap(int[] srcPixels, int srcWidth, int srcHeight) {
		double[][] distanceMap = new double[srcWidth - templateWidth][srcHeight - templateHeight];
		int numTemplatePixels = templateWidth * templateHeight;
		double averageTemplate = Arrays.stream(templatePixel).average().getAsDouble();
		int sumImageTemplateProduct = 0;
		int sumImageRegionSquared = 0;
		int sumImageRegion = 0;
		int sumTemplateSquared = Arrays.stream(templatePixel).map(x -> x * x).sum();

		// Loop over all pixels in source image
		for (int ySrc = 0; ySrc < srcHeight - templateHeight; ySrc++) {
			for (int xSrc = 0; xSrc < srcWidth - templateWidth; xSrc++) {
				double currentCorrCoef = 0;

				// Loop over all pixels in template image
				for (int yTmp = 0; yTmp < templateHeight; yTmp++) {
					for (int xTmp = 0; xTmp < templateWidth; xTmp++) {
						int posInTmp = yTmp * templateWidth + xTmp;
						int posInSrc = (ySrc + yTmp) * srcWidth + xSrc + xTmp;

						sumImageTemplateProduct += srcPixels[posInSrc] * templatePixel[posInTmp];
						sumImageRegionSquared += Math.pow(srcPixels[posInSrc], 2);
						sumImageRegion += srcPixels[posInSrc];
					}
				}

				int averageImageRegion = sumImageRegion / numTemplatePixels;
				currentCorrCoef = (sumImageTemplateProduct - (numTemplatePixels * averageImageRegion * averageTemplate)) / (Math.pow(sumImageRegionSquared - numTemplatePixels * Math.pow(averageImageRegion, 2), 0.5) * Math.pow(sumTemplateSquared - numTemplatePixels * Math.pow(averageTemplate, 2), 0.5));

				// Store distance to template for current window
				distanceMap[xSrc][ySrc] = currentCorrCoef;
			}
		}

		return distanceMap;
	}

	@Override
	public void distanceMapToIntARGB(double[][] distanceMap, int[] dstPixels, int dstWidth, int dstHeight) {
		// Normalizing distance map array
		double distanceMapMax = Arrays.stream(distanceMap).flatMapToDouble(Arrays::stream).max().getAsDouble();
		double distanceMapMin = Arrays.stream(distanceMap).flatMapToDouble(Arrays::stream).min().getAsDouble();

		// Loop over all values in distance map and write as ARGB into destination
		for (int y = 0; y < dstHeight; y++) {
			for (int x = 0; x < dstWidth; x++) {
				int norm = (int) ((distanceMap[x][y] - distanceMapMin) / (distanceMapMax - distanceMapMin) * 255);
				int pos = y * dstWidth + x;
				int lum = norm;
				dstPixels[pos] = 0xFF000000 | lum << 16 | lum << 8 | lum;
			}
		}
	}

	@Override
	public List<Point> findMaximas(double[][] distanceMap) {
		int regionSize = 50;
		boolean biggerValFound = false;
		ArrayList<Point> maxima = new ArrayList<>();

		// Loop over all distances to find maxima
		for (int y = 0; y < distanceMap[0].length; y++) {
			for (int x = 0; x < distanceMap.length; x++) {
				double currentDistance = distanceMap[x][y];

				// Loop over neighbour region and check if bigger neighbour exists
				for (int yR = -regionSize; yR < regionSize && !biggerValFound; yR++) {
					for (int xR = -regionSize; xR < regionSize; xR++) {
						// Treating edge cases
						if(x + xR < 0 || x + xR > distanceMap.length - 1 || y + yR < 0 || y + yR > distanceMap[0].length - 1) {
							// Continue if current neighbour is not in image region
							continue;
						}
						// Stop if bigger neighbor has been found
						if (distanceMap[x + xR][y + yR] > currentDistance) {
							biggerValFound = true;
							break;
						}
					}
				}

				// Add current distance to maxima if no bigger neighbour was found
				if(!biggerValFound) {
					maxima.add(new Point(x, y));
				}

				biggerValFound = false;
			}
		}

		return maxima;
	}	
}
