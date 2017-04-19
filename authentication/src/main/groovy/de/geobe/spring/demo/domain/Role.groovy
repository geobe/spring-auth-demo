package de.geobe.spring.demo.domain

import javax.persistence.*

/**
 * Created by georg beier on 12.04.2017.
 */
@Entity
@Table(name = "tbl_role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
//    @ManyToMany(mappedBy = "roles")
//    private Set<User> users;


//    public Set<User> getUsers() {
//        return Collections.unmodifiableSet(users);
//    }

//    public void addUser(User user) {
//        users.add(user)
//        if(! user.roles.contains(this)) {
//            user.roles.add(this)
//        }
//    }
//
//    public void removeUser(User user) {
//        users.remove(user)
//        if(user.roles.contains(this)) {
//            user.roles.remove(this)
//        }
//    }
}