package com.auth.model;

import org.springframework.data.relational.core.mapping.Table;

@Table("tbl_role_permission")
public class RolePermission {
    
    private Long roleId;
    private Long permissionId;

    public RolePermission() {}
    
    public RolePermission(Long roleId, Long permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    
    public Long getPermissionId() { return permissionId; }
    public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }
}