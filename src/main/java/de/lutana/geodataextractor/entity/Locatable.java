package de.lutana.geodataextractor.entity;

/**
 * An object that can be located by the user (get and set location).
 * 
 * @author Matthias Mohr
 */
public abstract class Locatable implements Located {
	
	protected Location location;

	@Override
	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}