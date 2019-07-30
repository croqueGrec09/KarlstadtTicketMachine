package de.uni_koeln.webapps.gentz;

/**
 * This is for the ingame use.
 * This class contains number, name and map coordinates of a station.
 * 
 * @author agalffy
 *
 */
public class Station {

	private String id;
	private String name;
	private String[] coordinates;
	
	public Station(String id,String name,String[] coordinates) {
		this.id = id;
		this.name = name;
		this.coordinates = coordinates;
	}
	
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String[] getCoordinates() {
		return coordinates;
	}
	
}
