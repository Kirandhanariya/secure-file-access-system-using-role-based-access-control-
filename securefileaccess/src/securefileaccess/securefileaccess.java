package securefileaccess;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class securefileaccess {

    // =========================================================
    // FILE STORAGE
    // =========================================================

    static final String DB_FILE = "SecureFileAccess.db";
    static final String FILE_FOLDER = "secure_files";

    // =========================================================
    // DATABASE LISTS
    // =========================================================

    static List<User> users = new ArrayList<>();
    static List<FileRecord> files = new ArrayList<>();
    static List<Activity> activities = new ArrayList<>();

    static User currentUser;

    static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // =========================================================
    // COLORS
    // =========================================================

    static final Color NAVY = new Color(18, 32, 55);
    static final Color BLUE = new Color(45, 105, 190);
    static final Color GREEN = new Color(40, 145, 95);
    static final Color RED = new Color(200, 65, 65);

    static final Color BACKGROUND =
            new Color(246, 248, 252);

    static final Color WHITE = Color.WHITE;

    static final Color TEXT =
            new Color(35, 45, 60);

    static final Color MUTED =
            new Color(105, 115, 130);

    static final Color BORDER =
            new Color(220, 225, 235);

    // =========================================================
    // MAIN METHOD
    // =========================================================

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            // Default look and feel
        }

        loadDatabase();

        // Create default accounts if database is empty
        if (users.isEmpty()) {

            users.add(
                    new User(
                            "admin",
                            "admin123",
                            "ADMIN"
                    )
            );

            users.add(
                    new User(
                            "kiran",
                            "user123",
                            "USER"
                    )
            );

            saveDatabase();
        }

        // Create file storage folder
        File folder = new File(FILE_FOLDER);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        SwingUtilities.invokeLater(
                () -> showLogin()
        );
    }

    // =========================================================
    // USER CLASS
    // =========================================================

    static class User implements Serializable {

        private static final long serialVersionUID = 1L;

        String username;
        String password;
        String role;

        User(
                String username,
                String password,
                String role
        ) {

            this.username = username;
            this.password = password;
            this.role = role;
        }
    }

    // =========================================================
    // FILE RECORD CLASS
    // =========================================================

    static class FileRecord implements Serializable {

        private static final long serialVersionUID = 1L;

        String fileName;
        String filePath;
        String uploadedBy;
        String uploadTime;

        FileRecord(
                String fileName,
                String filePath,
                String uploadedBy,
                String uploadTime
        ) {

            this.fileName = fileName;
            this.filePath = filePath;
            this.uploadedBy = uploadedBy;
            this.uploadTime = uploadTime;
        }
    }

    // =========================================================
    // ACTIVITY CLASS
    // =========================================================

    static class Activity implements Serializable {

        private static final long serialVersionUID = 1L;

        String username;
        String action;
        String fileName;
        String time;

        Activity(
                String username,
                String action,
                String fileName
        ) {

            this.username = username;
            this.action = action;
            this.fileName = fileName;

            this.time =
                    LocalDateTime.now()
                            .format(TIME_FORMAT);
        }
    }

    // =========================================================
    // SAVE DATABASE
    // =========================================================

    static void saveDatabase() {

        try {

            ObjectOutputStream out =
                    new ObjectOutputStream(
                            new FileOutputStream(DB_FILE)
                    );

            out.writeObject(users);
            out.writeObject(files);
            out.writeObject(activities);

            out.close();

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Could not save database:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // LOAD DATABASE
    // =========================================================

    @SuppressWarnings("unchecked")
    static void loadDatabase() {

        File database =
                new File(DB_FILE);

        if (!database.exists()) {

            users = new ArrayList<>();
            files = new ArrayList<>();
            activities = new ArrayList<>();

            return;
        }

        try {

            ObjectInputStream in =
                    new ObjectInputStream(
                            new FileInputStream(DB_FILE)
                    );

            users =
                    (List<User>) in.readObject();

            files =
                    (List<FileRecord>) in.readObject();

            activities =
                    (List<Activity>) in.readObject();

            in.close();

        } catch (Exception e) {

            // If an old/incompatible database exists,
            // create a fresh database.

            users = new ArrayList<>();
            files = new ArrayList<>();
            activities = new ArrayList<>();
        }
    }

    // =========================================================
    // ADD ACTIVITY
    // =========================================================

    static void addActivity(
            String action,
            String fileName
    ) {

        if (currentUser == null) {
            return;
        }

        activities.add(
                new Activity(
                        currentUser.username,
                        action,
                        fileName
                )
        );

        saveDatabase();
    }

    // =========================================================
    // LOGIN WINDOW
    // =========================================================

    static void showLogin() {

        JFrame frame =
                new JFrame(
                        "Secure File Access System"
                );

        frame.setSize(520, 450);

        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        frame.setResizable(false);

        // -----------------------------------------------------
        // MAIN BACKGROUND
        // -----------------------------------------------------

        JPanel background =
                new JPanel(
                        new GridBagLayout()
                );

        background.setBackground(
                BACKGROUND
        );

        // -----------------------------------------------------
        // LOGIN CARD
        // -----------------------------------------------------

        JPanel card =
                new JPanel(
                        new GridBagLayout()
                );

        card.setBackground(WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER
                        ),
                        BorderFactory.createEmptyBorder(
                                30,
                                40,
                                30,
                                40
                        )
                )
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(
                        8,
                        8,
                        8,
                        8
                );

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;

        gbc.gridwidth = 2;

        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------

        JLabel title =
                new JLabel(
                        "SECURE FILE ACCESS",
                        SwingConstants.CENTER
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        25
                )
        );

        title.setForeground(NAVY);

        gbc.gridy = 0;

        card.add(
                title,
                gbc
        );

        // -----------------------------------------------------
        // SUBTITLE
        // -----------------------------------------------------

        JLabel subtitle =
                new JLabel(
                        "Role-Based Access Control System",
                        SwingConstants.CENTER
                );

        subtitle.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        13
                )
        );

        subtitle.setForeground(MUTED);

        gbc.gridy = 1;

        card.add(
                subtitle,
                gbc
        );

        // -----------------------------------------------------
        // USERNAME LABEL
        // -----------------------------------------------------

        JLabel usernameLabel =
                new JLabel("Username");

        usernameLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 1;

        card.add(
                usernameLabel,
                gbc
        );

        // -----------------------------------------------------
        // USERNAME FIELD
        // -----------------------------------------------------

        JTextField username =
                new JTextField();

        username.setPreferredSize(
                new Dimension(
                        250,
                        38
                )
        );

        gbc.gridx = 1;

        card.add(
                username,
                gbc
        );

        // -----------------------------------------------------
        // PASSWORD LABEL
        // -----------------------------------------------------

        JLabel passwordLabel =
                new JLabel("Password");

        passwordLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        gbc.gridx = 0;
        gbc.gridy = 3;

        card.add(
                passwordLabel,
                gbc
        );

        // -----------------------------------------------------
        // PASSWORD FIELD
        // -----------------------------------------------------

        JPasswordField password =
                new JPasswordField();

        password.setPreferredSize(
                new Dimension(
                        250,
                        38
                )
        );

        gbc.gridx = 1;

        card.add(
                password,
                gbc
        );

        // -----------------------------------------------------
        // LOGIN BUTTON
        // -----------------------------------------------------

        JButton loginButton =
                createButton(
                        "LOGIN",
                        BLUE
                );

        loginButton.setPreferredSize(
                new Dimension(
                        250,
                        42
                )
        );

        gbc.gridx = 0;
        gbc.gridy = 4;

        gbc.gridwidth = 2;

        card.add(
                loginButton,
                gbc
        );

        // -----------------------------------------------------
        // DEMO ACCOUNT
        // -----------------------------------------------------

        JLabel demo =
                new JLabel(
                        "<html>"
                        + "<center>"
                        + "<b>Demo Accounts</b><br>"
                        + "Admin: admin / admin123<br>"
                        + "User: kiran / user123"
                        + "</center>"
                        + "</html>",
                        SwingConstants.CENTER
                );

        demo.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        12
                )
        );

        demo.setForeground(MUTED);

        gbc.gridy = 5;

        card.add(
                demo,
                gbc
        );

        background.add(card);

        frame.add(background);

        // -----------------------------------------------------
        // LOGIN ACTION
        // -----------------------------------------------------

        loginButton.addActionListener(e -> {

            String enteredUsername =
                    username.getText().trim();

            String enteredPassword =
                    new String(
                            password.getPassword()
                    );

            User loggedUser = null;

            for (User user : users) {

                if (user.username.equals(
                        enteredUsername
                )
                        && user.password.equals(
                                enteredPassword
                        )) {

                    loggedUser = user;

                    break;
                }
            }

            if (loggedUser != null) {

                currentUser = loggedUser;

                addActivity(
                        "LOGIN",
                        ""
                );

                frame.dispose();

                showDashboard();

            } else {

                JOptionPane.showMessageDialog(
                        frame,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        password.addActionListener(
                e -> loginButton.doClick()
        );

        frame.setVisible(true);
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    static void showDashboard() {

        JFrame frame =
                new JFrame(
                        "Secure File Access System - "
                                + currentUser.role
                );

        frame.setSize(
                1050,
                680
        );

        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        // -----------------------------------------------------
        // MAIN PANEL
        // -----------------------------------------------------

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(
                BACKGROUND
        );

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(
                NAVY
        );

        header.setBorder(
                BorderFactory.createEmptyBorder(
                        18,
                        25,
                        18,
                        25
                )
        );

        JLabel title =
                new JLabel(
                        "Secure File Access System"
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        24
                )
        );

        title.setForeground(
                WHITE
        );

        header.add(
                title,
                BorderLayout.WEST
        );

        // -----------------------------------------------------
        // USER AREA
        // -----------------------------------------------------

        JPanel userPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                12,
                                0
                        )
                );

        userPanel.setOpaque(false);

        JLabel userLabel =
                new JLabel(
                        "Welcome, "
                                + currentUser.username
                                + "   |   Role: "
                                + currentUser.role
                );

        userLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        userLabel.setForeground(
                WHITE
        );

        JButton logout =
                createButton(
                        "Logout",
                        RED
                );

        userPanel.add(
                userLabel
        );

        userPanel.add(
                logout
        );

        header.add(
                userPanel,
                BorderLayout.EAST
        );

        main.add(
                header,
                BorderLayout.NORTH
        );

        // -----------------------------------------------------
        // TABS
        // -----------------------------------------------------

        JTabbedPane tabs =
                new JTabbedPane();

        tabs.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        // File management
        tabs.addTab(
                "File Management",
                createFilePanel(frame)
        );

        // Admin tabs
        if (currentUser.role.equals("ADMIN")) {

            tabs.addTab(
                    "Users",
                    createUsersPanel()
            );

            tabs.addTab(
                    "Activity Log",
                    createActivityPanel()
            );

        } else {

            // User tab
            tabs.addTab(
                    "My Activity",
                    createActivityPanel()
            );
        }

        main.add(
                tabs,
                BorderLayout.CENTER
        );

        // -----------------------------------------------------
        // LOGOUT
        // -----------------------------------------------------

        logout.addActionListener(e -> {

            addActivity(
                    "LOGOUT",
                    ""
            );

            currentUser = null;

            frame.dispose();

            showLogin();
        });

        frame.add(main);

        frame.setVisible(true);
    }

    // =========================================================
    // FILE MANAGEMENT PANEL
    // =========================================================

    static JPanel createFilePanel(
            JFrame frame
    ) {

        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                15,
                                15
                        )
                );

        panel.setBackground(
                BACKGROUND
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        // -----------------------------------------------------
        // BUTTON PANEL
        // -----------------------------------------------------

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                5
                        )
                );

        buttonPanel.setBackground(
                WHITE
        );

        JButton upload =
                createButton(
                        "Upload File",
                        BLUE
                );

        JButton download =
                createButton(
                        "Download File",
                        GREEN
                );

        JButton refresh =
                createButton(
                        "Refresh",
                        NAVY
                );

        JButton delete =
                createButton(
                        "Delete File",
                        RED
                );

        buttonPanel.add(
                upload
        );

        buttonPanel.add(
                download
        );

        buttonPanel.add(
                refresh
        );

        // Only ADMIN gets Delete
        if (currentUser.role.equals("ADMIN")) {

            buttonPanel.add(
                    delete
            );
        }

        panel.add(
                buttonPanel,
                BorderLayout.NORTH
        );

        // -----------------------------------------------------
        // TABLE MODEL
        // -----------------------------------------------------

        DefaultTableModel model =
                new DefaultTableModel(
                        new Object[]{
                                "File Name",
                                "Uploaded By",
                                "Upload Time"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        // -----------------------------------------------------
        // TABLE
        // -----------------------------------------------------

        JTable table =
                new JTable(model);

        table.setRowHeight(
                32
        );

        table.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        13
                )
        );

        table.getTableHeader()
                .setFont(
                        new Font(
                                "Arial",
                                Font.BOLD,
                                13
                        )
                );

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.setGridColor(
                BORDER
        );

        JScrollPane scroll =
                new JScrollPane(
                        table
                );

        panel.add(
                scroll,
                BorderLayout.CENTER
        );

        // Load existing files
        loadFileTable(model);

        // =====================================================
        // UPLOAD BUTTON
        // =====================================================

        upload.addActionListener(e -> {

            JFileChooser chooser =
                    new JFileChooser();

            chooser.setDialogTitle(
                    "Select File to Upload"
            );

            int result =
                    chooser.showOpenDialog(
                            frame
                    );

            if (result !=
                    JFileChooser.APPROVE_OPTION) {

                return;
            }

            File selected =
                    chooser.getSelectedFile();

            try {

                // Make folder
                File folder =
                        new File(
                                FILE_FOLDER
                        );

                if (!folder.exists()) {
                    folder.mkdirs();
                }

                // Make unique stored filename
                String storedName =
                        System.currentTimeMillis()
                                + "_"
                                + selected.getName();

                File destination =
                        new File(
                                folder,
                                storedName
                        );

                // Copy file
                Files.copy(
                        selected.toPath(),
                        destination.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );

                // Create record
                FileRecord record =
                        new FileRecord(
                                selected.getName(),
                                destination.getAbsolutePath(),
                                currentUser.username,
                                LocalDateTime.now()
                                        .format(
                                                TIME_FORMAT
                                        )
                        );

                files.add(record);

                // Activity
                addActivity(
                        "UPLOAD",
                        selected.getName()
                );

                saveDatabase();

                // Update table immediately
                loadFileTable(model);

                JOptionPane.showMessageDialog(
                        frame,
                        "File uploaded successfully!\n\n"
                                + selected.getName(),
                        "Upload Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Upload failed:\n"
                                + ex.getMessage(),
                        "Upload Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // =====================================================
        // REFRESH BUTTON
        // =====================================================

        refresh.addActionListener(e -> {

            loadDatabase();

            loadFileTable(model);

            JOptionPane.showMessageDialog(
                    frame,
                    "File list refreshed.",
                    "Refresh",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        // =====================================================
        // DOWNLOAD BUTTON
        // =====================================================

        download.addActionListener(e -> {

            int row =
                    table.getSelectedRow();

            if (row == -1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please select a file first."
                );

                return;
            }

            int modelRow =
                    table.convertRowIndexToModel(
                            row
                    );

            String fileName =
                    model.getValueAt(
                            modelRow,
                            0
                    ).toString();

            FileRecord record =
                    findFile(fileName);

            if (record == null) {

                JOptionPane.showMessageDialog(
                        frame,
                        "File record not found."
                );

                return;
            }

            File source =
                    new File(
                            record.filePath
                    );

            if (!source.exists()) {

                JOptionPane.showMessageDialog(
                        frame,
                        "The stored file could not be found."
                );

                return;
            }

            JFileChooser chooser =
                    new JFileChooser();

            chooser.setDialogTitle(
                    "Choose Download Location"
            );

            chooser.setSelectedFile(
                    new File(fileName)
            );

            int result =
                    chooser.showSaveDialog(
                            frame
                    );

            if (result !=
                    JFileChooser.APPROVE_OPTION) {

                return;
            }

            File destination =
                    chooser.getSelectedFile();

            try {

                Files.copy(
                        source.toPath(),
                        destination.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );

                addActivity(
                        "DOWNLOAD",
                        fileName
                );

                JOptionPane.showMessageDialog(
                        frame,
                        "File downloaded successfully!",
                        "Download Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Download failed:\n"
                                + ex.getMessage(),
                        "Download Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // =====================================================
        // DELETE BUTTON
        // =====================================================

        delete.addActionListener(e -> {

            if (!currentUser.role.equals("ADMIN")) {
                return;
            }

            int row =
                    table.getSelectedRow();

            if (row == -1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please select a file first."
                );

                return;
            }

            int modelRow =
                    table.convertRowIndexToModel(
                            row
                    );

            String fileName =
                    model.getValueAt(
                            modelRow,
                            0
                    ).toString();

            int confirm =
                    JOptionPane.showConfirmDialog(
                            frame,
                            "Are you sure you want to delete:\n"
                                    + fileName,
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION
                    );

            if (confirm !=
                    JOptionPane.YES_OPTION) {

                return;
            }

            FileRecord record =
                    findFile(fileName);

            if (record != null) {

                File physicalFile =
                        new File(
                                record.filePath
                        );

                if (physicalFile.exists()) {

                    physicalFile.delete();
                }

                files.remove(record);

                addActivity(
                        "DELETE",
                        fileName
                );

                saveDatabase();

                loadFileTable(model);

                JOptionPane.showMessageDialog(
                        frame,
                        "File deleted successfully."
                );
            }
        });

        return panel;
    }

    // =========================================================
    // LOAD FILE TABLE
    // =========================================================

    static void loadFileTable(
            DefaultTableModel model
    ) {

        model.setRowCount(0);

        for (FileRecord file : files) {

            File actualFile =
                    new File(
                            file.filePath
                    );

            if (actualFile.exists()) {

                model.addRow(
                        new Object[]{
                                file.fileName,
                                file.uploadedBy,
                                file.uploadTime
                        }
                );
            }
        }
    }

    // =========================================================
    // FIND FILE
    // =========================================================

    static FileRecord findFile(
            String fileName
    ) {

        for (FileRecord file : files) {

            if (file.fileName.equals(
                    fileName
            )) {

                return file;
            }
        }

        return null;
    }

    // =========================================================
    // USERS PANEL
    // =========================================================

    static JPanel createUsersPanel() {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setBackground(
                BACKGROUND
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        DefaultTableModel model =
                new DefaultTableModel(
                        new Object[]{
                                "Username",
                                "Role"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        JTable table =
                new JTable(model);

        table.setRowHeight(
                32
        );

        table.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        14
                )
        );

        table.getTableHeader()
                .setFont(
                        new Font(
                                "Arial",
                                Font.BOLD,
                                14
                        )
                );

        for (User user : users) {

            model.addRow(
                    new Object[]{
                            user.username,
                            user.role
                    }
            );
        }

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // ACTIVITY PANEL
    // =========================================================

    static JPanel createActivityPanel() {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setBackground(
                BACKGROUND
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        DefaultTableModel model =
                new DefaultTableModel(
                        new Object[]{
                                "Username",
                                "Action",
                                "File",
                                "Time"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        JTable table =
                new JTable(model);

        table.setRowHeight(
                30
        );

        table.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        13
                )
        );

        table.getTableHeader()
                .setFont(
                        new Font(
                                "Arial",
                                Font.BOLD,
                                13
                        )
                );

        for (Activity activity :
                activities) {

            // ADMIN sees all activities
            // USER sees only own activities

            if (currentUser.role.equals(
                    "ADMIN"
            )
                    || activity.username.equals(
                            currentUser.username
                    )) {

                model.addRow(
                        new Object[]{
                                activity.username,
                                activity.action,
                                activity.fileName,
                                activity.time
                        }
                );
            }
        }

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // BUTTON DESIGN
    // =========================================================
    static JButton createButton(
            String text,
            Color color
       )
    {

        JButton button = new JButton(text);

        button.setText(text);

        button.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        13
                )
        );

        button.setForeground(Color.WHITE);

        button.setBackground(color);

        button.setOpaque(true);

        button.setContentAreaFilled(true);

        button.setBorderPainted(false);

        button.setFocusPainted(false);

        button.setPreferredSize(
                new Dimension(
                        135,
                        42
                )
        );

        button.setCursor(
                new java.awt.Cursor(
                        java.awt.Cursor.HAND_CURSOR
                )
        );

        return button;
    }
}
   