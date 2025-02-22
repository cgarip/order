package model;

public class Employee {
    private int id;
    private String userName;
    private String phone;
    private String email;

    public Employee(int id, String userName, String phone, String email) {
        this.id = id;
        this.userName = userName;
        this.phone = phone;
        this.email = email;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
