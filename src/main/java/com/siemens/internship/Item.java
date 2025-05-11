package com.siemens.internship;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String status;

    /**
     * Added email validation with regex that checks that the email has allowed characters and
     * annotation that ensures the field contains a valid email format
     */
    // Add email regex validation
    @Email(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")
    private String email;
}