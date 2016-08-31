/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

public class ChildVClassesWithParentCustomLabels extends ChildVClassesWithParent   {

    private static final String LEFT_BLANK = "";
    private String removeLabel = ""; //remove this line from the label
    private Collator collator = null;
    public ChildVClassesWithParentCustomLabels(String classUri, String removeLabel, Collator collator) throws Exception {
        super(classUri);
        this.removeLabel = removeLabel;
        this.collator = collator;
    }
   
    public ChildVClassesWithParentCustomLabels setDefaultOption(String label){
        this.defaultOptionLabel = label;
        return this;
    }
    
    @Override
    public Map<String, String> getOptions(
            EditConfigurationVTwo editConfig, 
            String fieldName, 
            WebappDaoFactory wDaoFact) throws Exception {
        
      Map<String, String> optionsMap = super.getOptions(editConfig, fieldName, wDaoFact);
      //Remove the "remove label" portion 
      for(String key: optionsMap.keySet()) {
    	  String value = optionsMap.get(key);
    	  if(value.endsWith(removeLabel)) {
    		  value = value.substring(0, value.lastIndexOf(removeLabel) + 1).trim();
    	  }
    	  //If key is the superclass, replace "Other" with "Contribution"
    	  if(key.equals("http://bib.ld4l.org/ontology/Contribution"))
    		  value = "Contribution";
    	  //Replacing value
    	  optionsMap.put(key, value);
      }
      
      return optionsMap;
    }
    
    public Comparator<String[]> getCustomComparator() {
    	return new SuperClassAndChildrenComparator(collator, this.classUri);
    }
    
    //Sorts by the value of the 2nd element in each of the arrays except in the case of the super class
    //which appears first
    private static class SuperClassAndChildrenComparator implements Comparator<String[]> {
        
        private Collator collator;
        private String classURI;
        public SuperClassAndChildrenComparator(Collator collator, String classURI) {
            this.collator = collator;
            this.classURI = classURI;
        }
        public int compare (String[] s1, String[] s2) {
            if (s2 == null) {
                return 1;
            } else if (s1 == null) {
                return -1;
            } else {
            	if ("".equals(s1[0])) {
            		return -1;
            	} else if ("".equals(s2[0])) {
            		return 1;
            	}
                if (s2[1]==null) {
                    return 1;
                } else if (s1[1] == null){
                    return -1;
                } else {
                	//if s1[1] == the class uri, then always make this the first item
                	if(s1[1].equals(this.classURI)) 
                		return -1;
                    return collator.compare(s1[1],s2[1]);
                }
            }
        }
    }   

}
