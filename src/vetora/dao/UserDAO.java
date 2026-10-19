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
    
    public boolean createFirstAdmin(String fullName , String username , String passwordHash) throws SQLException{
        
        try(Connection connection = DBConnection.getConnection()){
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);
            
            try{
                try(PreparedStatement check = connection.prepareStatement("SELECT user_id FROM users LIMIT 1");ResultSet result = check.executeQuery()){
                    if(result.next()){
                        connection.rollback();
                        return false;
                    }
                }
                
                String sql = "INSERT INTO users (full_name,username,password_hash,role,status,veterinarian_id) VALUES (?,?,?,'Admin','Active',NULL)";
                
                try(PreparedStatement statement = connection.prepareStatement(sql)){
                    statement.setString(1, fullName);
                    statement.setString(2, username);
                    statement.setString(3, passwordHash);
                    
                    if (statement.executeUpdate()!= 1){
                        throw new SQLException("User was not inserted.");
                    }
                }
                
                connection.commit();
                return true;
            }
            
            catch (SQLException ex){
                try{
                    connection.rollback();
                }
                catch (SQLException rollbackError){
                    ex.addSuppressed(rollbackError);
                }
                
                throw ex;
            }
        }
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
