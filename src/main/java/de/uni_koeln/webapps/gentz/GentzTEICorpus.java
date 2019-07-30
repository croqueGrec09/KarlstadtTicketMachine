package de.uni_koeln.webapps.gentz;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.springframework.stereotype.Service;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Core class of the webapp. This is a parser which reads all the XML documents in the given #
 * directory and creates for each transcript a Java representation, filling its fields with 
 * the information retrieved by DOMXPath.
 * 
 * @author agalffy
 *
 */
@Service
public class GentzTEICorpus {
	
	//as of June 19th, 2017, there are no scans fulfilling the awaits of the Gentz research place
	//private String faksimiles = "css/faksimiles/";
	private Map<String,GentzLetter> teiCorpus;
	private Map<String,List<String>> preprintIndex = new TreeMap<String,List<String>>(), personIndex = new TreeMap<String,List<String>>(), placeIndex = new TreeMap<String,List<String>>(), citationIndex = new TreeMap<String,List<String>>();
	private Map<String,Citation> workIndex = new TreeMap<String,Citation>();
	private Map<String,GentzPerson> personReference = new TreeMap<String,GentzPerson>();
	private Map<String,GentzPlace> placeReference = new TreeMap<String,GentzPlace>();
	/**
	 * an index category -> list of letters
	 */
	private Map<String,Set<String>> categoryIndex = new TreeMap<String,Set<String>>();
	/**
	 * a bidirectional map category key <-> name
	 */
	private BidiMap<String,String> catEnum = new TreeBidiMap<String,String>();
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
	/**
	 * Builds the corpus of passed documents.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	@PostConstruct
	public void buildCorpus() {
		File transcriptionsDir = new File("gentz");
		System.out.println(transcriptionsDir.getAbsolutePath());
		teiCorpus = new HashMap<String,GentzLetter>();
		for(File xmlFile: transcriptionsDir.listFiles()) {
			if(xmlFile.getName().matches("LETTER_GENTZ_IDNO_[0-9]{4}[.]xml")){
				System.out.println("Now attempting to parse: "+xmlFile.getName());
				try {
					GentzLetter gl = parseDocument(xmlFile);
					teiCorpus.put(gl.getId(), gl);
				} catch (GentzLetterParseException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Adds a given XML file to the corpus.
	 * @param xmlFile - the {@link File} object pointing to the transcript file
	 */
	public void addFileToCorpus(File xmlFile) {
		System.out.println("New file added to index. Attempt of parse for: "+xmlFile.getName());
		try {
			GentzLetter gl = parseDocument(xmlFile);
			teiCorpus.put(gl.getId(), gl);
		} catch (GentzLetterParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes a letter with the given ID from the corpus. The method purges also the 
	 * indices in which the letter reference may have been.
	 * @param xmlId - the ID of the letter which should be deleted
	 */
	public void deleteFileFromCorpus(String xmlId) {
		System.out.println("File has been deleted from index. Deletion of index entries for: "+xmlId);
		if(teiCorpus.containsKey(xmlId)) {
			teiCorpus.remove(xmlId);
			System.out.println("Java object representation has been deleted");
		}
		for(String preprint: preprintIndex.keySet()) {
			List<String> preprints = preprintIndex.get(preprint);
			if(preprints.remove(xmlId))
				System.out.println("Preprint index entry from "+preprint+" removed");
			preprintIndex.put(preprint, preprints);
		}
		for(String person: personIndex.keySet()) {
			List<String> persons = personIndex.get(person);
			if(persons.remove(xmlId))
				System.out.println("Person index entry from "+person+" removed");
			personIndex.put(person, persons);
		}
		for(String place: placeIndex.keySet()) {
			List<String> places = placeIndex.get(place);
			if(places.remove(xmlId))
				System.out.println("Place index entry from "+place+" removed");
			placeIndex.put(place, places);
		}
		for(String citation: citationIndex.keySet()) {
			List<String> citations = citationIndex.get(citation);
			if(citations.remove(xmlId))
				System.out.println("Citation index entry from "+citation+" removed");
			citationIndex.put(citation, citations);
		}
	}
	
	/**
	 * Builds an DOMDocument of the transcript, traverses all the fields and builds a 
	 * {@link GentzLetter} object with the information extracted by DOMXPath from the 
	 * letter. When encountering an entity to which an index exists, the letter key is 
	 * added to this index. If the index entry did not exist so far, it will be created.
	 * 
	 * @param xmlFile - the File pointer to the transcript
	 * @return the Java object representation of the TEI transcript
	 * @throws GentzLetterParseException
	 */
	public GentzLetter parseDocument(File xmlFile) throws GentzLetterParseException {
		try {
			DocumentBuilder builder = dbf.newDocumentBuilder();
			InputStream is = new FileInputStream(xmlFile);
			Reader reader = new InputStreamReader(is,"UTF-8");
			InputSource source = new InputSource(reader);
			source.setEncoding("UTF-8");
			Document doc = builder.parse(source);
			Element fileDesc = (Element) query(doc,"//fileDesc").item(0);
			GentzLetter gl = new GentzLetter(fileDesc.getAttribute("xml:id"));
			gl.setTitle(query(fileDesc,"//titleStmt/title").item(0).getTextContent());
			gl.setContent(xmlFile);
			Element creation = (Element) query(doc,"//creation/date").item(0);
			LocalDate date = null;
			//some date values are incomplete
			if(creation.hasAttribute("when-iso")) {
				try {
					date = LocalDate.parse(creation.getAttribute("when-iso"), DateTimeFormatter.ISO_DATE);
				}
				catch (DateTimeParseException e) {
					if(creation.getAttribute("when-iso").contains("-00-00"))
						date = LocalDate.parse(creation.getAttribute("when-iso").replace("-00-00", "-07-01"));
					else if(creation.getAttribute("when-iso").contains("-00"))
						date = LocalDate.parse(creation.getAttribute("when-iso").replace("-00", "-15"));
				}
			}
			else if(creation.hasAttribute("notBefore-iso") && creation.hasAttribute("notAfter-iso")) {
				LocalDate start = null, end = null;
				try {
					start = LocalDate.parse(creation.getAttribute("notBefore-iso"), DateTimeFormatter.ISO_DATE);
					end = LocalDate.parse(creation.getAttribute("notAfter-iso"), DateTimeFormatter.ISO_DATE);
				}
				catch (DateTimeParseException e) {
					if(creation.getAttribute("notBefore-iso").contains("-00-00"))
						start = LocalDate.parse(creation.getAttribute("notBefore-iso").replace("-00-00", "-07-01"));
					else if(creation.getAttribute("notBefore-iso").contains("-00"))
						start = LocalDate.parse(creation.getAttribute("notBefore-iso").replace("-00", "-15"));
					if(creation.getAttribute("notAfter-iso").contains("-00-00"))
						end = LocalDate.parse(creation.getAttribute("notAfter-iso").replace("-00-00", "-07-01"));
					else if(creation.getAttribute("notAfter-iso").contains("-00"))
						end = LocalDate.parse(creation.getAttribute("notAfter-iso").replace("-00", "-15"));
				}
				long middle = (end.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()+start.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())/2;
				date = Instant.ofEpochMilli(middle).atZone(ZoneOffset.UTC).toLocalDate();
			}
			gl.setDate(date);
			gl.setDateString(creation.getTextContent());
			NodeList msDesc = query(doc,"//msIdentifier/child::*");
			for(int md = 0;md < msDesc.getLength();md++) {
				switch(msDesc.item(md).getNodeName()) {
				case "country": gl.setCountry(msDesc.item(md).getTextContent());
				break;
				case "settlement": gl.setSettlement(msDesc.item(md).getTextContent());
				break;
				case "institution": gl.setInstitution(msDesc.item(md).getTextContent());
				break;
				case "repository": gl.setRepository(msDesc.item(md).getTextContent());
				break;
				case "collection": gl.setCollection(msDesc.item(md).getTextContent());
				break;
				case "idno": gl.setMsIdno(msDesc.item(md).getTextContent());
				break;
				}
			}
			Element origCit = (Element) query(doc,"//publicationStmt/publisher/link").item(0);
			String origCitLink = origCit.getAttribute("target").split(" ")[1];
			gl.setOriginalCitationLink(origCitLink);
			Element idno = (Element) query(doc,"//publicationStmt/idno").item(0);
			String idnoGentzUB = idno.getAttribute("n");
			gl.setIdnoGentzUB(idnoGentzUB);
			Element summary = (Element) query(doc,"//summary").item(0);
			gl.setSummary(summary.getTextContent());
			StringBuilder sb = new StringBuilder();
			NodeList textNodes = query(doc,"//body//text()[normalize-space()]");
			for(int t = 0;t < textNodes.getLength();t++) {
				sb.append(textNodes.item(t).getTextContent().replaceAll(" {2,}", " "));
			}
			gl.setSearchableContent(sb.toString());
			NodeList keywordList = query(doc,"//keywords[@scheme='internal']/term");
			List<String> keywords = new ArrayList<String>();
			for(int k = 0;k < keywordList.getLength();k++) {
				keywords.add(keywordList.item(k).getTextContent());
			}
			gl.setKeywords(keywords);
			NodeList categoriesList = query(doc,"//keywords[@scheme='categorisation']/term");
			Map<String,String> categories = new TreeMap<String,String>();
			for(int c = 0;c < categoriesList.getLength();c++) {
				Element category = (Element) categoriesList.item(c);
				categories.put(category.getAttribute("xml:id"), category.getTextContent());
				Set<String> catEntries = null;
				if(categoryIndex.get(category.getAttribute("xml:id")) != null) {
					catEntries = categoryIndex.get(category.getAttribute("xml:id"));
				}
				else catEntries = new TreeSet<String>();
				catEntries.add(gl.getId());
				catEnum.put(category.getAttribute("xml:id"), category.getTextContent());
				categoryIndex.put(category.getAttribute("xml:id"), catEntries);
			}
			gl.setCategories(categories);
			NodeList ppList = query(doc,"//listBibl[@type='preprints']");
			if(ppList.getLength() > 0 && ppList.item(0).getChildNodes().getLength() > 0) {
				gl.setPreprints(processCitationList(ppList.item(0).getChildNodes()));
			}
			NodeList lRList = query(doc,"//listBibl[@type='letterReferenced']");
			if(lRList.getLength() > 0 && lRList.item(0).getChildNodes().getLength() > 0) {
				gl.setLetterReferenced(processCitationList(lRList.item(0).getChildNodes()));
			}
			NodeList aWList = query(doc,"//listBibl[@type='alludedWorks']");
			if(aWList.getLength() > 0 && aWList.item(0).getChildNodes().getLength() > 0) {
				gl.setAlludedWorks(processCitationList(aWList.item(0).getChildNodes()));
			}
			NodeList dEList = query(doc,"//listBibl[@type='defExpr']");
			if(dEList.getLength() > 0 && dEList.item(0).getChildNodes().getLength() > 0) {
				gl.setDefExpr(processCitationList(dEList.item(0).getChildNodes()));
			}
			NodeList dRList = query(doc,"//listBibl[@type='defRep']");
			if(dRList.getLength() > 0 && dRList.item(0).getChildNodes().getLength() > 0) {
				gl.setDefRep(processCitationList(dRList.item(0).getChildNodes()));
			}
			NodeList eRList = query(doc,"//listBibl[@type='edRef']");
			if(eRList.getLength() > 0 && eRList.item(0).getChildNodes().getLength() > 0) {
				gl.setEdRef(processCitationList(eRList.item(0).getChildNodes()));
			}
			for(Citation pp : gl.getPreprints()) {
				List<String> ind;
				if(preprintIndex.get(pp.getWorkTitle()) == null) {
					ind = new LinkedList<String>();
				}
				else ind = preprintIndex.get(pp.getWorkTitle());
				ind.add(gl.getId());
				preprintIndex.put(pp.getWorkTitle(), ind);
			}
			NodeList citRefs = query(doc,"//biblStruct/@corresp");
			if(citRefs.getLength() > 0) {
				for(int cr = 0;cr < citRefs.getLength();cr++) {
					String cit = citRefs.item(cr).getTextContent();
					List<String> ind;
					if(citationIndex.get(cit) == null) {
						ind = new LinkedList<String>();
					}
					else ind = citationIndex.get(cit);
					ind.add(gl.getId());
					citationIndex.put(cit, ind);
				}
			}
			NodeList persList = query(doc,"//listPerson/person");
			gl.setListPerson(processPersonList(persList));
			for(GentzEntity ge : gl.getListPerson().get()) {
				if(ge instanceof GentzPerson) {
					GentzPerson gp = (GentzPerson) ge;
					List<String> ind;
					if(personIndex.get(gp.getId()) == null) {
						ind = new LinkedList<String>();
					}
					else ind = personIndex.get(gp.getId());
					ind.add(gl.getId());
					personIndex.put(gp.getId(), ind);
				}
			}
			NodeList placeList = query(doc,"//listPlace/place");
			gl.setListPlace(processPlaceList(placeList));
			for(GentzEntity ge : gl.getListPlace().get()) {
				if(ge instanceof GentzPlace) {
					GentzPlace gpl = (GentzPlace) ge;
					List<String> ind;
					if(placeIndex.get(gpl.getPointerId()) == null) {
						ind = new LinkedList<String>();
					}
					else ind = placeIndex.get(gpl.getPointerId());
					ind.add(gl.getId());
					placeIndex.put(gpl.getPointerId(), ind);
				}
			}
			reader.close();
			is.close();
			return gl;
		}
		catch (DOMException | XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
			throw new GentzLetterParseException();
		}
	}

	/**
	 * If a transcript file has been changed, the field of its Java representation will be 
	 * updated as well.
	 */
	public void updateContent() {
		File transcriptionsDir = new File("gentz");
		for(File xmlFile: transcriptionsDir.listFiles()) {
			if(xmlFile.getName().matches("LETTER_GENTZ_IDNO_[0-9]{4}[.]xml")){
				System.out.println("Now attempting to update content: "+xmlFile.getName());
				String xmlId = xmlFile.getName().replace("_IDNO","").split(".xml")[0];
				GentzLetter gl = teiCorpus.get(xmlId);
				gl.setContent(xmlFile);
				teiCorpus.put(gl.getId(), gl);
			}
		}
	}
	
	/**
	 * Creates a list of citations from the given NodeList and updates the work index with 
	 * the entries found.
	 * 
	 * @param citList - a list to be processed
	 * @return a list of citation for the given letter
	 * @throws XPathExpressionException
	 */
	public List<Citation> processCitationList(NodeList citList) throws XPathExpressionException {
		List<Citation> ret = new LinkedList<Citation>();
		for(int c = 0; c < citList.getLength(); c++) {
			Citation cit = null;
			if(citList.item(c).getNodeName().equals("bibl")) {
				cit = new Citation();
				Element bibl = (Element) citList.item(c);
				if(bibl.getAttribute("corresp").equals("#LETTER_GENTZ_NNNN"))
					cit.setId("no link");
				else cit.setId(bibl.getAttribute("corresp"));
				cit.setPointerId(bibl.getAttribute("xml:id"));
				cit.setBibl(bibl.getTextContent());
			}
			else if(citList.item(c).getNodeName().equals("biblStruct")) {
				cit = new Citation();
				Element biblStruct = (Element) citList.item(c);
				if(biblStruct.getAttribute("corresp").equals("#LETTER_GENTZ_NNNN"))
					cit.setId("no link");
				else cit.setId(biblStruct.getAttribute("corresp"));
				cit.setPointerId(biblStruct.getAttribute("xml:id"));
				NodeList hasPtr = query(biblStruct,"ptr");
				if(hasPtr.getLength() > 0) {
					Element ptr = (Element) hasPtr.item(0);
					cit.setLink(ptr.getAttribute("target"));
				}
				NodeList authorList = query(biblStruct,"monogr/author/persName");
				if(authorList.getLength() > 0) {
					GentzEntityList al = processPersonList(authorList);
					cit.setAuthors(al.getBibliographicResponsibleParty("author"));
				}
				NodeList editorList = query(biblStruct,"monogr/editor[not(@resp)]/persName");
				if(editorList.getLength() > 0) {
					cit.setEditors(processPersonList(editorList).getBibliographicResponsibleParty("editor"));
				}
				NodeList translatorList = query(biblStruct,"monogr/editor[@resp='translator']/persName");
				if(translatorList.getLength() > 0) {
					cit.setTranslators(processPersonList(translatorList).getBibliographicResponsibleParty("translator"));
				}
				NodeList authorListAnaly = query(biblStruct,"analytic/author/persName");
				if(authorListAnaly.getLength() > 0) {
					cit.setAuthorAnaly(processPersonList(authorListAnaly).getBibliographicResponsibleParty("author"));
				}
				NodeList editorListAnaly = query(biblStruct,"analytic/editor[not(@resp)]/persName");
				if(editorListAnaly.getLength() > 0) {
					cit.setEditorAnaly(processPersonList(editorListAnaly).getBibliographicResponsibleParty("editor"));
				}
				NodeList translatorListAnaly = query(biblStruct,"analytic/editor[@resp='translator']/persName");
				if(translatorListAnaly.getLength() > 0) {
					cit.setTranslatorAnaly(processPersonList(translatorListAnaly).getBibliographicResponsibleParty("translator"));
				}
				cit.setWorkTitle(query(biblStruct,"monogr/title").item(0).getTextContent());
				NodeList titAnaly = query(biblStruct,"analytic/title");
				if(titAnaly.getLength() > 0)
					cit.setTitleAnaly(titAnaly.item(0).getTextContent());
				NodeList imprint = query(biblStruct,"monogr/imprint");
				//to be sure ...
				if(imprint.getLength() > 0) {
					NodeList imprintDetails = imprint.item(0).getChildNodes();
					String pubPlace = "", publisher = "", date = "";
					for(int i = 0;i < imprintDetails.getLength(); i++) {
						if(imprintDetails.item(i).getNodeType() == Node.ELEMENT_NODE) {
							Element detail = (Element) imprintDetails.item(i);
							switch(detail.getNodeName()) {
							case "pubPlace": pubPlace = detail.getTextContent();
								break;
							case "publisher": publisher = detail.getTextContent();
								break;
							case "date": date = detail.getTextContent();
								break;
							}
						}
					}
					if(!pubPlace.isEmpty())
						cit.setPubPlace(pubPlace);
					if(!publisher.isEmpty())
						cit.setPublisher(publisher);
					if(!date.isEmpty())
						cit.setDate(date);
				}
				NodeList biblScopes = query(biblStruct,"monogr/biblScope"); 
				if(biblScopes.getLength() > 0){
					for(int bs = 0;bs < biblScopes.getLength();bs++) {
						Element biblScope = (Element) biblScopes.item(bs);
						switch(biblScope.getAttribute("unit")) {
						case "page": String[] pageRange = {biblScope.getAttribute("from"),biblScope.getAttribute("to")};
							cit.setPageRange(pageRange);
							break;
						case "volume": cit.setVolume(biblScope.getAttribute("n"));
							break;
						case "issue": cit.setIssue(biblScope.getAttribute("n"));
							break;
						}
					}
				}
			}
			if(cit != null) {
				ret.add(cit);
				if(workIndex.get(cit.getId()) == null)
					workIndex.put(cit.getId(),cit);
			}
		}
		return ret;
	}
	
	/**
	 * Creates a list of persons of the given person list and updates the role indices for 
	 * the respective roles.
	 * 
	 * @param persList - the NodeList to be processed
	 * @return the entity list
	 */
	public GentzEntityList processPersonList(NodeList persList) {
		List<GentzEntity> list = new LinkedList<GentzEntity>();
		for(int pl = 0; pl < persList.getLength(); pl++) {
			//to prevent errors in transcription
			if(persList.item(pl).getChildNodes().getLength() > 0) {
				Element person = (Element) persList.item(pl);
				String role = null;
				NodeList nameParts = null;
				//distinguish between cases: entity under listPerson or under listBibl?
				if(person.hasAttribute("role")) {
					role = person.getAttribute("role");
					try {
						nameParts = query(person,"persName").item(0).getChildNodes();
					} catch (XPathExpressionException e) {
						e.printStackTrace();
					}
				}
				else if(person.getNodeName().equals("persName")) {
					Element ancestorNode = (Element) person.getParentNode();
					nameParts = person.getChildNodes();
					if(ancestorNode.hasAttribute("resp"))
						role = "translator";
					else
						role = ancestorNode.getNodeName();
				}
				String gnd = "", forename = "", surname = "", roleName = "", roleNameType = "";
				for(int p = 0; p < nameParts.getLength(); p++) {
					Node namePart = nameParts.item(p);
					switch(namePart.getNodeName()) {
					case "ref": Element ref = (Element) namePart;
						if(ref.hasAttribute("target"))
							gnd = ref.getAttribute("target");
						break;
					case "forename":
						forename = namePart.getTextContent();
						break;
					case "surname":
						surname = namePart.getTextContent();
						break;
					case "roleName": Element rn = (Element) namePart;
						roleName = rn.getTextContent();
						roleNameType = rn.getAttribute("type");
						break;
					}
				}
				GentzPerson gp;
				try {
					gp = new GentzPerson(person.getAttribute("xml:id"),forename,surname,role);
					if(!gnd.isEmpty())
						gp.setGndKey(gnd);
					if(!roleName.isEmpty())
						gp.setRoleName(roleName);
					if(!roleNameType.isEmpty())
						gp.setRoleNameType(roleNameType);
					personReference.put(person.getAttribute("xml:id"),gp);
					list.add(gp);
				} catch (RoleNameMissingException e) {
					System.err.println(person.getAttribute("xml:id")+" has no role name!");
					e.printStackTrace();
				}
			}
		}
		GentzEntityList ret = new GentzEntityList(list);
		return ret;
	}
	
	/**
	 * Creates a list of places of the given place list and updates the role indices for 
	 * the respective roles.
	 * 
	 * @param placeList - the NodeList to be processed
	 * @return the entity list
	 */
	public GentzEntityList processPlaceList(NodeList placeList) throws XPathExpressionException {
		List<GentzEntity> list = new LinkedList<GentzEntity>();
		for(int pl = 0;pl < placeList.getLength();pl++) {
			Element place = (Element) placeList.item(pl);
			NodeList cData = place.getElementsByTagName("country");
			if(cData.getLength() > 0) {
				Element country = (Element) cData.item(0);
				String cName = null, role = place.getAttribute("type"), cGND = "", pointerId = place.getAttribute("xml:id");
				for(int c = 0;c < country.getChildNodes().getLength();c++) {
					if(country.getChildNodes().item(c).getNodeType() == Node.ELEMENT_NODE) {
						switch(country.getChildNodes().item(c).getNodeName()) {
						case "ref": Element ref = (Element) country.getChildNodes().item(c);
						cGND = ref.getAttribute("target");
						break;
						case "placeName": cName = country.getChildNodes().item(c).getTextContent();
						break;
						}
					}
				}
				NodeList subord = query(place,"*[not(self::country)]");
				if(subord.getLength() > 0) {
					Element subPlace = (Element) subord.item(0);
					NodeList subPlaceData = subPlace.getChildNodes();
					String name = null, type = null, gnd = null;
					if(subPlace.getNodeName().equals("geogName")) {
						type = subPlace.getAttribute("type");
					}
					else {
						type = subPlace.getNodeName();
					}
					for(int so = 0;so < subPlaceData.getLength();so++) {
						if(subPlaceData.item(so).getNodeType() == Node.ELEMENT_NODE) {
							switch(subPlaceData.item(so).getNodeName()) {
							case "ref": Element ref = (Element) subPlaceData.item(so);
							gnd = ref.getAttribute("target");
							break;
							case "placeName": name = subPlaceData.item(so).getTextContent();
							break;
							}
						}
					}
					if(name != null) {
						GentzPlace gp = new GentzPlace(name,type,role,pointerId);
						GentzPlace cp = new GentzPlace(cName,"country",role,pointerId);
						cp.setGndKey(cGND);
						gp.setGndKey(gnd);
						gp.setCountry(cp);
						placeReference.put(pointerId, gp);
						list.add(gp);
					}
				}
				else if(cName != null){
					GentzPlace gp = new GentzPlace(cName,"country",role,pointerId);
					gp.setGndKey(cGND);
					placeReference.put(pointerId,gp);
					list.add(gp);
				}
			}
			else {
				Element gName = (Element) place.getElementsByTagName("geogName").item(0);
				String cName = null, role = place.getAttribute("type"), cGND = "", pointerId = place.getAttribute("xml:id");
				for(int c = 0;c < gName.getChildNodes().getLength();c++) {
					if(gName.getChildNodes().item(c).getNodeType() == Node.ELEMENT_NODE) {
						switch(gName.getChildNodes().item(c).getNodeName()) {
						case "ref": Element ref = (Element) gName.getChildNodes().item(c);
						cGND = ref.getAttribute("target");
						break;
						case "placeName": Element placeName = (Element) gName.getChildNodes().item(c);
							cName = placeName.getTextContent();
						break;
						}
					}
				}
				GentzPlace gp = new GentzPlace(cName,gName.getAttribute("type"),role,pointerId);
				gp.setGndKey(cGND);
				placeReference.put(pointerId, gp);
				list.add(gp);
			}
		}
		GentzEntityList ret = new GentzEntityList(list);
		return ret;
	}
	
//---------------------------------- helper method section ----------------------------------
	
	/**
	 * Triggers an XPath query.
	 * 
	 * @param node - the node to be queried
	 * @param queryPath - the query path
	 * @return a NodeList of results (in case of failure, this is an empty list)
	 * @throws XPathExpressionException
	 */
	public NodeList query(Node node, String queryPath) throws XPathExpressionException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		xpath.setNamespaceContext(new NamespaceContext(){

			@Override
			public String getNamespaceURI(String prefix) {
				if("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
		        return XMLConstants.NULL_NS_URI;
			}

			@Override
			public String getPrefix(String namespaceURI) {
				return null;
			}

			@Override
			public Iterator getPrefixes(String namespaceURI) {
				return null;
			}
			
		});
		XPathExpression expr = xpath.compile(queryPath);
		NodeList res = (NodeList) expr.evaluate(node, XPathConstants.NODESET);
		return res;
	}
	
	public Map<String,GentzLetter> getCorpus() {
		return teiCorpus;
	}
	
	public Map<String, List<String>> getPreprintIndex() {
		return preprintIndex;
	}

	public Map<String, List<String>> getPersonIndex() {
		return personIndex;
	}

	public Map<String, List<String>> getPlaceIndex() {
		return placeIndex;
	}

	public Map<String, List<String>> getCitationIndex() {
		return citationIndex;
	}
	
	public Map<String, Set<String>> getCategoryIndex() {
		return categoryIndex;
	}
	
	public BidiMap<String,String> getCatEnum() {
		return catEnum;
	}
	
	public Map<String, Citation> getWorkIndex() {
		return workIndex;
	}

	public Map<String, GentzPerson> getPersonReference() {
		return personReference;
	}

	public Map<String, GentzPlace> getPlaceReference() {
		return placeReference;
	}

	/**
	 * debug control method to check if all letters arrived
	 * @return letterList - complete list of all letters which arrived
	 */
	public List<GentzLetter> getLetters() {
		List<GentzLetter> letterList = new ArrayList<GentzLetter>();
		letterList.addAll(teiCorpus.values());
		return letterList;
	}
	
	/**
	 * debug control method to check if the index is filled
	 * @return an index
	 */
	public Map<String,List<String>> getIndex() {
		return citationIndex;
	}
	
}
