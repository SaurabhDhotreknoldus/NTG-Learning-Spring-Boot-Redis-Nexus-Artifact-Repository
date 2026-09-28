package com.nashtech.learning.library.model;

/**
 * Enumeration representing company departments.
 */
public enum Department {
    ENGINEERING("Engineering", "ENG"),
    HUMAN_RESOURCES("Human Resources", "HR"),
    FINANCE("Finance", "FIN"),
    MARKETING("Marketing", "MKT"),
    SALES("Sales", "SLS"),
    OPERATIONS("Operations", "OPS");

    private final String displayName;
    private final String departmentCode;

    Department(String displayName, String departmentCode) {
        this.displayName = displayName;
        this.departmentCode = departmentCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }
}
