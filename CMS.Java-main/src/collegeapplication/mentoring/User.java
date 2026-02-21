package collegeapplication.mentoring;

public class User {
    private int id;
    private String name;
    private String role;
    private String username;

    public User(int id, String name, String role, String username) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }
}
