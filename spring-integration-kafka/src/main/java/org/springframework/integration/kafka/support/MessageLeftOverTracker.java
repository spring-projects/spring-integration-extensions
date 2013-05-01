package org.springframework.integration.kafka.support;

import kafka.message.MessageAndMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Soby Chacko
 */
public class MessageLeftOverTracker {

    private List<MessageAndMetadata> messageLeftOverFromPreviousPoll = new ArrayList<MessageAndMetadata>();

    public void addMessageAndMetadata(final MessageAndMetadata messageAndMetadata){
        messageLeftOverFromPreviousPoll.add(messageAndMetadata);
    }

    public List<MessageAndMetadata> getMessageLeftOverFromPreviousPoll(){
        return messageLeftOverFromPreviousPoll;
    }

    public void clearMessagesLeftOver(){
        messageLeftOverFromPreviousPoll.clear();
    }

    public int getCurrentCount() {
        return messageLeftOverFromPreviousPoll.size();
    }

}
