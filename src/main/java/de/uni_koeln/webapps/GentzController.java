package de.uni_koeln.webapps;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.webapps.gentz.GentzEntity;
import de.uni_koeln.webapps.gentz.GentzIndexerService;
import de.uni_koeln.webapps.gentz.GentzLetter;
import de.uni_koeln.webapps.gentz.GentzPlace;
import de.uni_koeln.webapps.gentz.GentzTEICorpus;

/**
 * This class manipulates the Gentz module.
 * This MVC is part of "Power of Data"-the game, soft ware part Karlstadt (detail map X b)
 * 
 * The MVC is the software logic of a Karlstadt ticket machine: it navigates in the TEI-XML-
 * encoded letters of Friedrich von Gentz and retrieves letters and enriched metadata.
 *
 * This version of the webapp is configured for *local* use; on studiodesessais.org, you 
 * find a running version of this code. The only differences are on the link generation.
 * 
 * This controller instance handles requests made *outside* the webapp. There is another 
 * instance ({@link IngameController}) for the ingame request handling.
 * 
 * The current version is submitted with the master thesis of matr. number 5584124.
 * 
 * @version 0.701 of July 7th, 2018
 * @author András Gálffy - mailto: andrisgalffy@gmail.com, matricula number #55484124
 *
 */
@Controller
@RequestMapping(value = "/KtM")
public class GentzController {

	@Autowired
	GentzTEICorpus gts;
	@Autowired
	GentzIndexerService gis;
	
	/**
	 * The home route.
	 * 
	 * @param model - gets one random letter from the corus
	 * @return the index view
	 */
	@RequestMapping(value = {"","/","/index","/home"})
	public String welcome(Model model) {
		int rnd = (int) Math.floor(Math.random()*gts.getLetters().size());
		model.addAttribute("letter", gts.getLetters().get(rnd));
		/*
		 * is left in as an adaptation for mobile devices
		if(device.isMobile())
			//return "gentzHomeMobile";
			mobileTest();
		else if(device.isTablet())
			return "gentzHomeTablet";
		*/
		return "gentzHome";
	}
	
	/**
	 * This is a request stub for distincting different device types. Should be used to 
	 * generate different views for different devices. Not implemented for the MA thesis.
	 * 
	 * @param device - the device which made the request
	 * @return A string which reveals the device originating the request
	 */
	@RequestMapping("/detect-device")
	@ResponseBody
	public String mobileTest(Device device) {
		String deviceType = "unknown";
        if (device.isNormal()) {
            deviceType = "normal";
        } else if (device.isMobile()) {
            deviceType = "mobile";
        } else if (device.isTablet()) {
            deviceType = "tablet";
        }
        return "Hello " + deviceType + " browser!";
	}
	
	/**
	 * Route for listing all letters in the corpus, without any filtering.
	 * 
	 * @param model - gets all letters in the corpus
	 * @return the list view
	 */
	@RequestMapping(value = "/list")
	public String allLetters(Model model) {
		List<GentzLetter> allLetters = gts.getLetters();
		model.addAttribute("letters",allLetters);
		return "gentzList";
	}
	
	/**
	 * Route for listing all the entities with their linked letters.
	 * 
	 * @param model - gets here the works, persons and places with their respective indices
	 * @return the entity list view
	 */
	@RequestMapping(value = "/entity")
	public String allEntities(Model model) {
		model.addAttribute("works", gts.getWorkIndex());
		model.addAttribute("workIndex", gts.getCitationIndex());
		model.addAttribute("persons", gts.getPersonReference());
		model.addAttribute("personIndex", gts.getPersonIndex());
		model.addAttribute("places", gts.getPlaceReference());
		model.addAttribute("placeIndex", gts.getPlaceIndex());
		model.addAttribute("corpus", gts.getCorpus());
		return "gentzEntityList";
	}
	
	/**
	 * Route for getting a work with a given ID.
	 * 
	 * @param id - the requested work
	 * @param model - containing the work data and all letters assigned to that work
	 * @return the work view
	 */
	@RequestMapping(value = "/fetchWork/{id}")
	public String fetchWork(@PathVariable("id") String id, Model model) {
		List<GentzLetter> res = new LinkedList<GentzLetter>();
		for(String letterId : gts.getCitationIndex().get(id)) {
			GentzLetter letter = gts.getCorpus().get(letterId);
			res.add(letter);
		}
		model.addAttribute("work",gts.getWorkIndex().get(id));
		model.addAttribute("letters", res);
		return "gentzWork";
	}
	
