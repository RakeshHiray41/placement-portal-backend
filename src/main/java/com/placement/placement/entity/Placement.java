package com.placement.placement.entity;
import jakarta.persistence.*;


import java.time.LocalDate;


@Entity
@Table(name = "placements")
public class Placement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne private Student student;
    @ManyToOne private Company company;

    private String role;
    private LocalDate placedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getPlacedDate() {
        return placedDate;
    }

    public void setPlacedDate(LocalDate placedDate) {
        this.placedDate = placedDate;
    }


}
