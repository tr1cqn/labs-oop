package repository;

import entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория для работы с User Entity
 */
public interface UserRepository {
    /**
     * Сохраняет пользователя
     */
    User save(User user);
    
    /**
     * Находит пользователя по ID
     */
    Optional<User> findById(Long id);
    
    /**
     * Находит всех пользователей
     */
    List<User> findAll();
    
    /**
     * Находит пользователя по login
     */
    Optional<User> findByLogin(String login);
    
    /**
     * Удаляет пользователя
     */
    void delete(User user);
    
    /**
     * Удаляет пользователя по ID
     */
    void deleteById(Long id);
}

