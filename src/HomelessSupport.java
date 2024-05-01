import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.io.PrintWriter;

public class HomelessSupport {
    private Statement statement;
    private Connection connection;

    private void UseDB(Connection connect) {
        try {
            statement = connect.createStatement();
            statement.execute("use latawa");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param serviceName
     * @param inspectionFrequency
     * @return False if the methods fails to add the data into the database or if any of the input validations fails
     */
    boolean defineService( String serviceName, int inspectionFrequency ) {
        PreparedStatement preparedStatement = null;
        try {
            //making DB connection
            connection = databaseTester.getDatabaseConnection();
            UseDB(connection);
            // Input validation for serviceName and inspectionFrequency
            if (serviceName == null || serviceName.trim().isEmpty() || inspectionFrequency <= 0) {

                return false;
            }

            // Check if the service already exists
            if (serviceExists(serviceName)) {
                // If the service already exists, update its information
                updateService(serviceName, inspectionFrequency);
            } else {
                // If the service doesn't exist, insert a new record
                insertService(serviceName, inspectionFrequency);
            }
            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // Close resources in the finally block
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Log or handle the exception as needed
            }
        }
    }

    private void resetServicesautoincrement() throws SQLException {
        try (PreparedStatement resetStatement = connection.prepareStatement("ALTER TABLE Services AUTO_INCREMENT = 1")) {
            resetStatement.executeUpdate();
        }
    }


    private boolean serviceExists(String serviceName) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM Services WHERE service_name = ?")) {
            preparedStatement.setString(1, serviceName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void insertService(String serviceName, int inspectionFrequency) throws SQLException {
        resetServicesautoincrement();
        try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO Services (service_name, inspection_frequency) VALUES (?, ?)")) {
            insertStatement.setString(1, serviceName);
            insertStatement.setInt(2, inspectionFrequency);
            insertStatement.executeUpdate();
        }
    }

    private void updateService(String serviceName, int inspectionFrequency) throws SQLException {
        try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE Services SET inspection_frequency = ? WHERE service_name = ?")) {
            updateStatement.setInt(1, inspectionFrequency);
            updateStatement.setString(2, serviceName);
            updateStatement.executeUpdate();
        }
    }

    /**
     *
     * @param name is the name of the shelter
     * @param location is the cordinates of the shelter which we are getting from the point class
     * @param maxCapacity is the capacity of the shelter or camp upto which they can occupy members
     * @param staffInCharge staff that is incharge of the camp or the shelter
     * @return true if the shelter is defined succesfully with all the data.
     * Returns false if it fails to add the data into the database or any of the input validation fails
     *
     */
    boolean defineShelter( String name, Point location, int maxCapacity, String staffInCharge ) {

        if (name == null || name.trim().isEmpty() || location == null || maxCapacity <= 0 || staffInCharge == null || staffInCharge.trim().isEmpty()) {

            return false;
        }

        try {
            //making DB connection
            connection = databaseTester.getDatabaseConnection();
            UseDB(connection);

            try {
                // Check if the shelter already exists
                if (shelterExists(name)) {
                    // If the shelter already exists, update its information
                    updateShelter(name, location, maxCapacity, staffInCharge);
                } else {
                    // If the shelter doesn't exist, insert a new record
                    insertShelter(name, location, maxCapacity, staffInCharge);
                }
                return true;
            } catch (SQLException e) {
                // Handle any SQL exception, log, and return false
                e.printStackTrace();
                return false;
            }

    } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void resetAutoIncrement() throws SQLException {
        try (PreparedStatement resetStatement = connection.prepareStatement("ALTER TABLE Point AUTO_INCREMENT = 1")) {
            resetStatement.executeUpdate();
        }
    }

    private boolean shelterExists(String name) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM Shelter WHERE name = ?")) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false; // Return false in case of an exception or an empty result set
    }


