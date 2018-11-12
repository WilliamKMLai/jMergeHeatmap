import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.imageio.ImageIO;

public class jMergeHeatmap {
	private static File senseFile = null;
	private static File antiFile = null;
	private static File OUTPUT = null;
	
	public static void main(String[] args) throws IOException {
		System.out.println(getTimeStamp());
		//Load command line arguments
		loadConfig(args);
		//Merge PNGs into output file		
		mergePNG(senseFile, antiFile, OUTPUT);
		System.out.println(getTimeStamp());
	}
	
	public static void mergePNG(File INPUT1, File INPUT2, File OUTPUT) throws IOException {
		BufferedImage image = ImageIO.read(INPUT1);
		BufferedImage overlay = ImageIO.read(INPUT2);
		if(image.getWidth() != overlay.getWidth()) {
			System.err.println("Unequal Pixel Width!!!\n" + INPUT1.getName() + ":\t" + image.getWidth() + "\n" + INPUT2.getName() + ":\t" + overlay.getWidth());
			System.exit(1);
		} else if(image.getHeight() != overlay.getHeight()) {
			System.err.println("Unequal Pixel Height!!!\n" + INPUT1.getName() + ":\t" + image.getHeight() + "\n" + INPUT2.getName() + ":\t" + overlay.getHeight());
			System.exit(1);
		} else {
			int w = Math.max(image.getWidth(), overlay.getWidth());
			int h = Math.max(image.getHeight(), overlay.getHeight());
			BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			for (int x = 0; x < combined.getWidth(); x++) {
				for (int y = 0; y < combined.getHeight(); y++) {
					int image_rgb = image.getRGB(x, y);
					int overlay_rgb = overlay.getRGB(x, y);
			
					int image_alpha = (image_rgb & 0xFF000000) >>> 24;
				    int image_red = (image_rgb & 0x00FF0000) >>> 16;
				    int image_green = (image_rgb & 0x0000FF00) >>> 8;
				    int image_blue  = (image_rgb & 0x000000FF) >>> 0;
							
					int overlay_alpha = (overlay_rgb & 0xFF000000) >>> 24;
				    int overlay_red = (overlay_rgb & 0x00FF0000) >>> 16;
				    int overlay_green = (overlay_rgb & 0x0000FF00) >>> 8;
				    int overlay_blue  = (overlay_rgb & 0x000000FF) >>> 0;
			
				    int new_rgb;
				    if(image_green >= 240 && image_blue >= 240 && image_red >= 240) {
				    	new_rgb = (overlay_alpha << 24) | (overlay_red << 16) | (overlay_green << 8) | overlay_blue;
				    } else if(overlay_green >= 240 && overlay_blue >= 240 && overlay_red >= 240) {
				    	new_rgb = (image_alpha << 24) | (image_red << 16) | (image_green << 8) | image_blue;
				    } else {
				    	int new_alpha = (image_alpha + overlay_alpha) / 2;
				    	int new_red = (image_red + overlay_red) / 2;
				    	int new_green = (image_green + overlay_green) / 2;
				    	int new_blue = (image_blue + overlay_blue) / 2;
			
				    	new_rgb = (new_alpha << 24) | (new_red << 16) | (new_green << 8) | new_blue;
				    }
					combined.setRGB(x, y, new_rgb);
				}
			}
			//Output new image
			ImageIO.write(combined, "PNG", OUTPUT);
		}
	}
	
	public static void loadConfig(String[] command){
		for (int i = 0; i < command.length; i++) {
			switch (command[i].charAt((1))) {
				case 's':
					senseFile = new File(command[i + 1]);
					i++;
					break;
				case 'a':
					antiFile = new File(command[i + 1]);
					i++;
					break;
				case 'o':
					OUTPUT = new File(command[i + 1]);
					i++;
					break;
				case 'h':
					printUsage();
					System.exit(0);
			}
		}
		if(senseFile == null || antiFile == null) {
			printUsage();
			System.exit(1);
		}
		if(OUTPUT == null) {
			OUTPUT = new File(System.getProperty("user.dir") + File.separator + "merge.png");
		}

		System.out.println("-----------------------------------------\nCommand Line Arguments:");
		System.out.println("Sense file: " + senseFile);
		System.out.println("Antisense file: " + antiFile);
		System.out.println("Output file: " + OUTPUT);
	}
	
	public static void printUsage() {
		System.err.println("\nUsage: java -jar MergeHeatMap.jar -s [sense.png] -a [anti.png] -o [Output file]");
		System.err.println("-----------------------------------------");
		System.err.println("Required Parameter:");
		System.err.println("Sense PNG:\t-s");
		System.err.println("Antisense PNG:\t-a");
		System.err.println("\nSupported Options:");
		System.err.println("Output file:\t\t-o\tDefault current directory");
		System.err.println("Help:\t\t\t-h\tPrint this message");
	}
	
	private static String getTimeStamp() {
		Date date= new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}
}
