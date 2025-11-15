package com.auth.model;

/**
 * @author Roeurt Kesei
 * Enum representing the status of a user.
 */
public enum UserStatus {
    ACTIVE(1, "Active"),
    INACTIVE(2, "Inactive"),
    DELETED(3, "Deleted");
    
    private final int value;
    private final String displayName;
    
    UserStatus(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static UserStatus fromValue(int value) {
        for (UserStatus status : UserStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid user status value: " + value);
    }
}