    private void insertShelter(String name, Point location, int maxCapacity, String staffInCharge) throws SQLException {
        resetAutoIncrement();

        if (shelterExistsAtLocation(location)) {
            throw new SQLException("A shelter already exists at the specified location");
        }
        // Insert into PointTable
        try (PreparedStatement locationStatement = connection.prepareStatement("INSERT INTO Point (latitude, longitude) VALUES (?, ?)")) {
            locationStatement.setDouble(1, location.getX());
            locationStatement.setDouble(2, location.getY());
            locationStatement.executeUpdate();
        }

        // Get the location_id of the newly inserted location
        int locationId;
        try (PreparedStatement locationIdStatement = connection.prepareStatement("SELECT LAST_INSERT_ID()")) {
            try (ResultSet resultSet = locationIdStatement.executeQuery()) {
                if (resultSet.next()) {
                    locationId = resultSet.getInt(1);
                } else {
                    // Handle the case where no ID was returned
                    throw new SQLException("Failed to retrieve location ID");
                }
            }
        }

        // Insert into ShelterTable
        try (PreparedStatement shelterStatement = connection.prepareStatement("INSERT INTO Shelter (name, location_id, max_capacity, staff_in_charge) VALUES (?, ?, ?, ?)")) {
            shelterStatement.setString(1, name);
            shelterStatement.setInt(2, locationId);
            shelterStatement.setInt(3, maxCapacity);
            shelterStatement.setString(4, staffInCharge);
            shelterStatement.executeUpdate();
        }
    }
    private boolean shelterExistsAtLocation(Point location) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM Shelter S JOIN Point P ON S.location_id = P.location_id WHERE P.latitude = ? AND P.longitude = ?")) {
            preparedStatement.setDouble(1, location.getX());
            preparedStatement.setDouble(2, location.getY());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    private void updateShelter(String name, Point location, int maxCapacity, String staffInCharge) throws SQLException {
        // Get the location_id of the existing location

        int locationId;
        try (PreparedStatement locationIdStatement = connection.prepareStatement("SELECT location_id FROM Shelter WHERE name = ?")) {
            locationIdStatement.setString(1, name);
            try (ResultSet resultSet = locationIdStatement.executeQuery()) {
                if (resultSet.next()) {
                    locationId = resultSet.getInt(1);
                } else {
                    // Handle the case where no ID was returned
                    throw new SQLException("Shelter not found for name: " + name);
                }
            }
        }
        // Update PointTable
        try (PreparedStatement locationUpdateStatement = connection.prepareStatement("UPDATE Point SET latitude = ?, longitude = ? WHERE location_id = ?")) {
            locationUpdateStatement.setDouble(1, location.getX());
            locationUpdateStatement.setDouble(2, location.getY());
            locationUpdateStatement.setInt(3, locationId);
            locationUpdateStatement.executeUpdate();
        }

        // Update ShelterTable
        try (PreparedStatement shelterUpdateStatement = connection.prepareStatement("UPDATE Shelter SET max_capacity = ?, staff_in_charge = ? WHERE name = ?")) {
            shelterUpdateStatement.setInt(1, maxCapacity);
            shelterUpdateStatement.setString(2, staffInCharge);
            shelterUpdateStatement.setString(3, name);
            shelterUpdateStatement.executeUpdate();
        }
    }

