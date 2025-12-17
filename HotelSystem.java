import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

public class HotelSystem extends JFrame {

    // --- GUI COMPONENTS ---
    // Tab 1 Components
    private JTextField idField, nameField, contactField;
    private JTextArea displayArea; // System Log

    // Tab 2 Components
    private JTextField bookGuestIdField, bookRoomField, bookNightsField, bookDateField;
    private JRadioButton rCash, rCard;
    private ButtonGroup paymentGroup;
    private JTextArea receiptArea;

    // Files
    private final String GUEST_FILE = "guests.txt";
    private final String BOOKING_FILE = "bookings.txt";

    public HotelSystem() {
        // 1. Main Window Setup
        setTitle("YakYakYay(YYY) Hotel");
        setSize(700, 600); // Increased height slightly for better view
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 2. Create Tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // 3. Add Panels to Tabs
        tabbedPane.addTab("Manage Guests & History", createGuestPanel());
        tabbedPane.addTab("New Booking", createBookingPanel());

        add(tabbedPane);
    }

    // --- TAB 1: GUEST MANAGEMENT PANEL ---
    private JPanel createGuestPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Top: Input Fields
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Guest Details"));
        
        inputPanel.add(new JLabel("Guest ID:"));
        idField = new JTextField();
        inputPanel.add(idField);

        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Contact:"));
        contactField = new JTextField();
        inputPanel.add(contactField);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton btnAdd = new JButton("Add Guest");
        JButton btnSearch = new JButton("Search ID");
        JButton btnDelete = new JButton("Delete ID");
        JButton btnClear = new JButton("Clear");
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        // Center: Display Area
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Log / Results"));

        // --- BUTTON LOGIC ---
        
