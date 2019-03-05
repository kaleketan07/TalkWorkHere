package edu.northeastern.ccs.im.models;

import java.sql.Timestamp;

/**
 * Class to store conversational messages
 * @author rahul
 *
 */
public class ConversationalMessage {

	private String sourceName;
	private String destinationName;
	private String messageText;
	private Timestamp messageTimeStamp;
	private String messageUniquekey;
	
	/**
	 * 
	 * @param sourceName to have the username of the sender 
	 * @param destinationName to have the username of the receiver
	 * @param messageText to have the text of the message
	 * @param messageUniquekey to have the uniqueKey for the message
	 */
	public ConversationalMessage(String sourceName, String destinationName, String messageText, Timestamp messageTimeStamp, String messageUniquekey) {
		this.sourceName = sourceName;
		this.destinationName = destinationName;
		this.messageText = messageText;
		this.messageTimeStamp = messageTimeStamp;
		this.messageUniquekey = messageUniquekey;
		
	}
	
	/**
	 * 
	 * @return username of the sender of the message
	 */
	public String getSourceName() {
		return sourceName;
	}

	/**
	 * 
	 * @return username of the receiver of the message
	 */
	public String getDestinationName() {
		return destinationName;
	}
	
	/**
	 * 
	 * @return text in the message
	 */
	public String getMessageText() {
		return messageText;
	}
	
	/**
	 * 
	 * @return uniquekey assigned to the message
	 */
	public String getMessageUniquekey() {
		return messageUniquekey;
	}

	/**
	 * 
	 * @return timestamp for the given message
	 */
	public Timestamp getMessageTimeStamp() {
		return messageTimeStamp;
	}


}
