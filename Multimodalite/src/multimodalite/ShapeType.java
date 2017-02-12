/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multimodalite;

/**
 * Represents a Shape Type as an enumeration
 * @author Lioz
 */ 
public enum ShapeType {
    RECTANGLE ("rectangle"),
    ELLIPSE ("ellipse");
    
    private final String name;
    
    ShapeType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static ShapeType getShapeType(String name) {
        ShapeType toReturn = null;
        
        switch(name) {
            case "rectangle" :
                toReturn = RECTANGLE;
                break;
            case "ellipse" :
                toReturn = ELLIPSE;
                break;
        }
        
        return toReturn;
    }
}
