/****************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ****************************************************************************************/

package edu.northeastern.ccs.im.models;

import java.sql.Timestamp;

/**
 * Class to store conversational messages
 *
 * @author rahul
 */
public class ConversationalMessage {

    private String sourceName;
    private String destinationName;
    private String messageText;
    private Timestamp messageTimeStamp;
    private String messageUniquekey;
    private String groupUniqueKey;

    /**
     * @param sourceName       to have the username of the sender
     * @param destinationName  to have the username of the receiver
     * @param messageText      to have the text of the message
     * @param messageUniquekey to have the uniqueKey for the message
     */
    public ConversationalMessage(String sourceName, String destinationName, String messageText, Timestamp messageTimeStamp, String messageUniquekey) {
        this.sourceName = sourceName;
        this.destinationName = destinationName;
        this.messageText = messageText;
        this.messageTimeStamp = messageTimeStamp;
        this.messageUniquekey = messageUniquekey;
        this.groupUniqueKey = null;

    }

    /**
     * Gets the group unique key.
     *
     * @return the group unique key
     */
    public String getGroupUniqueKey() {
        return groupUniqueKey;
    }

    /**
     * Sets the group unique key.
     *
     * @param groupUniqueKey the new group unique key
     */
    public void setGroupUniqueKey(String groupUniqueKey) {
        this.groupUniqueKey = groupUniqueKey;
    }

    /**
     * @return username of the sender of the message
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * @return username of the receiver of the message
     */
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * @return text in the message
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * @return uniquekey assigned to the message
     */
    public String getMessageUniquekey() {
        return messageUniquekey;
    }

    /**
     * @return timestamp for the given message
     */
    public Timestamp getMessageTimeStamp() {
        return messageTimeStamp;
    }

    /**
     * @return the string representation of the msg
     */
    public String toString() {
        return this.getMessageUniquekey();
    }

}