        // ADD BUTTON
        btnAdd.addActionListener(e -> {
            String id = idField.getText();
            String name = nameField.getText();
            String contact = contactField.getText();

            if (id.isEmpty() || name.isEmpty()) {
                log("Error: ID and Name are required!");
                return;
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(GUEST_FILE, true))) {
                bw.write(id + "," + name + "," + contact);
                bw.newLine();
                log("Success: Guest " + name + " added.");
                clearFields();
            } catch (IOException ex) {
                log("Error saving: " + ex.getMessage());
            }
        });

        // SEARCH BUTTON
        btnSearch.addActionListener(e -> {
            String targetId = idField.getText().trim();
            if (targetId.isEmpty()) {
                log("Please enter an ID to search.");
                return;
            }
            
            displayArea.setText(""); // Clear log
            boolean foundGuest = false;

            // 1. Search Guest File
            try (BufferedReader br = new BufferedReader(new FileReader(GUEST_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length >= 3 && data[0].equals(targetId)) {
                        nameField.setText(data[1]);
                        contactField.setText(data[2]);
                        log("--- GUEST FOUND ---");
                        log("Name:    " + data[1]);
                        log("Contact: " + data[2]);
                        foundGuest = true;
                        break;
                    }
                }
            } catch (IOException ex) {
                log("Error reading guest file.");
            }

            if (!foundGuest) {
                log("Guest ID not found.");
                return;
            }

            // 2. Search Booking File (History)
            log("\n--- BOOKING HISTORY ---");
            boolean foundBooking = false;
            try (BufferedReader br = new BufferedReader(new FileReader(BOOKING_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Format: ID, Room, Nights, Total, Payment, Date
                    String[] data = line.split(",");
                    if (data.length >= 5 && data[0].equals(targetId)) {
                        String date = (data.length > 5) ? data[5] : "N/A";
                        log("Date: " + date + " | Room: " + data[1] + " | " + data[2] + " Nights | Paid: " + data[4]);
                        foundBooking = true;
                    }
                }
            } catch (IOException ex) {}

            if (!foundBooking) {
                log("No previous bookings found.");
            }
        });

        // DELETE BUTTON
        btnDelete.addActionListener(e -> {
            String targetId = idField.getText().trim();
            if (targetId.isEmpty()) return;

            ArrayList<String> tempLines = new ArrayList<>();
            boolean deleted = false;

            try {
                File file = new File(GUEST_FILE);
                if (!file.exists()) return;

                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length > 0 && data[0].equals(targetId)) {
                        deleted = true; 
                    } else {
                        tempLines.add(line);
                    }
                }
                br.close();

                if (deleted) {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                    for (String s : tempLines) {
                        bw.write(s);
                        bw.newLine();
                    }
                    bw.close();
                    log("Guest " + targetId + " deleted successfully.");
                    clearFields();
                } else {
                    log("Guest ID not found to delete.");
                }
            } catch (IOException ex) {
                log("Error deleting: " + ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> {
            clearFields();
            displayArea.setText("");
        });

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(inputPanel, BorderLayout.CENTER);
        topContainer.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topContainer, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // --- TAB 2: BOOKING PANEL (FIXED LAYOUT) ---
    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 1. FORM PANEL (Top)
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("New Booking Input"));

        formPanel.add(new JLabel("Guest ID:"));
        bookGuestIdField = new JTextField();
        formPanel.add(bookGuestIdField);

        formPanel.add(new JLabel("Room Number:"));
        bookRoomField = new JTextField();
        formPanel.add(bookRoomField);

        formPanel.add(new JLabel("Nights:"));
        bookNightsField = new JTextField();
        formPanel.add(bookNightsField);

        formPanel.add(new JLabel("Date (DD/MM/YYYY):"));
        bookDateField = new JTextField();
        formPanel.add(bookDateField);

        formPanel.add(new JLabel("Payment Method:"));
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rCash = new JRadioButton("Cash", true);
        rCard = new JRadioButton("Card");
        
        paymentGroup = new ButtonGroup();
        paymentGroup.add(rCash);
        paymentGroup.add(rCard);
        
        radioPanel.add(rCash);
        radioPanel.add(rCard);
        formPanel.add(radioPanel);

        // 2. BUTTON PANEL (Small button, not huge)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnBook = new JButton("Confirm Booking");
        btnBook.setPreferredSize(new Dimension(150, 30)); // Set a reasonable size
        buttonPanel.add(btnBook);

        // Wrapper to keep Form and Button together at the top
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(buttonPanel, BorderLayout.SOUTH);

        // 3. BOOKING DETAILS AREA (Big area)
        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Booking Details")); // RENAMED

        // BOOKING LOGIC
        btnBook.addActionListener(e -> {
            String gid = bookGuestIdField.getText();
            String room = bookRoomField.getText();
            String nights = bookNightsField.getText();
            String date = bookDateField.getText();
            String paymentMethod = rCash.isSelected() ? "Cash" : "Card";

            if (gid.isEmpty() || room.isEmpty() || nights.isEmpty() || date.isEmpty()) {
                receiptArea.setText("Please fill all fields.");
                return;
            }

            try {
                int n = Integer.parseInt(nights);
                double total = n * 100.00; // Assuming RM100 per night

                // Write to file
                BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKING_FILE, true));
                bw.write(gid + "," + room + "," + n + "," + total + "," + paymentMethod + "," + date);
                bw.newLine();
                bw.close();

                receiptArea.setText("--- BOOKING CONFIRMED ---\n");
                receiptArea.append("Guest ID : " + gid + "\n");
                receiptArea.append("Date     : " + date + "\n");
                receiptArea.append("Room No  : " + room + "\n");
                receiptArea.append("Duration : " + n + " Nights\n");
                receiptArea.append("Payment  : " + paymentMethod + "\n");
                receiptArea.append("TOTAL    : RM " + total + "\n");
                receiptArea.append("-------------------------");

            } catch (Exception ex) {
                receiptArea.setText("Error: Nights must be a number.");
            }
        });

        // Add Components to Main Panel
        panel.add(topContainer, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER); // This fills the rest of the screen!

        return panel;
    }

    // --- HELPER METHODS ---
    private void log(String message) {
        displayArea.append(message + "\n");
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        contactField.setText("");
    }

    // --- MAIN METHOD ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HotelSystem().setVisible(true);
        });
    }
}
