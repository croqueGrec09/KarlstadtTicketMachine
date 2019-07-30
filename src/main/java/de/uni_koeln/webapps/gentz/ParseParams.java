package de.uni_koeln.webapps.gentz;

/**
 * Container class for the configuration parameters for the letter formatting.
 * Currently not used because there was no time to mark up the letters so diligently
 * 
 * @author agalffy
 *
 */
public class ParseParams {

	/**
	 * for the &lt;choice&gt;-Tag
	 * default: display regularised variant
	 */
	private boolean reg = true;
	/**
	 * default: display expanded variant
	 */
	private boolean expand = true;
	
	public boolean isReg() {
		return reg;
	}

	public void setReg(boolean reg) {
		this.reg = reg;
	}
	
	public boolean isExpand() {
		return expand;
	}

	public void setExpand(boolean expand) {
		this.expand = expand;
	}	
	
}
