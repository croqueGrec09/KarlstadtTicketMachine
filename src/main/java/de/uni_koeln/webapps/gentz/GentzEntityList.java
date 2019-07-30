package de.uni_koeln.webapps.gentz;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class for retrieving different entity roles from their respective indices. See methods 
 * for details.
 * 
 * @author agalffy
 *
 */
public class GentzEntityList {

	/**
	 * A map, keeping sets of all entites mapped to their respective roles
	 */
	private Map<String,Set<GentzEntity>> map = new TreeMap<String,Set<GentzEntity>>();
	
	/**
	 * Constructor method for building up the map entry for each role.
	 * 
	 * @param list - the list of all entites
	 */
	public GentzEntityList(List<GentzEntity> list) {
		//loop the list
		for(GentzEntity gp: list) {
			//build up a set for the role if it is not defined already
			Set<GentzEntity> roleEntities = map.get(gp.getRole());
			if(roleEntities == null)
				roleEntities = new TreeSet<GentzEntity>();
			//add the current entity
			roleEntities.add(gp);
			//put the current set at the given entity 
			map.put(gp.getRole(), roleEntities);
		}
	}
	
	/**
	 * Get all senders of letters
	 * @return the set of senders or an empty set if there are no senders
	 */
	public Set<GentzEntity> getSender() {
		if(map.get("sender") != null)
			return map.get("sender");
		return new TreeSet<GentzEntity>();
	}
	
	/**
	 * Get all receivers of letters
	 * @return the set of receivers or an empty set if there are no receivers
	 */
	public Set<GentzEntity> getReceiver() {
		if(map.get("receiver") != null)
			return map.get("receiver");
		return new TreeSet<GentzEntity>();
	}
	
	/**
	 * Get all entities named in a letter
	 * @return the set of named entities or an empty set if there are no entites named
	 */
	public Set<GentzEntity> getNamed() {
		if(map.get("named") != null)
			return map.get("named");
		return new TreeSet<GentzEntity>();
	}
	
	/**
	 * Get all entites for a given bibliographic responsibility (e.g. editor)
	 * @param role - the requested role
	 * @return the list for the given role or an empty list if there are no such entities
	 */
	public List<GentzPerson> getBibliographicResponsibleParty(String role) {
		List<GentzPerson> ret = new LinkedList<GentzPerson>();
		for(GentzEntity ge: map.get(role)) {
			if(ge instanceof GentzPerson)
				ret.add((GentzPerson) ge);
		}
		return ret;
	}
	
	/**
	 * Get all senders, receivers and entities alluded in a letter
	 * @return the set of senders, receivers and alluded entities
	 */
	public Set<GentzEntity> get() {
		Set<GentzEntity> ret = new TreeSet<GentzEntity>();
		if(map.get("sender") != null)
			ret.addAll(map.get("sender"));
		if(map.get("receiver") != null)
			ret.addAll(map.get("receiver"));
		if(map.get("named") != null)
			ret.addAll(map.get("named"));
		return ret;
	}
	
}
