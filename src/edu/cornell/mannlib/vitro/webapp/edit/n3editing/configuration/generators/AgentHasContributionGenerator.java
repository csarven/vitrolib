package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.AntiXssValidation;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils.EditMode;
import edu.cornell.mannlib.vitro.webapp.utils.generators.EditModeUtils;

/** DRAFT VERSION - DO NOT USE! **/

public class AgentHasContributionGenerator extends BaseEditConfigurationGenerator implements EditConfigurationGenerator {

    private Log log = LogFactory.getLog(AgentHasContributionGenerator.class);
    private static String template = "agentHasContribution.ftl";
    final static String ld4l = "http://bib.ld4l.org/ontology/";
    final static String madsrdf = "http://www.loc.gov/mads/rdf/v1#";
    final static String prov = "http://www.w3.org/ns/prov#";
    final static String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
    
    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
            HttpSession session) throws Exception {
            
        EditConfigurationVTwo conf = new EditConfigurationVTwo();
                
        initBasics(conf, vreq);
        initPropertyParameters(vreq, session, conf);
        initObjectPropForm(conf, vreq);               
        
        conf.setTemplate(template);
        
        conf.setVarNameForSubject("agent");
        conf.setVarNameForPredicate("contributedTo");
        conf.setVarNameForObject("contribution");
                
        conf.setN3Required( Arrays.asList( n3ForNewContribution, n3ForNewWork, n3ForNewTitle, workLabelAssertion, titleLabelAssertion ) );
        conf.setN3Optional(Arrays.asList( n3ForExistingContribution, n3ForExistingWork, n3ForExistingTitle ) );
        
        conf.addNewResource("contribution", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("work",DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("title", DEFAULT_NS_FOR_NEW_RESOURCE);
        
        // uris in scope: none   
        // literals in scope: none
        
        // Should include contribution type, but ignoring for now
        conf.setUrisOnform( Arrays.asList( "existingContribution", "existingWork", "existingTitle"));  
        conf.setLiteralsOnForm( Arrays.asList(/* "contributionLabel", */ "workLabel", "titleLabel"));

        conf.addSparqlForExistingLiteral("contributionLabel", contributionLabelQuery);
        conf.addSparqlForExistingLiteral("workLabel", workLabelQuery);
        conf.addSparqlForExistingLiteral("titleLabel", contributionLabelQuery);

        conf.addSparqlForExistingUris("existingContribution", existingContributionQuery);
        conf.addSparqlForExistingUris("existingWork", existingWorkQuery);
        conf.addSparqlForExistingUris("existingTitle", existingTitleQuery);
                        
        conf.addField( new FieldVTwo().
                setName("titleLabel").
                setRangeDatatypeUri(XSD.xstring.toString() ));
        
        // Future: select field for contribution type

        // Add validator
        conf.addValidator(new AntiXssValidation());
        
        // Adding additional data, specifically edit mode
        addFormSpecificData(conf, vreq);
        prepare(vreq, conf);
        return conf;
    }
    
    /* N3 assertions for adding a work contribution to an agent */
    
    final static String n3ForNewContribution =       
        "@prefix ld4l: <"+ ld4l +"> .\n" +
        "@prefix rdfs: <"+ rdfs +">  . \n"+
        "?agent   ld4l:isAgentOf ?contribution .\n" +
        "?contribution  a ld4l:Contribution .\n" +
        "?contribution rdfs:label ?contributionLabel . \n" ;

    final static String n3ForNewWork  =      
        "@prefix ld4l: <"+ ld4l +"> .\n" +
        "@prefix rdfs: <"+ rdfs +">  . \n"+
        "?contribution ld4l:contributedTo ?work . \n" +
        "?work a ld4l:Work . \n" +
        "?work rdfs:label ?workLabel . ";
    
    final static String n3ForNewTitle  =      
        "@prefix ld4l: <"+ ld4l +"> .\n" +
        "@prefix rdfs: <"+ rdfs +">  . \n"+
        "@prefix madsrdf: <" + madsrdf +">  . \n"+
        "?work ld4l:hasTitle ?title . \n" +
        "?title a madsrdf:Title . \n" +
        "?title rdfs:label ?titleLabel . ";

    final static String n3ForExistingContribution =    
        "@prefix ld4l: <"+ ld4l +"> .\n" +
        "@prefix prov: <"+ prov +"> .\n" +
        "?agent ld4l:isAgentOf ?existingContribution . \n" +
        "?existingContribution prov:agent ?agent . \n" +
        " ";
    
    final static String n3ForExistingWork =  
        "@prefix ld4l: <"+ ld4l +"> .\n" +
        "?contribution ld4l:contributedTo ?existingWork . \n" +
        "?existingWork ld4l:hasContribution ?contribution . \n" +
        " ";
    
    final static String n3ForExistingTitle =      
        "@prefix ld4l: <"+ ld4l +"> .\n" +
        "@prefix madsrdf: <" + madsrdf +">  . \n"+
        "?work madsrdf:hasTitle ?existingTitle . \n" +
        "?existingTitle ld4l:isTitleOf ?work . \n" +
        " ";
    
    final static String contributionLabelAssertion  =      
        "@prefix rdfs: <"+ rdfs +">  . \n"+
        "?contribution rdfs:label ?contributionLabel" +
        " ";                
    
    final static String workLabelAssertion  =      
        "@prefix rdfs: <"+ rdfs +">  . \n"+
        "?work rdfs:label ?workLabel" +
        " ";              

    final static String titleLabelAssertion  =      
        "@prefix rdfs: <"+ rdfs +">  . \n"+
        "?title rdfs:label ?titleLabel" +
        " ";   
    
    /* Queries for editing an existing work contribution */

    final static String existingContributionQuery =    
        "PREFIX ld4l: <" + ld4l +">\n" +
        "SELECT ?existingContribution WHERE {\n"+
        "?agent ld4l:isAgentOf ?existingContribution .\n" +
        "?existingContribution a ld4l:Contribution .\n" +
        " }";                

    final static String contributionLabelQuery  =      
        "PREFIX rdfs: <"+ rdfs +">   \n"+
        "PREFIX ld4l: <" + ld4l +">\n" +
        "SELECT ?existingContribution WHERE {\n"+
        "?agent ld4l:isAgentOf ?existingContribution .\n" +
        "?existingContribution a ld4l:Contribution .\n" +
        "?existingContribution rdfs:label ?existingContributionLabel .\n" +
        " }";   
    
    final static String existingWorkQuery  =      
        "PREFIX ld4l: <"+ ld4l +">\n" +
        "SELECT ?existingWork WHERE {\n"+
        "?contribution ld4l:contributedTo ?existingWork .\n" +
        "?existingWork a ld4l:Work .\n" +
        " }";    

    final static String workLabelQuery  =      
        "PREFIX ld4l: <"+ ld4l +">\n" +
        "PREFIX rdfs: <"+ rdfs + ">\n" +
        "SELECT ?existingWork WHERE {\n"+
        "?contribution ld4l:contributedTo ?existingWork .\n" +
        "?existingWork a ld4l:Work .\n" +
        "?existingWork rdfs:label ?existingWorkLabel . \n" +
        " }";  
    
    final static String existingTitleQuery  =      
        "PREFIX ld4l: <"+ ld4l +">\n" +
        "PREFIX madsrdf: <" + madsrdf + ">\n" +
        "SELECT ?existingTitle WHERE {\n"+
        "?work madsrdf:hasTitle ?existingTitle .\n" +
        "?existingTitle a madsrdf:Title .\n" +
        " }";    

    final static String titleLabelQuery  =      
        "PREFIX ld4l: <"+ ld4l +">\n" +
        "PREFIX rdfs: <"+ rdfs +">\n" +
        "PREFIX madsrdf: <" + madsrdf + ">\n" +
        "SELECT ?existingTitle WHERE {\n"+
        "?work madsrdf:hasTitle ?existingTitle .\n" +
        "?existingTitle a madsrdf:Title .\n" +
        "?existingtitle rdfs:label ?existingTitleLabel . \n" +
        " }";    
    
    // Adding form specific data such as edit mode
    public void addFormSpecificData(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
        HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
        formSpecificData.put("editMode", getEditMode(vreq).name().toLowerCase());
        editConfiguration.setFormSpecificData(formSpecificData);
    }
    
    public EditMode getEditMode(VitroRequest vreq) {
        List<String> predicates = new ArrayList<String>();
        predicates.add(ld4l + "isAgentOf");
        return EditModeUtils.getEditMode(vreq, predicates);
    }
}
