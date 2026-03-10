package com.smartbite.models;

/**
 * Model representing a single chat message in the Bitey chatbot.
 * Must be saved at: app/src/main/java/com/smartbite/models/ChatMessage.java
 */
public class ChatMessage {

    private String  message;
    private boolean isUser;
    private boolean isTyping;
    private long    timestamp;

    /** Required by Firebase / serialization. */
    public ChatMessage() {}

    /** Standard constructor — isTyping defaults to false. */
    public ChatMessage(String message, boolean isUser, long timestamp) {
        this.message   = message;
        this.isUser    = isUser;
        this.timestamp = timestamp;
        this.isTyping  = false;
    }

    /** Full constructor — used for the typing indicator bubble. */
    public ChatMessage(String message, boolean isUser, long timestamp, boolean isTyping) {
        this.message   = message;
        this.isUser    = isUser;
        this.timestamp = timestamp;
        this.isTyping  = isTyping;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String  getMessage()          { return message;   }
    public void    setMessage(String v)  { message = v;      }

    public boolean isUser()              { return isUser;    }
    public void    setUser(boolean v)    { isUser = v;       }

    public boolean isTyping()            { return isTyping;  }
    public void    setTyping(boolean v)  { isTyping = v;     }

    public long    getTimestamp()        { return timestamp; }
    public void    setTimestamp(long v)  { timestamp = v;    }
}