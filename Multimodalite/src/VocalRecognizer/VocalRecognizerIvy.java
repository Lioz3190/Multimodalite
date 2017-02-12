/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VocalRecognizer;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import multimodalite.Shape;
import multimodalite.ShapeType;

/**
 *
 * @author Lioz
 */
public class VocalRecognizerIvy extends Ivy{
    private enum Etats {IDLE, GESTURERECEIVED, CLICKED, TALKED};
    private enum Action {RECTANGLE, CIRCLE, MOVE, DELETE, IDLE};
    private Action action = Action.IDLE;
    private Etats etat = Etats.IDLE;
    private Shape finalShape = new Shape();
    private boolean waitingForColor = false;
    private boolean waitingForPoint = false;
    private boolean waitingForObject = false;

    
    private Timer timerGestureReceived = new Timer(); 
    private Timer timerClicked = new Timer();
    private Timer timerTalked = new Timer();
   
    
    private Shape selectedInfos = new Shape();
    
    public VocalRecognizerIvy() throws IvyException {
        super("VocalRecognizer","VocalRecognizer Ready",null);
        
        /* Messages from the Gesture RecognizerIvy */
            this.bindMsg("^GestureRecognizer:ActionRecognized nom=(.*)", (IvyClient ic, String[] strings) -> {
                gestureRecognized(strings[0]);
            });
            
            /* Messages from the Palette */
            this.bindMsg("^Palette:MousePressed x=([^ ]*) y=([^ ]*)", (IvyClient ic, String[] strings) -> {
                int x = Integer.valueOf(strings[0]);
                int y = Integer.valueOf(strings[1]);
                
                mousePressed(x, y);
            });
            
            this.bindMsg("^Palette:ResultatTesterPoint x=([^ ]*) y=([^ ]*) nom=([^ ]*)", (IvyClient client, String[] args) -> {
                String name = args[2];
            
                resultatTesterPoint(name);
            });
            
            this.bindMsg("^Palette:Info nom=([^ ]*) x=([^ ]*) y=([^ ]*) longueur=(.*) hauteur=(.*) couleurFond=([^ ]*) *", (IvyClient client, String[] args) -> {   
                String name = args[0];
                int x = Integer.valueOf(args[1]);
                int y = Integer.valueOf(args[2]);
                String couleur = args[5];
                
                resultatInfo(name, couleur, x, y);
            });
            
            
            
            
         /* Messages from the sra */
            this.bindMsg("^sra5 Text=rouge*.",(IvyClient client, String[] strings) -> {
                selectionnerCouleur("RED");
            });
            this.bindMsg("^sra5 Text=noir*.", (IvyClient ic, String[] strings) -> {
                selectionnerCouleur("BLACK");
            });
            this.bindMsg("^sra5 Text=vert*.", (IvyClient ic, String[] strings) -> {
                selectionnerCouleur("GREEN");
            });
            this.bindMsg("^sra5 Text=blanc*.", (IvyClient ic, String[] strings) -> {
                selectionnerCouleur("WHITE");
            });
            
            this.bindMsg("^sra5 Text=ici*.", (IvyClient ic, String[] strings) -> {
                selectionnerIci();
            });
            
             this.bindMsg("^sra5 Text=la*.", (IvyClient ic, String[] strings) -> {
                selectionnerIci();
            });
            
            this.bindMsg("^sra5 Text=cet objet*.", (IvyClient ic, String[] strings) -> {
                selectionnerObjectAt("");
            });
            this.bindMsg("^sra5 Text=ce rectangle*.", (IvyClient ic, String[] strings) -> {
                selectionnerObjectAt("rectangle");
            });
            this.bindMsg("^sra5 Text=cette ellipse*.", (IvyClient ic, String[] strings) -> {
                selectionnerObjectAt("ellipse");
            });
                
            this.bindMsg("^sra5 Text=de cette couleur*.", (IvyClient ic, String[] strings) -> {
                selectionnerCouleurAt();
            });
            this.start(null);
    }
    
    private void setAction(String name){
        switch(name){
            case "circle" :
                action = Action.CIRCLE;
                break;
            case "rectangle" :
                action = Action.RECTANGLE;
                break;
            case "barre" :
                action = Action.MOVE;
                break;
            case "croix" :
                action = Action.DELETE;
                break;
        }
    }
    
