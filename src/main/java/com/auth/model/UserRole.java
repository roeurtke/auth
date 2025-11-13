package com.auth.model;

import org.springframework.data.relational.core.mapping.Table;

/**
 * @author Roeurt Kesei
 * UserRole entity representing the association between users and roles.
 */
@Table("tbl_user_role")
public class UserRole {
    
    private Long userId;
    private Long roleId;

    public UserRole() {}
    
    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) {this.roleId = roleId; }
}