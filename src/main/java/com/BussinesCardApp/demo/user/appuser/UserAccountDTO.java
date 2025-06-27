// src/main/java/com/BussinesCardApp/demo/user/dto/UserAccountDTO.java
package com.BussinesCardApp.demo.user.appuser;

public class UserAccountDTO {
    private final String id;
    private final String email;
    private final String firstName;
    private final String lastName;

    public UserAccountDTO(String id, String email, String firstName,
                          String lastName, String role) {
        this.id        = id;
        this.email     = email;
        this.firstName = firstName;
        this.lastName  = lastName;
    }

    public UserAccountDTO(String id, String email, Object firstName, String lastName) {
        this.id        = id;
        this.email     = email;
        this.firstName = firstName.toString();
        this.lastName  = lastName.toString();
    }

    public String getId()        { return id;        }
    public String getEmail()     { return email;     }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName;  }
}
