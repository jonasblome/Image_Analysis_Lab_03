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
public class TemplateMatcherMaximum extends TemplateMatcherBase {

	public TemplateMatcherMaximum(int[] templatePixel, int templateWidth, int templateHeight) {
		super(templatePixel, templateWidth, templateHeight);
	}
	
	@Override
	public double[][] getDistanceMap(int[] srcPixels, int srcWidth, int srcHeight) {
		double[][] distanceMap = new double[srcWidth - templateWidth][srcHeight - templateHeight];

		// Loop over all pixels in source image
		for (int ySrc = 0; ySrc < srcHeight - templateHeight; ySrc++) {
			for (int xSrc = 0; xSrc < srcWidth - templateWidth; xSrc++) {
				double currentDistanceAbs = 0;

				// Loop over all pixels in template image
				// Add up total absolute difference of template and window
				for (int yTmp = 0; yTmp < templateHeight; yTmp++) {
					for (int xTmp = 0; xTmp < templateWidth; xTmp++) {
						int posInTmp = yTmp * templateWidth + xTmp;
						int posInSrc = (ySrc + yTmp) * srcWidth + xSrc + xTmp;

						currentDistanceAbs = Math.max(currentDistanceAbs, Math.abs((templatePixel[posInTmp] & 0xFF) - (srcPixels[posInSrc] & 0xFF)));
					}
				}

				// Store distance to template for current window
				distanceMap[xSrc][ySrc] = currentDistanceAbs;
			}
		}

		return distanceMap;
	}

	@Override
	public void distanceMapToIntARGB(double[][] distanceMap, int[] dstPixels, int dstWidth, int dstHeight) {
		// Normalizing distance map array
		double distanceMapMax = Arrays.stream(distanceMap).flatMapToDouble(x -> Arrays.stream(x)).max().getAsDouble();
		double distanceMapMin = Arrays.stream(distanceMap).flatMapToDouble(x -> Arrays.stream(x)).min().getAsDouble();

		// Loop over all values in distance map and write as ARGB into destination
		for (int y = 0; y < dstHeight; y++) {
			for (int x = 0; x < dstWidth; x++) {
				int norm = (int) ((distanceMap[x][y] - distanceMapMin) / (distanceMapMax - distanceMapMin) * 255);
				int pos = y * dstWidth + x;
				int lum = 255 - norm;
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
