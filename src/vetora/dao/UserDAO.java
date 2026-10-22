/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vetora.model.User;
import vetora.config.DBConnection;
import java.security.GeneralSecurityException;
import vetora.util.PasswordUtils;


/**
 *
 * @author ASUS
 */
public class UserDAO {
    
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        
        String sql = "SELECT user_id,full_name,username,role,status FROM users ORDER BY user_id";
        
        try(Connection con = DBConnection.getConnection(); PreparedStatement statement = con.prepareStatement(sql); ResultSet result = statement.executeQuery())
        {
            while(result.next()){
                users.add(new User(result.getLong("user_id"),result.getString("full_name"),result.getString("username"),result.getString("role"),result.getString("status")));
            }
        }
        
        return users;
    }
    
    public User authenticate(String username , char[] password) throws SQLException,GeneralSecurityException{
        User user = null;
        // Used when the username is not found.
        String storedHash = "pbkdf2_sha256$600000$AAAAAAAAAAAAAAAAAAAAAA==$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
        
        String sql = "SELECT user_id,full_name,username,role,status,password_hash FROM users WHERE username = ?";
        try(Connection con = DBConnection.getConnection();PreparedStatement stat = con.prepareStatement(sql)){
            stat.setString(1, username);
            
            try(ResultSet result = stat.executeQuery()){
                if(result.next()){
                    storedHash = result.getString("password_hash");
                    user = new User(result.getLong("user_id"),result.getString("full_name"),result.getString("username"),result.getString("role"),result.getString("status"));
                }
            }
        }
        
        boolean passwordMatches = PasswordUtils.verifyPassword(password, storedHash);
        if (user == null || !passwordMatches || !"Active".equals(user.getStatus())){
            return null;
        }
        
        return user;
    }
    
}
