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
import multimodalite.Shape;
import multimodalite.ShapeType;

/**
 *
 * @author Lioz
 */
public class VocalRecognizerIvy extends Ivy{
    private enum Etats {IDLE, GESTURERECEIVED, CLICKED, TALKED};
    private Etats etat = Etats.IDLE;
    private Shape finalShape = new Shape();
    private boolean waitingForColor = false;
    private boolean waitingForPoint = false;
    private boolean waitingForObject = false;
    private Timer timerTalked = new Timer();
    private Shape selectedInfos = new Shape();
    
    public VocalRecognizerIvy() throws IvyException {
        super("VocalRecognizer","VocalRecognizer Ready",null);
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
            
            this.bindMsg("^sra5 Text=rectangle*.", (IvyClient ic, String[] strings) -> {
                selectionnerShape(ShapeType.RECTANGLE);
            });
            this.bindMsg("^sra5 Text=ellipse*.", (IvyClient ic, String[] strings) -> {
                selectionnerShape(ShapeType.ELLIPSE);
            });
            
            this.bindMsg("^sra5 Text=ici*.", (IvyClient ic, String[] strings) -> {
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
    /**
     * Action réalisée quand on choisit une couleur à l'oral
     * @param couleur 
     */
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
    private void selectionnerShape(ShapeType shape) {
        switch(etat) {
            case GESTURERECEIVED : case CLICKED: case TALKED:
                finalShape.setType(shape);
                etat = Etats.GESTURERECEIVED;  
                break;
            case IDLE: break;
        } 
    }

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
                //GestureRecognizer.timerClicked.cancel();
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
                selectionnerPoints();
                break;
            case IDLE: case TALKED : default : break;
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
}
