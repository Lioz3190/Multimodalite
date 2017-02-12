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
    
    /**
     * Gets all the recognized templates
     * @return 
     */
    public ArrayList<Template> getListeTemplates() {
        return listeTemplates;
    }
    
    /**
     * Reads a given template
     * @param templatename
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void readTemplate(String templatename) throws IOException {
        System.out.println("Reading template " + templatename);
        BufferedReader in;
        
        in = new BufferedReader(new FileReader("./ressources/" + templatename));
           
    
        in.close();
    }

    /**
     * Creates a template file to stock with given Template and adds the given template to the list
     * @param templatename 
     * @param template 
     * @throws java.io.IOException 
     */
    public void writeTemplate(String templatename, Template template) throws IOException {
        System.out.println("Writing template " + templatename);
        
        File file = new File("ressources/" + templatename);
        
        if(!file.createNewFile()) {
            throw new IOException("Couldn't create file !");
        }
        
        listeTemplates.add(template);
    }
}
