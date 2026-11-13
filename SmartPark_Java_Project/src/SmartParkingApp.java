import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SmartPark - Pure Java Smart Parking Management System
 * Run: javac src/SmartParkingApp.java
 *      java -cp src SmartParkingApp
 */
public class SmartParkingApp extends JFrame {
    private static final String DATA_FILE = "smartpark-data.ser";
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final List<Slot> slots = new ArrayList<Slot>();
    private final List<ParkingRecord> history = new ArrayList<ParkingRecord>();
    private JPanel slotPanel;
    private JLabel totalLabel, occupiedLabel, availableLabel, revenueLabel;
    private JTextArea historyArea;

    static class Slot implements Serializable {
        private static final long serialVersionUID = 1L;
        int number;
        String type; // CAR or BIKE
        boolean occupied;
        String plate;
        LocalDateTime entryTime;

        Slot(int number, String type) {
            this.number = number;
            this.type = type;
        }
    }

    static class ParkingRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        String plate, vehicleType, slot;
        LocalDateTime entryTime, exitTime;
        double fee;

        ParkingRecord(String plate, String vehicleType, String slot,
                      LocalDateTime entryTime, LocalDateTime exitTime, double fee) {
            this.plate = plate;
            this.vehicleType = vehicleType;
            this.slot = slot;
            this.entryTime = entryTime;
            this.exitTime = exitTime;
            this.fee = fee;
        }
    }

    public SmartParkingApp() {
        super("SmartPark | Intelligent Parking Management");
        loadData();
        if (slots.isEmpty()) initializeSlots();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 720);
        setMinimumSize(new Dimension(850, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(244, 247, 251));

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 18, 16, 18));
        root.setBackground(new Color(244, 247, 251));
        root.add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.addTab("Parking Dashboard", buildDashboard());
        tabs.addTab("Parking History", buildHistory());
        root.add(tabs, BorderLayout.CENTER);

        setContentPane(root);
        refreshDashboard();
        refreshHistory();
    }

    private void initializeSlots() {
        // 12 car spaces and 8 bike spaces.
        for (int i = 1; i <= 12; i++) slots.add(new Slot(i, "CAR"));
        for (int i = 13; i <= 20; i++) slots.add(new Slot(i, "BIKE"));
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(24, 43, 73));
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("SMARTPARK");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        JLabel subtitle = new JLabel("Intelligent Parking Management System");
        subtitle.setForeground(new Color(205, 218, 235));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        JLabel mode = new JLabel("LIVE SIMULATION");
        mode.setForeground(new Color(155, 238, 190));
        mode.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(titles, BorderLayout.WEST);
        panel.add(mode, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildDashboard() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBorder(new EmptyBorder(14, 4, 4, 4));
        page.setBackground(new Color(244, 247, 251));

        JPanel stats = new JPanel(new GridLayout(1, 4, 10, 10));
        stats.setOpaque(false);
        totalLabel = new JLabel("20", SwingConstants.CENTER);
        occupiedLabel = new JLabel("0", SwingConstants.CENTER);
        availableLabel = new JLabel("20", SwingConstants.CENTER);
        revenueLabel = new JLabel("₹0", SwingConstants.CENTER);
        stats.add(statCard("TOTAL SLOTS", totalLabel, new Color(49, 95, 160)));
        stats.add(statCard("OCCUPIED", occupiedLabel, new Color(211, 91, 76)));
        stats.add(statCard("AVAILABLE", availableLabel, new Color(40, 145, 100)));
        stats.add(statCard("TOTAL REVENUE", revenueLabel, new Color(132, 91, 174)));

        slotPanel = new JPanel(new GridLayout(0, 5, 10, 10));
        slotPanel.setBackground(Color.WHITE);
        slotPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel slotContainer = new JPanel(new BorderLayout(0, 8));
        slotContainer.setBackground(Color.WHITE);
        slotContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235)),
                new EmptyBorder(10, 10, 10, 10)));
        JLabel slotTitle = new JLabel("PARKING SLOT STATUS");
        slotTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        slotTitle.setForeground(new Color(35, 52, 77));
        slotContainer.add(slotTitle, BorderLayout.NORTH);
        slotContainer.add(slotPanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actions.setOpaque(false);
        JButton entryButton = actionButton(" +  Vehicle Entry", new Color(35, 132, 91));
        JButton exitButton = actionButton(" -  Vehicle Exit", new Color(190, 77, 66));
        JButton refreshButton = actionButton("Refresh", new Color(63, 100, 151));
        entryButton.addActionListener(e -> vehicleEntry());
        exitButton.addActionListener(e -> vehicleExit());
        refreshButton.addActionListener(e -> refreshDashboard());
        actions.add(entryButton);
        actions.add(exitButton);
        actions.add(refreshButton);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(slotContainer, BorderLayout.CENTER);
        center.add(actions, BorderLayout.SOUTH);

        page.add(stats, BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JPanel statCard(String heading, JLabel value, Color color) {
        JPanel card = new JPanel(new BorderLayout(4, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238)),
                new EmptyBorder(12, 8, 12, 8)));
        JLabel name = new JLabel(heading, SwingConstants.CENTER);
        name.setFont(new Font("SansSerif", Font.BOLD, 11));
        name.setForeground(new Color(105, 116, 132));
        value.setFont(new Font("SansSerif", Font.BOLD, 23));
        value.setForeground(color);
        card.add(name, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JButton actionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        return button;
    }

    private JPanel buildHistory() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(new Color(244, 247, 251));
        panel.setBorder(new EmptyBorder(14, 4, 4, 4));
        JLabel heading = new JLabel("COMPLETED PARKING SESSIONS");
        heading.setFont(new Font("SansSerif", Font.BOLD, 15));
        heading.setForeground(new Color(35, 52, 77));
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyArea.setBackground(Color.WHITE);
        historyArea.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.add(heading, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        return panel;
    }

    private void vehicleEntry() {
        JTextField plateField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<String>(new String[]{"CAR", "BIKE"});
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Vehicle number:"));
        form.add(plateField);
        form.add(new JLabel("Vehicle type:"));
        form.add(typeBox);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Register Vehicle Entry", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String plate = plateField.getText().trim().toUpperCase();
        String type = (String) typeBox.getSelectedItem();
        if (plate.isEmpty()) {
            showError("Please enter the vehicle number.");
            return;
        }
        for (Slot slot : slots) {
            if (slot.occupied && slot.plate.equalsIgnoreCase(plate)) {
                showError("This vehicle is already parked in slot " + slot.number + ".");
                return;
            }
        }

        Slot available = null;
        for (Slot slot : slots) {
            if (!slot.occupied && slot.type.equals(type)) {
                available = slot;
                break;
            }
        }
        if (available == null) {
            showError("Sorry! No " + type.toLowerCase() + " parking slot is available.");
            return;
        }

        available.occupied = true;
        available.plate = plate;
        available.entryTime = LocalDateTime.now();
        saveData();
        refreshDashboard();
        JOptionPane.showMessageDialog(this,
                "Vehicle entry successful!\nAssigned slot: " + available.number +
                "\nEntry time: " + available.entryTime.format(TIME_FORMAT),
                "Slot Allocated", JOptionPane.INFORMATION_MESSAGE);
    }

    private void vehicleExit() {
        String plate = JOptionPane.showInputDialog(this,
                "Enter vehicle number:", "Vehicle Exit", JOptionPane.QUESTION_MESSAGE);
        if (plate == null || plate.trim().isEmpty()) return;
        plate = plate.trim().toUpperCase();

        Slot parked = null;
        for (Slot slot : slots) {
            if (slot.occupied && slot.plate.equalsIgnoreCase(plate)) {
                parked = slot;
                break;
            }
        }
        if (parked == null) {
            showError("No active parking record found for " + plate + ".");
            return;
        }

        LocalDateTime exitTime = LocalDateTime.now();
        long minutes = Math.max(1, Duration.between(parked.entryTime, exitTime).toMinutes());
        long hours = (long) Math.ceil(minutes / 60.0);
        double firstHour = parked.type.equals("CAR") ? 30.0 : 15.0;
        double extraHour = parked.type.equals("CAR") ? 20.0 : 10.0;
        double fee = firstHour + Math.max(0, hours - 1) * extraHour;

        ParkingRecord record = new ParkingRecord(parked.plate, parked.type,
                "P-" + parked.number, parked.entryTime, exitTime, fee);
        history.add(0, record);

        String details = "Vehicle: " + parked.plate +
                "\nSlot: P-" + parked.number +
                "\nDuration: " + minutes + " minute(s)" +
                "\nBillable hours: " + hours +
                "\nParking fee: ₹" + String.format("%.2f", fee);
        parked.occupied = false;
        parked.plate = null;
        parked.entryTime = null;

        saveData();
        refreshDashboard();
        refreshHistory();
        JOptionPane.showMessageDialog(this, details + "\n\nSlot is now available.",
                "Parking Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshDashboard() {
        int occupied = 0;
        for (Slot slot : slots) if (slot.occupied) occupied++;
        double revenue = 0;
        for (ParkingRecord record : history) revenue += record.fee;

        totalLabel.setText(String.valueOf(slots.size()));
        occupiedLabel.setText(String.valueOf(occupied));
        availableLabel.setText(String.valueOf(slots.size() - occupied));
        revenueLabel.setText("₹" + String.format("%.0f", revenue));

        slotPanel.removeAll();
        for (Slot slot : slots) {
            JPanel card = new JPanel(new BorderLayout(3, 4));
            card.setBorder(BorderFactory.createLineBorder(
                    slot.occupied ? new Color(220, 125, 115) : new Color(120, 190, 150), 2));
            card.setBackground(slot.occupied ? new Color(255, 239, 236) : new Color(235, 249, 240));
            JLabel number = new JLabel("P-" + slot.number, SwingConstants.CENTER);
            number.setFont(new Font("SansSerif", Font.BOLD, 16));
            JLabel type = new JLabel(slot.type, SwingConstants.CENTER);
            type.setFont(new Font("SansSerif", Font.PLAIN, 10));
            JLabel status = new JLabel(slot.occupied ? "OCCUPIED" : "AVAILABLE", SwingConstants.CENTER);
            status.setFont(new Font("SansSerif", Font.BOLD, 10));
            status.setForeground(slot.occupied ? new Color(180, 65, 55) : new Color(35, 130, 80));
            card.add(number, BorderLayout.CENTER);
            card.add(type, BorderLayout.NORTH);
            card.add(status, BorderLayout.SOUTH);
            if (slot.occupied) card.setToolTipText("Vehicle: " + slot.plate);
            slotPanel.add(card);
        }
        slotPanel.revalidate();
        slotPanel.repaint();
    }

    private void refreshHistory() {
        if (historyArea == null) return;
        if (history.isEmpty()) {
            historyArea.setText("No completed parking sessions yet.");
            return;
        }
        StringBuilder out = new StringBuilder();
        out.append(String.format("%-14s %-7s %-7s %-20s %-20s %s%n",
                "VEHICLE", "TYPE", "SLOT", "ENTRY", "EXIT", "FEE"));
        out.append("-----------------------------------------------------------------------------------------------\n");
        for (ParkingRecord r : history) {
            out.append(String.format("%-14s %-7s %-7s %-20s %-20s ₹%.2f%n",
                    r.plate, r.vehicleType, r.slot,
                    r.entryTime.format(TIME_FORMAT), r.exitTime.format(TIME_FORMAT), r.fee));
        }
        historyArea.setText(out.toString());
        historyArea.setCaretPosition(0);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "SmartPark",
                JOptionPane.WARNING_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            List<Slot> savedSlots = (List<Slot>) in.readObject();
            List<ParkingRecord> savedHistory = (List<ParkingRecord>) in.readObject();
            slots.addAll(savedSlots);
            history.addAll(savedHistory);
        } catch (Exception ex) {
            slots.clear();
            history.clear();
            JOptionPane.showMessageDialog(this,
                    "Saved data could not be loaded. A new parking layout will be created.",
                    "Data Notice", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(slots);
            out.writeObject(history);
        } catch (IOException ex) {
            showError("Could not save data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SmartParkingApp().setVisible(true));
    }
}