	/**
	 * Route for getting a person with a given ID.
	 * 
	 * @param id - the requested person
	 * @param model - containing data about the requested person and all letters assigned to the person
	 * @return the entity view
	 */
	@RequestMapping(value = "/fetchPerson/{id}")
	public String fetchPerson(@PathVariable("id") String id, Model model) {
		List<GentzLetter> res = new LinkedList<GentzLetter>();
		for(String letterId : gts.getPersonIndex().get(id)) {
			GentzLetter letter = gts.getCorpus().get(letterId);
			res.add(letter);
		}
		model.addAttribute("person",gts.getPersonReference().get(id));
		model.addAttribute("letters", res);
		return "gentzEntity";
	}
	
	/**
	 * Route for getting a place with a given ID.
	 * 
	 * @param id - the requested place
	 * @param model - containing data about the requested place and all letters assigned to the place
	 * @return the entity view
	 */
	@RequestMapping(value = "/fetchPlace/{id}")
	public String fetchPlace(@PathVariable("id") String id, Model model) {
		List<GentzLetter> res = new LinkedList<GentzLetter>();
		for(String letterId : gts.getPlaceIndex().get(id)) {
			GentzLetter letter = gts.getCorpus().get(letterId);
			res.add(letter);
		}
		model.addAttribute("place",gts.getPlaceReference().get(id));
		model.addAttribute("letters", res);
		return "gentzEntity";
	}
	
	/**
	 * Retrieves a letter with the given ID. If it contains a hashtag, it will be chopped.
	 * 
	 * @param id - the requested letter
	 * @param model - containing the object representation, formatted body and notes output, 
	 * sender and receiver objects.
	 * @return the single letter view
	 * @see {@link GentzLetter}, {@link GentzEntity}
	 */
	@RequestMapping(value = "/letter/{id}")
	public String singleLetter(@PathVariable("id") String id,Model model) {
		GentzLetter letter = null;
		if(id.contains("#"))
			letter = gts.getCorpus().get(id.substring(1));
		else
			letter = gts.getCorpus().get(id);
		model.addAttribute("letter",letter);
		model.addAttribute("content", letter.outputContent("KtM"));
		model.addAttribute("notes", letter.outputNotes("KtM"));
		model.addAttribute("sender",letter.getPersonEnumeration("sender"));
		model.addAttribute("receiver",letter.getPersonEnumeration("receiver"));
		return "singleLetter";
	}
	
	/**
	 * Returns the search form.
	 * 
	 * @param model - containing all categories
	 * @return the search form view
	 */
	@RequestMapping(value = "/searchForm")
	public String searchForm(Model model) {
		model.addAttribute("categories", gts.getCatEnum());
		return "gentzSearch";
	}
	
	/**
	 * Returns the metadata view.
	 * 
	 * @param model containing all letters of the corpus
	 * @return the metadata view
	 */
	@RequestMapping(value = "/metadata")
	public String metadata(Model model) {
		List<GentzLetter> allLetters = gts.getLetters();
		model.addAttribute("letters", allLetters);
		return "gentzMetadata";
	}
	
	/**
	 * debug control method
	 * @return a list of all places as plain text
	 */
	@ResponseBody
	@RequestMapping(value = "/allPlaces")
	public String printAllPlaces(){
		List<GentzLetter> allLetters = gts.getLetters();
		StringBuilder sb = new StringBuilder();
		for(GentzLetter letter: allLetters){
			for(GentzEntity ge: letter.getListPlace().get()){
				if(ge instanceof GentzPlace) {
					GentzPlace place = (GentzPlace) ge;
					sb.append(place.toString());
					sb.append("\n");
				}
			}
		}
		return sb.toString();
	}

	/** 
	 * debug control method
	 * @return a list of an index dump as plain text
	 */
	@ResponseBody
	@RequestMapping(value = "/testIndex")
	public String printAllFaksimiles(){
		StringBuilder sb = new StringBuilder();
		for(String paths : gts.getIndex().keySet()){
			sb.append(paths+": ");
			for(String path: gts.getIndex().get(paths)){
				sb.append(path);
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
}
