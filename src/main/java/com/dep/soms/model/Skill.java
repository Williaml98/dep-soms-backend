package com.dep.soms.model;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "skills")
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;
}
