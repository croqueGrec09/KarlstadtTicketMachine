package de.uni_koeln.webapps.gentz;

/**
 * This class represents the &lt;person&gt; elements of a TEI transcript.
 * It implements the {@link Comparable} interface to make persons comparable to their name.
 * 
 * @author agalffy
 * @see <a href="http://www.tei-c.org/release/doc/tei-p5-doc/en/html/ref-person.html">the TEI specification for the &lt;person&gt;-element</a>
 */
public class GentzPerson implements GentzEntity, Comparable<GentzPerson> {

	/**
	 * id, forname, surname and role are mandatory
	 */
	private String id;
	private String forename;
	private String surname;
	private String role;
	/**
	 * roleName, roleNameType, gndKey and pointerId are optional
	 */
	private String roleName;
	private String roleNameType;
	private String gndKey;
	private String pointerId;
	
	public GentzPerson(String id, String forename, String surname, String role) throws RoleNameMissingException {
		this.id = id;
		this.forename = forename;
		this.surname = surname;
		if(role == null)
			throw new RoleNameMissingException();
		else
			this.role = role;
	}

	public String getId() {
		return id;
	}
	
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleNameType() {
		return roleNameType;
	}

	public void setRoleNameType(String roleNameType) {
		this.roleNameType = roleNameType;
	}

	public String getGndKey() {
		return gndKey;
	}

	public void setGndKey(String gndKey) {
		this.gndKey = gndKey;
	}

	public String getForename() {
		return forename;
	}

	public String getSurname() {
		return surname;
	}

	public String getRole() {
		return role;
	}

	public String getPointerId() {
		return pointerId;
	}

	public void setPointerId(String pointerId) {
		this.pointerId = pointerId;
	}

	/**
	 * Prints the full name.
	 * 
	 * @param lastNameFirst - should the surname precede the first name?
	 * @return the formatted name
	 */
	public String getFullName(boolean lastNameFirst) {
		if(lastNameFirst)
			return surname+", "+forename;
		else return forename+" "+surname;
	}
	
	@Override
	public int compareTo(GentzPerson arg0) {
		int cmp = arg0.getSurname().compareTo(surname);
		if(cmp == 0) {
			return arg0.getForename().compareTo(forename);
		}
		return cmp;
	}
	
}
