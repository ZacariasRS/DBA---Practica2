package p2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.emory.mathcs.backport.java.util.Arrays;

public class Map extends JPanel {
	
	// Map states (each pixel of the mapcan be known, unknown, obstacle, etc)
	public enum MapState {
		
		KNOWN(Color.WHITE),
		UNKNOWN(Color.BLACK),
		OBSTACLE(Color.RED),
		ROBOT(Color.GRAY),
		GOAL(Color.GREEN);
		
		// Each state has a color to be represented on a 2D map
		private Color color;
		
		private MapState(Color c) {
			this.color = c;
		}
		
		public Color getColor() {
			return color;
		}
	}

	
    private BufferedImage image;
    
    // Pixel size
    private int pixelWidth = 4;
    private int pixelHeight = 4;
    
    // Map size
    private int width = 100;
    private int height = 100;
    
    // Pixel buffers to draw on the map. Will be filled with pixels of the right color
    private int[] robotSurface    = new int[pixelHeight * pixelWidth];
    private int[] unknownSurface  = new int[pixelHeight * pixelWidth];
    private int[] knownSurface    = new int[pixelHeight * pixelWidth];
    private int[] obstacleSurface = new int[pixelHeight * pixelWidth];
    private int[] goalSurface     = new int[pixelHeight * pixelWidth];
    
    
    // Map composition: for each position (x, y), contains the state of the map (known, obstacle,...) 
    private MapState[] mapComposition = new MapState[width * height];
    
    // Singleton
    private static Map instance = new Map();
    public static Map getInstance() {
    	return instance;
    }
    
    // Private because singleton
	private Map() {
		this.setPreferredSize(new Dimension(width * pixelWidth, height * pixelHeight));
		
		// Initially the map is completely unknown
		Arrays.fill(mapComposition, MapState.UNKNOWN);
		
		// Fill buffers with the according colors. We do this in the constructor instead
		// Of doing it at each repaint for optimization
		Arrays.fill(robotSurface, MapState.ROBOT.getColor().getRGB());
		Arrays.fill(unknownSurface, MapState.UNKNOWN.getColor().getRGB());
		Arrays.fill(knownSurface, MapState.KNOWN.getColor().getRGB());
		Arrays.fill(obstacleSurface, MapState.OBSTACLE.getColor().getRGB());
		Arrays.fill(goalSurface, MapState.GOAL.getColor().getRGB());
		
		// Show map in window
		SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(Map.getInstance());
                frame.pack();
                frame.setVisible(true);
            }
        });
	}
	
	public void setMapComposition(int x, int y, MapState newState) {
		mapComposition[width * (x-1) + (y-1)] = newState;
		super.repaint();
	}
	
    @Override
    public void paintComponent(Graphics g) {
    	if (image == null) {
    		image = (BufferedImage) createImage(width * pixelWidth, height * pixelHeight);
    	}
        for (int row = 0; row < width; ++row) {
            for (int col = 0; col < height; ++col) {
            	
            	// How is the map composition at this pixel? Known? Unknown? Goal?...
            	int[] surface;
            	switch (mapComposition[col * width + row]) {
                	case KNOWN:
                		surface = knownSurface; break;
                	case UNKNOWN:
                		surface = unknownSurface; break;
                	case GOAL:
                		surface = goalSurface; break;
                	case ROBOT:
                		surface = robotSurface; break;
                	default:
            			throw new IllegalStateException("Unknown surface type " + mapComposition[col * width + row]);
            	}
            	
                image.setRGB(col * pixelWidth, row * pixelHeight, pixelWidth, pixelHeight, surface, 0, pixelWidth);
            }
        }
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }
}
