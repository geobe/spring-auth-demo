package de.geobe.spring.demo.domain

import org.springframework.security.core.GrantedAuthority

import javax.persistence.*

/**
 * Created by georg beier on 12.04.2017.
 */
@Entity
@Table(name = "tbl_role")
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;

    @Override
    String getAuthority() {
        return name
    }
}