package com.scaler.userauthenticationservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter // 1:M cardinality
public class Session extends BaseModel {

    private String token;

    @ManyToOne
    private User user;
}
