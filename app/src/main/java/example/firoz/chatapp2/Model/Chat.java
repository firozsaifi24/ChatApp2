package example.firoz.chatapp2.Model;

import java.util.ArrayList;

public class Chat {
    private String sender, receiver, message;
    private boolean isseen;
    private boolean ismedia;
    private Media media;
    private long messageTime;

    public Chat() {
    }

    public Chat(String sender, String receiver, String message, boolean isseen, boolean ismedia, Media media, long messageTime) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.ismedia = ismedia;
        this.media = media;
        this.messageTime = messageTime;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public boolean isIsmedia() {
        return ismedia;
    }

    public void setIsmedia(boolean ismedia) {
        this.ismedia = ismedia;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
