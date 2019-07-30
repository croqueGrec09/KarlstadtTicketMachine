package de.uni_koeln.webapps.gentz;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Class to represent TEI bibl and biblStruct (references to further lecture) nodes.
 * It contains fields for identifiers (an internal, at pointerId and an external, for the 
 * linking beyond the letter), titles, authors, editors and translators and this both for 
 * a monograph and an analytic work. Publishing place, date and publisher are also recorded 
 * in this class. In case of an analytic work, page ranges, volumes and/or issues may be 
 * set as well.
 * 
 * This class contains also methods for formatting citations according to MLA style just as 
 * for assembling work metadata for a table view. Citations are also comparable and thus
 * implememting the {@link Comparable} interface.
 * 
 * @author agalffy
 * @version 0.701 from July 7th, 2018
 *
 */
public class Citation implements Comparable<Citation> {

	//these fields are mandatory
	//@corresp, this identifier is thus a link to some further lecture, if there is one!
	private String id;
	//this is the internal link in the letter itself and used only as pointer target!
	private String pointerId;
	private String link;
	//to set only if no detailled entry is available
	private String bibl;
	//otherwise
	private List<GentzPerson> authors = new LinkedList<GentzPerson>(), editors = new LinkedList<GentzPerson>(), translators = new LinkedList<GentzPerson>();
	private List<GentzPerson> authorAnaly = new LinkedList<GentzPerson>(), editorAnaly = new LinkedList<GentzPerson>(), translatorAnaly = new LinkedList<GentzPerson>();
	private String workTitle;
	private String titleAnaly;
	private String publisher;
	private String pubPlace;
	private String date;
	private String[] pageRange = new String[2];
	private String volume;
	private String issue;
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setPointerId(String pointerId) {
		this.pointerId = pointerId;
	}
	
	public String getPointerId() {
		return pointerId;
	}
	
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getBibl() {
		return bibl;
	}

	public void setBibl(String bibl) {
		this.bibl = bibl;
	}

	public List<GentzPerson> getAuthors() {
		return authors;
	}

	public void setAuthors(List<GentzPerson> authors) {
		this.authors = authors;
	}

	public List<GentzPerson> getEditors() {
		return editors;
	}

	public void setEditors(List<GentzPerson> editors) {
		this.editors = editors;
	}

	public List<GentzPerson> getTranslators() {
		return translators;
	}

	public void setTranslators(List<GentzPerson> translators) {
		this.translators = translators;
	}

	public List<GentzPerson> getAuthorAnaly() {
		return authorAnaly;
	}

	public void setAuthorAnaly(List<GentzPerson> authorAnaly) {
		this.authorAnaly = authorAnaly;
	}

	public List<GentzPerson> getEditorAnaly() {
		return editorAnaly;
	}

	public void setEditorAnaly(List<GentzPerson> editorAnaly) {
		this.editorAnaly = editorAnaly;
	}

	public List<GentzPerson> getTranslatorAnaly() {
		return translatorAnaly;
	}

	public void setTranslatorAnaly(List<GentzPerson> translatorAnaly) {
		this.translatorAnaly = translatorAnaly;
	}

	public String getWorkTitle() {
		return workTitle;
	}

	public void setWorkTitle(String workTitle) {
		this.workTitle = workTitle;
	}

	public String getTitleAnaly() {
		return titleAnaly;
	}

