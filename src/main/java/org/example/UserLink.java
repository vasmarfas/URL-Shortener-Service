package org.example;

public class UserLink {
    String id;
    String creatorUserUUID;
    String sourceURL;
    String shortURL;
    int usageLimit;
    int usages;
    long validUntil;


    public UserLink(String id,
                    String creatorUserUUID,
                    String sourceURL,
                    String shortURL,
                    int usageLimit,
                    int usages,
                    long validUntil
    ) {
        this.id = id;
        this.creatorUserUUID = creatorUserUUID;
        this.sourceURL = sourceURL;
        this.shortURL = shortURL;
        this.usageLimit = usageLimit;
        this.usages = usages;
        this.validUntil = validUntil;
    }
}
