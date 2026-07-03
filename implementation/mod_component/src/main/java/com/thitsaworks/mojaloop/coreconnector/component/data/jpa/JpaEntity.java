/*
 * Copyright (c) 2024-2026 ThitsaWorks Pte. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thitsaworks.mojaloop.coreconnector.component.data.jpa;

import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;
import java.io.Serializable;
import java.time.Instant;

@Setter
@MappedSuperclass
public abstract class JpaEntity<ID> implements Serializable {

    private static final long serialVersionUID = -5526155220442471285L;

    @Column(name = "created_date", updatable = false)
    @Convert(converter = JpaInstantConverter.class)
    protected Instant createdDate;

    @Column(name = "updated_date")
    @Convert(converter = JpaInstantConverter.class)
    protected Instant updatedDate;

    @Column(name = "version")
    @Version
    protected Integer version;

    @PrePersist
    public void prePersist() {

        this.createdDate = Instant.now();
        this.updatedDate = Instant.now();

    }

    @PreUpdate
    public void preUpdate() {

        this.updatedDate = Instant.now();

    }

    @Override
    public int hashCode() {

        return this.getPrimaryId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {return true;}

        if (obj instanceof JpaEntity) {

            JpaEntity<?> entity = (JpaEntity<?>) obj;

            return entity.getPrimaryId().equals(this.getPrimaryId());
        }

        return false;
    }

    protected abstract ID getPrimaryId();
}