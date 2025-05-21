import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
public class Driver {
	
	// Declare class data
	
	    private static JComboBox<Integer> comboBox;
	    private static JCheckBox checkBox;
	    private static JButton playButton;
	    private static JMapViewer mapViewer;
	    private static int animationTime;
	    private static boolean includeStops;

    public static void main(String[] args) throws FileNotFoundException, IOException {

    	// Read file and call stop detection
    	
    	  TripPoint.readFile("triplog.csv");
         
    	
    	// Set up frame, include your name in the title
    	
        
          JFrame frame = new JFrame("Map GUI - Brinly Jaja");
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.setSize(800, 600);
        // Set up Panel for input selections
          JPanel topPanel = new JPanel(new FlowLayout());

    	
        // Play Button
          playButton = new JButton("Play");
          playButton.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                  playAnimation();
              }
          });    	
        // CheckBox to enable/disable stops
          checkBox = new JCheckBox("Include Stops");
          checkBox.setSelected(false);
    	
        // ComboBox to pick animation time
          comboBox = new JComboBox<>(new Integer[]{15, 30, 60, 90});

    	
        // Add all to top panel
          topPanel.add(playButton);
          topPanel.add(checkBox);
          topPanel.add(comboBox);

        
        // Set up mapViewer
          mapViewer = new JMapViewer();

        
        // Add listeners for GUI components
          frame.add(topPanel, BorderLayout.NORTH);
          frame.add(mapViewer, BorderLayout.CENTER);

        // Set the map center and zoom level
          mapViewer.setDisplayPosition(new Coordinate(0, 0), 2);

          frame.setVisible(true);
      
        
    }
    
    // Animate the trip based on selections from the GUI components
    private static void playAnimation() {
        // Stop any ongoing animation
    	 mapViewer.removeAllMapMarkers();
         mapViewer.removeAllMapPolygons();

         animationTime = (int) comboBox.getSelectedItem();
         includeStops = checkBox.isSelected();

         ArrayList<TripPoint> tripData = includeStops ? TripPoint.getTrip() : TripPoint.getMovingTrip();

         new Thread(new Runnable() {
             @Override
             public void run() {
                 for (int i = 0; i < tripData.size(); i++) {
                     TripPoint currentPoint = tripData.get(i);
                     Coordinate coord = new Coordinate(currentPoint.getLat(), currentPoint.getLon());

                     // Add marker for the current trip point
                     SwingUtilities.invokeLater(new Runnable() {
                         @Override
                         public void run() {
                             mapViewer.addMapMarker(new IconMarker(coord, Toolkit.getDefaultToolkit().getImage("raccoon.png")));
                         }
                     });

                     // Draw red line segment connecting previous and current trip points
                     if (i > 0) {
                         TripPoint prevPoint = tripData.get(i - 1);
                         Coordinate prevCoord = new Coordinate(prevPoint.getLat(), prevPoint.getLon());
                         MapPolygonImpl line = new MapPolygonImpl(new Coordinate[]{prevCoord, coord});
                         line.setColor(Color.RED);
                         mapViewer.addMapPolygon(line);
                     }

                     // Set map center to current point
                     mapViewer.setDisplayPosition(coord, 15);

                     try {
                         Thread.sleep(animationTime * 1000 / tripData.size());
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }).start();
     }
  
}