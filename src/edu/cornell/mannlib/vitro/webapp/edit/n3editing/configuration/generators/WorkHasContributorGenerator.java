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
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.ChildVClassesWithParent;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.ChildVClassesWithParentCustomLabels;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.AntiXssValidation;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils.EditMode;
import edu.cornell.mannlib.vitro.webapp.utils.generators.EditModeUtils;

/** DRAFT VERSION - DO NOT USE! **/

public class WorkHasContributorGenerator extends BaseEditConfigurationGenerator implements EditConfigurationGenerator {

    private Log log = LogFactory.getLog(WorkHasContributorGenerator.class);
    private static String template = "workHasContributor.ftl";
    final static String ld4l = "http://bib.ld4l.org/ontology/";
    final static String madsrdf = "http://www.loc.gov/mads/rdf/v1#";
    final static String prov = "http://www.w3.org/ns/prov#";
    final static String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
    
    final static String contributionClass = ld4l + "Contribution";
    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
            HttpSession session) throws Exception {
            
        EditConfigurationVTwo conf = new EditConfigurationVTwo();
                
        initBasics(conf, vreq);
        initPropertyParameters(vreq, session, conf);
        initObjectPropForm(conf, vreq);               
        
        conf.setTemplate(template);
        
        conf.setVarNameForSubject("agent");
        conf.setVarNameForPredicate("predicate");
        conf.setVarNameForObject("contribution");
                
        conf.setN3Required( Arrays.asList( n3ForContribution, n3ForWorkToContribution, n3ForContributionToAgent ) );
        conf.setN3Optional(Arrays.asList(  n3ForNewAgent) );
        
        conf.addNewResource("contribution", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("work",DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("agent", DEFAULT_NS_FOR_NEW_RESOURCE);

        // uris in scope: none   
        // literals in scope: none
        
        // Should include contribution type, but ignoring for now
        //There could be an existing WORK URI but would need to create new contribution URI for that
        //
        conf.setUrisOnform( Arrays.asList( "work", "contributorRoleType", "contribution"));  
        conf.setLiteralsOnForm( Arrays.asList("contributionLabel",  "agentName"));

        conf.addSparqlForExistingLiteral("contributionLabel", contributionLabelQuery);
        conf.addSparqlForExistingUris("contribution", existingContributionQuery);
        //conf.addSparqlForExistingUris("contributorRoleType", existingContributorRoleTypeQuery);

       //I don't think we need an existing title, do we ?
                
        //Add fields
        conf.addField( new FieldVTwo().
                setName("workLabel").
                setRangeDatatypeUri(XSD.xstring.toString() ));
        conf.addField( new FieldVTwo().
                setName("contributionLabel").
                setRangeDatatypeUri(XSD.xstring.toString() )); //This should be a hidden field, based on type selected from drop-down
        
        conf.addField( new FieldVTwo().                        
                setName("contributionType").
                setValidators( list("nonempty") ).
                setOptions( new ChildVClassesWithParentCustomLabels(contributionClass, vreq.getCollator()))
                );
        // Future: select field for contribution type

        // Add validator
        conf.addValidator(new AntiXssValidation());
        
        // Adding additional data, specifically edit mode
        addFormSpecificData(conf, vreq);
        prepare(vreq, conf);
        return conf;
    }
    
    /* N3 assertions for adding a work contribution to an agent */
    
    /* New contribution always created, even for existing work */
    final static String n3ForContribution =       
        "@prefix ld4l: <"+ ld4l +"> .\n" +
        "@prefix rdfs: <"+ rdfs +">  . \n"+
        "@prefix prov: <"+ prov +">  . \n"+
        "?contribution ld4l:contributedTo ?work .\n " +
        "?contribution prov:agent ?agent .\n " +
        "?contribution  a ld4l:Contribution .\n" +
        "?contribution  a ?contributionType .\n" +
        "?contribution rdfs:label ?contributionLabel . \n" ;

    //Work and contribution relationships that will always be created
    //Since a new contribution is always generated
    final static String n3ForWorkToContribution  =      
        "@prefix ld4l: <"+ ld4l +"> .\n" +
        "@prefix rdfs: <"+ rdfs +">  . \n"+
        "?work ld4l:hasContribution ?contribution . \n";
    
    final static String n3ForContributionToAgent  =      
            "@prefix ld4l: <"+ ld4l +"> .\n" +
            "@prefix rdfs: <"+ rdfs +">  . \n"+
            "?contribution ld4l:contributedTo ?work . \n" +
            "?work ld4l:hasContribution ?contribution . \n";
    
    //If new work is being created
    final static String n3ForNewAgent =
	   "@prefix ld4l: <"+ ld4l +"> .\n" +
        "@prefix rdfs: <"+ rdfs +">  . \n"+
        "?work a ld4l:Work . \n" +
        "?work rdfs:label ?workLabel . ";
    
    final static String n3ForNewTitle  =      
        "@prefix ld4l: <"+ ld4l +"> .\n" +
        "@prefix rdfs: <"+ rdfs +">  . \n"+
        "@prefix madsrdf: <" + madsrdf +">  . \n"+
        "@prefix dcterms: <http://purl.org/dc/terms/> . \n" +
        "?work ld4l:hasTitle ?title . \n" +
        "?title a madsrdf:Title . \n" +
        "?title ld4l:isTitleOf ?work . \n" +
        "?title rdfs:label ?workLabel . \n" +
        "?title dcterms:hasPart ?titleElement . \n" +  
    	"?titleElement a madsrdf:MainTitleElement. \n" +
    	"?titleElement rdfs:label ?workLabel. \n" + 
    	"?titleElement dcterms:isPartOf ?title. \n"; 

    
    
    //use the same variable since these will always be the same
    //No "existing" contributions
    /*
    final static String n3ForExistingContribution =    
        "@prefix ld4l: <"+ ld4l +"> .\n" +
        "@prefix prov: <"+ prov +"> .\n" +
        "?agent ld4l:isAgentOf ?existingContribution . \n" +
        "?existingContribution prov:agent ?agent . \n" +
        " ";
    
    */
    final static String n3ForExistingWork =  
        "@prefix ld4l: <"+ ld4l +"> .\n" +
        "?contribution ld4l:contributedTo ?existingWork . \n" +
        "?existingWork ld4l:hasContribution ?contribution . \n" +
        " ";
    //Don't need this because title would already exist
    /*
    final static String n3ForExistingTitle =      
        "@prefix ld4l: <"+ ld4l +"> .\n" +
        "@prefix madsrdf: <" + madsrdf +">  . \n"+
        "?work madsrdf:hasTitle ?existingTitle . \n" +
        "?existingTitle ld4l:isTitleOf ?work . \n" +
        " ";
   */ 
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
