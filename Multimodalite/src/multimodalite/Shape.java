/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multimodalite;

import java.awt.Point;

/**
 * Represents a shape to draw (can be ellipse or rectangle)
 * @author Lioz
 */
public class Shape {
    
    private String name;
    private Point position;
    private String color;
    private ShapeType type;
    
    public Shape (){
        color = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Point getPosition() {
        return position;
    }
    
    public void setPosition(int x, int y) {
        this.position = new Point(x, y);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public ShapeType getType() {
        return type;
    }

    public void setType(ShapeType type) {
        this.type = type;
    }
}