    /**
     *
     * @param shelterName is the name of the shelter
     * @param serviceName is the services provided at the shelters
     * @return true if the se4rvice and shelter names are loaded into the database correctly.
     * retyrns false if unable to do so or fails due to any bad data.
     *
     */
    boolean serviceForShelter( String shelterName, String serviceName ) {
        PreparedStatement preparedStatement = null;

        try {
            connection = databaseTester.getDatabaseConnection();
            UseDB(connection);

            if (shelterName == null || shelterName.trim().isEmpty() || serviceName == null || serviceName.trim().isEmpty()) {
                return false;
            }
            // Check if the service is already associated with the shelter
            if (serviceExistsForShelter(shelterName, serviceName)) {
                // Service is already associated with the shelter, return false
                return false;
            }

            // Insert the association between the service and shelter
            insertServiceForShelter(shelterName, serviceName);

            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // Close resources in the finally block
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean serviceExistsForShelter(String shelterName, String serviceName) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM Shelter_Services WHERE shelter_name = ? AND service_name = ?")) {
            preparedStatement.setString(1, shelterName);
            preparedStatement.setString(2, serviceName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void insertServiceForShelter(String shelterName, String serviceName) throws SQLException {
        try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO Shelter_Services (shelter_name, service_name) VALUES (?, ?)")) {
            insertStatement.setString(1, shelterName);
            insertStatement.setString(2, serviceName);
            insertStatement.executeUpdate();
        }
    }

    /**
     *
     * @param name is the name of the shelters or camps
     * @param date is parameter which tells us how many occupants are present on the given date
     * @param occupancy it is the no. of members present in the shelter or camps
     * @return true if the data is recorded successfully with the occupants and date along with the shelters, false otherwise
     *
     */
    boolean declareShelterOccupancy( String name, String date, int occupancy ) {
        PreparedStatement preparedStatement = null;

        try {
            connection = databaseTester.getDatabaseConnection();
            UseDB(connection);

            if (name == null || name.trim().isEmpty() || date == null || date.trim().isEmpty() || !Validdate(date) || occupancy < 0) {

                return false;
            }

            if (occupancyExceedsCapacity(name, occupancy)) {
                // Handle the case where occupancy exceeds the max capacity
                return false;
            }
            // Insert the occupancy information
            insertShelterOccupancy(name, date, occupancy);
            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // Close resources in the finally block
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean occupancyExceedsCapacity(String name, int occupancy) throws SQLException {
        try (PreparedStatement capacityStatement = connection.prepareStatement("SELECT max_capacity FROM Shelter WHERE name = ?")) {
            capacityStatement.setString(1, name);
            try (ResultSet resultSet = capacityStatement.executeQuery()) {
                if (resultSet.next()) {
                    int maxCapacity = resultSet.getInt("max_capacity");
                    return occupancy > maxCapacity;
                }
            }
        }
        return false;
    }

    private void insertShelterOccupancy(String name, String date, int occupancy) throws SQLException {
        try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO Shelter_Ocupancy (shelter_name, date, occupancy) VALUES (?, ?, ?)")) {
            insertStatement.setString(1, name);
            insertStatement.setString(2, date);
            insertStatement.setInt(3, occupancy);
            insertStatement.executeUpdate();
        }
    }

    private boolean Validdate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     *
     * @param name is the names of the staff members that can be added or already present
     * @param services it is the set of the services associated with the staffs
     * @param volunteer it tells wether the newly added staff is volunteer or not. If yes then it is represent by 1 ,0 otherwise
     * @param manager it is staff associated with group of staff members
     * @return true if the user is able to add the new staff to the database
     * without fail along with the other data , false otherwise
     *
     */
    boolean addStaff(String name, Set<String> services, boolean volunteer, String manager) {
        PreparedStatement staffStatement = null;
        PreparedStatement servicesStatement = null;

        try {
            connection = databaseTester.getDatabaseConnection();
            UseDB(connection);

            if (name == null || name.trim().isEmpty()) {

                return false;
            }
            if (manager == null || manager.trim().isEmpty()) {

                return false;
            }
            if (services != null) {
                for (String service : services) {
                    if (service == null || service.trim().isEmpty()) {
                        return false;
                    }
                }
            }

            // Check if the staff with the given name already exists
            String existingStaffName = getStaffNameByName(name);
            if (existingStaffName.equals(name)) {
                // If the staff exists, update the information
                 updateStaff(existingStaffName, services, volunteer, manager);
            }
            resetStaffTableAutoIncrement();

            // Insert into STAFF table
            String staffInsertSql = "INSERT INTO Staff (staff_name, volunteer_Y_N, manager) VALUES (?, ?, ?)";
            staffStatement = connection.prepareStatement(staffInsertSql);
            staffStatement.setString(1, name);
            staffStatement.setBoolean(2, volunteer);
            staffStatement.setString(3, manager);
            staffStatement.executeUpdate();

            // Get the staff name of the newly inserted staff member
            if (services != null && !services.isEmpty()) {
                String servicesInsertSql = "INSERT INTO Staff_Services (services_staff_name, service_name) VALUES (?, ?)";
                servicesStatement = connection.prepareStatement(servicesInsertSql);
                for (String service : services) {
                    servicesStatement.setString(1, name);
                    servicesStatement.setString(2, service);
                    servicesStatement.executeUpdate();
                }
            }
            return true;

        } catch (SQLException e) {
            // Handle any SQL exception, log, and return false
            e.printStackTrace();
            return false;
        } finally {
            // Close resources in the finally block
            try {
                if (staffStatement != null) {
                    staffStatement.close();
                }
                if (servicesStatement != null) {
                    servicesStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String getStaffNameByName(String name) throws SQLException {
        String sql = "SELECT staff_name FROM Staff WHERE staff_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("staff_name") : "";
            }
        }
    }


    private boolean updateStaff(String staffName, Set<String> services, boolean volunteer, String manager) throws SQLException {
        String updateStaffSql = "UPDATE Staff SET volunteer_Y_N = ?, manager = ? WHERE staff_name = ?";
        try (PreparedStatement updateStaffStatement = connection.prepareStatement(updateStaffSql)) {
            updateStaffStatement.setBoolean(1, volunteer);
            updateStaffStatement.setString(2, manager);
            updateStaffStatement.setString(3, staffName);
            int rowsUpdated = updateStaffStatement.executeUpdate();

            if (rowsUpdated > 0) {
                // Update existing services in the STAFF_SERVICES table
                if (services != null && !services.isEmpty()) {
                    // Delete existing services
                    String deleteServicesSql = "DELETE FROM Staff_Services WHERE services_staff_name = ?";
                    try (PreparedStatement deleteServicesStatement = connection.prepareStatement(deleteServicesSql)) {
                        deleteServicesStatement.setString(1, staffName);
                        deleteServicesStatement.executeUpdate();
                    }

                    // Insert new services
                    String servicesInsertSql = "INSERT INTO Staff_Services (services_staff_name, service_name) VALUES (?, ?)";
                    try (PreparedStatement servicesInsertStatement = connection.prepareStatement(servicesInsertSql)) {
                        for (String service : services) {
                            servicesInsertStatement.setString(1, staffName);
                            servicesInsertStatement.setString(2, service);
                            servicesInsertStatement.executeUpdate();
                        }
                    }
                }
                return true; // Staff information and services updated successfully
            } else {
                return false; // Staff not found for the given name
            }
        }
    }


    private void resetStaffTableAutoIncrement() throws SQLException {
        try (PreparedStatement resetStatement = connection.prepareStatement("ALTER TABLE Staff AUTO_INCREMENT = 1")) {
            resetStatement.executeUpdate();
        }
    }


    /**
     *
     * @param name it is the name of the donor who will provide the donation
     * @param centralOffice central office that oversees / manages all the shelters or camps. They have a
     * location and coordinate all of the staff members who would be sent to inspect shelters and camps.r
     * @param contact it is the contact information of the donor like phone number or email address
     * @param fundingPrograms these are the programs through which the donors are donating the amount
     * @return true if the data is recorded correctly into the database ,false otherwise
     */
    boolean defineDonor(String name, Point centralOffice, String contact, Set<String> fundingPrograms) {
        try {
            connection = databaseTester.getDatabaseConnection();
            UseDB(connection);

            if (name == null || name.trim().isEmpty()) {
                return false;
            }

            // Input validation for central office location
            if (centralOffice == null || centralOffice.getX() == 0.0 || centralOffice.getY() == 0.0) {

                return false;
            }
            // Input validation for contact information
            if (contact == null || contact.trim().isEmpty()) {
                return false;
            }
            // Check if the donor with the given name already exists
            int existingDonorId = getDonorIdByName(name);
            if (existingDonorId != -1) {
                // If the donor exists, update the information
                updateDonor(existingDonorId, centralOffice, contact);
            } else {
                // If the donor doesn't exist, insert into Donors table and retrieve the generated donor_id
                int donorId = insertDonor(name, centralOffice, contact);

                // Insert into Donor_Funding_Programs table
                insertFundingPrograms(donorId, fundingPrograms);
            }

            return true;

        } catch (SQLException e) {
            // Handle any SQL exception, log, and return false
            e.printStackTrace();
            return false;
        } finally {
            // Close resources in the finally block
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Log or handle the exception as needed
            }
        }
    }
    private void resetdonorIdautoincrement() throws SQLException {
        try (PreparedStatement resetStatement = connection.prepareStatement("ALTER TABLE Donors AUTO_INCREMENT = 1")) {
            resetStatement.executeUpdate();
        }
    }

    private int getDonorIdByName(String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT donor_id FROM Donors WHERE donor_name = ?")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : -1;
            }
        }
    }


    private int insertDonor(String name, Point centralOffice, String contact) throws SQLException {
        // Insert into Donors table
        resetdonorIdautoincrement();

        try (PreparedStatement donorStatement = connection.prepareStatement("INSERT INTO Donors (donor_name, location_id, contact) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            donorStatement.setString(1, name);
            donorStatement.setInt(2, insertLocation(centralOffice));
            donorStatement.setString(3, contact);
            donorStatement.executeUpdate();

            try (ResultSet generatedKeys = donorStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve donor_id");
                }
            }
        }
    }

    private void updateDonor(int donorId, Point centralOffice, String contact) throws SQLException {
        // Update Donors table
        try (PreparedStatement updateDonorStatement = connection.prepareStatement("UPDATE Donors SET location_id = ?, contact = ? WHERE donor_id = ?")) {
            updateDonorStatement.setInt(1, insertLocation(centralOffice));
            updateDonorStatement.setString(2, contact);
            updateDonorStatement.setInt(3, donorId);
            updateDonorStatement.executeUpdate();
        }
    }

    private int insertLocation(Point location) throws SQLException {
        // Insert into LocationTable
        try (PreparedStatement locationStatement = connection.prepareStatement("INSERT INTO Point (latitude, longitude) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            locationStatement.setDouble(1, location.getX());
            locationStatement.setDouble(2, location.getY());
            locationStatement.executeUpdate();

            try (ResultSet generatedKeys = locationStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve location_id");
                }
            }
        }
    }

    private void insertFundingPrograms(int donorId, Set<String> fundingPrograms) throws SQLException {
        // Insert into Donor_Funding_Programs table
        try (PreparedStatement fundingProgramsStatement = connection.prepareStatement("INSERT INTO Donor_Funding_Programs (donor_id, program_id) VALUES (?, ?)")) {
            for (String program : fundingPrograms) {
                int programId = getOrCreateProgramIdByName(program);

                // Insert only if the program exists
                fundingProgramsStatement.setInt(1, donorId);
                fundingProgramsStatement.setInt(2, programId);
                fundingProgramsStatement.executeUpdate();
            }
        }
    }
    private void resetprogramidautoincrement() throws SQLException {
        try (PreparedStatement resetStatement = connection.prepareStatement("ALTER TABLE Donor_Programs AUTO_INCREMENT = 1")) {
            resetStatement.executeUpdate();
        }
    }

    private int getOrCreateProgramIdByName(String name) throws SQLException {
        // Try to get the program_id by name
        int programId = getProgramIdByName(name);
        resetprogramidautoincrement();

        if (programId == -1) {
            // If the program doesn't exist, insert into Donor_Programs and retrieve the generated program_id
            try (PreparedStatement insertProgramStatement = connection.prepareStatement("INSERT INTO Donor_Programs (program_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                insertProgramStatement.setString(1, name);
                insertProgramStatement.executeUpdate();

                try (ResultSet generatedKeys = insertProgramStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        programId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException();
                    }
                }
            }
        }

        return programId;
    }

    private int getProgramIdByName(String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT program_id FROM Donor_Programs WHERE program_name = ?")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : -1;
            }
        }
    }

