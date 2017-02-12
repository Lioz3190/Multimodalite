/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GestureRecognizer;

import fr.dgac.ivy.IvyException;
import java.io.IOException;

/**
 *
 * @author VALENTIN-WIN
 */
public class GestureRecognizer {
    
    private final TemplateRecognizer templateRecognizer = new TemplateRecognizer();
    
    private final GestureRecognizerIvy ivybus;
    
    public GestureRecognizer () throws IvyException{
     ivybus = new GestureRecognizerIvy();   
    }
    
    
    private void recognizeTemplate() {
        double distance = 100000.;
        Template recognized = new Template("Recognized", null);

        for(Template t : templateRecognizer.getListeTemplates()) {
            t.getStroke().normalize();
            
            double tempdistance = 100000.;
            try {
                tempdistance = t.normalization(ivybus.getStroke());
            } catch(IndexOutOfBoundsException ex) {}
            
            if(tempdistance < distance) {
                distance = t.normalization(ivybus.getStroke());
                recognized = t; 
            }
        }

        if(!recognized.getName().equals("Recognized")) {       
            ivybus.envoyerMessage(recognized.getName());
        }
    }
    

    private void writeTemplate() throws IOException {
        Template toAdd = new Template("circle", ivybus.getStroke());
        templateRecognizer.writeTemplate("circle.txt", toAdd);
        
    }
    
}
