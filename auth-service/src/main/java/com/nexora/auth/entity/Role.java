package com.nexora.auth.entity;

import com.nexora.auth.enums.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity{
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false, length = 50)
    private RoleType name;

    @Column(length = 200)
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    public Role(RoleType name) {
        this.name = name;
    }

    public Role(RoleType name, String description) {
        this.name = name;
        this.description = description;
    }
}
