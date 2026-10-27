/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import vetora.dao.DashboardDAO;

/**
 *
 * @author ASUS
 */
public class DashboardModel {
    private final DashboardDAO dashboardDao = new DashboardDAO();

    public Dashboard loadDashboard() throws SQLException {
        return dashboardDao.getDashboard(LocalDate.now(ZoneId.of("Asia/Colombo")));
    }
}
