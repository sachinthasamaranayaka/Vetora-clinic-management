/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.util;

import vetora.model.User;

/**
 *
 * @author ASUS
 */
public class AccessControl {
    private User currentUser;

    public AccessControl(User currentUser) {
        this.currentUser = currentUser;
    }
    
    private boolean hasRole(String role) {
        return currentUser != null && "Active".equals(currentUser.getStatus()) && role.equals(currentUser.getRole());
    }
    
    public boolean canViewDashboard(){
        return hasRole("Admin") || hasRole("Staff") || hasRole("Veterinarian");
    }
    
    public boolean canViewOwnwrsAndPets(){
        return hasRole("Admin") || hasRole("Staff") || hasRole("Veterinarian");
    }
    
    public boolean canEditOwnersAndPets() {
        return hasRole("Admin") || hasRole("Staff");
    }
    
    public boolean canDeleteOwnersAndPets(){
        return hasRole("Admin");
    }
    
    public boolean canViewAppointments(){
        return hasRole("Admin") || hasRole("Staff") || hasRole("Veterinarian");
    }
    
    public boolean canManageAppointments(){
        return hasRole("Admin") || hasRole("Staff");
    }
    
    public boolean canEditConsultation(){
        return hasRole("Veterinarian");
    }
    
    public boolean canManageBilling(){
        return hasRole("Admin") || hasRole("Staff");
    }
    
    public boolean canViewClinicalReports(){
        return hasRole("Admin") || hasRole("Veterinarian");
    }
    
    public boolean canViewPaymentReports() {
        return hasRole("Admin");
    }
    
    public boolean canManageUsers(){
        return hasRole("Admin");
    }
    public void endSession(){
        currentUser = null;
    }

}
