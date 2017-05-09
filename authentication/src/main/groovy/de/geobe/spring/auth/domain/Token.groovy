package de.geobe.spring.auth.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * Created by georg beier on 19.04.2017.
 */
@Entity
@Table(name='tbl_token',
        indexes = @Index(name ='token_index',unique = true,columnList = 'key')
)
class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id
    @Column(length = 1024)
    private String key
    private Date generated
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = 'user_id', nullable = false)
    private User user
}
