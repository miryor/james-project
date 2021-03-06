/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.domainlist.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;

import org.apache.james.domainlist.api.DomainListException;
import org.apache.james.domainlist.jpa.model.JPADomain;
import org.apache.james.domainlist.lib.AbstractDomainList;

import com.google.common.collect.ImmutableList;

/**
 * JPA implementation of the DomainList.<br>
 * This implementation is compatible with the JDBCDomainList, meaning same
 * database schema can be reused.
 */
public class JPADomainList extends AbstractDomainList {

    /**
     * The entity manager to access the database.
     */
    private EntityManagerFactory entityManagerFactory;

    /**
     * Set the entity manager to use.
     *
     * @param entityManagerFactory
     */
    @PersistenceUnit(unitName = "James")
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @PostConstruct
    public void init() {
        createEntityManager().close();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<String> getDomainListInternal() throws DomainListException {
        List<String> domains = new ArrayList<String>();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            domains = entityManager.createNamedQuery("listDomainNames").getResultList();
            transaction.commit();
        } catch (PersistenceException e) {
            getLogger().error("Failed to list domains", e);
            rollback(transaction);
            throw new DomainListException("Unable to retrieve domains", e);
        } finally {
            entityManager.close();
        }
        return ImmutableList.copyOf(domains);
    }

    @Override
    public boolean containsDomain(String domain) throws DomainListException {
        String lowerCasedDomain = domain.toLowerCase();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            boolean result = containsDomainInternal(lowerCasedDomain, entityManager);
            transaction.commit();
            return result;
        } catch (PersistenceException e) {
            getLogger().error("Failed to find domain", e);
            rollback(transaction);
            throw new DomainListException("Unable to retrieve domains", e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void addDomain(String domain) throws DomainListException {
        String lowerCasedDomain = domain.toLowerCase();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            if (containsDomainInternal(lowerCasedDomain, entityManager)) {
                transaction.commit();
                throw new DomainListException(lowerCasedDomain + " already exists.");
            }
            JPADomain jpaDomain = new JPADomain(lowerCasedDomain);
            entityManager.persist(jpaDomain);
            transaction.commit();
        } catch (PersistenceException e) {
            getLogger().error("Failed to save domain", e);
            rollback(transaction);
            throw new DomainListException("Unable to add domain " + domain, e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void removeDomain(String domain) throws DomainListException {
        String lowerCasedDomain = domain.toLowerCase();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            if (!containsDomainInternal(lowerCasedDomain, entityManager)) {
                transaction.commit();
                throw new DomainListException(domain + " was not found.");
            }
            entityManager.createNamedQuery("deleteDomainByName").setParameter("name", lowerCasedDomain).executeUpdate();
            transaction.commit();
        } catch (PersistenceException e) {
            getLogger().error("Failed to remove domain", e);
            rollback(transaction);
            throw new DomainListException("Unable to remove domain " + domain, e);
        } finally {
            entityManager.close();
        }
    }

    private void rollback(EntityTransaction transaction) {
        if (transaction.isActive()) {
            transaction.rollback();
        }
    }

    private boolean containsDomainInternal(String domain, EntityManager entityManager) {
        try {
            return entityManager.createNamedQuery("findDomainByName")
                .setParameter("name", domain)
                .getSingleResult() != null;
        } catch (NoResultException e) {
            getLogger().debug("No domain found", e);
            return false;
        }
    }

    /**
     * Return a new {@link EntityManager} instance
     *
     * @return manager
     */
    private EntityManager createEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

}
