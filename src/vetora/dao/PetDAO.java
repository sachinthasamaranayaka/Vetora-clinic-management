/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import vetora.model.Pet;
import vetora.config.DBConnection;
import vetora.exception.ValidationException;
/**
 *
 * @author ASUS
 */
public class PetDAO {
    
    
    public void createPet(long ownerId, String petName, String species, String gender, String breed, LocalDate dob) throws SQLException {
        String sql = "INSERT INTO pets (owner_id,pet_name,species,gender,breed,date_of_birth) VALUES(?,?,?,?,?,?)";
        
        try (Connection con = DBConnection.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)){
            stmt.setLong(1, ownerId);
            stmt.setString(2, petName);
            stmt.setString(3, species);
            stmt.setString(4, gender);
            stmt.setString(5,breed);
            
            if(dob == null){
                stmt.setNull(6, Types.DATE);
            }
            else{
                stmt.setDate(6, Date.valueOf(dob));
            }
            
            if (stmt.executeUpdate() != 1){
                throw new SQLException("Pet was not inserted.");
            }
        }
    }
    
    public List<Pet> getAllPets() throws SQLException {
        List<Pet> pets = new ArrayList<>();
        
        String sql = "SELECT p.pet_id,p.owner_id,p.pet_name,p.species,p.gender,p.breed,p.date_of_birth,o.full_name AS owner_name FROM pets p JOIN owners o ON o.owner_id = p.owner_id ORDER BY p.pet_id DESC";
        
        try (Connection con = DBConnection.getConnection(); PreparedStatement stmt = con.prepareStatement(sql); ResultSet result = stmt.executeQuery()){
            while (result.next()){
                pets.add(new Pet(result.getLong("pet_id"),result.getLong("owner_id"),result.getString("pet_name"), result.getString("species"), result.getString("gender"), result.getString("breed"), result.getDate("date_of_birth"), result.getString("owner_name")));
            }
        }
        
        return pets;
    }
    
    public void updatePet(long petId, long ownerId, String petName, String species, String gender, String breed, LocalDate dob) throws SQLException {
        String sql = "UPDATE pets SET owner_id = ?, pet_name = ?, species = ?, gender = ?, breed = ?, date_of_birth = ? WHERE pet_id = ?";
        
        try (Connection con = DBConnection.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)){
            stmt.setLong(1, ownerId);
            stmt.setString(2, petName);
            stmt.setString(3, species);
            stmt.setString(4, gender);
            stmt.setString(5, breed);
            if(dob == null){
                stmt.setNull(6, Types.DATE);
            }
            else{
                stmt.setDate(6, Date.valueOf(dob));
            }
            stmt.setLong(7, petId);
            
            if(stmt.executeUpdate() != 1){
                throw new SQLException("Pet was not update.");
            }
        }
    }

    public List<Pet> searchPets(String keyword, String species) throws SQLException {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT p.pet_id, p.owner_id, p.pet_name, p.species, p.gender, p.breed, "
                + "p.date_of_birth, o.full_name AS owner_name FROM pets p JOIN owners o ON o.owner_id = p.owner_id "
                + "WHERE (p.pet_name LIKE ? ESCAPE '!' OR o.full_name LIKE ? ESCAPE '!' "
                + "OR CAST(p.pet_id AS CHAR) LIKE ? ESCAPE '!')";
        if (species != null) {
            sql += " AND p.species = ?";
        }
        sql += " ORDER BY p.pet_id DESC";
        String pattern = "%" + keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        try (Connection con = DBConnection.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            if (species != null) {
                stmt.setString(4, species);
            }
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    pets.add(new Pet(result.getLong("pet_id"), result.getLong("owner_id"),
                            result.getString("pet_name"), result.getString("species"), result.getString("gender"),
                            result.getString("breed"), result.getDate("date_of_birth"), result.getString("owner_name")));
                }
            }
        }
        return pets;
    }

    public void deletePet(long petId) throws SQLException, ValidationException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = con.prepareStatement("SELECT pet_id FROM pets WHERE pet_id = ? FOR UPDATE")) {
                    stmt.setLong(1, petId);
                    try (ResultSet result = stmt.executeQuery()) {
                        if (!result.next()) {
                            throw new ValidationException("This pet no longer exists. Refresh the list.");
                        }
                    }
                }
                String checkSql = "SELECT appointment_id FROM appointments WHERE pet_id = ? LIMIT 1 FOR UPDATE";
                try (PreparedStatement stmt = con.prepareStatement(checkSql)) {
                    stmt.setLong(1, petId);
                    try (ResultSet result = stmt.executeQuery()) {
                        if (result.next()) {
                            throw new ValidationException("This pet has appointment history and cannot be deleted.");
                        }
                    }
                }
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM pets WHERE pet_id = ?")) {
                    stmt.setLong(1, petId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Pet was not deleted.");
                    }
                }
                con.commit();
            }
            catch (SQLException | ValidationException ex) {
                try {
                    con.rollback();
                }
                catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                if (ex instanceof SQLException && ((SQLException)ex).getErrorCode() == 1451) {
                    throw new ValidationException("This pet is used by another record and cannot be deleted.");
                }
                throw ex;
            }
        }
    }
}
