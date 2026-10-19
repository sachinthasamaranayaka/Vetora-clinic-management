/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author ASUS
 */
public class TestDBConnection {
    public static void main(String[] args) {

        String sql = "SELECT COUNT(*) AS total_users FROM users";

        try (
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery())
        {

            if (result.next()) {
                
                System.out.println("Connected to database: "+ connection.getCatalog());
                System.out.println("Users found: "+ result.getLong("total_users"));
            }
        }
        
        catch (SQLException ex) {
            System.err.println("Database test failed: " + ex.getMessage());
        }
    }
}
