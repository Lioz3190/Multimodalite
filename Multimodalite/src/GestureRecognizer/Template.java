/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GestureRecognizer;

import java.awt.geom.Point2D;

/**
 *
 * @author Lioz
 */
public class Template {
    String name;
    Stroke stroke;
    
    public Template ( String name_, Stroke stroke_){
        this.name = name_;
        this.stroke = stroke_;
    }

    public String getName() {
        return name;
    }

    public Stroke getStroke() {
        return stroke;
    }
    
    /**
     * return the distance between the actual stroke on the 
     * screen and the equivalent stroke in the template
     * @param stroke_ 
     */
    public double normalization(Stroke stroke_){
        double distance = 0;
        for (int i = 0; i < this.stroke.size() ; i++){
            distance = stroke_.getPoint(i).distance(this.stroke.getPoint(i));
        }
        return distance;      
    }
    
    /**
     * Creates a template file to stock with given Template and adds the given template to the list
     * @param templatename 
     * @param template 
     * @throws java.io.IOException 
     */
    
    
    
    
}
