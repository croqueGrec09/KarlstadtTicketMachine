package de.uni_koeln.webapps.gentz;

/**
 * This class represents a TEI &lt;place&gt;-element.
 * It implements the {@link Comparable} interface to make the places comparable to their 
 * name.
 * 
 * @author agalffy
 * @see <a href="http://www.tei-c.org/release/doc/tei-p5-doc/en/html/ref-place.html">the TEI specification for the &lt;place&gt;-element</a>
 *
 */
public class GentzPlace implements GentzEntity, Comparable<GentzPlace>{

	/**
	 * These fields are mandatory
	 */
	private String placeName;
	private String placeType;
	private PlaceTypeGer placeTypeGer;
	private String role;
	private String pointerId;
	/**
	 * These are optional
	 */
	private String gndKey;
	private GentzPlace country;
	
	public GentzPlace(String placeName, String placeType, String role, String pointerId) {
		this.placeName = placeName;
		this.placeType = placeType;
		switch(placeType) {
		case "country": placeTypeGer = PlaceTypeGer.Land;
		break;
		case "settlement": placeTypeGer = PlaceTypeGer.Stadt;
		break;
		case "region": placeTypeGer = PlaceTypeGer.Region;
		break;
		case "locality": placeTypeGer = PlaceTypeGer.Ortschaft;
		break;
		case "river": placeTypeGer = PlaceTypeGer.Fluss;
		break;
		case "lake": placeTypeGer = PlaceTypeGer.See;
		break;
		case "fortress": placeTypeGer = PlaceTypeGer.Festung;
		break;
		case "island": placeTypeGer = PlaceTypeGer.Insel;
		break;
		case "continent": placeTypeGer = PlaceTypeGer.Kontinent;
		break;
		default: System.out.println(placeType+" - not documented!");
		break;
		}
		this.role = role;
		this.pointerId = pointerId;
	}
	
	public String getGndKey() {
		return gndKey;
	}
	
	public void setGndKey(String gndKey) {
		this.gndKey = gndKey;
	}
	
	public String getPlaceName() {
		return placeName;
	}
	
	public String getPlaceType() {
		return placeType;
	}
	
	public String getRole() {
		return role;
	}
	
	public String getPointerId() {
		return pointerId;
	}
	
	public GentzPlace getCountry() {
		return country;
	}
	
	public void setCountry(GentzPlace country) {
		this.country = country;
	}
	
	public String printPlaceType(String lang) {
		switch(lang) {
		case "ger":
			return placeTypeGer.toString();
		}
		return placeType;
	}
	
	@Override
	public String toString() {
		return role+": "+placeType+": "+placeName+" ("+gndKey+")";
	}

	@Override
	public int compareTo(GentzPlace arg0) {
		return arg0.getPlaceName().compareTo(placeName);
	}
	
}
