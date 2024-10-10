package org.acme.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class MyEntity {
    private @Id String id;

    public MyEntity() {}

    public MyEntity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
