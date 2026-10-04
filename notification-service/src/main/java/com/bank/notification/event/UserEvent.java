package com.bank.notification.event;

import java.util.UUID;


public class UserEvent {

    private String type;
    private UUID userId;
    private String name;
    private String email;
    private String phone;

    public UserEvent() {
        // needed for JSON deserialization
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
