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
    

    
    
}