    private void gestureRecognized(String name) {
            switch(etat) {
                case IDLE : // Wait 10sec for the rest of the command
                    timerGestureReceived = new Timer();
                    timerGestureReceived.schedule(timerGestureReceivedEventHandler(), 10000);

                    setAction(name);
                    etat = Etats.GESTURERECEIVED;                        
                    break;
                case CLICKED : case GESTURERECEIVED : case TALKED : default:
                    break;
            }
        }

    
    private void selectionnerCouleur(String couleur) {
        switch(etat) {
            case GESTURERECEIVED : case CLICKED: case TALKED:
                if(waitingForObject) {
                    selectedInfos.setColor(couleur);
                } else {
                    finalShape.setColor(couleur);
                    etat = Etats.GESTURERECEIVED;  
                }
                break;
            case IDLE: break;
        } 
    }


    /**
     * Action réalisée quand on choisit une couleur à l'oral
     * @param couleur 
     */

    /**
     * Action réalisée quand on choisit la position "ici" à l'oral
     */
    private void selectionnerIci() {
        switch(etat) {
            case GESTURERECEIVED :
                etat = Etats.TALKED;
                waitingForPoint = true;
                timerTalked = new Timer();
                timerTalked.schedule(timerTalkedEventHandler(), 5000);
                break;
            case TALKED :
                etat = Etats.TALKED;
                waitingForPoint = true;
                break;
            case CLICKED :
                etat = Etats.GESTURERECEIVED;
                finalShape.setPosition(selectedInfos.getPosition().x, selectedInfos.getPosition().y);
                timerClicked.cancel();
                break;
            case IDLE : default : break;
        }  
    }

    /**
     * Action réalisée quand on choisit "cet objet" à l'oral
     * @param couleur 
     */
    private void selectionnerObjectAt(String objectType) {
        
        waitingForObject = true;
        selectedInfos.setType(ShapeType.getShapeType(objectType));

        switch(etat) {
            case GESTURERECEIVED : 
                timerTalked = new Timer();
                timerTalked.schedule(timerTalkedEventHandler(), 5000);
               
                etat = Etats.TALKED;
                break;
            case CLICKED :
                etat = Etats.GESTURERECEIVED;
                selectionnerPoints();
                break;
            case IDLE: case TALKED : default : break;
        }
    }

