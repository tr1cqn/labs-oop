package servlet;

/**
 * Роли пользователей для Basic Auth / RBAC.
 */
public enum UserRole {
    ADMIN,
    USER;

    public static UserRole from(String value) {
        if (value == null) return USER;
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (Exception ignored) {
            return USER;
        }
    }
}


