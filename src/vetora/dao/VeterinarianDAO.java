/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import vetora.config.DBConnection;
import vetora.model.Veterinarian;
/**
 *
 * @author ASUS
 */
public class VeterinarianDAO {
    
    
    public Veterinarian getProfile() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return readProfile(con);
        }
    }

    private Veterinarian readProfile(Connection con)throws SQLException {

        String sql = "SELECT veterinarian_id, full_name, registration_number, specialization, phone, email, availability, status FROM veterinarians WHERE status = 'Active' ORDER BY veterinarian_id LIMIT 2";

        try (PreparedStatement statement = con.prepareStatement(sql); ResultSet result = statement.executeQuery()) {

            if (!result.next()) {
                return null;
            }

            Veterinarian vet = new Veterinarian(
                    result.getLong("veterinarian_id"),
                    result.getString("full_name"),
                    result.getString("registration_number"),
                    result.getString("specialization"),
                    result.getString("phone"),
                    result.getString("email"),
                    "Available".equals(result.getString("availability")),
                    "Active".equals(result.getString("status"))
            );

            if (result.next()) {
                throw new SQLException("More than one veterinarian profile exists.");
            }

            return vet;
        }
    }
}
