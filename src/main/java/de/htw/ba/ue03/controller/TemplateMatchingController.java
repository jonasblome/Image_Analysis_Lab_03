/**
 * @author Nico Hezel
 */
package de.htw.ba.ue03.controller;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import de.htw.ba.ue03.matching.TemplateMatcher;
import de.htw.ba.ue03.matching.TemplateMatcherFactory;
import de.htw.ba.ue03.matching.TemplateMatcherFactory.Methods;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;

public class TemplateMatchingController {
	
	@FXML
	private ImageView leftImageView;
	
	@FXML
	private ImageView rightImageView;
	
	@FXML
	private ComboBox<Methods> methodSelection;
	
	@FXML
	private Label runtimeLabel;
	
	/**
	 * The pure image data
	 */
	private int[] cleanSrc;
	
	/**
	 * Template matcher factory which holds the template information
	 */
	private TemplateMatcherFactory factory;
		
	@FXML
	public void initialize() throws IOException {
		methodSelection.getItems().addAll(Methods.values());
		methodSelection.setValue(Methods.Copy);
		methodSelection.setOnAction(this::applyTemplateMatching);
		
		// load the template
		final BufferedImage template = ImageIO.read(Paths.get("template.png").toFile());
		final int templateWidth = template.getWidth();
		final int templateHeight = template.getHeight();
		final int[] templatePixel = new int[templateWidth*templateHeight];
		template.getRGB(0, 0, templateWidth, templateHeight, templatePixel, 0, templateWidth);

		// template matcher factory
		factory = new TemplateMatcherFactory(templatePixel, templateWidth, templateHeight);

		// load the default image
		loadImage(new File("textur-tapete.png"));
	}

	@FXML
	public void onOpenFileClick() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(".")); 
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images (*.jpg, *.png, *.gif)", "*.jpeg", "*.jpg", "*.png", "*.gif"));
		loadImage(fileChooser.showOpenDialog(null));
	}
	
	public void loadImage(File file) {		
		if(file != null) {
			leftImageView.setImage(new Image(file.toURI().toString()));
			cleanSrc = imageToPixel(leftImageView.getImage());
			applyTemplateMatching(null);
		}		
	}
	
	public int[] imageToPixel(Image image) {
		int width = (int)image.getWidth();
		int height = (int)image.getHeight();
		int[] pixels = new int[width * height];
		image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
		return pixels;
	}
	
	public int[] imageToPixel(BufferedImage image) {
		int width = (int)image.getWidth();
		int height = (int)image.getHeight();
		int[] pixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);
		return pixels;
	}
	
	public Image pixelToImage(int[] pixels, int width, int height) {
		WritableImage wr = new WritableImage(width, height);
		PixelWriter pw = wr.getPixelWriter();
		pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
		return wr;
	}
	
	@FXML
	public void applyTemplateMatching(ActionEvent event) {

		// no images loaded
		if(leftImageView.getImage() == null)
			return;
		
		// get method choice 
		final Methods currentMethod = methodSelection.getSelectionModel().getSelectedItem();
		final TemplateMatcher matcher = factory.getTemplateMatcher(currentMethod);
		
	  	// get image dimensions
    	int srcWidth = (int)leftImageView.getImage().getWidth();
    	int srcHeight = (int)leftImageView.getImage().getHeight();
    	int templateWidth = matcher.getTemplateWidth();
    	int templateHeight = matcher.getTemplateHeight();
    	int dstWidth  = srcWidth-templateWidth;
    	int dstHeight = srcHeight-templateHeight;

    	// get pixels arrays
    	int srcPixels[] = Arrays.copyOf(cleanSrc, cleanSrc.length);
    	int dstPixels[] = new int[dstWidth * dstHeight];
    	
		long startTime = System.currentTimeMillis();

		// run the template matcher
		try {
			runMethod(matcher, srcPixels, srcWidth, srcHeight, dstPixels, dstWidth, dstHeight);
		} catch (Exception e) { e.printStackTrace();
			e.printStackTrace();
		}
		
		rightImageView.setImage(pixelToImage(dstPixels, dstWidth, dstHeight));
		leftImageView.setImage(pixelToImage(srcPixels, srcWidth, srcHeight));
    	runtimeLabel.setText("Methode " + currentMethod + " ausgeführt in " + (System.currentTimeMillis() - startTime) + " ms");
	}

	public void runMethod(TemplateMatcher matcher, int[] srcPixels, int srcWidth, int srcHeight, 
										  		   int[] dstPixels, int dstWidth, int dstHeight) throws Exception {
		
		// compute the distance map
		double[][] distanceMap = matcher.getDistanceMap(srcPixels, srcWidth, srcHeight);
		
		// draw the distance map into the destination pixel array
		matcher.distanceMapToIntARGB(distanceMap, dstPixels, dstWidth, dstHeight);
		
		// find local maxims in the distance map
		List<Point> maximas = matcher.findMaximas(distanceMap);
		
		// draw the maxims into the source image
		int templateWidth = matcher.getTemplateWidth();
		int templateHeight = matcher.getTemplateHeight();
		
		BufferedImage bufferedImage = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_ARGB);
    	bufferedImage.setRGB(0, 0, srcWidth, srcHeight, srcPixels, 0, srcWidth);
    	Graphics2D g2d = bufferedImage.createGraphics();
    	g2d.setColor(Color.GREEN);
    	
    	for (Point point : maximas)
    	{
    		int xMin = (int) point.getX();
    		int yMin = (int) point.getY();
    		
    		int xMax = xMin + templateWidth;
			int yMax = yMin + templateHeight;
			
	    	g2d.drawLine(xMin, yMin, xMax, yMin);
	    	g2d.drawLine(xMax, yMin, xMax, yMax);
	    	g2d.drawLine(xMax, yMax, xMin, yMax);
	    	g2d.drawLine(xMin, yMax, xMin, yMin);
		}
    	
    	g2d.dispose();
		srcPixels = bufferedImage.getRGB(0, 0, srcWidth, srcHeight, srcPixels, 0, srcWidth);
	}
}