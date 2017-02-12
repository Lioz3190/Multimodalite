/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GestureRecognizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author VALENTIN-WIN
 */
public final class TemplateRecognizer {
    private final ArrayList<Template> listeTemplates = new ArrayList<>();
    
    public TemplateRecognizer() throws IOException{
        this.readTemplate("circle.txt");
        this.readTemplate("rectangle.txt");
        this.readTemplate("croix.txt");
        this.readTemplate("barre.txt");
    }

    public ArrayList<Template> getListeTemplates() {
        return listeTemplates;
    }
    

    public void readTemplate(String templatename) throws IOException {
        BufferedReader in;
        in = new BufferedReader(new FileReader("./ressources/" + templatename));
        
        String str=in.readLine();
        if(!str.trim().equals("")) {
           listeTemplates.add(Template.read(str));
        }
    
        in.close();
    }


    public void writeTemplate(String templatename, Template template) throws IOException {
        File file = new File("ressources/" + templatename);
        
        if(!file.createNewFile()) {
            throw new IOException("Couldn't create file !");
        }
        template.write(new PrintWriter(file));
        listeTemplates.add(template);
    }
}
