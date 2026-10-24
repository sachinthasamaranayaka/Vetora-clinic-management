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
    
    public void saveOwner(Long ownerId, String fullName, String phone, String email,String address)throws ValidationException,SQLException {
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
        
        String savedEmail = email.isEmpty() ? null : email;
        String savedAddress = address.isEmpty() ? null : address;
        
        if (ownerId == null){
            ownerDao.createOwner(fullName, phone, email, address);
        }
        else{
            ownerDao.updateOwner(ownerId, fullName, phone, savedEmail, savedAddress);
        }
    }
    
    public List<Owner> searchOwners(String keyword) throws SQLException {
        keyword = keyword == null ? "" : keyword.trim();
        
        return ownerDao.searchOwners(keyword);
    }
    
    public void deleteOwner(Long ownerId) throws SQLException,ValidationException {
        if (ownerId == null || ownerId <= 0){
            throw new ValidationException("Select an owner to delete.");
        }
        
        try {
            boolean deleted = ownerDao.deleteOwner(ownerId);
            
            if (!deleted){
                throw new ValidationException("Owner was not found. Reload the owners list.");
            }
        }
        catch (SQLException ex){
            if (ex.getErrorCode() == 1451){
                throw new ValidationException("Cannot delete this owner because pets or other records are linked.");
            }
            
            throw ex;
        }
    }
}
