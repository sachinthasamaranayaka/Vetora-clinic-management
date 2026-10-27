/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vetora.view;

import java.util.logging.Logger;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import vetora.controller.AppointmentController;
import vetora.controller.ConsultationController;
import vetora.controller.BillingController;
import vetora.controller.DashboardController;
import vetora.model.DashboardModel;
import vetora.model.BillingModel;
import vetora.model.UserModel;
import vetora.controller.UserController;
import vetora.model.OwnerModel;
import vetora.controller.OwnerController;
import vetora.controller.LoginController;
import vetora.model.PetModel;
import vetora.controller.PetController;
import vetora.model.AppointmentModel;
import vetora.model.ConsultationModel;
import vetora.model.User;
import vetora.util.AccessControl;

/**
 *
 * @author ASUS
 */
public class FormMain extends javax.swing.JFrame {
    
    private User currentUser;
    private AccessControl accessControl;
    
    private static final Logger logger = Logger.getLogger(FormMain.class.getName());

    public FormMain(User user) {
        
        if (user == null || !"Active".equals(user.getStatus())){
            throw new IllegalArgumentException("An active signed-in user is required.");
        }
        this.currentUser = user;
        this.accessControl = new AccessControl(currentUser);
        if (!accessControl.canViewDashboard()){
            throw new IllegalArgumentException("Unknownb user role.");
        }
        
        initComponents();
        
        jLabelUserName.setText(currentUser.getFullName());
        jLabelUserName.setToolTipText(currentUser.getFullName());
        jLabelUserRole.setText(currentUser.getRole());
        
        applyNavigationPermissions();
        setupNavigation();
        
        //Set Date
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Colombo"));
        jLabelDate.setText(today.format(DateTimeFormatter.ofPattern("dd MMM yyyy",Locale.ENGLISH)));
        
        jScrollPaneMenu.getViewport().setBackground(new Color(23,43,54));
        jScrollPaneMenu.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPaneMenu.getVerticalScrollBar().setPreferredSize(new Dimension(10,0));
        
        setLocationRelativeTo(null);
        
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    private void showPage(String cardName, JButton selectedButton, String title, String subtitle){
        if (!canOpenPage(cardName)){
            JOptionPane.showMessageDialog(this, "You do not have permission to open this page.", "Access denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CardLayout layout = (CardLayout)jPanelView.getLayout();
        layout.show(jPanelView,cardName);
        jLabelPageTitle.setText(title);
        jLabelPageSubtitle.setText(subtitle);
        jLabelDate.setText(LocalDate.now(ZoneId.of("Asia/Colombo")).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)));
        
        JButton[] menuButtons = {btnDashboard,btnOwners,btnPets,btnAppointments,btnConsultations,btnBilling,btnReports,btnUser};
            for(JButton button : menuButtons){
                boolean selected = button == selectedButton;
                button.setContentAreaFilled(selected);
                button.setBackground(selected ? new Color(15,118,110): new Color(23,43,54));
                button.setForeground(selected ? Color.WHITE: new Color(203,213,225));
                button.setFont(button.getFont().deriveFont(selected ? Font.BOLD : Font.PLAIN));
            }
    }
    
    private void registerPage(String cardName,JPanel panel,JButton button,String title,String subtitle){
        if (!canOpenPage(cardName)){
            return;
        }
        jPanelView.add(panel,cardName);
        button.addActionListener(e -> showPage(cardName,button,title,subtitle));
    }
    
    private void setupNavigation(){
        PanelDashboard dashboard = new PanelDashboard();
        DashboardController dashboardController = new DashboardController(new DashboardModel(), dashboard, accessControl);
        dashboard.setController(dashboardController);
        registerPage("dashboard",dashboard,btnDashboard,"Dashboard","Clinic at a glance");
        btnDashboard.addActionListener(e -> dashboardController.loadDashboard());
        
        
        PanelOwner ownerView = new PanelOwner();
        OwnerModel ownerModel = new OwnerModel();
        OwnerController ownnerController = new OwnerController(ownerModel, ownerView, accessControl);
        ownerView.setController(ownnerController);
        registerPage("owners",ownerView,btnOwners,"Owners","Manage owner details");
        ownnerController.loadOwners();
        
        PanelPet petView = new PanelPet();
        PetModel petModel = new PetModel();
        PetController petController = new PetController(petModel, petView, accessControl);
        petView.setController(petController);
        registerPage("pets",petView,btnPets,"Pets","Register pets, update their details and find existing records");
        btnPets.addActionListener(e ->{ petController.loadOwnerOptions(); petController.loadPets();});
        
        PanelAppointment appointmentView = new PanelAppointment();
        AppointmentModel appointmentModel = new AppointmentModel();
        AppointmentController appointmentController = new AppointmentController(appointmentModel, appointmentView, accessControl);
        appointmentView.setController(appointmentController);
        registerPage("appointments", appointmentView , btnAppointments,"Appointments","Book visits, check patients in and manage the daily schedule");
        btnAppointments.addActionListener(e -> {appointmentController.loadFormOptions(); appointmentController.loadAppointments();});
        
        if (accessControl.canEditConsultation()) {
        PanelConsultation consultationView = new PanelConsultation();
        ConsultationModel consultationModel = new ConsultationModel();
        ConsultationController consultationController = new ConsultationController(consultationModel, consultationView, accessControl);
        consultationView.setController(consultationController);
        registerPage("consultations",consultationView , btnConsultations,"Consultations", "Record clinical notes and prescriptions");
        btnConsultations.addActionListener(e -> consultationController.loadAppointments());
        }
        
        
        if (accessControl.canManageBilling()) {
        PanelBilling billingView = new PanelBilling();
        BillingModel billingModel = new BillingModel();
        BillingController billingController = new BillingController(billingModel, billingView, accessControl);
        billingView.setController(billingController);
        registerPage("billing", billingView, btnBilling, "Billing", "Manage invoices");
        btnBilling.addActionListener(e -> billingController.loadConsultations());
        }
        
        
        if (accessControl.canViewClinicalReports()) {
        registerPage("reports", new PanelReports(accessControl),btnReports,"Reports","View consultation history and export reports");
        }
        
        
        if (accessControl.canManageUsers()) {
        PanelUser userView = new PanelUser();
        UserModel userModel = new UserModel();
        UserController userController = new UserController(userModel, userView, currentUser.getUserId(), accessControl);
        registerPage("users",userView,btnUser,"Users","Create accounts, update details and manage account status");
        userView.setController(userController);
        userView.setSavedUserAction(saved -> {
                if (saved.getUserId() == currentUser.getUserId()) {
                    if (!currentUser.getRole().equals(saved.getRole())
                            || !"Active".equals(saved.getStatus())) {
                        JOptionPane.showMessageDialog(this, "Your account permissions changed. Please sign in again.");
                        btnLogoutActionPerformed(null);
                        return;
                    }
                    currentUser = saved;
                    jLabelUserName.setText(saved.getFullName());
                    jLabelUserName.setToolTipText(saved.getFullName());
                    jLabelUserRole.setText(saved.getRole());
                }
            });
        btnUser.addActionListener(e -> userController.loadUsers());
        }

        dashboard.setNavigation(
                () -> {
                    appointmentView.showBookedDateInFilters(LocalDate.now(ZoneId.of("Asia/Colombo")));
                    btnAppointments.doClick();
                },
                () -> {
                    if (!accessControl.canManageAppointments()) { return; }
                    appointmentController.newAppointment();
                    btnAppointments.doClick();
                },
                () -> {
                    if (!accessControl.canEditOwnersAndPets()) { return; }
                    ownnerController.newOwner();
                    btnOwners.doClick();
                },
                () -> { if (accessControl.canManageBilling()) { btnBilling.doClick(); } });
        
        btnDashboard.doClick();
    }
    
    private boolean canOpenPage(String cardName){
        AccessControl access = accessControl;
        
        switch (cardName){
            case "dashboard":
                return access.canViewDashboard();
                
            case "owners":
            case "pets":
                return access.canViewOwnwrsAndPets();
            
            case "appointments":
                return access.canViewAppointments();
                
            case "consultations":
                return access.canEditConsultation();
                
            case "billing":
                return access.canManageBilling();
                
            case "reports":
                return access.canViewClinicalReports();
                
            case "users":
                return access.canManageUsers();
                
            default:
                return false;
        }
    }
    
    private void applyNavigationPermissions(){
        String[] pages = {"dashboard", "owners", "pets", "appointments", "consultations", "billing", "reports", "users"};
        
        JButton[] buttons = {btnDashboard, btnOwners,btnPets,btnAppointments,btnConsultations,btnBilling,btnReports,btnUser};
        
        for (int i=0; i < pages.length; i++){
            boolean allowed = canOpenPage(pages[i]);
            
            buttons[i].setVisible(allowed);
            buttons[i].setEnabled(allowed);
        }
        
        jPanelNavMenu.revalidate();
        jPanelNavMenu.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelbody = new javax.swing.JPanel();
        jPanelContent = new javax.swing.JPanel();
        jPanelHeader = new javax.swing.JPanel();
        jPanelHeaderUser = new javax.swing.JPanel();
        jLabelAvatar = new javax.swing.JLabel();
        jLabelDate = new javax.swing.JLabel();
        pnlSignedInUser = new javax.swing.JPanel();
        jLabelUserName = new javax.swing.JLabel();
        jLabelUserRole = new javax.swing.JLabel();
        jPanelHeading = new javax.swing.JPanel();
        jLabelPageTitle = new javax.swing.JLabel();
        jLabelPageSubtitle = new javax.swing.JLabel();
        jPanelView = new javax.swing.JPanel();
        jPanelSidebar = new javax.swing.JPanel();
        jPanelTitle = new javax.swing.JPanel();
        jLabelLogo = new javax.swing.JLabel();
        pnlBrandText = new javax.swing.JPanel();
        jLabelBrandName = new javax.swing.JLabel();
        jLabelBrandSubtitle = new javax.swing.JLabel();
        jScrollPaneMenu = new javax.swing.JScrollPane();
        jPanelNavMenu = new javax.swing.JPanel();
        btnDashboard = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        btnOwners = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        btnPets = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        btnAppointments = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        btnConsultations = new javax.swing.JButton();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        btnBilling = new javax.swing.JButton();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        btnReports = new javax.swing.JButton();
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        btnUser = new javax.swing.JButton();
        jPanelFooter = new javax.swing.JPanel();
        jSeparatorFooter = new javax.swing.JSeparator();
        btnLogout = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Vetora - Veterinary Clinic Management");
        setBackground(new java.awt.Color(245, 247, 250));
        setMinimumSize(new java.awt.Dimension(1100, 680));
        setSize(new java.awt.Dimension(1440, 900));

        jPanelbody.setBackground(new java.awt.Color(245, 247, 250));
        jPanelbody.setPreferredSize(new java.awt.Dimension(1200, 700));
        jPanelbody.setLayout(new java.awt.BorderLayout());

        jPanelContent.setBackground(new java.awt.Color(245, 247, 250));
        jPanelContent.setBorder(javax.swing.BorderFactory.createEmptyBorder(21, 21, 21, 21));
        jPanelContent.setPreferredSize(new java.awt.Dimension(1173, 700));
        jPanelContent.setLayout(new java.awt.BorderLayout(0, 13));

        jPanelHeader.setBackground(new java.awt.Color(245, 247, 250));
        jPanelHeader.setPreferredSize(new java.awt.Dimension(0, 68));
        jPanelHeader.setLayout(new java.awt.BorderLayout(21, 0));

        jPanelHeaderUser.setBackground(new java.awt.Color(245, 247, 250));
        jPanelHeaderUser.setPreferredSize(new java.awt.Dimension(260, 68));
        jPanelHeaderUser.setLayout(new java.awt.BorderLayout(13, 5));

        jLabelAvatar.setBackground(new java.awt.Color(204, 251, 241));
        jLabelAvatar.setFont(new java.awt.Font("Segoe UI Emoji", 1, 21)); // NOI18N
        jLabelAvatar.setForeground(new java.awt.Color(15, 118, 110));
        jLabelAvatar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelAvatar.setText("👨‍💻");
        jLabelAvatar.setOpaque(true);
        jLabelAvatar.setPreferredSize(new java.awt.Dimension(42, 42));
        jPanelHeaderUser.add(jLabelAvatar, java.awt.BorderLayout.WEST);

        jLabelDate.setForeground(new java.awt.Color(71, 85, 105));
        jLabelDate.setText("13 oct 2026");
        jLabelDate.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelDate.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jPanelHeaderUser.add(jLabelDate, java.awt.BorderLayout.NORTH);

        pnlSignedInUser.setBackground(new java.awt.Color(245, 247, 250));
        pnlSignedInUser.setLayout(new java.awt.BorderLayout(13, 8));

        jLabelUserName.setForeground(new java.awt.Color(15, 23, 42));
        jLabelUserName.setText("Administrator");
        jLabelUserName.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        pnlSignedInUser.add(jLabelUserName, java.awt.BorderLayout.NORTH);

        jLabelUserRole.setForeground(new java.awt.Color(71, 85, 105));
        jLabelUserRole.setText("Clinic Staff");
        jLabelUserRole.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlSignedInUser.add(jLabelUserRole, java.awt.BorderLayout.CENTER);

        jPanelHeaderUser.add(pnlSignedInUser, java.awt.BorderLayout.CENTER);

        jPanelHeader.add(jPanelHeaderUser, java.awt.BorderLayout.EAST);

        jPanelHeading.setBackground(new java.awt.Color(245, 247, 250));
        jPanelHeading.setLayout(new java.awt.BorderLayout(0, 5));

        jLabelPageTitle.setForeground(new java.awt.Color(15, 23, 42));
        jLabelPageTitle.setText("Dashboard");
        jLabelPageTitle.setFont(new java.awt.Font("Segoe UI", 1, 34)); // NOI18N
        jPanelHeading.add(jLabelPageTitle, java.awt.BorderLayout.NORTH);

        jLabelPageSubtitle.setForeground(new java.awt.Color(71, 85, 105));
        jLabelPageSubtitle.setText("Your clinic at a glance");
        jLabelPageSubtitle.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jPanelHeading.add(jLabelPageSubtitle, java.awt.BorderLayout.CENTER);

        jPanelHeader.add(jPanelHeading, java.awt.BorderLayout.CENTER);

        jPanelContent.add(jPanelHeader, java.awt.BorderLayout.NORTH);

        jPanelView.setBackground(new java.awt.Color(245, 247, 250));
        jPanelView.setLayout(new java.awt.CardLayout());
        jPanelContent.add(jPanelView, java.awt.BorderLayout.CENTER);

        jPanelbody.add(jPanelContent, java.awt.BorderLayout.CENTER);

        jPanelSidebar.setBackground(new java.awt.Color(23, 43, 54));
        jPanelSidebar.setPreferredSize(new java.awt.Dimension(233, 0));
        jPanelSidebar.setLayout(new java.awt.BorderLayout());

        jPanelTitle.setBackground(new java.awt.Color(23, 43, 54));
        jPanelTitle.setPreferredSize(new java.awt.Dimension(233, 89));
        jPanelTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        jPanelTitle.setLayout(new java.awt.BorderLayout(8, 8));

        jLabelLogo.setFont(new java.awt.Font("Segoe UI Symbol", 1, 41)); // NOI18N
        jLabelLogo.setForeground(new java.awt.Color(0, 204, 102));
        jLabelLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelLogo.setText("🐾");
        jLabelLogo.setToolTipText("");
        jLabelLogo.setPreferredSize(new java.awt.Dimension(42, 42));
        jPanelTitle.add(jLabelLogo, java.awt.BorderLayout.WEST);

        pnlBrandText.setBackground(new java.awt.Color(23, 43, 54));
        pnlBrandText.setLayout(new java.awt.BorderLayout(8, 8));

        jLabelBrandName.setForeground(new java.awt.Color(255, 255, 255));
        jLabelBrandName.setText("Vetora");
        jLabelBrandName.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        pnlBrandText.add(jLabelBrandName, java.awt.BorderLayout.NORTH);

        jLabelBrandSubtitle.setForeground(new java.awt.Color(203, 213, 225));
        jLabelBrandSubtitle.setText("Clinic management");
        jLabelBrandSubtitle.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabelBrandSubtitle.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlBrandText.add(jLabelBrandSubtitle, java.awt.BorderLayout.CENTER);

        jPanelTitle.add(pnlBrandText, java.awt.BorderLayout.CENTER);

        jPanelSidebar.add(jPanelTitle, java.awt.BorderLayout.NORTH);

        jScrollPaneMenu.setBackground(new java.awt.Color(23, 43, 54));
        jScrollPaneMenu.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jScrollPaneMenu.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanelNavMenu.setBackground(new java.awt.Color(23, 43, 54));
        jPanelNavMenu.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        jPanelNavMenu.setLayout(new javax.swing.BoxLayout(jPanelNavMenu, javax.swing.BoxLayout.Y_AXIS));

        btnDashboard.setBackground(new java.awt.Color(15, 118, 110));
        btnDashboard.setFont(new java.awt.Font("Segoe UI Symbol", 1, 16)); // NOI18N
        btnDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnDashboard.setText("🏠  Dashboard");
        btnDashboard.setBorderPainted(false);
        btnDashboard.setContentAreaFilled(false);
        btnDashboard.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnDashboard.setIconTextGap(13);
        btnDashboard.setMargin(new java.awt.Insets(0, 13, 0, 13));
        btnDashboard.setMaximumSize(new java.awt.Dimension(207, 42));
        btnDashboard.setMinimumSize(new java.awt.Dimension(207, 42));
        btnDashboard.setOpaque(true);
        btnDashboard.setPreferredSize(new java.awt.Dimension(207, 42));
        jPanelNavMenu.add(btnDashboard);
        jPanelNavMenu.add(filler1);

        btnOwners.setBackground(new java.awt.Color(23, 43, 54));
        btnOwners.setFont(new java.awt.Font("Segoe UI Symbol", 0, 16)); // NOI18N
        btnOwners.setForeground(new java.awt.Color(203, 213, 255));
        btnOwners.setText("👨  Owners");
        btnOwners.setBorderPainted(false);
        btnOwners.setContentAreaFilled(false);
        btnOwners.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnOwners.setIconTextGap(13);
        btnOwners.setMargin(new java.awt.Insets(0, 13, 0, 13));
        btnOwners.setMaximumSize(new java.awt.Dimension(207, 42));
        btnOwners.setMinimumSize(new java.awt.Dimension(207, 42));
        btnOwners.setOpaque(true);
        btnOwners.setPreferredSize(new java.awt.Dimension(207, 42));
        jPanelNavMenu.add(btnOwners);
        jPanelNavMenu.add(filler2);

        btnPets.setBackground(new java.awt.Color(23, 43, 54));
        btnPets.setFont(new java.awt.Font("Segoe UI Symbol", 0, 16)); // NOI18N
        btnPets.setForeground(new java.awt.Color(203, 213, 255));
        btnPets.setText("🐶  Pets");
        btnPets.setBorderPainted(false);
        btnPets.setContentAreaFilled(false);
        btnPets.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnPets.setIconTextGap(13);
        btnPets.setMargin(new java.awt.Insets(0, 13, 0, 13));
        btnPets.setMaximumSize(new java.awt.Dimension(207, 42));
        btnPets.setMinimumSize(new java.awt.Dimension(207, 42));
        btnPets.setOpaque(true);
        btnPets.setPreferredSize(new java.awt.Dimension(207, 42));
        jPanelNavMenu.add(btnPets);
        jPanelNavMenu.add(filler3);

        btnAppointments.setBackground(new java.awt.Color(23, 43, 54));
        btnAppointments.setFont(new java.awt.Font("Segoe UI Symbol", 0, 16)); // NOI18N
        btnAppointments.setForeground(new java.awt.Color(203, 213, 255));
        btnAppointments.setText("🗓️  Appointments");
        btnAppointments.setBorderPainted(false);
        btnAppointments.setContentAreaFilled(false);
        btnAppointments.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnAppointments.setIconTextGap(13);
        btnAppointments.setMargin(new java.awt.Insets(0, 13, 0, 13));
        btnAppointments.setMaximumSize(new java.awt.Dimension(207, 42));
        btnAppointments.setMinimumSize(new java.awt.Dimension(207, 42));
        btnAppointments.setOpaque(true);
        btnAppointments.setPreferredSize(new java.awt.Dimension(207, 42));
        jPanelNavMenu.add(btnAppointments);
        jPanelNavMenu.add(filler5);

        btnConsultations.setBackground(new java.awt.Color(23, 43, 54));
        btnConsultations.setFont(new java.awt.Font("Segoe UI Symbol", 0, 16)); // NOI18N
        btnConsultations.setForeground(new java.awt.Color(203, 213, 255));
        btnConsultations.setText("💊  Consultations");
        btnConsultations.setBorderPainted(false);
        btnConsultations.setContentAreaFilled(false);
        btnConsultations.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnConsultations.setIconTextGap(13);
        btnConsultations.setMargin(new java.awt.Insets(0, 13, 0, 13));
        btnConsultations.setMaximumSize(new java.awt.Dimension(207, 42));
        btnConsultations.setMinimumSize(new java.awt.Dimension(207, 42));
        btnConsultations.setOpaque(true);
        btnConsultations.setPreferredSize(new java.awt.Dimension(207, 42));
        jPanelNavMenu.add(btnConsultations);
        jPanelNavMenu.add(filler6);

        btnBilling.setBackground(new java.awt.Color(23, 43, 54));
        btnBilling.setFont(new java.awt.Font("Segoe UI Symbol", 0, 16)); // NOI18N
        btnBilling.setForeground(new java.awt.Color(203, 213, 255));
        btnBilling.setText("💵  Billing");
        btnBilling.setBorderPainted(false);
        btnBilling.setContentAreaFilled(false);
        btnBilling.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnBilling.setIconTextGap(13);
        btnBilling.setMargin(new java.awt.Insets(0, 13, 0, 13));
        btnBilling.setMaximumSize(new java.awt.Dimension(207, 42));
        btnBilling.setMinimumSize(new java.awt.Dimension(207, 42));
        btnBilling.setOpaque(true);
        btnBilling.setPreferredSize(new java.awt.Dimension(207, 42));
        jPanelNavMenu.add(btnBilling);
        jPanelNavMenu.add(filler7);

        btnReports.setBackground(new java.awt.Color(23, 43, 54));
        btnReports.setFont(new java.awt.Font("Segoe UI Symbol", 0, 16)); // NOI18N
        btnReports.setForeground(new java.awt.Color(203, 213, 255));
        btnReports.setText("📋  Reports");
        btnReports.setBorderPainted(false);
        btnReports.setContentAreaFilled(false);
        btnReports.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnReports.setIconTextGap(13);
        btnReports.setMargin(new java.awt.Insets(0, 13, 0, 13));
        btnReports.setMaximumSize(new java.awt.Dimension(207, 42));
        btnReports.setMinimumSize(new java.awt.Dimension(207, 42));
        btnReports.setOpaque(true);
        btnReports.setPreferredSize(new java.awt.Dimension(207, 42));
        jPanelNavMenu.add(btnReports);
        jPanelNavMenu.add(filler8);

        btnUser.setBackground(new java.awt.Color(23, 43, 54));
        btnUser.setFont(new java.awt.Font("Segoe UI Symbol", 0, 16)); // NOI18N
        btnUser.setForeground(new java.awt.Color(203, 213, 255));
        btnUser.setText("👤  Users");
        btnUser.setBorderPainted(false);
        btnUser.setContentAreaFilled(false);
        btnUser.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnUser.setIconTextGap(13);
        btnUser.setMargin(new java.awt.Insets(0, 13, 0, 13));
        btnUser.setMaximumSize(new java.awt.Dimension(207, 42));
        btnUser.setMinimumSize(new java.awt.Dimension(207, 42));
        btnUser.setOpaque(true);
        btnUser.setPreferredSize(new java.awt.Dimension(207, 42));
        jPanelNavMenu.add(btnUser);

        jScrollPaneMenu.setViewportView(jPanelNavMenu);

        jPanelSidebar.add(jScrollPaneMenu, java.awt.BorderLayout.CENTER);

        jPanelFooter.setBackground(new java.awt.Color(23, 43, 54));
        jPanelFooter.setPreferredSize(new java.awt.Dimension(233, 68));
        jPanelFooter.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 13, 8, 13));
        jPanelFooter.setLayout(new java.awt.BorderLayout(0, 8));
        jPanelFooter.add(jSeparatorFooter, java.awt.BorderLayout.NORTH);

