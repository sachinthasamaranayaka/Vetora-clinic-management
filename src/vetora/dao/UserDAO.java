/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Objects;
import vetora.exception.ValidationException;
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
        
        String sql = "SELECT u.user_id, u.full_name, u.username, u.role, u.status, u.password_hash FROM users u LEFT JOIN veterinarians v ON v.veterinarian_id = u.veterinarian_id WHERE u.username = ? AND (u.role <> 'Veterinarian' OR (v.veterinarian_id IS NOT NULL AND v.status = 'Active'))";
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
    

    public List<User> searchUsers(String keyword) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, full_name, username, role, status FROM users "
                + "WHERE full_name LIKE ? ESCAPE '!' OR username LIKE ? ESCAPE '!' ORDER BY user_id";
        String pattern = "%" + keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        try (Connection con = DBConnection.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    users.add(new User(result.getLong("user_id"), result.getString("full_name"),
                            result.getString("username"), result.getString("role"), result.getString("status")));
                }
            }
        }
        return users;
    }

    public User saveUser(User original, String fullName, String username, String role, String status, String passwordHash, long currentUserId) throws SQLException, ValidationException {
        long userId = original == null ? 0 : original.getUserId();
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
               
                User stored = null;
                int otherAdmins = 0;
                String lockSql = "SELECT user_id, full_name, username, role, status FROM users ORDER BY user_id FOR UPDATE";
                try (PreparedStatement stmt = con.prepareStatement(lockSql); ResultSet result = stmt.executeQuery()) {
                    while (result.next()) {
                        long id = result.getLong("user_id");
                        if (id == userId) {
                            stored = new User(id, result.getString("full_name"), result.getString("username"),
                                    result.getString("role"), result.getString("status"));
                        }
                        else if ("Admin".equals(result.getString("role")) && "Active".equals(result.getString("status"))) {
                            otherAdmins++;
                        }
                    }
                }
                if (original != null) {
                    if (stored == null || !Objects.equals(stored.getFullName(), original.getFullName())
                            || !Objects.equals(stored.getUsername(), original.getUsername())
                            || !Objects.equals(stored.getRole(), original.getRole())
                            || !Objects.equals(stored.getStatus(), original.getStatus())) {
                        throw new ValidationException("This account changed. Close this dialog, refresh and select it again.");
                    }
                    if (userId == currentUserId && !"Active".equals(status)) {
                        throw new ValidationException("You cannot deactivate your signed-in account.");
                    }
                    if ("Admin".equals(stored.getRole()) && "Active".equals(stored.getStatus())
                            && otherAdmins == 0 && (!"Admin".equals(role) || !"Active".equals(status))) {
                        throw new ValidationException("Keep at least one active administrator account.");
                    }
                }
                String duplicateSql = "SELECT user_id FROM users WHERE username = ? AND user_id <> ?";
                try (PreparedStatement stmt = con.prepareStatement(duplicateSql)) {
                    stmt.setString(1, username);
                    stmt.setLong(2, userId);
                    try (ResultSet result = stmt.executeQuery()) {
                        if (result.next()) {
                            throw new ValidationException("That username is already used. Choose another username.");
                        }
                    }
                }
                
                Long veterinarianId = getVeterinarianId(con, role, userId);
                
                if (original == null) {
                    if (passwordHash == null) {
                        throw new ValidationException("A password is required for a new account.");
                    }
                    String sql = "INSERT INTO users (full_name, username, role, status, password_hash, veterinarian_id) VALUES (?, ?, ?, ?, ?,?)";
                    try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, fullName);
                        stmt.setString(2, username);
                        stmt.setString(3, role);
                        stmt.setString(4, status);
                        stmt.setString(5, passwordHash);
                        if (veterinarianId == null){
                            stmt.setNull(6, Types.BIGINT);
                        }
                        else {
                            stmt.setLong(6, veterinarianId);
                        }
                        
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("User was not inserted.");
                        }
                        try (ResultSet keys = stmt.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException("User ID was not returned.");
                            }
                            userId = keys.getLong(1);
                        }
                    }
                }
                else {
                    String sql = "UPDATE users SET full_name = ?, username = ?, role = ?, status = ?, veterinarian_id = ?"
                            + (passwordHash == null ? "" : ", password_hash = ?") + " WHERE user_id = ?";
                    try (PreparedStatement stmt = con.prepareStatement(sql)) {
                        stmt.setString(1, fullName);
                        stmt.setString(2, username);
                        stmt.setString(3, role);
                        stmt.setString(4, status);
                        if (veterinarianId == null) {
                            stmt.setNull(5, Types.BIGINT);
                        }
                        else {
                            stmt.setLong(5, veterinarianId);
                        }
                        int index = 6;
                        if (passwordHash != null) {
                            stmt.setString(index++, passwordHash);
                        }
                        stmt.setLong(index, userId);
                        stmt.executeUpdate();
                    }
                }
                con.commit();
                return new User(userId, fullName, username, role, status);
            }
            catch (SQLException | ValidationException ex) {
                try {
                    con.rollback();
                }
                catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                if (ex instanceof SQLException && ((SQLException)ex).getErrorCode() == 1062) {
                    throw new ValidationException("The username or veterinarian profile is already linked to another account.");
                }
                throw ex;
            }
        }
    }
    
    private Long getVeterinarianId(Connection con, String role, long userId) throws SQLException, ValidationException {

        if (!"Veterinarian".equals(role)) {
            return null;
        }

        Long veterinarianId = null;

        String sql = "SELECT veterinarian_id FROM veterinarians WHERE status = 'Active' ORDER BY veterinarian_id FOR UPDATE";

        try (PreparedStatement stmt = con.prepareStatement(sql);
           ResultSet result = stmt.executeQuery()) {

            if (result.next()) {
                veterinarianId = result.getLong("veterinarian_id");
            }

            if (result.next()) {
                throw new ValidationException("More than one active veterinarian profile exists.");
            }
        }

        if (veterinarianId == null) {
            throw new ValidationException("Create an active veterinarian profile before saving this account.");
        }

        String checkSql = "SELECT user_id FROM users WHERE veterinarian_id = ? AND user_id <> ?";

        try (PreparedStatement stmt = con.prepareStatement(checkSql)) {
            stmt.setLong(1, veterinarianId);
            stmt.setLong(2, userId);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    throw new ValidationException("This veterinarian already has a linked user account.");
                }
            }
        }

        return veterinarianId;
    }
}
