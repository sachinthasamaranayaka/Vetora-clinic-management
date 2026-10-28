/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

/**
 *
 * @author ASUS
 */
public class Owner {
    private final long ownerId;
    private final String fullName;
    private final String phone;
    private final String email;
    private final String adress;
    private int petCount;
    
    public Owner(long ownerID, String fullName, String phone, String email, String adress){
        this.ownerId = ownerID;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.adress = adress;
    }
    
    public void setPetCount(int petCount) {
        this.petCount = petCount;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAdress() {
        return adress;
    } 

    public int getPetCount() {
        return petCount;
    }
    
}
