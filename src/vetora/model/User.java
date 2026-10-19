/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

/**
 *
 * @author ASUS
 */
public class User {
    private final long userId;
    private final String fullName;
    private final String username;
    private final String role;
    private final String status;
    
    public User(long userId , String fullName , String username , String role , String status){
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.role = role;
        this.status = status;
    }

    public long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
    
}
