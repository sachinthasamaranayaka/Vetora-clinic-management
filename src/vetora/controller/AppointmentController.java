/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import vetora.exception.ValidationException;
import vetora.model.Appointment;
import vetora.model.AppointmentModel;
import vetora.model.Pet;
import vetora.model.Veterinarian;
import vetora.view.PanelAppointment;
/**
 *
 * @author ASUS
 */
public class AppointmentController {
    private final AppointmentModel model;
    private final PanelAppointment view;
    private boolean savingAppointment;
    private int appointmentLoadVersion;
    private boolean loadingOptions;
    private boolean changingAppointmentStatus;

    public AppointmentController(AppointmentModel model, PanelAppointment view) {
        this.model = model;
        this.view = view;
    }
    
    public void loadFormOptions() {
        if (loadingOptions || savingAppointment || changingAppointmentStatus) {
            return;
        }

        loadingOptions = true;
        view.setAppointmentBusy(true);
        view.showAppointmentError("Loading appointment details...");

        new SwingWorker<Void, Void>() {
            private List<Pet> pets;
            private Veterinarian veterinarian;

            @Override
            protected Void doInBackground() throws Exception {
                pets = model.getPetOptions();
                veterinarian = model.getClinicVeterinarian();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    view.displayPetOptions(pets);
                    view.displayClinicVet(veterinarian);

                    view.showAppointmentError(pets.isEmpty() ? "Register a pet before booking an appointment." : " " );

                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();

                    view.clearAppointmentOptions();
                    view.showAppointmentError("Loading interrupted.");

                }
                catch (ExecutionException ex) {
                    view.clearAppointmentOptions();

                    Throwable cause = ex.getCause();

                    if (cause instanceof ValidationException) {
                        view.showAppointmentError(cause.getMessage());
                    }
                    else {
                        cause.printStackTrace();

                        view.showAppointmentError("Could not load appointment details.");
                    }

                }
                finally {
                    loadingOptions = false;
                    view.setAppointmentBusy(false);
                }
            }
        }.execute();
    }
    
    public void petSelected(){
        view.showSelectedPetOwner();
    }
    
    public void bookAppointment() {
        if (loadingOptions || savingAppointment || changingAppointmentStatus) {
            return;
        }

        Long petId = view.getAppointmentPetId();

        LocalDate date;
        LocalTime time;

        try {
            date = view.getAppointmentDate();
            time = view.getAppointmentTime();

        } 
        catch (ParseException | DateTimeParseException ex) {
            view.showAppointmentError("Enter a valid date and select a time slot.");
            return;
        }

        String reason = view.getAppointmentReason();

        savingAppointment = true;
        view.setAppointmentBusy(true);
        view.showAppointmentError(" ");

        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                return model.bookAppointment(petId, date, time, reason);
            }

            @Override
            protected void done() {
                Long savedId = null;

                try {
                    savedId = get();

                } 
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();

                    view.showAppointmentError("Booking interrupted. Check the database before retrying.");

                }
                catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();

                    if (cause instanceof ValidationException) {
                        view.showAppointmentError(cause.getMessage());

                    } 
                    else {
                        cause.printStackTrace();
                        view.showAppointmentError("Could not confirm the booking. Check the database before retrying.");
                    }

                } 
                finally {
                    savingAppointment = false;
                   view.setAppointmentBusy(false);
                }

                if (savedId != null) {
                    view.clearAppointmentForm();
                    view.showBookedDateInFilters(date);
                    loadAppointments();
                    view.showAppointmentBooked(savedId);
                }
            }
        }.execute();
    }
    
    public void newAppointment(){
        if(!loadingOptions && !savingAppointment && !changingAppointmentStatus){
            view.clearAppointmentForm();
        }
    }
    
    public void loadAppointments() {
        if(changingAppointmentStatus){
            return;
        }
        
        int requestVersion = ++appointmentLoadVersion;

        view.clearAppointmentList();

        LocalDate date;

        try {
            date = view.getAppointmentFilterDate();

        }
        catch (ParseException ex) {
            view.showAppointmentListMessage("Enter a valid filter date.");
            return;
        }

        String status = view.getAppointmentStatusFilter();

        view.showAppointmentListMessage("Loading appointments...");

        new SwingWorker<List<Appointment>, Void>() {
            @Override
            protected List<Appointment> doInBackground() throws Exception {
                return model.getAppointments(date, status);
            }

            @Override
            protected void done() {
                if (requestVersion != appointmentLoadVersion) {
                    return;
                }

                try {
                    view.displayAppointments(get(), date, status);
                } 
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showAppointmentListMessage("Loading interrupted.");

                } 
                catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();

                    if (cause instanceof ValidationException) {
                        view.showAppointmentListMessage(cause.getMessage());

                    } 
                    else {
                        cause.printStackTrace();
                        view.showAppointmentListMessage("Could not load appointments. Click Refresh to retry.");
                    }
                }
            }
        }.execute();
    }
    
    public void checkInAppointment() {
        changeSelectedAppointmentStatus("Checked-In");
    }

    public void cancelAppointment() {
        changeSelectedAppointmentStatus("Cancelled");
    }

    private void changeSelectedAppointmentStatus(String newStatus) {
        if (loadingOptions || savingAppointment || changingAppointmentStatus) {
            return;
        }

        Appointment selected = view.getSelectedAppointment();

        if (selected == null) {
            view.showAppointmentListMessage("Select an appointment.");
            return;
        }

        long appointmentId = selected.getAppointmentId();

        if ("Cancelled".equals(newStatus) && !view.confirmCancelAppointment(appointmentId)) {
            return;
        }

        changingAppointmentStatus = true;

        appointmentLoadVersion++;

        view.setAppointmentBusy(true);
        view.showAppointmentListMessage("Updating appointment...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                model.changeAppointmentStatus(appointmentId, newStatus);
                return null;
            }

            @Override
            protected void done() {
                String error = null;

                try {
                    get();

                } 
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    error = "Status update interrupted. Check the schedule.";

                } 
                catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();

                    if (cause instanceof ValidationException) {
                        error = cause.getMessage();

                    } 
                    else {
                        cause.printStackTrace();

                        error =  "Could not confirm the status update. Check the schedule.";
                    }

                } 
                finally {
                    changingAppointmentStatus = false;
                    view.setAppointmentBusy(false);
                }

                loadAppointments();

                if (error != null) {
                    view.showAppointmentActionError(error);
                }
            }
        }.execute();
    }
}
