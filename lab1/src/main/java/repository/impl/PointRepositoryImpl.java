package repository.impl;

import entity.Function;
import entity.Point;
import repository.PointRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория для Point Entity
 */
@Repository
public class PointRepositoryImpl implements PointRepository {
    
    @Override
    public Point save(Point point) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            em.getTransaction().begin();
            if (point.getId() == null) {
                em.persist(point);
            } else {
                point = em.merge(point);
            }
            em.getTransaction().commit();
            return point;
        } finally {
            em.close();
        }
    }
    
    @Override
    public Optional<Point> findById(Long id) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            Point point = em.find(Point.class, id);
            return Optional.ofNullable(point);
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Point> findAll() {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            TypedQuery<Point> query = em.createQuery("SELECT p FROM Point p", Point.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Point> findByFunction(Function function) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            TypedQuery<Point> query = em.createQuery(
                "SELECT p FROM Point p WHERE p.function = :function", Point.class);
            query.setParameter("function", function);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    public void delete(Point point) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            em.getTransaction().begin();
            Point managedPoint = em.find(Point.class, point.getId());
            if (managedPoint != null) {
                em.remove(managedPoint);
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
            Point point = em.find(Point.class, id);
            if (point != null) {
                em.remove(point);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}

