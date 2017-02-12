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
public class VocalRecognizerIvy extends Ivy {

    private enum Etats {
        IDLE, GESTURERECEIVED, CLICKED, TALKED
    };

    private enum Action {
        RECTANGLE, CIRCLE, MOVE, DELETE, IDLE
    };
    private Action action = Action.IDLE;
    private Etats etat = Etats.IDLE;
    private Shape finalShape = new Shape();
    private boolean waitingForColor = false;
    private boolean waitingForPos = false;
    private boolean waitingForObject = false;

    private Timer timerGestureReceived = new Timer();
    private Timer timerClicked = new Timer();
    private Timer timerTalked = new Timer();

    private Shape currentShape = new Shape();

    public VocalRecognizerIvy() throws IvyException {
        super("VocalRecognizer", "VocalRecognizer Ready", null);

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
        this.bindMsg("^sra5 Text=rouge*.", (IvyClient client, String[] strings) -> {
            selectColor("RED");
        });
        this.bindMsg("^sra5 Text=noir*.", (IvyClient ic, String[] strings) -> {
            selectColor("BLACK");
        });
        this.bindMsg("^sra5 Text=vert*.", (IvyClient ic, String[] strings) -> {
            selectColor("GREEN");
        });
        this.bindMsg("^sra5 Text=blanc*.", (IvyClient ic, String[] strings) -> {
            selectColor("WHITE");
        });

        this.bindMsg("^sra5 Text=ici*.", (IvyClient ic, String[] strings) -> {
            selectPosition();
        });

        this.bindMsg("^sra5 Text=la*.", (IvyClient ic, String[] strings) -> {
            selectPosition();
        });

        this.bindMsg("^sra5 Text=a cette position*.", (IvyClient ic, String[] strings) -> {
            selectPosition();
        });

        this.bindMsg("^sra5 Text=cet objet*.", (IvyClient ic, String[] strings) -> {
            selectShape("");
        });
        this.bindMsg("^sra5 Text=ce rectangle*.", (IvyClient ic, String[] strings) -> {
            selectShape("rectangle");
        });
        this.bindMsg("^sra5 Text=cette ellipse*.", (IvyClient ic, String[] strings) -> {
            selectShape("ellipse");
        });

        this.bindMsg("^sra5 Text=de cette couleur*.", (IvyClient ic, String[] strings) -> {
            selectColorAt();
        });

        this.bindMsg("^sra5 Text=annuler*.", (IvyClient ic, String[] strings) -> {
            annuler();
        });

        this.bindMsg("^sra5 Text=fini*.", (IvyClient ic, String[] strings) -> {
            fini();
        });
        this.start(null);
    }

    private void setAction(String name) {
        switch (name) {
            case "circle":
                action = Action.CIRCLE;
                break;
            case "rectangle":
                action = Action.RECTANGLE;
                break;
            case "barre":
                action = Action.MOVE;
                break;
            case "croix":
                action = Action.DELETE;
                break;
        }
    }

    private void gestureRecognized(String name) {
        switch (etat) {
            case IDLE: // Wait 10sec for the rest of the command
                timerGestureReceived = new Timer();
                timerGestureReceived.schedule(timerGestureReceivedEventHandler(), 10000);

                setAction(name);
                etat = Etats.GESTURERECEIVED;
                break;
            case CLICKED:
            case GESTURERECEIVED:
            case TALKED:
            default:
                break;
        }
    }

    private void selectColor(String couleur) {
        switch (etat) {
            case GESTURERECEIVED:
            case CLICKED:
            case TALKED:
                if (waitingForObject) {
                    currentShape.setColor(couleur);
                } else {
                    finalShape.setColor(couleur);
                    etat = Etats.GESTURERECEIVED;
                }
                break;
            case IDLE:
                break;
        }
    }

    private void selectPosition() {
        switch (etat) {
            case GESTURERECEIVED:
                etat = Etats.TALKED;
                waitingForPos = true;
                timerTalked = new Timer();
                timerTalked.schedule(timerTalkedEventHandler(), 5000);
                break;
            case TALKED:
                etat = Etats.TALKED;
                waitingForPos = true;
                break;
            case CLICKED:
                etat = Etats.GESTURERECEIVED;
                finalShape.setPosition(currentShape.getPosition().x, currentShape.getPosition().y);
                timerClicked.cancel();
                break;
            case IDLE:
            default:
                break;
        }
    }

    private void selectShape(String objectType) {

        waitingForObject = true;
        currentShape.setType(ShapeType.getShapeType(objectType));

        switch (etat) {
            case GESTURERECEIVED:
                timerTalked = new Timer();
                timerTalked.schedule(timerTalkedEventHandler(), 5000);

                etat = Etats.TALKED;
                break;
            case CLICKED:
                etat = Etats.GESTURERECEIVED;
                selectionnerPoints();
                break;
            case IDLE:
            case TALKED:
            default:
                break;
        }
    }

    private void selectColorAt() {

        waitingForColor = true;

        switch (etat) {
            case GESTURERECEIVED:
                timerTalked = new Timer();
                timerTalked.schedule(timerTalkedEventHandler(), 5000);

                etat = Etats.TALKED;
                break;
            case CLICKED:
                etat = Etats.GESTURERECEIVED;
                selectionnerPoints();

                break;
            case IDLE:
            case TALKED:
            default:
                break;
        }
    }

