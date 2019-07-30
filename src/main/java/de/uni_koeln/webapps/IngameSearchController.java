 package de.uni_koeln.webapps;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.uni_koeln.webapps.gentz.GentzLetter;
import de.uni_koeln.webapps.gentz.GentzSearcherService;
import de.uni_koeln.webapps.gentz.GentzTEICorpus;

/**
 * 
 * This is the game-interior search controller, handling all search requests and returning 
 * a list view of the letters matching to the given search criteria.
 * 
 * There is another controller instance ({@link GentzSearchController}) which maps the 
 * retrieved results upon distinct template files. Moreover, that offers a quick search 
 * field.
 * 
 * @author András Gálffy - mailto: andrisgalffy@gmail.com, matricula number #5584124
 * @version 0.701 of July 7th, 2018
 */
@CrossOrigin
@Controller
@RequestMapping(value = "/PoD/search")
public class IngameSearchController {
	
	@Autowired
	GentzSearcherService gss;
	@Autowired
	GentzTEICorpus gts;
	
	/**
	 * Performs a search with all given parameters. They are OR-connected.
	 * 
	 * @param model - containing a result list of letters
	 * @return the result list fragment
	 */
	@RequestMapping(value = {"","/"}, method = RequestMethod.POST)
	public String getLettersByAll(
			@RequestParam(value = "sender", required = false) String sender,
			@RequestParam(value = "receiver", required = false) String receiver,
			@RequestParam(value = "person", required = false) String person,
			@RequestParam(value = "place", required = false) String place,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "category[]", required = false) String[] category,
			@RequestParam(value = "content", required = false) String content,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo,
			Model model){
		Map<String,String> searchParams = new HashMap<String,String>();
		/*
		 * check if field is set; put then value and check in GentzSearcherService if mapping 
		 * is set!
		 */
		searchParams.put("sender", sender);
		searchParams.put("receiver", receiver);
		searchParams.put("person", person);
		searchParams.put("place", place);
		searchParams.put("keyword", keyword);
		StringJoiner sj = new StringJoiner(",");
		if(category != null) {
			for(String catQuery: category) {
				sj.add(catQuery);
			}
		}
		searchParams.put("category", sj.toString());
		searchParams.put("content", content);
		//insert date limits
		if(dateFrom != null && !dateFrom.isEmpty()) {
			String[] dateElements = dateFrom.split("\\.");
			searchParams.put("dateFrom",dateElements[2]+"-"+dateElements[1]+"-"+dateElements[0]);
		}
		if(dateTo != null && !dateTo.isEmpty()) {
			String[] dateElements = dateTo.split("\\.");
			searchParams.put("dateTo",dateElements[2]+"-"+dateElements[1]+"-"+dateElements[0]);
		}
		Set<GentzLetter> result = new HashSet<GentzLetter>();
		try {
			result.addAll(gss.search(searchParams,true));
		}
		catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		model.addAttribute("letters",result);
		return "ingameFrags :: letterListTable";
	}
	
}
