package repository.impl;

import entity.Function;
import entity.User;
import repository.FunctionRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория для Function Entity
 */
@Repository
public class FunctionRepositoryImpl implements FunctionRepository {
    
    @Override
    public Function save(Function function) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            em.getTransaction().begin();
            if (function.getId() == null) {
                em.persist(function);
            } else {
                function = em.merge(function);
            }
            em.getTransaction().commit();
            return function;
        } finally {
            em.close();
        }
    }
    
    @Override
    public Optional<Function> findById(Long id) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            Function function = em.find(Function.class, id);
            return Optional.ofNullable(function);
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Function> findAll() {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            TypedQuery<Function> query = em.createQuery("SELECT f FROM Function f", Function.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Function> findByUser(User user) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            TypedQuery<Function> query = em.createQuery(
                "SELECT f FROM Function f WHERE f.user = :user", Function.class);
            query.setParameter("user", user);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    public void delete(Function function) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            em.getTransaction().begin();
            Function managedFunction = em.find(Function.class, function.getId());
            if (managedFunction != null) {
                em.remove(managedFunction);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    @Override
    public void deleteById(Long id) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            em.getTransaction().begin();
            Function function = em.find(Function.class, id);
            if (function != null) {
                em.remove(function);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}

