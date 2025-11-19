package com.auth.util;

/**
 * @author Roeurt Kesei
 * Enum representing the status of a user.
 */
public enum EnumStatus {
    ACTIVE(1, "Active"),
    INACTIVE(0, "Inactive"),
    DELETED(2, "Deleted");
    
    private final int value;
    private final String displayName;
    
    EnumStatus(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static EnumStatus fromValue(int value) {
        for (EnumStatus status : EnumStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid user status value: " + value);
    }
}
