package com.max.erp.identity.dto;

import java.time.Instant;

public class EmployeeDto {
    private String id;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private String jobTitle;
    private String managerId;
    private String managerName;
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACTOR
    private String workLocation;   // HEADQUARTERS, REMOTE, REGIONAL_HUB
    private String status;         // ACTIVE, ON_LEAVE, TERMINATED
    private String hireDate;
    private int remainingPtoDays;
    private Instant createdAt;

    public EmployeeDto() {}

    public EmployeeDto(String id, String employeeNumber, String firstName, String lastName,
                       String email, String phone, String department, String jobTitle,
                       String managerId, String managerName, String employmentType,
                       String workLocation, String status, String hireDate,
                       int remainingPtoDays, Instant createdAt) {
        this.id = id;
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.jobTitle = jobTitle;
        this.managerId = managerId;
        this.managerName = managerName;
        this.employmentType = employmentType;
        this.workLocation = workLocation;
        this.status = status;
        this.hireDate = hireDate;
        this.remainingPtoDays = remainingPtoDays;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getWorkLocation() { return workLocation; }
    public void setWorkLocation(String workLocation) { this.workLocation = workLocation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getHireDate() { return hireDate; }
    public void setHireDate(String hireDate) { this.hireDate = hireDate; }

    public int getRemainingPtoDays() { return remainingPtoDays; }
    public void setRemainingPtoDays(int remainingPtoDays) { this.remainingPtoDays = remainingPtoDays; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
