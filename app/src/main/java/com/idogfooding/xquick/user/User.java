package com.idogfooding.xquick.user;

import com.idogfooding.backbone.network.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class User extends BaseEntity {

    private long id;
    private String name;

}