    private void resetdonationidautoincrement() throws SQLException {
        try (PreparedStatement resetStatement = connection.prepareStatement("ALTER TABLE Donations AUTO_INCREMENT = 1")) {
            resetStatement.executeUpdate();
        }
    }

    /**
     *
     * @param donor Name of the donor
     * @param fundingProgram these are the programs through which the donors are donating the amount
     * @param date date on which the donation was made
     * @param donation the amount which is being donated
     * @return true if the the data is loaded successfully into the database , false otherwise
     */
    boolean receiveDonation(String donor, String fundingProgram, String date, int donation) {
        try {
            connection = databaseTester.getDatabaseConnection();
            UseDB(connection);
            // Input validation for donor name
            if (donor == null || donor.trim().isEmpty()) {
                return false;
            }

            // Input validation for funding program name
            if (fundingProgram == null || fundingProgram.trim().isEmpty()) {
                return false;
            }

            // Input validation for donation date
            if (!isValidDateFormat(date)) {
                return false;
            }
            // Input validation for donation amount
            if (donation <= 0) {
                return false;
            }
            // Check if the donor and funding program exist
            int donorId = getDonorIdByName(donor);
            int programId = getProgramIdByName(fundingProgram);

            if (donorId == -1 || programId == -1) {
                // Donor or funding program not found
                return false;
            }
            resetdonationidautoincrement();
            // Insert into Donations table
            try (PreparedStatement donationStatement = connection.prepareStatement("INSERT INTO Donations (donor_id, program_id, donation_date, donation_amount) VALUES (?, ?, ?, ?)")) {
                donationStatement.setInt(1, donorId);
                donationStatement.setInt(2, programId);
                donationStatement.setDate(3, convertStringToDate(date));
                donationStatement.setInt(4, donation);
                donationStatement.executeUpdate();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            return true;

        } catch (SQLException e) {
            // Handle any SQL exception, log, and return false
            e.printStackTrace();
            return false;
        } finally {
            // Close resources in the finally block
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Log or handle the exception as needed
            }
        }
    }

