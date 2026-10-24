/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;


import java.sql.SQLException;
import vetora.dao.VeterinarianDAO;
/**
 *
 * @author ASUS
 */
public class VeterinarianModel {
    private final VeterinarianDAO vetDao = new VeterinarianDAO();
    
    
    public Veterinarian getProfile() throws SQLException {
        return vetDao.getProfile();
    }
    
}
