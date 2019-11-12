package com.hisd3.utils.Model

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*


@Entity
@Table(name="interface", schema = "utils")
class Interface {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "uuid")
    @Type(type = "pg-uuid")
    var id: UUID? = null

    @Column(name = "name", columnDefinition = "varchar")
    var name: String? = null

    @Column(name = "value", columnDefinition = "varchar")
    var value: String? = null
}