package edu.northeastern.ccs.im.models;

import java.util.List;
import java.util.Set;

/**
 * The Group class depicts the concept of a group. 
 * @author - team 201 - Ketan Kale
 */
public class Group implements Member {
	

	/**
	 * Instantiates a new group.
	 *
	 * @param name, the name of the group
	 */
	public Group() {
		this.groupName = null;
		this.moderatorName = null;
	}
	
	/** The group name. */
	private String groupName;
	
	/** The moderator name. */
	private String moderatorName;
	
	/** The member users. */
	public Set<User> memberUsers;
	
	/** The member groups. */
	public Set<Group> memberGroups;
	
	/**
	 * Gets the group name.
	 *
	 * @return the group name
	 */
	public String getGroupName() {
		return this.groupName;
	}
	
	/**
	 * Sets the group name.
	 *
	 * @param name, the new group name
	 */
	public void setGroupName(String name) {
		this.groupName = name;
	}
	
	/**
	 * Gets the moderator name.
	 *
	 * @return the moderator name
	 */
	public String  getModeratorName() {
		return this.moderatorName;
	}
	
	/**
	 * Sets the moderator name.
	 *
	 * @param name, the new moderator name
	 */
	public void setModeratorName(String name) {
		this.moderatorName = name;
	}
}
