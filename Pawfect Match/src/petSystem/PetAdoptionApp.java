package petSystem;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PetAdoptionApp extends JFrame {
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private static final String DB_USER = "system";
    private static final String DB_PASSWORD = "password";

    private Connection connection;
    private JTabbedPane tabbedPane;
    private BackgroundPanel loginPanel, registrationPanel, homePanel, petPanel, adopterPanel, adoptionPanel, statusPanel;
    private JTextField loginUsername, loginPassword, regUsername, regPassword;
    private JTextField petName, petSpecies, petAge, petIdToUpdate;
    private JTextField adopterFirstName, adopterLastName, adopterPhone, adopterIdToUpdate;
    private JTextField adoptionPetId, adoptionAdopterId;
    private JButton loginButton, registerButton, logoutButton, addPetButton, editPetButton, clearPetButton;
    private JButton addAdopterButton, editAdopterButton, clearAdopterButton;
    private JButton addAdoptionButton, updateStatusButton, helpButton;
    private JTable petTable, adopterTable, adoptionTable, statusTable;
    private JComboBox<String> statusComboBox;
    private boolean isEditingPet = false;
    private boolean isEditingAdopter = false;
    private ImageIcon backgroundImage;
    private Image scaledImage;

    // Custom JPanel class for consistent background
    private class BackgroundPanel extends JPanel {
        public BackgroundPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (scaledImage != null) {
                g.drawImage(scaledImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public PetAdoptionApp() {
        initializeDatabase();
        setTitle("Pawfect Match : Pet Adoption Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            backgroundImage = new ImageIcon(getClass().getResource("/pet1.jpg"));
            scaledImage = backgroundImage.getImage().getScaledInstance(800, 600, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            System.out.println("Background image not found, using default background.");
        }

        tabbedPane = new JTabbedPane();
        createLoginPanel();
        createRegistrationPanel();
        createHomePanel();
        createPetPanel();
        createAdopterPanel();
        createAdoptionPanel();
        createStatusPanel();

        tabbedPane.addTab("Login", loginPanel);
        tabbedPane.addTab("Registration", registrationPanel);
        tabbedPane.addTab("Home", homePanel);
        tabbedPane.addTab("Pets", petPanel);
        tabbedPane.addTab("Adopters", adopterPanel);
        tabbedPane.addTab("Adoptions", adoptionPanel);
        tabbedPane.addTab("Update Status", statusPanel);

        for (int i = 2; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setEnabledAt(i, false);
        }

        add(tabbedPane);
        tabbedPane.setSelectedIndex(0);
        createMenuBar();
    }

    private void initializeDatabase() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the database.");
            createTablesIfNotExist();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createTablesIfNotExist() {
        try {
            Statement stmt = connection.createStatement();

            // Drop existing tables and sequences
            try {
                stmt.execute("DROP TABLE Adoptions");
                stmt.execute("DROP TABLE Adopters");
                stmt.execute("DROP TABLE Pets");
                stmt.execute("DROP TABLE Users");
                stmt.execute("DROP SEQUENCE adoption_seq");
                stmt.execute("DROP SEQUENCE adopter_seq");
                stmt.execute("DROP SEQUENCE pet_seq");
                stmt.execute("DROP SEQUENCE user_seq");
            } catch (SQLException e) {
                // Ignore if they don’t exist
            }

            // Create sequences
            stmt.execute("CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1 NOCACHE");
            stmt.execute("CREATE SEQUENCE pet_seq START WITH 1 INCREMENT BY 1 NOCACHE");
            stmt.execute("CREATE SEQUENCE adopter_seq START WITH 1 INCREMENT BY 1 NOCACHE");
            stmt.execute("CREATE SEQUENCE adoption_seq START WITH 1 INCREMENT BY 1 NOCACHE");

            // Create tables
            stmt.execute("CREATE TABLE Users (user_id NUMBER PRIMARY KEY, username VARCHAR2(50) UNIQUE NOT NULL, password VARCHAR2(50) NOT NULL, role VARCHAR2(20) NOT NULL)");
            stmt.execute("CREATE TABLE Pets (pet_id NUMBER PRIMARY KEY, pet_name VARCHAR2(50) NOT NULL, species VARCHAR2(50) NOT NULL, age NUMBER NOT NULL)");
            stmt.execute("CREATE TABLE Adopters (adopter_id NUMBER PRIMARY KEY, first_name VARCHAR2(50) NOT NULL, last_name VARCHAR2(50) NOT NULL, phone VARCHAR2(20) NOT NULL)");
            stmt.execute("CREATE TABLE Adoptions (adoption_id NUMBER PRIMARY KEY, adopter_id NUMBER REFERENCES Adopters(adopter_id), pet_id NUMBER REFERENCES Pets(pet_id), adoption_date DATE NOT NULL, status VARCHAR2(20) NOT NULL)");

            stmt.close();
            System.out.println("Database tables and sequences created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to initialize database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void createLoginPanel() {
        loginPanel = new BackgroundPanel(new GridBagLayout());

        JPanel loginFormPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        loginFormPanel.setOpaque(false);
        loginFormPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        loginUsername = new JTextField(15);
        loginPassword = new JPasswordField(15);
        loginButton = new JButton("Login");
        styleButton(loginButton);

        loginFormPanel.add(new JLabel("Username:"));
        loginFormPanel.add(loginUsername);
        loginFormPanel.add(new JLabel("Password:"));
        loginFormPanel.add(loginPassword);
        loginFormPanel.add(new JLabel());
        loginFormPanel.add(loginButton);

        loginPanel.add(loginFormPanel);

        loginButton.addActionListener(e -> {
            String username = loginUsername.getText();
            String password = loginPassword.getText();
            if (validateCredentials(username, password)) {
                JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                for (int i = 2; i < tabbedPane.getTabCount(); i++) {
                    tabbedPane.setEnabledAt(i, true);
                }
                tabbedPane.setSelectedIndex(2);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void createRegistrationPanel() {
        registrationPanel = new BackgroundPanel(new GridBagLayout());

        JPanel regFormPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        regFormPanel.setOpaque(false);
        regFormPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        regUsername = new JTextField(15);
        regPassword = new JPasswordField(15);
        registerButton = new JButton("Register");
        styleButton(registerButton);

        regFormPanel.add(new JLabel("Username:"));
        regFormPanel.add(regUsername);
        regFormPanel.add(new JLabel("Password:"));
        regFormPanel.add(regPassword);
        regFormPanel.add(new JLabel());
        regFormPanel.add(registerButton);

        registrationPanel.add(regFormPanel);

        registerButton.addActionListener(e -> {
            String username = regUsername.getText();
            String password = regPassword.getText();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (registerUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                tabbedPane.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed. Username may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void createHomePanel() {
        homePanel = new BackgroundPanel(new BorderLayout());
        homePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome to the Pet Adoption Management System!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.setOpaque(false);

        helpButton = new JButton("Help");
        logoutButton = new JButton("Logout");
        styleButton(helpButton);
        styleButton(logoutButton);

        buttonsPanel.add(helpButton);
        buttonsPanel.add(logoutButton);

        contentPanel.add(welcomeLabel, BorderLayout.CENTER);
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);

        homePanel.add(contentPanel, BorderLayout.CENTER);

        helpButton.addActionListener(e -> {
            String helpContent = "1. Login or Register to access the system.\n" +
                    "2. Use the 'Pets' tab to add, edit, or delete pets.\n" +
                    "3. Use the 'Adopters' tab to add, edit, or delete adopters.\n" +
                    "4. Use the 'Adoptions' tab to create new adoptions.\n" +
                    "5. Use the 'Update Status' tab to update adoption status.\n" +
                    "6. Logout when done.";
            JOptionPane.showMessageDialog(this, helpContent, "Help", JOptionPane.INFORMATION_MESSAGE);
        });

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                for (int i = 2; i < tabbedPane.getTabCount(); i++) {
                    tabbedPane.setEnabledAt(i, false);
                }
                tabbedPane.setSelectedIndex(0);
                loginUsername.setText("");
                loginPassword.setText("");
            }
        });
    }

    private void createPetPanel() {
        petPanel = new BackgroundPanel(new BorderLayout());
        petPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Pet Information"));
        inputPanel.setOpaque(false);

        petName = new JTextField(20);
        petSpecies = new JTextField(20);
        petAge = new JTextField(5);
        petIdToUpdate = new JTextField(5);
        petIdToUpdate.setEditable(false);

        inputPanel.add(new JLabel("Pet Name:"));
        inputPanel.add(petName);
        inputPanel.add(new JLabel("Species:"));
        inputPanel.add(petSpecies);
        inputPanel.add(new JLabel("Age:"));
        inputPanel.add(petAge);
        inputPanel.add(new JLabel("Pet ID (for update):"));
        inputPanel.add(petIdToUpdate);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        addPetButton = new JButton("Add Pet");
        editPetButton = new JButton("Update Pet");
        clearPetButton = new JButton("Clear");
        styleButton(addPetButton);
        styleButton(editPetButton);
        styleButton(clearPetButton);

        buttonPanel.add(addPetButton);
        buttonPanel.add(editPetButton);
        buttonPanel.add(clearPetButton);

        inputPanel.add(new JLabel());
        inputPanel.add(buttonPanel);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Pet List"));
        tablePanel.setOpaque(false);

        petTable = new JTable();
        petTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        petTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollPane = new JScrollPane(petTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        petPanel.add(inputPanel, BorderLayout.NORTH);
        petPanel.add(tablePanel, BorderLayout.CENTER);

        addPetButton.addActionListener(e -> {
            if (isEditingPet) {
                JOptionPane.showMessageDialog(this, "Finish editing or clear the form.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (validatePetInput()) {
                addPet();
                clearPetFields();
            }
        });

        editPetButton.addActionListener(e -> {
            if (isEditingPet) {
                updatePet();
                isEditingPet = false;
                addPetButton.setEnabled(true);
                editPetButton.setText("Update Pet");
                clearPetFields();
            } else {
                JOptionPane.showMessageDialog(this, "Select a pet to edit from the table.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        clearPetButton.addActionListener(e -> {
            clearPetFields();
            isEditingPet = false;
            addPetButton.setEnabled(true);
            editPetButton.setText("Update Pet");
        });

        petTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && petTable.getSelectedRow() != -1) {
                int selectedRow = petTable.getSelectedRow();
                petIdToUpdate.setText(petTable.getValueAt(selectedRow, 0).toString());
                petName.setText(petTable.getValueAt(selectedRow, 1).toString());
                petSpecies.setText(petTable.getValueAt(selectedRow, 2).toString());
                petAge.setText(petTable.getValueAt(selectedRow, 3).toString());
                isEditingPet = true;
                addPetButton.setEnabled(false);
                editPetButton.setText("Save Changes");
            }
        });

        loadPets();
    }

    private void createAdopterPanel() {
        adopterPanel = new BackgroundPanel(new BorderLayout());
        adopterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Adopter Information"));
        inputPanel.setOpaque(false);

        adopterFirstName = new JTextField(20);
        adopterLastName = new JTextField(20);
        adopterPhone = new JTextField(15);
        adopterIdToUpdate = new JTextField(5);
        adopterIdToUpdate.setEditable(false);

        inputPanel.add(new JLabel("First Name:"));
        inputPanel.add(adopterFirstName);
        inputPanel.add(new JLabel("Last Name:"));
        inputPanel.add(adopterLastName);
        inputPanel.add(new JLabel("Phone:"));
        inputPanel.add(adopterPhone);
        inputPanel.add(new JLabel("Adopter ID (for update):"));
        inputPanel.add(adopterIdToUpdate);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        addAdopterButton = new JButton("Add Adopter");
        editAdopterButton = new JButton("Update Adopter");
        clearAdopterButton = new JButton("Clear");
        styleButton(addAdopterButton);
        styleButton(editAdopterButton);
        styleButton(clearAdopterButton);

        buttonPanel.add(addAdopterButton);
        buttonPanel.add(editAdopterButton);
        buttonPanel.add(clearAdopterButton);

        inputPanel.add(new JLabel());
        inputPanel.add(buttonPanel);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Adopter List"));
        tablePanel.setOpaque(false);

        adopterTable = new JTable();
        adopterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        adopterTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollPane = new JScrollPane(adopterTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        adopterPanel.add(inputPanel, BorderLayout.NORTH);
        adopterPanel.add(tablePanel, BorderLayout.CENTER);

        addAdopterButton.addActionListener(e -> {
            if (isEditingAdopter) {
                JOptionPane.showMessageDialog(this, "Finish editing or clear the form.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (validateAdopterInput()) {
                addAdopter();
                clearAdopterFields();
            }
        });

        editAdopterButton.addActionListener(e -> {
            if (isEditingAdopter) {
                updateAdopter();
                isEditingAdopter = false;
                addAdopterButton.setEnabled(true);
                editAdopterButton.setText("Update Adopter");
                clearAdopterFields();
            } else {
                JOptionPane.showMessageDialog(this, "Select an adopter to edit from the table.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        clearAdopterButton.addActionListener(e -> {
            clearAdopterFields();
            isEditingAdopter = false;
            addAdopterButton.setEnabled(true);
            editAdopterButton.setText("Update Adopter");
        });

        adopterTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && adopterTable.getSelectedRow() != -1) {
                int selectedRow = adopterTable.getSelectedRow();
                adopterIdToUpdate.setText(adopterTable.getValueAt(selectedRow, 0).toString());
                adopterFirstName.setText(adopterTable.getValueAt(selectedRow, 1).toString());
                adopterLastName.setText(adopterTable.getValueAt(selectedRow, 2).toString());
                adopterPhone.setText(adopterTable.getValueAt(selectedRow, 3).toString());
                isEditingAdopter = true;
                addAdopterButton.setEnabled(false);
                editAdopterButton.setText("Save Changes");
            }
        });

        loadAdopters();
    }

    private void createAdoptionPanel() {
        adoptionPanel = new BackgroundPanel(new BorderLayout());
        adoptionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Adoption Information"));
        inputPanel.setOpaque(false);

        adoptionPetId = new JTextField(5);
        adoptionAdopterId = new JTextField(5);
        addAdoptionButton = new JButton("Add Adoption");
        styleButton(addAdoptionButton);

        inputPanel.add(new JLabel("Pet ID:"));
        inputPanel.add(adoptionPetId);
        inputPanel.add(new JLabel("Adopter ID:"));
        inputPanel.add(adoptionAdopterId);
        inputPanel.add(new JLabel());
        inputPanel.add(addAdoptionButton);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Adoption List"));
        tablePanel.setOpaque(false);

        adoptionTable = new JTable();
        adoptionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        adoptionTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollPane = new JScrollPane(adoptionTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        adoptionPanel.add(inputPanel, BorderLayout.NORTH);
        adoptionPanel.add(tablePanel, BorderLayout.CENTER);

        addAdoptionButton.addActionListener(e -> {
            if (validateAdoptionInput()) {
                addAdoption();
                clearAdoptionFields();
            }
        });

        loadAdoptions();
    }

    private void createStatusPanel() {
        statusPanel = new BackgroundPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Update Adoption Status"));
        inputPanel.setOpaque(false);

        statusComboBox = new JComboBox<>(new String[]{"Pending", "Completed", "Cancelled"});
        updateStatusButton = new JButton("Update Status");
        styleButton(updateStatusButton);

        inputPanel.add(new JLabel("Select Status:"));
        inputPanel.add(statusComboBox);
        inputPanel.add(new JLabel());
        inputPanel.add(updateStatusButton);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Adoption List"));
        tablePanel.setOpaque(false);

        statusTable = new JTable();
        statusTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        statusTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollPane = new JScrollPane(statusTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        statusPanel.add(inputPanel, BorderLayout.NORTH);
        statusPanel.add(tablePanel, BorderLayout.CENTER);

        updateStatusButton.addActionListener(e -> {
            int selectedRow = statusTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an adoption to update.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int adoptionId = (Integer) statusTable.getValueAt(selectedRow, 0);
            String newStatus = (String) statusComboBox.getSelectedItem();

            try (Connection conn = getConnection()) {
                String updateQuery = "UPDATE Adoptions SET status = ? WHERE adoption_id = ?";
                PreparedStatement stmt = conn.prepareStatement(updateQuery);
                stmt.setString(1, newStatus);
                stmt.setInt(2, adoptionId);
                int result = stmt.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Status updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAdoptionsForStatus();
                    loadPets();
                    loadAdopters();
                    loadAdoptions();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loadAdoptionsForStatus();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(100, 25));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setMargin(new Insets(2, 5, 2, 5));
        button.setBackground(new Color(220, 220, 220));
        button.setBorder(BorderFactory.createRaisedBevelBorder());
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem homeItem = new JMenuItem("Home");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");

        fileMenu.add(homeItem);
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu managementMenu = new JMenu("Management");
        JMenuItem petsItem = new JMenuItem("Pets");
        JMenuItem adoptersItem = new JMenuItem("Adopters");
        JMenuItem adoptionsItem = new JMenuItem("Adoptions");
        JMenuItem statusItem = new JMenuItem("Update Status");

        managementMenu.add(petsItem);
        managementMenu.add(adoptersItem);
        managementMenu.add(adoptionsItem);
        managementMenu.add(statusItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        JMenuItem helpItem = new JMenuItem("Help");

        helpMenu.add(aboutItem);
        helpMenu.add(helpItem);

        menuBar.add(fileMenu);
        menuBar.add(managementMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        homeItem.addActionListener(e -> tabbedPane.setSelectedIndex(2));
        logoutItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                for (int i = 2; i < tabbedPane.getTabCount(); i++) {
                    tabbedPane.setEnabledAt(i, false);
                }
                tabbedPane.setSelectedIndex(0);
                loginUsername.setText("");
                loginPassword.setText("");
            }
        });
        exitItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) System.exit(0);
        });

        petsItem.addActionListener(e -> tabbedPane.setSelectedIndex(3));
        adoptersItem.addActionListener(e -> tabbedPane.setSelectedIndex(4));
        adoptionsItem.addActionListener(e -> tabbedPane.setSelectedIndex(5));
        statusItem.addActionListener(e -> tabbedPane.setSelectedIndex(6));

        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Pet Adoption Management System\nVersion 1.0\nDeveloped by: Shreishta Manoj", "About", JOptionPane.INFORMATION_MESSAGE));
        helpItem.addActionListener(e -> {
            String helpContent = "1. Login or Register to access the system.\n" +
                    "2. Use the 'Pets' tab to add, edit, or delete pets.\n" +
                    "3. Use the 'Adopters' tab to add, edit, or delete adopters.\n" +
                    "4. Use the 'Adoptions' tab to create new adoptions.\n" +
                    "5. Use the 'Update Status' tab to update adoption status.\n" +
                    "6. Logout when done.";
            JOptionPane.showMessageDialog(this, helpContent, "Help", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private boolean validateCredentials(String username, String password) {
        String query = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean registerUser(String username, String password) {
        String query = "INSERT INTO Users (user_id, username, password, role) VALUES (user_seq.NEXTVAL, ?, ?, 'user')";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean validatePetInput() {
        if (petName.getText().trim().isEmpty() || petSpecies.getText().trim().isEmpty() || petAge.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All pet fields must be filled.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int age = Integer.parseInt(petAge.getText().trim());
            if (age < 0) {
                JOptionPane.showMessageDialog(this, "Age must be positive.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Age must be a number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validateAdopterInput() {
        if (adopterFirstName.getText().trim().isEmpty() || adopterLastName.getText().trim().isEmpty() || adopterPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All adopter fields must be filled.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validateAdoptionInput() {
        try {
            int petId = Integer.parseInt(adoptionPetId.getText().trim());
            int adopterId = Integer.parseInt(adoptionAdopterId.getText().trim());
            if (!checkIfExists("Pets", "pet_id", petId) || !checkIfExists("Adopters", "adopter_id", adopterId) || isPetAdopted(petId)) {
                JOptionPane.showMessageDialog(this, "Invalid Pet ID, Adopter ID, or pet already adopted.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Pet ID and Adopter ID must be numbers.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean checkIfExists(String table, String column, int id) {
        String query = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isPetAdopted(int petId) {
        String query = "SELECT COUNT(*) FROM Adoptions WHERE pet_id = ? AND status = 'Completed'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, petId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addPet() {
        String query = "INSERT INTO Pets (pet_id, pet_name, species, age) VALUES (pet_seq.NEXTVAL, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, petName.getText().trim());
            stmt.setString(2, petSpecies.getText().trim());
            stmt.setInt(3, Integer.parseInt(petAge.getText().trim()));
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Pet added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPets();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add pet: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePet() {
        String query = "UPDATE Pets SET pet_name = ?, species = ?, age = ? WHERE pet_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, petName.getText().trim());
            stmt.setString(2, petSpecies.getText().trim());
            stmt.setInt(3, Integer.parseInt(petAge.getText().trim()));
            stmt.setInt(4, Integer.parseInt(petIdToUpdate.getText().trim()));
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Pet updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPets();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update pet: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePet(int petId) {
        String query = "DELETE FROM Pets WHERE pet_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, petId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Pet deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPets();
            } else {
                JOptionPane.showMessageDialog(this, "No pet found with ID: " + petId, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete pet: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addAdopter() {
        String query = "INSERT INTO Adopters (adopter_id, first_name, last_name, phone) VALUES (adopter_seq.NEXTVAL, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, adopterFirstName.getText().trim());
            stmt.setString(2, adopterLastName.getText().trim());
            stmt.setString(3, adopterPhone.getText().trim());
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Adopter added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAdopters();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add adopter: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAdopter() {
        String query = "UPDATE Adopters SET first_name = ?, last_name = ?, phone = ? WHERE adopter_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, adopterFirstName.getText().trim());
            stmt.setString(2, adopterLastName.getText().trim());
            stmt.setString(3, adopterPhone.getText().trim());
            stmt.setInt(4, Integer.parseInt(adopterIdToUpdate.getText().trim()));
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Adopter updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAdopters();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update adopter: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAdopter(int adopterId) {
        String query = "DELETE FROM Adopters WHERE adopter_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, adopterId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Adopter deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAdopters();
            } else {
                JOptionPane.showMessageDialog(this, "No adopter found with ID: " + adopterId, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete adopter: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addAdoption() {
        String query = "INSERT INTO Adoptions (adoption_id, adopter_id, pet_id, adoption_date, status) VALUES (adoption_seq.NEXTVAL, ?, ?, SYSDATE, 'Pending')";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(adoptionAdopterId.getText().trim()));
            stmt.setInt(2, Integer.parseInt(adoptionPetId.getText().trim()));
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Adoption added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAdoptions();
            loadAdoptionsForStatus();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add adoption: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPets() {
        String query = "SELECT p.pet_id, p.pet_name, p.species, p.age " +
                       "FROM Pets p " +
                       "WHERE NOT EXISTS (SELECT 1 FROM Adoptions a WHERE a.pet_id = p.pet_id AND a.status = 'Completed') " +
                       "ORDER BY p.pet_id";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = buildTableModel(rs);
            model.addColumn("Actions");
            petTable.setModel(model);
            TableColumn actionColumn = petTable.getColumnModel().getColumn(petTable.getColumnCount() - 1);
            actionColumn.setCellRenderer(new ButtonRenderer());
            actionColumn.setCellEditor(new ButtonEditor(new JCheckBox(), petTable, this::deletePet));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load pets: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAdopters() {
        String query = "SELECT a.adopter_id, a.first_name, a.last_name, a.phone " +
                       "FROM Adopters a " +
                       "WHERE NOT EXISTS (SELECT 1 FROM Adoptions ad WHERE ad.adopter_id = a.adopter_id AND ad.status = 'Completed') " +
                       "ORDER BY a.adopter_id";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = buildTableModel(rs);
            model.addColumn("Actions");
            adopterTable.setModel(model);
            TableColumn actionColumn = adopterTable.getColumnModel().getColumn(adopterTable.getColumnCount() - 1);
            actionColumn.setCellRenderer(new ButtonRenderer());
            actionColumn.setCellEditor(new ButtonEditor(new JCheckBox(), adopterTable, this::deleteAdopter));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load adopters: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAdoptions() {
        String query = "SELECT a.adoption_id, a.pet_id, p.pet_name, a.adopter_id, ad.first_name || ' ' || ad.last_name AS adopter_name, a.adoption_date, a.status " +
                       "FROM Adoptions a " +
                       "JOIN Pets p ON a.pet_id = p.pet_id " +
                       "JOIN Adopters ad ON a.adopter_id = ad.adopter_id " +
                       "ORDER BY a.adoption_id";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            adoptionTable.setModel(buildTableModel(rs));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load adoptions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAdoptionsForStatus() {
        String query = "SELECT a.adoption_id, a.pet_id, p.pet_name, a.adopter_id, ad.first_name || ' ' || ad.last_name AS adopter_name, a.adoption_date, a.status " +
                       "FROM Adoptions a " +
                       "JOIN Pets p ON a.pet_id = p.pet_id " +
                       "JOIN Adopters ad ON a.adopter_id = ad.adopter_id " +
                       "WHERE a.status != 'Completed' " +
                       "ORDER BY a.adoption_id";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            statusTable.setModel(buildTableModel(rs));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load adoptions for status: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearPetFields() {
        petName.setText("");
        petSpecies.setText("");
        petAge.setText("");
        petIdToUpdate.setText("");
    }

    private void clearAdopterFields() {
        adopterFirstName.setText("");
        adopterLastName.setText("");
        adopterPhone.setText("");
        adopterIdToUpdate.setText("");
    }

    private void clearAdoptionFields() {
        adoptionPetId.setText("");
        adoptionAdopterId.setText("");
    }

    private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i - 1] = metaData.getColumnName(i);
        }
        List<Object[]> data = new ArrayList<>();
        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                if (metaData.getColumnTypeName(i).equals("NUMBER")) {
                    row[i - 1] = rs.getInt(i);
                } else {
                    row[i - 1] = rs.getObject(i);
                }
            }
            data.add(row);
        }
        return new DefaultTableModel(data.toArray(new Object[0][0]), columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == getColumnCount() - 1;
            }
        };
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText("Delete");
            setForeground(Color.RED);
            setPreferredSize(new Dimension(80, 20));
            setFont(new Font("Arial", Font.PLAIN, 11));
            setBorder(BorderFactory.createRaisedBevelBorder());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(Color.RED);
            }
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private JTable table;
        private java.util.function.Consumer<Integer> deleteAction;

        public ButtonEditor(JCheckBox checkBox, JTable table, java.util.function.Consumer<Integer> deleteAction) {
            super(checkBox);
            this.table = table;
            this.deleteAction = deleteAction;
            button = new JButton("Delete");
            button.setOpaque(true);
            button.setForeground(Color.RED);
            button.setPreferredSize(new Dimension(80, 20));
            button.setFont(new Font("Arial", Font.PLAIN, 11));
            button.setBorder(BorderFactory.createRaisedBevelBorder());
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            button.addActionListener(e -> {
                int id = (Integer) table.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(table, "Are you sure you want to delete this record?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteAction.accept(id);
                }
            });
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Delete";
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            PetAdoptionApp app = new PetAdoptionApp();
            app.setVisible(true);
        });
    }
}
