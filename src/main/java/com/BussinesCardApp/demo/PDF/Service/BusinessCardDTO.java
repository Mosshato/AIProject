package com.BussinesCardApp.demo.PDF.Service;

import java.io.Serializable;

public class BusinessCardDTO implements Serializable {

    private String scientificTitle;     // Titlu științific (ex: Ph.D.)
    private String firstName;           // Prenume
    private String lastName;            // Nume
    private String academicPosition;    // Funcție academică (ex: Assistant Professor)
    private String officePhone;         // Număr birou
    private String mobilePhone;         // Număr mobil
    private String email;               // Email
    private String website;             // Site web

    // Constructor gol (necesar pentru Spring / Jackson)
    public BusinessCardDTO() {
    }

    // Getters and Setters

    public String getScientificTitle() {
        return scientificTitle;
    }

    public void setScientificTitle(String scientificTitle) {
        this.scientificTitle = scientificTitle;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAcademicPosition() {
        return academicPosition;
    }

    public void setAcademicPosition(String academicPosition) {
        this.academicPosition = academicPosition;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
