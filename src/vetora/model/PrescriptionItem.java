/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

/**
 * One medicine line in a consultation prescription.
 * @author ASUS
 */
public class PrescriptionItem {
    private final String medicineName;
    private final String fulfilment;
    private final int quantity;
    private final String doseText;
    private final String frequency;
    private final String duration;
    private final String instructions;

    public PrescriptionItem(String medicineName, String fulfilment, int quantity, String doseText, String frequency, String duration, String instructions) {
        this.medicineName = medicineName == null ? "" : medicineName.trim();
        this.fulfilment = fulfilment == null ? "" : fulfilment.trim();
        this.quantity = quantity;
        this.doseText = doseText == null ? "" : doseText.trim();
        this.frequency = frequency == null ? "" : frequency.trim();
        this.duration = duration == null ? "" : duration.trim();
        this.instructions = instructions == null ? "" : instructions.trim();
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getFulfilment() {
        return fulfilment;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDoseText() {
        return doseText;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getDuration() {
        return duration;
    }

    public String getInstructions() {
        return instructions;
    }

    public boolean sameDetails(PrescriptionItem other) {
        return other != null
                && medicineName.equals(other.medicineName)
                && fulfilment.equals(other.fulfilment)
                && quantity == other.quantity
                && doseText.equals(other.doseText)
                && frequency.equals(other.frequency)
                && duration.equals(other.duration)
                && instructions.equals(other.instructions);
    }
}
