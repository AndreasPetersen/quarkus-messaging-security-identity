package org.acme.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class Dao {
    @Inject
    EntityManager entityManager;

    @Transactional
    public void persist(MyEntity myEntity) {
        entityManager.persist(myEntity);
    }
}