        btnLogout.setBackground(new java.awt.Color(23, 43, 54));
        btnLogout.setForeground(new java.awt.Color(203, 213, 255));
        btnLogout.setText("Logout");
        btnLogout.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLogout.setIconTextGap(13);
        btnLogout.setMargin(new java.awt.Insets(0, 13, 0, 13));
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setOpaque(true);
        btnLogout.setMinimumSize(new java.awt.Dimension(207, 42));
        btnLogout.setPreferredSize(new java.awt.Dimension(207, 42));
        btnLogout.setMaximumSize(new java.awt.Dimension(207, 42));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnLogout.addActionListener(this::btnLogoutActionPerformed);
        jPanelFooter.add(btnLogout, java.awt.BorderLayout.CENTER);

        jPanelSidebar.add(jPanelFooter, java.awt.BorderLayout.SOUTH);

        jPanelbody.add(jPanelSidebar, java.awt.BorderLayout.WEST);

        getContentPane().add(jPanelbody, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        accessControl.endSession();
        FormLogin loginForm = new FormLogin();
        loginForm.setController(new LoginController(new UserModel(), loginForm));
        loginForm.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnLogoutActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAppointments;
    private javax.swing.JButton btnBilling;
    private javax.swing.JButton btnConsultations;
    private javax.swing.JButton btnDashboard;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnOwners;
    private javax.swing.JButton btnPets;
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnUser;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.JLabel jLabelAvatar;
    private javax.swing.JLabel jLabelBrandName;
    private javax.swing.JLabel jLabelBrandSubtitle;
    private javax.swing.JLabel jLabelDate;
    private javax.swing.JLabel jLabelLogo;
    private javax.swing.JLabel jLabelPageSubtitle;
    private javax.swing.JLabel jLabelPageTitle;
    private javax.swing.JLabel jLabelUserName;
    private javax.swing.JLabel jLabelUserRole;
    private javax.swing.JPanel jPanelContent;
    private javax.swing.JPanel jPanelFooter;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JPanel jPanelHeaderUser;
    private javax.swing.JPanel jPanelHeading;
    private javax.swing.JPanel jPanelNavMenu;
    private javax.swing.JPanel jPanelSidebar;
    private javax.swing.JPanel jPanelTitle;
    private javax.swing.JPanel jPanelView;
    private javax.swing.JPanel jPanelbody;
    private javax.swing.JScrollPane jScrollPaneMenu;
    private javax.swing.JSeparator jSeparatorFooter;
    private javax.swing.JPanel pnlBrandText;
    private javax.swing.JPanel pnlSignedInUser;
    // End of variables declaration//GEN-END:variables
}
