package servlet;

/**
 * Данные аутентифицированного пользователя, которые кладём в request attributes.
 */
public class AuthContext {
    public static final String ATTR_NAME = "auth.context";

    private final Long userId;
    private final String login;
    private final UserRole role;

    public AuthContext(Long userId, String login, UserRole role) {
        this.userId = userId;
        this.login = login;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLogin() {
        return login;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}


