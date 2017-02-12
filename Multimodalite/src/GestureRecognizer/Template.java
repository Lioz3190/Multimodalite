
package GestureRecognizer;

import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.StringTokenizer;

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
    

    public double normalization(Stroke stroke_){
        
         double dist = 0.0;

        for(int i=0;i<stroke.size();i++)
        {
            Point2D.Double pTemplate = stroke.getPoint(i);
            Point2D.Double p = stroke_.getPoint(i);
            dist+=p.distance(pTemplate);
        }

        return dist;
    }
    
    public void write(PrintWriter outputTemplate)
    {
        outputTemplate.print(name + " ");
        for ( int i = 0 ; i < this.stroke.size() ; i++)
        {
            outputTemplate.print(String.valueOf(Math.round(this.stroke.getPoint(i).getX()))+":");
            outputTemplate.print(String.valueOf(Math.round(this.stroke.getPoint(i).getY()))+" ");
        }
        outputTemplate.close();
    }
    
    public static Template read(String line)
    {
        StringTokenizer tok = new StringTokenizer(line, " ");
        String name_ = tok.nextToken();
        Stroke stroke_ = new Stroke();
        while(tok.hasMoreTokens())
        {
            StringTokenizer tok2 = new StringTokenizer(tok.nextToken(), ":");
            stroke_.addPoint(new Point2D.Double(Double.parseDouble(tok2.nextToken()),Double.parseDouble(tok2.nextToken())));
        }
        return new Template(name_, stroke_);
    }
    
}
