package com.rootimpact.transaction.entity;

import com.rootimpact.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "type")
    private String type;

    @Column(name = "date")
    private Date date;

}
