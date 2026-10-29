/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.dao;


import java.util.List;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import vetora.config.DBConnection;
import vetora.model.Owner;
/**
 *
 * @author ASUS
 */
public class OwnerDAO {
    
    public List<Owner> getAllOwners() throws SQLException{
        List<Owner> owners = new ArrayList<>();
        
        String sql = "SELECT o.owner_id,o.full_name,o.phone,o.email,o.address, "
                + "(SELECT COUNT(*) FROM pets p WHERE p.owner_id = o.owner_id) AS pet_count "
                + "FROM owners o ORDER BY o.owner_id";
        
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet result = stmt.executeQuery()) {
            while (result.next()){
                Owner owner= new Owner (result.getLong("owner_id"),result.getString("full_name"),result.getString("phone"),result.getString("email"),result.getString("address"));
                owner.setPetCount(result.getInt("pet_count"));
                owners.add(owner);
            }
        }
        
        return owners;
    }
    
    public void createOwner(String fullName,String phone, String email, String address)throws SQLException{
        String sql = "INSERT INTO owners (full_name,phone,email,address) VALUES(?,?,?,?)";
        try(Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, fullName);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.setString(4, address);
            
            if(stmt.executeUpdate()!= 1){
                throw new SQLException("Owner was not saved.");
            }
        }
    }
    
    public List<Owner> searchOwners(String keyword) throws SQLException {
        List<Owner> owners = new ArrayList<>();
        
        String sql = "SELECT o.owner_id,o.full_name,o.phone,o.email,o.address, "
                + "(SELECT COUNT(*) FROM pets p WHERE p.owner_id = o.owner_id) AS pet_count "
                + "FROM owners o "
                + "WHERE o.full_name LIKE ? OR o.phone LIKE ? "
                + "ORDER BY o.owner_id";
        
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            
            try (ResultSet result = stmt.executeQuery()){
                while (result.next()){
                    Owner owner = new Owner(result.getLong("owner_id"),result.getString("full_name"),result.getString("phone"),result.getString("email"),result.getString("address"));
                    owner.setPetCount(result.getInt("pet_count"));
                    owners.add(owner);
                }
            }
        }
        
        return owners;
    }
    
    public void updateOwner(long ownerId, String fullName, String phone, String email, String address) throws  SQLException {
        String sql = "UPDATE owners SET full_name = ?, phone = ?, email = ?, address = ? WHERE owner_id = ?";
        
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, fullName);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.setString(4, address);
            stmt.setLong(5, ownerId);
            
            if (stmt.executeUpdate() != 1){
                throw new SQLException("Owner was not updated. Reload the list.");
            }
        }
    }
    
    public boolean deleteOwner(long ownerId) throws SQLException {
        String sql = "DELETE FROM owners WHERE owner_id = ?";
        
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1,ownerId);
            
            return stmt.executeUpdate() == 1;
        }
    }
}
