package de.uni_koeln.webapps.gentz;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This service keeps an index of the stations and categories for the use from within the
 * game.
 * 
 * @author agalffy
 *
 */
@Service
public class TicketMachine {

	@Autowired
	GentzTEICorpus gts;
	private String mappingPath = "gentz/karlstadtLetterMapping.xml";
	private String townPath = "E:/htdocs/Keleti_pu/analysyscities/karlstadt.xml";
	private String townPrefix;
	private Map<String,Set<Integer>> mappingCatStat = new TreeMap<String,Set<Integer>>();
	private Map<Integer,Set<String>> mappingStatCat = new TreeMap<Integer,Set<String>>();
	private Map<Integer,Station> stations = new TreeMap<Integer,Station>();
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	/**
	 * Builds the station-category index, its inverse and the station repository for the 
	 * machine, using the information from the given town path and station mapping file.
	 * 
	 * @throws MappingCreationException
	 */
	@PostConstruct
	public void init() throws MappingCreationException {
		try {
			File mappingXML = new File(mappingPath);
			DocumentBuilder builder = dbf.newDocumentBuilder();
			InputStream is = new FileInputStream(mappingXML);
			Reader reader = new InputStreamReader(is,"UTF-8");
			InputSource source = new InputSource(reader);
			source.setEncoding("UTF-8");
			Document doc = builder.parse(is);
			townPrefix = query(doc,"//@idPrefix").item(0).getTextContent();
			NodeList termRefs = query(doc,"//termRef");
			for(int n = 0;n < termRefs.getLength();n++) {
				if(termRefs.item(n).getNodeType() == Node.ELEMENT_NODE) {
					Element termRef = (Element) termRefs.item(n);
					String key = termRef.getAttribute("key");
					NodeList values = termRef.getChildNodes();
					for(int c = 0;c < values.getLength();c++) {
						if(values.item(c).getNodeName().equals("value")) {
							Element valNode = (Element) values.item(c);
							Integer value = Integer.parseInt(valNode.getTextContent());
							Set<Integer> valSet;
							//direction category -> stations
							if(mappingCatStat.get(key) == null)
								valSet = new TreeSet<Integer>();
							else valSet = mappingCatStat.get(key);
							valSet.add(value);
							mappingCatStat.put(key,valSet);
							//inverse direction station -> categories
							Set<String> keySet;
							if(mappingStatCat.get(value) == null)
								keySet = new TreeSet<String>();
							else keySet = mappingStatCat.get(value);
							keySet.add(key);
							mappingStatCat.put(value, keySet);
						}
					}
				}
			}
			reader.close();
			File townFile = new File(townPath);
			builder = dbf.newDocumentBuilder();
			is = new FileInputStream(townFile);
			reader = new InputStreamReader(is,"UTF-8");
			source = new InputSource(reader);
			source.setEncoding("UTF-8");
			doc = builder.parse(is);
			for(Integer statRef: mappingStatCat.keySet()) {
				String statNumber = townPrefix+statRef;
				Element statNode = (Element) query(doc,"//station[@number='"+statNumber+"']").item(0);
				String name = statNode.getElementsByTagName("name").item(0).getTextContent();
				String[] coords = new String[2];
				coords[0] = statNode.getAttribute("mapPointX");
				coords[1] = statNode.getAttribute("mapPointY");
				Station station = new Station(statNumber,name,coords);
				stations.put(statRef, station);
			}
		}
		catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
			throw new MappingCreationException();
		}
	}
	
	public Map<String,Set<Integer>> getMappingCatStat() {
		return mappingCatStat;
	}
	
	public Map<Integer, Set<String>> getMappingStatCat() {
		return mappingStatCat;
	}
	
	public Map<Integer, Station> getStatList() {
		return stations;
	}
	
//---------------------------------- helper method section ----------------------------------

	/**
	 * Processes a query upon the given node.
	 * 
	 * @param node - the node to be queried
	 * @param queryPath - the query path
	 * @return the NodeList with the results - in case of failure, this is an empty list
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
	
}
