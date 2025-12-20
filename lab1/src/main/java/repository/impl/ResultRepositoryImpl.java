package repository.impl;

import entity.Function;
import entity.Result;
import repository.ResultRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория для Result Entity
 */
public class ResultRepositoryImpl implements ResultRepository {
    
    @Override
    public Result save(Result result) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            em.getTransaction().begin();
            if (result.getId() == null) {
                em.persist(result);
            } else {
                result = em.merge(result);
            }
            em.getTransaction().commit();
            return result;
        } finally {
            em.close();
        }
    }
    
    @Override
    public Optional<Result> findById(Long id) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            Result result = em.find(Result.class, id);
            return Optional.ofNullable(result);
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Result> findAll() {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            TypedQuery<Result> query = em.createQuery("SELECT r FROM Result r", Result.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Result> findByFunction(Function function) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            TypedQuery<Result> query = em.createQuery(
                "SELECT r FROM Result r WHERE r.function = :function", Result.class);
            query.setParameter("function", function);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    public void delete(Result result) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            em.getTransaction().begin();
            Result managedResult = em.find(Result.class, result.getId());
            if (managedResult != null) {
                em.remove(managedResult);
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
            Result result = em.find(Result.class, id);
            if (result != null) {
                em.remove(result);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}

