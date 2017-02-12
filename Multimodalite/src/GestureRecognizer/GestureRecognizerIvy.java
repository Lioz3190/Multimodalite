/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GestureRecognizer;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VALENTIN-WIN
 */
public class GestureRecognizerIvy extends Ivy{
    
    private enum State {PRESS, RELEASE};
    
    private State state;
    
     public GestureRecognizerIvy() throws IvyException {
            super("GestureRecognizer","GestureRecognizer Ready",null);

            state = State.PRESS;
            /* Messages from the Palette */
            this.bindMsg("^Palette:MousePressed x=([^ ]*) y=([^ ]*)", (IvyClient client, String[] args) -> {
                mousePressed(args);
            });
            
            this.bindMsg("^Palette:MouseDragged x=([^ ]*) y=([^ ]*)", (IvyClient client, String[] args) -> {
                mouseDragged(args);
            });

            this.bindMsg("^Palette:MouseReleased x=([^ ]*) y=([^ ]*)", (IvyClient client, String[] args) -> {
                mouseReleased(args);
                System.out.println("Release");
            });
            
            this.start(null);
        }
        
        private Stroke stroke = new Stroke();
        
        public Stroke getStroke() {
            return stroke;
        }

        
        /**
         * Starts to draw on the specified panel according to current state
         * @param args 
         */
        private void mousePressed(String[] args) {
            int x = Integer.valueOf(args[0]);
            int y = Integer.valueOf(args[1]);
            
          
            
            switch(state) {
                case PRESS :
                    state = State.RELEASE;
                    stroke = new Stroke();
                    stroke.addPoint(new Point2D.Double(x, y));
                    break;
                case RELEASE :
                   //interdit
                    break;
            }
        }
        
        /**
         * Continues the drawing on the corresponding panel
         * @param args 
         */
        private void mouseDragged(String[] args) {
            int x = Integer.valueOf(args[0]);
            int y = Integer.valueOf(args[1]);
            
            
            
            switch(state) {
                case PRESS :
                    //interdit
                    break;
                case RELEASE :
                    state = State.RELEASE;
                    stroke.addPoint(new Point2D.Double(x, y));
                    break;
            }
        }
        

        private void mouseReleased(String[] args) {
            int x = Integer.valueOf(args[0]);
            int y = Integer.valueOf(args[1]);
            
            
            switch(state) {
                case PRESS :
                    //interdit
                    break;
                case RELEASE :
                    state = State.PRESS;
                    stroke.addPoint(new Point2D.Double(x, y));
                    stroke.normalize();
                    envoyerMessage(""+stroke);
                    break;
            }
            
            
        }
        
        /**
         * Sends a message telling it recognized a shape
         * @param nom 
         */
        public void envoyerMessage(String nom) {
            try {
                this.sendMsg("GestureRecognizer:ActionRecognized nom="+nom);
            } catch (IvyException ex) {
                Logger.getLogger(GestureRecognizerIvy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }