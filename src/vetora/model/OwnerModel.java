/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;


import java.util.List;
import java.sql.SQLException;

import vetora.dao.OwnerDAO;
import vetora.exception.ValidationException;
/**
 *
 * @author ASUS
 */
public class OwnerModel {
    
    private final OwnerDAO ownerDao = new OwnerDAO();
    
    public List<Owner> getAllOwners()throws SQLException{
        return ownerDao.getAllOwners();
    }
    
    public void createOwner(String fullName, String phone, String email,String address)throws ValidationException,SQLException {
        fullName = fullName == null ? "" : fullName.trim();
        phone = phone == null ? "" : phone.trim();
        email = email == null ? "" : email.trim();
        address = address == null ? "" : address.trim();
        
        if (fullName.isEmpty() || fullName.length() > 100){
            throw new ValidationException("Enter a full name of 1-100 characters.");
        }
        if (phone.isEmpty() || phone.length() > 11 || !phone.matches("\\+?[0-9][0-9 -]*")) {
            throw new ValidationException("Enter a phone number, up to 11 characters.");
        }
        if (!email.isEmpty()&& (email.length() > 254 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) {
            throw new ValidationException("Enter a valid email address.");
        }
        
        ownerDao.createOwner(fullName, phone, email.isEmpty()? null : email, address.isEmpty() ? null : address);
    }
    
    public List<Owner> searchOwners(String keyword) throws SQLException {
        keyword = keyword == null ? "" : keyword.trim();
        
        return ownerDao.searchOwners(keyword);
    }
}
