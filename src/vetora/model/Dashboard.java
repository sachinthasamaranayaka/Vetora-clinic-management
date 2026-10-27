/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ASUS
 */
public class Dashboard {
    private final LocalDate date;
    private final List<Appointment> appointments;
    private final BigDecimal income;

    public Dashboard(LocalDate date, List<Appointment> appointments, BigDecimal income){
        this.date = date;
        this.appointments = new ArrayList<>(appointments);
        this.income = income;
    }

    public LocalDate getDate(){
        return date;
    }

    public List<Appointment> getAppointments(){
        return new ArrayList<>(appointments);
    }

    public int getAppointmentCount(){
        return appointments.size();
    }

    public int getWaitingCount(){
        int count = 0;
        for (Appointment appointment : appointments) {
            if ("Checked-In".equals(appointment.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public int getCompletedCount(){
        int count = 0;
        for (Appointment appointment : appointments) {
            if ("Completed".equals(appointment.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public BigDecimal getIncome(){
        return income;
    }
}