    /**
     * Action réalisée quand on choisit une couleur à l'oral
     * @param couleur 
     */
    private void selectionnerCouleurAt() {
       
        waitingForColor = true;

        switch(etat) {
            case GESTURERECEIVED : 
                timerTalked = new Timer();
                timerTalked.schedule(timerTalkedEventHandler(), 5000);
                
                etat = Etats.TALKED;
                break;
            case CLICKED :
                etat = Etats.GESTURERECEIVED;
                selectionnerPoints();
                
                break;
            case IDLE: case TALKED : default : break;
        }
    }
        private void mousePressed(int x, int y) {
            
            switch(etat) {
                case TALKED :
                    if(waitingForPoint) {
                        if(action == Action.MOVE) {
                            //finalShape.setPosition(x - selectedInfos.getPosition().x, y - selectedInfos.getPosition().y);                            
                            finalShape.setPosition(x, y);
                        } else {  
                            finalShape.setPosition(x, y);
                        }
                        waitingForPoint = false;
                        etat = Etats.GESTURERECEIVED;
                        timerTalked.cancel();
                    }
                    
                    if(waitingForObject || waitingForColor) {
                        selectedInfos.setPosition(x, y);
                        selectionnerPoints();
                    }
                    
                    break;
                case GESTURERECEIVED : 
                    if(waitingForPoint && action == action.MOVE) {
                        selectedInfos.setPosition(x - selectedInfos.getPosition().x, y - selectedInfos.getPosition().y);
                    } else {  
                        selectedInfos.setPosition(x, y);
                    }

                    timerClicked = new Timer();
                    timerClicked.schedule(timerClickedEventHandler(), 5000);
                    etat = Etats.CLICKED;
                    break;
                case IDLE : case CLICKED : default :
                    break;
            }
        }
    /** 
    * Tests the selected point or [0;0] if nothing was selected
    */
    public void selectionnerPoints() {
        int x = 0, y = 0;
        if(selectedInfos.getPosition() != null) {
           x = selectedInfos.getPosition().x;
           y = selectedInfos.getPosition().y;
        }
        
        try {
                this.sendMsg("Palette:TesterPoint x=" + x + " y=" + y);
            } catch (IvyException ex) {
                Logger.getLogger(VocalRecognizerIvy.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    private void resultatTesterPoint(String name) {            
            if(waitingForColor || (waitingForObject && selectedInfos.getColor() != null)) {
                try {
                    this.sendMsg("Palette:DemanderInfo nom=" + name);
                    } catch (IvyException ex) {
                        Logger.getLogger(VocalRecognizerIvy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                selectedInfos.setName(name);
                
            } else {
                finalShape.setName(name);
            }
    }
     private void resultatInfo(String name, String couleur, int x, int y) {  
            if(waitingForColor) {
                finalShape.setColor(couleur);
            }
            
            if(waitingForObject && selectedInfos.getColor() != null && selectedInfos.getColor().equals(couleur.toUpperCase())) {
                finalShape.setName(name);
            }
        }

    /**
     * Event handler of the timer talked
     * @return 
     */
    public TimerTask timerTalkedEventHandler() {
        return new TimerTask() {

            @Override
            public void run() {
                switch(etat) {
                    case TALKED :
                        etat = Etats.GESTURERECEIVED;
                        waitingForPoint = false;
                        waitingForObject = false;
                        waitingForColor = false;
                        break;
                    case CLICKED : case GESTURERECEIVED : case IDLE :
                        break;
                }
            }
        };
    }
    public TimerTask timerGestureReceivedEventHandler() {
        return new TimerTask() {
            @Override
            public void run() {
                switch(etat) {
                    case GESTURERECEIVED : case CLICKED : case TALKED :
                        actionFinal();
                        etat = Etats.IDLE;
                        break;
                    case IDLE : default :
                        break;
                }
            }
        };
        
        
    }
    
    public TimerTask timerClickedEventHandler() {
        return new TimerTask() {

            @Override
            public void run() {
                switch(etat) {
                    case CLICKED :
                        etat = Etats.GESTURERECEIVED;
                        selectedInfos = new Shape();
                        break;
                    case TALKED : case GESTURERECEIVED : case IDLE :
                        break;
                }
            }
        };
    }
    
    
    
    public void actionFinal(){
        switch(action){
            case RECTANGLE :
                action = action.IDLE;
                dessinerRectangle();
                break;
            case CIRCLE :
                action = action.IDLE;
                dessinerCercle();
                break;
            case MOVE :
                action = action.IDLE;
                move();
                break;
            case DELETE :
                action = action.IDLE;
                delete();
                break;
            case IDLE :
                //interdit
                break;
        }
        
        reset();
    }
    /**
         * Draws a rectangle according to the variables
         */
        public void dessinerRectangle() {
            int x=0;
            int y=0;
            String couleur = "WHITE";
            
            if(finalShape.getPosition() != null) {
                x = finalShape.getPosition().x;
                y = finalShape.getPosition().y;
            }
            
            if(finalShape.getColor() != null) {
                couleur = finalShape.getColor();
            }            
            
            try {
                this.sendMsg("Palette:CreerRectangle x=" + x + " y=" + y
                    + " couleurFond=" + couleur + " couleurContour=BLACK");
            } catch (IvyException ex) {
                Logger.getLogger(VocalRecognizerIvy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * Draws an ellipse according to the variables
         */
        public void dessinerCercle() {
            int x=0;
            int y=0;
            String couleur = "WHITE";
            
            if(finalShape.getPosition() != null) {
                x = finalShape.getPosition().x;
                y = finalShape.getPosition().y;
            }
            
            if(finalShape.getColor() != null) {
                couleur = finalShape.getColor();
            }
            
            try {
                this.sendMsg("Palette:CreerEllipse x=" + x + " y=" + y
                    + " couleurFond=" + couleur + " couleurContour=BLACK");
            } catch (IvyException ex) {
                Logger.getLogger(VocalRecognizerIvy.class.getName()).log(Level.SEVERE, null, ex);
            }    
        }

        /**
         * Moves the designated object to designated position
         */
        public void move() {
            if(finalShape.getName() != null) {
                int x=0;
                int y=0;

                if(finalShape.getPosition() != null) {
                    x = finalShape.getPosition().x;
                    y = finalShape.getPosition().y;
                }

                try {
                    this.sendMsg("Palette:DeplacerObjetAbsolu nom=" + finalShape.getName() + " x=" + x + " y=" + y);
                } catch (IvyException ex) {
                    Logger.getLogger(VocalRecognizerIvy.class.getName()).log(Level.SEVERE, null, ex);
                }
            }  
        }

        /**
         * Deletes designated object
         */
        public void delete() {
            if(finalShape.getName() != null) {
                try {
                    this.sendMsg("Palette:SupprimerObjet nom=" + finalShape.getName());
                } catch (IvyException ex) {
                    Logger.getLogger(VocalRecognizerIvy.class.getName()).log(Level.SEVERE, null, ex);
                }
            }  
        }
        
        public void reset(){
            finalShape = new Shape();
            waitingForColor = false;
            waitingForPoint = false;
            waitingForObject = false;
            selectedInfos = new Shape(); 
        }
}
