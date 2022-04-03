package de.uni_koeln.webapps;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.webapps.gentz.GentzIndexerService;
import de.uni_koeln.webapps.gentz.GentzLetter;
import de.uni_koeln.webapps.gentz.GentzTEICorpus;
import de.uni_koeln.webapps.gentz.TicketMachine;

/**
 * This class manipulates the Gentz module.
 * This MVC is part of "Power of Data"-the game, software part Karlstadt (detail map X b)
 * 
 * This controller instance handles requests made *inside* the webapp. Those are done by 
 * AJAX. There is another instance ({@link GentzController}) for the ingame request handling. 
 * The main differences are that there is the category <-> station mapping which is relevant 
 * only for inside the game, then, instead of distinct templates, template fragments are 
 * returned which are grouped in one template file. The formatted output will be inserted by 
 * jQuery into the #automatOutput container.
 * 
 * The current version is submitted with the master thesis of matr. number 5584124.
 * 
 * @version 0.701 of July 7th, 2018
 * @author András Gálffy - mailto: andrisgalffy@gmail.com, matricula number #55484124
 *
 */
@CrossOrigin
@Controller
@RequestMapping(value = "/PoD")
public class IngameController {

	@Autowired
	GentzTEICorpus gts;
	@Autowired
	GentzIndexerService gis;
	@Autowired
	TicketMachine tm;
	
	/**
	 * The home route.
	 * 
	 * @param model - gets one random letter from the corus
	 * @return the index fragment
	 */
	@RequestMapping(value = {"","/","/index","/home"})
	public String welcome(Model model) {
		return "ingameFrags :: mainTable";
	}
	
	/**
	 * Retrieves all the categories with their stations assigned. This is done in both 
	 * directions.
	 * 
	 * This request is made from inside the game only.
	 * 
	 * @param model containing both mappings category <-> station and station <-> category,
	 * then, their names.
	 * @return the category list fragment
	 */
	@RequestMapping(value = "/catList")
	public String getAllCats(Model model) {
		model.addAttribute("categories", tm.getMappingCatStat());
		model.addAttribute("catNames", gts.getCatEnum());
		model.addAttribute("stations", tm.getMappingStatCat());
		model.addAttribute("statNames", tm.getStatList());
		return "ingameFrags :: catList";
	}
	
	/**
	 * Retrieves for a given category the assigned stations. Additionally, for each station 
	 * assigned to the category, other categories are retrieved as well to display also the 
	 * inverse mapping as an answer to the question "Where to get from where I am?"
	 * 
	 * This request is made from within the game only.
	 * 
	 * @param cat - the requested category
	 * @param model containing the given category with its letters, the category names, the 
	 * stations assigned to the category, the station names and the categories mapped to the 
	 * retrieved stations
	 * @return the single category list fragment
	 */
	@RequestMapping(value = "/stationsByCat/{cat}")
	public String getStationsByCategory(@PathVariable("cat") String cat, Model model) {
		model.addAttribute("category", gts.getCatEnum().get(cat));
		model.addAttribute("catNames", gts.getCatEnum());
		//this is the query result
		Set<Integer> stations = tm.getMappingCatStat().get(cat);
		model.addAttribute("stations", stations);
		model.addAttribute("statNames", tm.getStatList());
		//this is its inverse - find other stations
		Map<Integer,Set<String>> inverseStatCat = new TreeMap<Integer,Set<String>>();
		for(Integer stat: stations) {
			Set<String> intersect = tm.getMappingStatCat().get(stat);
			intersect.remove(cat);
			inverseStatCat.put(stat, intersect);
		}
		model.addAttribute("inverseStatCat", inverseStatCat);
		List<GentzLetter> res = new LinkedList<GentzLetter>();
		for(String key: gts.getCategoryIndex().get(cat)) {
			res.add(gts.getCorpus().get(key));
		}
		model.addAttribute("letters", res);
		return "ingameFrags :: catListSingle";
	}
	
	/**
	 * Route for listing all letters in the corpus, without any filtering.
	 * 
	 * @param model - gets all letters in the corpus
	 * @return the list fragment
	 */
	@RequestMapping(value = "/list")
	public String allLetters(Model model) {
		List<GentzLetter> allLetters = gts.getLetters();
		model.addAttribute("letters",allLetters);
		return "ingameFrags :: letterListTable";
	}
	
	/**
	 * Retrieves a letter with the given ID. If it contains a hashtag, it will be chopped.
	 * 
	 * @param id - the requested letter
	 * @param model - containing the object representation, formatted body and notes output, 
	 * sender and receiver objects.
	 * @return the single letter fragment
	 * @see GentzLetter
	 */
	@RequestMapping(value = "/letter/{id}")
	public String singleLetter(@PathVariable("id") String id,Model model) {
		GentzLetter letter = null;
		if(id.contains("#"))
			letter = gts.getCorpus().get(id.substring(1));
		else
			letter = gts.getCorpus().get(id);
		model.addAttribute("letter",letter);
		model.addAttribute("content", letter.outputContent("PoD"));
		model.addAttribute("notes", letter.outputNotes("PoD"));
		model.addAttribute("sender",letter.getPersonEnumeration("sender"));
		model.addAttribute("receiver",letter.getPersonEnumeration("receiver"));
		return "ingameFrags :: singleLetter";
	}

	/**
	 * Route for listing all the entities with their linked letters.
	 * 
	 * @param model - gets here the works, persons and places with their respective indices
	 * @return the entity list fragment
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
		return "ingameFrags :: entityList";
	}

	/**
	 * Route for getting a work with a given ID.
	 * 
	 * @param id - the requested work
	 * @param model - containing the work data and all letters assigned to that work
	 * @return the work fragment
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
		return "ingameFrags :: worksView";
	}
	
	/**
	 * Route for getting a person with a given ID.
	 * 
	 * @param id - the requested person
	 * @param model - containing data about the requested person and all letters assigned 
	 * to the person
	 * @return the entity fragment
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
		return "ingameFrags :: entityView";
	}
	
	/**
	 * Route for getting a place with a given ID.
	 * 
	 * @param id - the requested place
	 * @param model - containing data about the requested place and all letters assigned to the place
	 * @return the entity fragment
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
		return "ingameFrags :: entityView";
	}
	
	/**
	 * Returns the search form.
	 * 
	 * @param model - containing all categories
	 * @return the search form fragment
	 */
	@RequestMapping(value = "/searchForm")
	public String searchForm(Model model) {
		model.addAttribute("categories", gts.getCatEnum());
		return "ingameFrags :: search";
	}
	
	/**
	 * debug control method
	 * @return a JSON-object of all categories and their respective IDs
	 */
	@ResponseBody
	@RequestMapping(value = "/catEnum",produces = "application/json")
	public Map<String,String> printAllCats() {
		Map<String,String> ret = new TreeMap<String,String>();
		for(String key: gts.getCatEnum().keySet()) {
			ret.put(key,gts.getCatEnum().get(key));
		}
		return ret;
	}
	
}
