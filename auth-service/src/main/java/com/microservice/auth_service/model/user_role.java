package com.microservice.auth_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class user_role {

    public Long getId() {
        return (long) role_id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role__id")
@Getter
    private Long role_id;


    @Getter @Setter
    @ToString.Exclude
    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "role_name")
    private acces userRole;



    public user_role(acces userRole) {
        this.userRole = userRole;
    }



    // Optional: bidirectional relationship, mappedBy here
//    @ManyToMany(mappedBy = "roles")
//    private Set<Users> users = new HashSet<>();
}

