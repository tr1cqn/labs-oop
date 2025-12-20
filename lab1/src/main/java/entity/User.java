package entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity класс для таблицы users
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "login", unique = true, nullable = false, length = 100)
    private String login;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    @Column(name = "email", length = 255)
    private String email;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Function> functions = new ArrayList<>();
    
    // Конструкторы
    public User() {
    }
    
    public User(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getLogin() {
        return login;
    }
    
    public void setLogin(String login) {
        this.login = login;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public List<Function> getFunctions() {
        return functions;
    }
    
    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }
    
    // Вспомогательные методы
    public void addFunction(Function function) {
        functions.add(function);
        function.setUser(this);
    }
    
    public void removeFunction(Function function) {
        functions.remove(function);
        function.setUser(null);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

