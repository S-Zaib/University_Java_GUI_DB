import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class UniversityDatabaseGUI extends JFrame {

    private JButton addButton;
    private JButton alterButton;
    private JButton deleteButton;
    private JButton showTableButton;
    private JComboBox<String> showTableComboBox;
    private JTable dataTable;
    private JButton clearButton;

    private Connection connection;

    public UniversityDatabaseGUI() {
        setTitle("University Database");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        alterButton = new JButton("Alter");
        deleteButton = new JButton("Delete");
        showTableButton = new JButton("Show Table");
        clearButton = new JButton("Clear");

        showTableComboBox = new JComboBox<>(new String[]{"Student", "Section", "Department", "Chairman", "Student_has_Section"});

        buttonPanel.add(addButton);
        buttonPanel.add(alterButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(showTableComboBox);
        buttonPanel.add(showTableButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.NORTH);

        dataTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(dataTable);
        add(scrollPane, BorderLayout.CENTER);

        pack();

        setVisible(true);

        connectToDatabase();

        // the these methods are to make buttons work
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedEntity = (String) showTableComboBox.getSelectedItem();
                addRecord(selectedEntity);
            }
        });

        alterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedEntity = (String) showTableComboBox.getSelectedItem();
                alterRecord(selectedEntity);
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedEntity = (String) showTableComboBox.getSelectedItem();
                deleteRecord(selectedEntity);
            }
        });

        showTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedEntity = (String) showTableComboBox.getSelectedItem();
                displayTable(selectedEntity);
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedEntity = (String) showTableComboBox.getSelectedItem();
                clearTable(selectedEntity);
            }
        });
    }

    private void connectToDatabase() {
        String host = "localhost";
        String port = "3306";
        String database = "mydb";
        String username = "root";
        String password = "TuskMusk";

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void displayTable(String entity) {
        try {
            String query = "SELECT * FROM " + entity;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Get column count
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Set table 
            DefaultTableModel model = new DefaultTableModel();
            dataTable.setModel(model);

            // Add columns 
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

            // Add rows 
            while (resultSet.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    rowData[i - 1] = resultSet.getObject(i);
                }
                model.addRow(rowData);
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to retrieve data from the database.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addRecord(String entity) {
        try {
            DefaultTableModel model = (DefaultTableModel) dataTable.getModel();

            // Create new row
            Object[] rowData = new Object[model.getColumnCount()];
            model.addRow(rowData);

            int lastRowIndex = model.getRowCount() - 1;

            // Fetch foreign key information from the database schema
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet foreignKeyResultSet = metaData.getImportedKeys(connection.getCatalog(), null, entity);

            // Create a map to store foreign key column names and their corresponding combo boxes
            Map<String, JComboBox<String>> foreignKeyComboBoxes = new HashMap<>();

            // Iterate over the foreign key result set and populate combo boxes
            while (foreignKeyResultSet.next()) {
                String fkColumnName = foreignKeyResultSet.getString("FKCOLUMN_NAME");
                String pkTableName = foreignKeyResultSet.getString("PKTABLE_NAME");
                String pkColumnName = foreignKeyResultSet.getString("PKCOLUMN_NAME");

                // Fetch primary key values for the foreign key column
                Statement primaryKeyStatement = connection.createStatement();
                ResultSet primaryKeyResultSet = primaryKeyStatement.executeQuery("SELECT " + pkColumnName + " FROM " + pkTableName);

                // Create combo box with primary key values
                JComboBox<String> foreignKeyComboBox = new JComboBox<>();
                while (primaryKeyResultSet.next()) {
                    foreignKeyComboBox.addItem(primaryKeyResultSet.getString(pkColumnName));
                }
                primaryKeyStatement.close();

                foreignKeyComboBoxes.put(fkColumnName, foreignKeyComboBox);
            }
            foreignKeyResultSet.close();

            // Ask for data
            JPanel inputPanel = new JPanel(new GridLayout(model.getColumnCount(), 2));
            for (int i = 0; i < model.getColumnCount(); i++) {
                String columnName = model.getColumnName(i);
                JLabel label = new JLabel(columnName);
                JComponent component;
                if (foreignKeyComboBoxes.containsKey(columnName)) {
                    component = foreignKeyComboBoxes.get(columnName);
                } else {
                    component = new JTextField();
                }
                inputPanel.add(label);
                inputPanel.add(component);
            }

            // Show dialog box
            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Enter Record Details", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                // Get values
                StringBuilder valuesBuilder = new StringBuilder();
                Component[] components = inputPanel.getComponents();
                for (int i = 1; i < components.length; i += 2) {
                    Object value;
                    if (components[i] instanceof JComboBox) {
                        value = ((JComboBox<?>) components[i]).getSelectedItem();
                    } else {
                        value = ((JTextField) components[i]).getText();
                    }
                    if (value instanceof String) {
                        valuesBuilder.append("'").append(value).append("'");
                    } else {
                        valuesBuilder.append(value);
                    }
                    if (i < components.length - 2) {
                        valuesBuilder.append(", ");
                    }
                }

                // Insert record
                StringBuilder queryBuilder = new StringBuilder("INSERT INTO ")
                        .append(entity).append(" VALUES (")
                        .append(valuesBuilder.toString()).append(")");

                Statement statement = connection.createStatement();
                statement.executeUpdate(queryBuilder.toString());
                statement.close();

                displayTable(entity);

                // Clear input fields
                for (int i = 1; i < components.length; i += 2) {
                    if (components[i] instanceof JTextField) {
                        ((JTextField) components[i]).setText("");
                    }
                }
            } else {
                model.removeRow(lastRowIndex);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add record to the database.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void alterRecord(String entity) {
        try {
            DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
            int selectedRow = dataTable.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "No record selected.",
                        "Alter Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // get new data
            for (int i = 0; i < model.getColumnCount(); i++) {
                String columnName = model.getColumnName(i);
                Object currentValue = model.getValueAt(selectedRow, i);
                String input = JOptionPane.showInputDialog(this, "Enter new value for " + columnName + ":", currentValue);
                model.setValueAt(input, selectedRow, i);
            }

            StringBuilder queryBuilder = new StringBuilder("UPDATE ")
                    .append(entity).append(" SET ");

            for (int i = 1; i < model.getColumnCount(); i++) {
                String columnName = model.getColumnName(i);
                Object value = model.getValueAt(selectedRow, i);
                String valueStr;
                if (value instanceof String) {
                    valueStr = "'" + value + "'";
                } else {
                    valueStr = String.valueOf(value);
                }

                queryBuilder.append(columnName).append("=").append(valueStr);

                if (i < model.getColumnCount() - 1) {
                    queryBuilder.append(", ");
                }
            }

            String primaryKeyColumnName = model.getColumnName(0);
            Object primaryKeyValue = model.getValueAt(selectedRow, 0);
            String primaryKeyValueStr = String.valueOf(primaryKeyValue);

            queryBuilder.append(" WHERE ")
                    .append(primaryKeyColumnName).append("=")
                    .append(primaryKeyValueStr);

            Statement statement = connection.createStatement();
            statement.executeUpdate(queryBuilder.toString());
            statement.close();

            displayTable(entity);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to alter record in the database.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deleteRecord(String entity) {
        try {
            DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
            int selectedRow = dataTable.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "No record selected.",
                        "Delete Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this record?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                Object primaryKeyValue = model.getValueAt(selectedRow, 0);

                String query = "DELETE FROM " + entity + " WHERE " +
                        model.getColumnName(0) + "=" + primaryKeyValue;

                Statement statement = connection.createStatement();
                statement.executeUpdate(query);
                statement.close();

                model.removeRow(selectedRow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete record from the database.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearTable(String entity) {
        try {
            int option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to clear all records from this table?", "Confirm Clear",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                String query = "DELETE FROM " + entity;

                Statement statement = connection.createStatement();
                statement.executeUpdate(query);
                statement.close();

                DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
                model.setRowCount(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to clear records from the database.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UniversityDatabaseGUI());
    }
}
