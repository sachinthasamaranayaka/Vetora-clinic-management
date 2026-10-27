/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.controller;

import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import vetora.model.Dashboard;
import vetora.model.DashboardModel;
import vetora.view.PanelDashboard;

/**
 *
 * @author ASUS
 */
public class DashboardController {
    private final DashboardModel model;
    private final PanelDashboard view;
    private int dashboardLoadVersion;

    public DashboardController(DashboardModel model, PanelDashboard view){
        this.model = model;
        this.view = view;
    }

    public void loadDashboard(){
        int requestVersion = ++dashboardLoadVersion;
        view.setDashboardBusy(true);
        view.showDashboardMessage("Loading today's clinic activity...");
        new SwingWorker<Dashboard, Void>(){
            @Override
            protected Dashboard doInBackground() throws Exception {
                return model.loadDashboard();
            }
            @Override
            protected void done(){
                if (requestVersion != dashboardLoadVersion) {
                    return;
                }
                try {
                    view.displayDashboard(get());
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.clearDashboard();
                    view.showDashboardMessage("Loading interrupted. Click Refresh to retry.");
                }
                catch (ExecutionException ex) {
                    ex.getCause().printStackTrace();
                    view.clearDashboard();
                    view.showDashboardMessage("Could not load dashboard. Check the database and click Refresh.");
                }
                finally {
                    view.setDashboardBusy(false);
                }
            }
        }.execute();
    }
}