    private Date convertStringToDate(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return new Date(dateFormat.parse(date).getTime());
    }

    /**
     *
     * @param shelterReceiving name of the shelter receiving the funds
     * @param date date on which the shelters receive their respective funds
     * @param funds the amount of money they receive
     * @return true if the data was loaded successfully , false otherwise
     */
    public boolean disburseFunds(String shelterReceiving, String date, int funds) {
        String sqlCheckShelter = "select name from Shelter where name = ?";
        String sqlInsertDisbursement = "insert into funds(shelter_name, disbursement_date, disbursement_amount) values (?, ?, ?)";

        try {
            connection = databaseTester.getDatabaseConnection();
            UseDB(connection);

            if (shelterReceiving == null || shelterReceiving.trim().isEmpty()) {
                return false;
            }
            // Input validation for disbursement date
            if (!isValidDateFormat(date)) {
                return false;
            }
            // Input validation for funds amount
            if (funds <= 0) {
                return false;
            }
            PreparedStatement preparedStatementShelterCheck = connection.prepareStatement(sqlCheckShelter);
            preparedStatementShelterCheck.setString(1, shelterReceiving);

            ResultSet shelterResult = preparedStatementShelterCheck.executeQuery();

            if (!shelterResult.next()) {
                return false;
            }

            String shelterName = shelterResult.getString("name");

            PreparedStatement preparedStatementInsertDisbursement = connection.prepareStatement(sqlInsertDisbursement);
            preparedStatementInsertDisbursement.setString(1, shelterName); // Using shelter name
            preparedStatementInsertDisbursement.setString(2, date);
            preparedStatementInsertDisbursement.setInt(3, funds);

            int rowsAffected = preparedStatementInsertDisbursement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @param threshold TThe threshold parameter is the percentage...once divided by 100.
     * So threshold=90 means report those shelters operating at 90% of capacity or more.
     * @return set of the shelters operating above or equal to threshold value , otherwise empty set
     */
    Set<String> shelterAtCapacity( int threshold ) {

        Set<String> shelters = new HashSet<>();
        if (threshold < 0 || threshold > 100) {

            return shelters;
        }
            try {
                connection = databaseTester.getDatabaseConnection();
                UseDB(connection);

                // Query to get the Most recent occupancy accoprding to most recent date
                String sql = "SELECT s.name, o.occupancy, s.max_capacity " +
                        "FROM Shelter s " +
                        "JOIN Shelter_Ocupancy o ON s.name = o.shelter_name " +
                        "WHERE o.date = (SELECT MAX(o.date) FROM Shelter_Ocupancy WHERE shelter_name = s.name)";

                try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        String shelterName = resultSet.getString("name");
                        int occupancy = resultSet.getInt("occupancy");
                        int capacity = resultSet.getInt("max_capacity");

                        double occupancyPercentage = (occupancy * 100.0) / capacity;

                        if (occupancyPercentage >= threshold) {
                            shelters.add(shelterName);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return shelters;
        }

    /**
     *
     * @param startDate
     * @param endDate
     * @param threshold The threshold parameter is the percentage...once divided by 100. So threshold=10 means report
     * those shelters operating with a variance of 10% of capacity or more.
     * @return Set of shelters which are operating above or equal to the threshold parameter , otherwise empty set
     *
     */
    public Set<String> occupancyVariance(String startDate, String endDate, int threshold) {
        Set<String> occupancyvariancethresholdshelters = new HashSet<>();
        try {
            connection = databaseTester.getDatabaseConnection();
            UseDB(connection);

            if (!isValidDateFormat(startDate) || !isValidDateFormat(endDate) || startDate.compareTo(endDate) > 0) {
                return occupancyvariancethresholdshelters;
            }


            // Input validation for the threshold
            if (threshold < 0 || threshold > 100) {
                return occupancyvariancethresholdshelters;
            }

            String query = "SELECT s.name " +
                    "FROM Shelter s " +
                    "JOIN Shelter_Ocupancy o ON s.name = o.shelter_name " +
                    "WHERE o.date BETWEEN ? AND ? " +
                    "GROUP BY s.name, s.max_capacity " +
                    "HAVING ((MAX(o.occupancy) - MIN(o.occupancy)) / s.max_capacity) * 100 >= ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setString(1, startDate);
                preparedStatement.setString(2, endDate);
                preparedStatement.setInt(3, threshold);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        occupancyvariancethresholdshelters.add(resultSet.getString("name"));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return occupancyvariancethresholdshelters;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param startDate
     * @param endDate
     * @param outstream recordes the donor’s name on a line by itself. Then, for each of the donor’s funding programs, print a tab, the funding program’s name, a tab,
     * and then the total funding provided under that program in the time range. Finishes the reporting block for a donor with a blank line.
     */
    public void donorReport(String startDate, String endDate, PrintWriter outstream) {
        // Input validation for date formats and boundaries
        if (!isValidDateFormat(startDate) || !isValidDateFormat(endDate) || startDate.compareTo(endDate) > 0) {
            return;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = databaseTester.getDatabaseConnection();
            UseDB(connection);

            String sql = "SELECT d.donor_name, dp.program_name, SUM(do.donation_amount) AS total_donations " +
                    "FROM latawa.Donations do " +
                    "JOIN latawa.Donor_Programs dp ON dp.program_id = do.program_id " +
                    "JOIN latawa.Donors d ON do.donor_id = d.donor_id " +
                    "WHERE do.donation_date BETWEEN ? AND ? " +
                    "GROUP BY d.donor_name, dp.program_name";

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, startDate);
            preparedStatement.setString(2, endDate);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                String currentDonor = null;
                while (resultSet.next()) {
                    String donorName = resultSet.getString("donor_name");
                    String programName = resultSet.getString("program_name");
                    int totalFunding = resultSet.getInt("total_donations");

                    if (!donorName.equals(currentDonor)) {
                        outstream.println();
                        // Print donor's name on a new line
                        outstream.println(donorName);
                        currentDonor = donorName;
                    }

                    // Print funding program details
                    outstream.println("\t" + programName + "\t" + totalFunding);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in the finally block
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                // Close the PrintWriter
                if (outstream != null) {
                    outstream.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Helper method to validate date format (assuming "yyyy-MM-dd" format)
    private boolean isValidDateFormat(String date) {
        try {
            LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     *
     * @param startDate
     * @param endDate
     * @param distance reords the distance from the point class which is used to measure the shelter or camps which exists in this range
     * @param threshold
     * @return the set of shelters who have the lowest per-occupant funding in the given reporting period (including both start and end dates).
     *
     */
    Set<String> underfundedShelter( String startDate, String endDate, int distance, int threshold ) {

        if (!isValidDateFormat(startDate) || !isValidDateFormat(endDate) || startDate.compareTo(endDate) > 0) {

            return null;
        }

        if (distance <= 0 || threshold < 0 || threshold > 100) {

            return null;
        }
        return null;
    }

    /**
     *
     * @param scheduleDays
     * @param inspectLimit
     * @return
     */
    Map<String, List<String>> inspectionSchedule( int scheduleDays, int inspectLimit ) {

        if (scheduleDays <= 0 || inspectLimit <= 0) {

            return null;
        }
        return null;
    }
}