    private void mousePressed(int x, int y) {

        switch (etat) {
            case TALKED:
                if (waitingForPos) {
                    if (action == Action.MOVE) {
                        //finalShape.setPosition(x - selectedInfos.getPosition().x, y - selectedInfos.getPosition().y);                            
                        finalShape.setPosition(x, y);
                    } else {
                        finalShape.setPosition(x, y);
                    }
                    waitingForPos = false;
                    etat = Etats.GESTURERECEIVED;
                    timerTalked.cancel();
                }

                if (waitingForObject || waitingForColor) {
                    currentShape.setPosition(x, y);
                    selectionnerPoints();
                }

                break;
            case GESTURERECEIVED:
                if (waitingForPos && action == action.MOVE) {
                    currentShape.setPosition(x - currentShape.getPosition().x, y - currentShape.getPosition().y);
                } else {
                    currentShape.setPosition(x, y);
                }

                timerClicked = new Timer();
                timerClicked.schedule(timerClickedEventHandler(), 5000);
                etat = Etats.CLICKED;
                break;
            case IDLE:
            case CLICKED:
            default:
                break;
        }
    }

    public void selectionnerPoints() {
        int x = 0, y = 0;
        if (currentShape.getPosition() != null) {
            x = currentShape.getPosition().x;
            y = currentShape.getPosition().y;
        }

        try {
            this.sendMsg("Palette:TesterPoint x=" + x + " y=" + y);
        } catch (IvyException ex) {
            Logger.getLogger(VocalRecognizerIvy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void resultatTesterPoint(String name) {
        if (waitingForColor || (waitingForObject && currentShape.getColor() != null)) {
            try {
                this.sendMsg("Palette:DemanderInfo nom=" + name);
            } catch (IvyException ex) {
                Logger.getLogger(VocalRecognizerIvy.class.getName()).log(Level.SEVERE, null, ex);
            }
            currentShape.setName(name);

        } else {
            finalShape.setName(name);
        }
    }

    private void resultatInfo(String name, String couleur, int x, int y) {
        if (waitingForColor) {
            finalShape.setColor(couleur);
        }

        if (waitingForObject && currentShape.getColor() != null && currentShape.getColor().equals(couleur.toUpperCase())) {
            finalShape.setName(name);
        }
    }

    public TimerTask timerTalkedEventHandler() {
        return new TimerTask() {

            @Override
            public void run() {
                switch (etat) {
                    case TALKED:
                        etat = Etats.GESTURERECEIVED;
                        waitingForPos = false;
                        waitingForObject = false;
                        waitingForColor = false;
                        break;
                    case CLICKED:
                    case GESTURERECEIVED:
                    case IDLE:
                        break;
                }
            }
        };
    }

    public TimerTask timerGestureReceivedEventHandler() {
        return new TimerTask() {
            @Override
            public void run() {
                switch (etat) {
                    case GESTURERECEIVED:
                    case CLICKED:
                    case TALKED:
                        actionFinal();
                        etat = Etats.IDLE;
                        break;
                    case IDLE:
                    default:
                        break;
                }
            }
        };

    }

    public TimerTask timerClickedEventHandler() {
        return new TimerTask() {

            @Override
            public void run() {
                switch (etat) {
                    case CLICKED:
                        etat = Etats.GESTURERECEIVED;
                        currentShape = new Shape();
                        break;
                    case TALKED:
                    case GESTURERECEIVED:
                    case IDLE:
                        break;
                }
            }
        };
    }

    public void actionFinal() {
        switch (action) {
            case RECTANGLE:
                action = Action.IDLE;
                dessinerRectangle();
                break;
            case CIRCLE:
                action = Action.IDLE;
                dessinerCercle();
                break;
            case MOVE:
                action = Action.IDLE;
                move();
                break;
            case DELETE:
                action = Action.IDLE;
                delete();
                break;
            case IDLE:
                //interdit
                break;
        }

        reset();
    }

    public void annuler() {
        action = Action.IDLE;
        etat = Etats.IDLE;
        timerGestureReceived.cancel();
        reset();
    }

    public void fini() {
        etat = Etats.IDLE;
        actionFinal();
    }

    public void dessinerRectangle() {
        int x = 0;
        int y = 0;
        String couleur = "WHITE";

        if (finalShape.getPosition() != null) {
            x = finalShape.getPosition().x;
            y = finalShape.getPosition().y;
        }

        if (finalShape.getColor() != null) {
            couleur = finalShape.getColor();
        }

        try {
            this.sendMsg("Palette:CreerRectangle x=" + x + " y=" + y
                    + " couleurFond=" + couleur + " couleurContour=BLACK");
        } catch (IvyException ex) {
            Logger.getLogger(VocalRecognizerIvy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void dessinerCercle() {
        int x = 0;
        int y = 0;
        String couleur = "WHITE";

        if (finalShape.getPosition() != null) {
            x = finalShape.getPosition().x;
            y = finalShape.getPosition().y;
        }

        if (finalShape.getColor() != null) {
            couleur = finalShape.getColor();
        }

        try {
            this.sendMsg("Palette:CreerEllipse x=" + x + " y=" + y
                    + " couleurFond=" + couleur + " couleurContour=BLACK");
        } catch (IvyException ex) {
            Logger.getLogger(VocalRecognizerIvy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void move() {
        if (finalShape.getName() != null) {
            int x = 0;
            int y = 0;

            if (finalShape.getPosition() != null) {
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

    public void delete() {
        if (finalShape.getName() != null) {
            try {
                this.sendMsg("Palette:SupprimerObjet nom=" + finalShape.getName());
            } catch (IvyException ex) {
                Logger.getLogger(VocalRecognizerIvy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void reset() {
        finalShape = new Shape();
        waitingForColor = false;
        waitingForPos = false;
        waitingForObject = false;
        currentShape = new Shape();
    }
}