	public void setTitleAnaly(String titleAnaly) {
		this.titleAnaly = titleAnaly;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPubPlace() {
		return pubPlace;
	}

	public void setPubPlace(String pubPlace) {
		this.pubPlace = pubPlace;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String[] getPageRange() {
		return pageRange;
	}

	public void setPageRange(String[] pageRange) {
		this.pageRange = pageRange;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getIssue() {
		return issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	@Override
	public int compareTo(Citation arg0) {
		int ret = 0;
		ret = authors.get(0).getSurname().compareTo(arg0.getAuthors().get(0).getSurname());
		if(ret != 0)
			return ret;
		else 
			ret = authors.get(0).getForename().compareTo(arg0.getAuthors().get(0).getForename());
		if(ret != 0)
			return ret;
		else 
			ret = titleAnaly.compareTo(arg0.getTitleAnaly());
		if(ret != 0)
			return ret;
		else 
			return workTitle.compareTo(arg0.getWorkTitle());
	}
	
	/**
	 * This toString()-method prints a citation in human-readable way. Fields are 
	 * hard-coded in German. An internationalisation is not implemented yet.
	 * 
	 * @return a citation according to the MLA citation style.
	 */
	@Override
	public String toString() {
		String ret = "";
		if(bibl == null) {
			if(titleAnaly != null) {
				String authorString = "";
				String editorString = "";
				String translatorString = "";
				if(authorAnaly.size() > 2)
					authorString = authorAnaly.get(0).getFullName(true)+" et al. ";
				else if(authorAnaly.size() > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append(authorAnaly.get(0).getFullName(true));
					if(authorAnaly.size() == 2) {
						sb.append(", ");
						sb.append(authorAnaly.get(1).getFullName(false));
					}
					sb.append(". ");
					authorString = sb.toString();
				}
				//one never knows ...
				if(editorAnaly.size() > 2)
					editorString = editorAnaly.get(0).getFullName(true)+" et al. (eds.)";
				else if(editorAnaly.size() > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append(editorAnaly.get(0).getFullName(true));
					if(editorAnaly.size() == 2) {
						sb.append(", ");
						sb.append(editorAnaly.get(1).getFullName(false));
						sb.append(" (eds.). ");
					}
					else 
						sb.append(" (ed.). ");
					editorString = sb.toString();
				}
				if(translatorAnaly.size() > 2)
					translatorString = "Übersetzt von: "+translatorAnaly.get(0).getFullName(true)+" et al. ";
				else if(translatorAnaly.size() > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append("Übersetzt von: ");
					sb.append(translatorAnaly.get(0).getFullName(true));
					if(translatorAnaly.size() == 2) {
						sb.append(", ");
						sb.append(translatorAnaly.get(1).getFullName(false));
						sb.append(". ");
					}
					else 
						sb.append(". ");
					translatorString = sb.toString();
				}
				if(!editorString.isEmpty())
					ret = editorString+translatorString+' '+date+". "+'"'+titleAnaly+'"'+". ";
				else ret = authorString+translatorString+date+". "+'"'+titleAnaly+'"'+". ";
				authorString = "";
				editorString = "";
				translatorString = "";
				String pubPlaceString = "";
				String publisherString = "";
				String issueString = "";
				String volumeString = "";
				String pageRangeString = "";
				if(authors.size() > 2)
					authorString = authors.get(0).getFullName(true)+" et al. ";
				else if(authors.size() > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append(authors.get(0).getFullName(true));
					if(authors.size() == 2) {
						sb.append(", ");
						sb.append(authors.get(1).getFullName(false));
					}
					sb.append(". ");
					authorString = sb.toString();
				}
				if(editors.size() > 2)
					editorString = editors.get(0).getFullName(true)+" et al. (eds.)";
				else if(editors.size() > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append(editors.get(0).getFullName(true));
					if(editors.size() == 2) {
						sb.append(", ");
						sb.append(editors.get(1).getFullName(false));
						sb.append(" (eds.). ");
					}
					else 
						sb.append(" (ed.). ");
					editorString = sb.toString();
				}
				if(translators.size() > 2)
					translatorString = "Übersetzt von: "+translators.get(0).getFullName(true)+" et al. ";
				else if(translators.size() > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append("Übersetzt von: ");
					sb.append(translators.get(0).getFullName(true));
					if(translators.size() == 2) {
						sb.append(", ");
						sb.append(translators.get(1).getFullName(false));
						sb.append(". ");
					}
					else 
						sb.append(". ");
					translatorString = sb.toString();
				}
				if(pubPlace != null)
					pubPlaceString = pubPlace+": ";
				if(publisher != null)
					publisherString = publisher+". ";
				if(issue != null)
					issueString = ' '+issue+' ';
				if(volume != null)
					volumeString = '('+volume+')';
				if(pageRange.length > 0)
					pageRangeString = pageRange[0]+'-'+pageRange[1];
				if(!editorString.isEmpty())
					ret += editorString+translatorString+' '+workTitle+issueString+volumeString+". "+pubPlaceString+publisherString+pageRangeString;
				else ret += authorString+translatorString+' '+workTitle+issueString+volumeString+". "+pubPlaceString+publisherString+pageRangeString;
				return ret;
			}
			String authorString = "";
			String editorString = "";
			String translatorString = "";
			String pubPlaceString = "";
			String publisherString = "";
			String dateString = "";
			String issueString = "";
			String volumeString = "";
			String pageRangeString = "";
			if(authors.size() > 2)
				authorString = authors.get(0).getFullName(true)+" et al. ";
			else if(authors.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append(authors.get(0).getFullName(true));
				if(authors.size() == 2) {
					sb.append(", ");
					sb.append(authors.get(1).getFullName(false));
				}
				sb.append(". ");
				authorString = sb.toString();
			}
			if(editors.size() > 2)
				editorString = editors.get(0).getFullName(true)+" et al. (eds.)";
			else if(editors.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append(editors.get(0).getFullName(true));
				if(editors.size() == 2) {
					sb.append(", ");
					sb.append(editors.get(1).getFullName(false));
					sb.append(" (eds.). ");
				}
				else 
					sb.append(" (ed.). ");
				editorString = sb.toString();
			}
			if(translators.size() > 2)
				translatorString = "Übersetzt von: "+translators.get(0).getFullName(true)+" et al. ";
			else if(translators.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("Übersetzt von: ");
				sb.append(translators.get(0).getFullName(true));
				if(translators.size() == 2) {
					sb.append(", ");
					sb.append(translators.get(1).getFullName(false));
					sb.append(". ");
				}
				else 
					sb.append(". ");
				translatorString = sb.toString();
			}
			if(date != null)
				dateString = date+". ";
			if(pubPlace != null)
				pubPlaceString = pubPlace+": ";
			if(publisher != null)
				publisherString = publisher+". ";
			if(issue != null)
				issueString = ' '+issue+' ';
			if(volume != null)
				volumeString = '('+volume+')';
			if(pageRange.length > 0 && pageRange[0] != null)
				pageRangeString = pageRange[0]+'-'+pageRange[1];
			if(!editorString.isEmpty())
				ret = editorString+translatorString+dateString+' '+workTitle+issueString+volumeString+". "+pubPlaceString+publisherString+pageRangeString;
			else ret = authorString+translatorString+dateString+' '+workTitle+issueString+volumeString+". "+pubPlaceString+publisherString+pageRangeString;
		}
		else {
			ret = bibl;
		}
		return ret;
	}

	/**
	 * Formats for a given entitiy the appropriate data for output.
	 * 
	 * @param entity - the entity requested
	 * @return a concatenated string enumerating authors, editors or translators of the work 
	 */
	public String printEntityList(String entity) {
		StringJoiner sj = new StringJoiner("; ");
		List<GentzPerson> entityList = new LinkedList<GentzPerson>();
		String prefix = "", suffix = "";
		switch(entity) {
			case "authors": entityList = authors;
			break;
			case "editors": entityList = editors;
			suffix = " (ed(s).)";
			break;
			case "translators": entityList = translators;
			prefix = "trans. ";
			break;
			case "authorAnaly": entityList = authorAnaly;
			break;
			case "editorAnaly": entityList = editorAnaly;
			suffix = " (ed(s).)";
			break;
			case "translatorAnaly" : entityList = translatorAnaly;
			prefix = "trans. ";
			break;
		}
		for(GentzPerson gp: entityList) {
			sj.add(gp.getFullName(true));
		}
		return prefix+sj.toString()+suffix;
	}
	
}
