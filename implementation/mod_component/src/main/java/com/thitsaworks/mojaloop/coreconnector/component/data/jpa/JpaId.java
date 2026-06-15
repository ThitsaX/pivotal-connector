package com.thitsaworks.mojaloop.coreconnector.component.data.jpa;

import java.io.Serializable;

public abstract class JpaId<T> implements Serializable {

    public abstract T getEntityId();
}
