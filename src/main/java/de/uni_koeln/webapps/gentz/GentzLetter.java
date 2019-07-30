package de.uni_koeln.webapps.gentz;

import java.io.File;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A Java object representation of a letter transcript. It serves as a wrapper for the 
 * TEI-XML-elements, for the field documentations see 
 * <a href="http://www.tei-c.org/release/doc/tei-p5-doc/en/html/index.html">the TEI guidelines.</a>
 * 
 * @author agalffy
 *
 */
@Document
public class GentzLetter {

	/**
	 * internal letter ID
	 */
	@Id
	private String id;
	/**
	 * the original identifier number assigned by the Gentz research place
	 */
	private String idnoGentzUB;
	private String title;
	private long date;
	private LocalDate dateObj;
	private String dateString;
	private String summary;
	private String country;
	private String settlement;
	private String institution;
	private String repository;
	private String collection;
	private String msIdno;
	private String originalCitationLink;
	private File content;
	private String searchableContent;
	private List<String> keywords = new ArrayList<String>();
	private Map<String,String> categories = new TreeMap<String,String>();
	private List<Citation> preprints = new LinkedList<Citation>(), letterReferenced = new LinkedList<Citation>(), alludedWorks = new LinkedList<Citation>(), defExpr = new LinkedList<Citation>(), defRep = new LinkedList<Citation>(), edRef = new LinkedList<Citation>();
	private GentzEntityList listPerson;
	private GentzEntityList listPlace;
	//as of June 19th, 2017, the Gentz research place does not rate the current scans as presentable, so, there are no images presented.
	//private Map<String,String> faksimilePaths = new HashMap<String,String>();
	
	public GentzLetter(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public String getIdnoGentzUB() {
		return idnoGentzUB;
	}

	public void setIdnoGentzUB(String idnoGentzUB) {
		this.idnoGentzUB = idnoGentzUB;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public long getDate() {
		return date;
	}

	public void setDate(LocalDate dateObj) {
		this.dateObj = dateObj;
		date = dateObj.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
	}

	public LocalDate getDateObj() {
		return dateObj;
	}
	
	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getSettlement() {
		return settlement;
	}

	public void setSettlement(String settlement) {
		this.settlement = settlement;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getMsIdno() {
		return msIdno;
	}

	public void setMsIdno(String msIdno) {
		this.msIdno = msIdno;
	}

	public String getOriginalCitationLink() {
		return originalCitationLink;
	}

	public void setOriginalCitationLink(String originalCitationLink) {
		this.originalCitationLink = originalCitationLink;
	}

	public File getContent() {
		return content;
	}

	public void setContent(File content) {
		this.content = content;
	}
	
	public String getSearchableContent() {
		return searchableContent;
	}

	public void setSearchableContent(String searchableContent) {
		this.searchableContent = searchableContent;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public Map<String,String> getCategories() {
		return categories;
	}

	public void setCategories(Map<String,String> categories) {
		this.categories = categories;
	}

	public List<Citation> getPreprints() {
		return preprints;
	}

	public void setPreprints(List<Citation> preprints) {
		this.preprints = preprints;
	}

	public List<Citation> getLetterReferenced() {
		return letterReferenced;
	}

	public void setLetterReferenced(List<Citation> list) {
		this.letterReferenced = list;
	}

	public List<Citation> getAlludedWorks() {
		return alludedWorks;
	}

	public void setAlludedWorks(List<Citation> alludedWorks) {
		this.alludedWorks = alludedWorks;
	}

	public List<Citation> getDefExpr() {
		return defExpr;
	}

	public void setDefExpr(List<Citation> defExpr) {
		this.defExpr = defExpr;
	}

	public List<Citation> getDefRep() {
		return defRep;
	}

	public void setDefRep(List<Citation> defRep) {
		this.defRep = defRep;
	}

	public List<Citation> getEdRef() {
		return edRef;
	}

	public void setEdRef(List<Citation> edRef) {
		this.edRef = edRef;
	}

	public GentzEntityList getListPerson() {
		return listPerson;
	}

	public void setListPerson(GentzEntityList listPerson) {
		this.listPerson = listPerson;
	}

	public GentzEntityList getListPlace() {
		return listPlace;
	}

	public void setListPlace(GentzEntityList listPlace) {
		this.listPlace = listPlace;
	}

	/**
	 * Get the set for the given role
	 * @param role - the role requested
	 * @return a list of formatted entity names
	 */
	public String getPersonEnumeration(String role) {
		StringJoiner sj = new StringJoiner(", ");
		Set<GentzEntity> personSet = new HashSet<GentzEntity>();
		switch(role) {
		case "sender": personSet = listPerson.getSender();
		break;
		case "receiver": personSet = listPerson.getReceiver();
		break;
		case "named": personSet = listPerson.getNamed();	
		break;
		default:
			List<GentzPerson> personList = listPerson.getBibliographicResponsibleParty(role);
			for(GentzPerson gp: personList) {
				sj.add(gp.getFullName(true));
			}
			return sj.toString();
		}
		for(GentzEntity ge: personSet) {
			if(ge instanceof GentzPerson) {
				GentzPerson gp = (GentzPerson) ge;
				sj.add(gp.getFullName(true));
			}
		}
		return sj.toString();
	}
	
	/**
	 * Outputs the content. It expects as parameter the originator, i.e. the mode (ingame 
	 * call our external call) in which the stylesheet should work.
	 * 
	 * @param mode - PoD for ingame, KtM for outside of the game
	 * @return a HTML formatted string with the letter transcript
	 */
	public String outputContent(String mode) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory factory = TransformerFactory.newInstance();
			Source xslt = new StreamSource(new File("gentz/gentzLetter_v2.xsl"));
			Transformer transformer = factory.newTransformer(xslt);
			transformer.setParameter("mode", mode);
			Source xml = new StreamSource(content);
			transformer.transform(xml,new StreamResult(sw));
			return sw.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return "Transformation errors occured!";
	}
	
	/**
	 * Outputs the notes according to the given mode (ingame or outside game).
	 * 
	 * @param mode - PoD for an ingame call or KtM for the outside processing
	 * @return a HTML formatted string with the annotations
	 */
	public String outputNotes(String mode) {
		try {
			StringWriter sw = new StringWriter();
			Source notesXSL = new StreamSource(new File("gentz/gentzLetter_notes.xsl"));
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(notesXSL);
			transformer.setParameter("mode", mode);
			Source xml = new StreamSource(content);
			transformer.transform(xml, new StreamResult(sw));
			return sw.toString();
		}
		catch (TransformerException e) {
			e.printStackTrace();
		}
		return "Transformation errors occured!";
	}
	
	/*
	 * see above. Since no scans are provided yet, they have been taken off.
	public Map<String,String> getFaksimilePaths() {
		return faksimilePaths;
	}

	public void addFaksimilePath(String place, String faksimilePath) {
		faksimilePaths.put(place,faksimilePath);
	}
	*/
	
}
