import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddStaffTest {

    @BeforeEach
    public void resetTable() {
        DatabaseConnector databaseConnector = new DatabaseConnector();

        Connection connection = databaseConnector.getConnection();
        try {
            Statement statement = connection.createStatement();

            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            statement.execute("truncate table staff_services");
            statement.execute("truncate table staff");
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");


        } catch (SQLException e) {
            // Some error occurred.
            System.out.println(e.getMessage());
            System.out.println("Some error occurred");
        }
    }

    @Test
    public void nullStaffName() {
        HomelessSupport homelessSupport = new HomelessSupport();

        String name;
        Set<String> services = new HashSet<>();
        boolean volunteer;
        String managerName;

        name = null;
        volunteer = false;
        managerName = "Pqr";
        assertFalse(homelessSupport.addStaff(name, services, volunteer, managerName), "Null staff name");
    }

    @Test
    public void emptyStaffName() {
        HomelessSupport homelessSupport = new HomelessSupport();

        String name;
        Set<String> services = new HashSet<>();
        boolean volunteer;
        String managerName;

        name = "";
        volunteer = false;
        managerName = "Pqr";
        assertFalse(homelessSupport.addStaff(name, services, volunteer, managerName), "Empty staff name");
    }

    @Test
    public void managerNotPresent() {
        HomelessSupport homelessSupport = new HomelessSupport();

        String name;
        Set<String> services = new HashSet<>();
        boolean volunteer;
        String managerName;

        name = "Abc";
        volunteer = false;
        managerName = null;
        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName), "Null staff name");
    }

    @Test
    public void managerNotPresentInDB() {
        HomelessSupport homelessSupport = new HomelessSupport();

        String name;
        Set<String> services = new HashSet<>();
        boolean volunteer;
        String managerName;

        name = "Abcd";
        volunteer = true;
        managerName = "Pqr";
        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName), "New manager name");
    }

    @Test
    public void managerPresentInDB() {
        HomelessSupport homelessSupport = new HomelessSupport();

        String name;
        Set<String> services = new HashSet<>();
        boolean volunteer;
        String managerName;

        name = "Abcd";
        volunteer = true;
        managerName = "Pqr";
        assertTrue(homelessSupport.addStaff(managerName, services, volunteer, null), "adding manager name");

        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName), "Old manager name");
    }

    @Test
    public void singleServicePresent() {
        HomelessSupport homelessSupport = new HomelessSupport();

        String name;
        Set<String> services = new HashSet<>();
        boolean volunteer;
        String managerName;

        services.add("shower");

        name = "Abcd";
        volunteer = true;
        managerName = "Pqr";
        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName), "New manager name");

    }

    @Test
    public void multipleServicePresent() {
        HomelessSupport homelessSupport = new HomelessSupport();

        String name;
        Set<String> services = new HashSet<>();
        boolean volunteer;
        String managerName;

        services.add("shower");
        services.add("food");

        name = "Abcd";
        volunteer = true;
        managerName = "Pqr";
        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName), "New manager name");
    }

    @Test
    public void updateStaff() {
        HomelessSupport homelessSupport = new HomelessSupport();

        String name;
        Set<String> services = new HashSet<>();
        boolean volunteer;
        String managerName;

        services.add("shower");
        services.add("food");

        name = "Abcd";
        volunteer = true;
        managerName = "Pqr";

        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName), "New manager name");

        services = new HashSet<>();

        services.add("shower");
        volunteer = false;
        managerName = "Xyz";

        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName));
    }

    @Test
    public void updateStaffServices(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String name;
        Set<String> services = new HashSet<>();
        boolean volunteer;
        String managerName;

        services.add("shower");

        name = "Abcd";
        volunteer = true;
        managerName = "Pqr";

        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName), "New manager name");

        services = new HashSet<>();

        services.add("shower");
        services.add("food");

        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName));
    }

    @Test
    public void updateStaffIsVolunteer(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String name;
        Set<String> services = new HashSet<>();
        boolean volunteer;
        String managerName;

        services.add("shower");

        name = "Abcd";
        volunteer = true;
        managerName = "Pqrs";

        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName), "New manager name");

        volunteer = false;

        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName));
    }

    @Test
    public void updateNotPresentManager(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String name;
        Set<String> services = new HashSet<>();
        boolean volunteer;
        String managerName;

        services.add("shower");

        name = "Abcd";
        volunteer = true;
        managerName = "Pqrs";

        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName), "New manager name");

        managerName = "Tuvw";

        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName));
    }

    @Test
    public void updatePresentManager(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String name;
        Set<String> services = new HashSet<>();
        boolean volunteer;
        String managerName;

        services.add("shower");

        name = "Abcd";
        volunteer = true;
        managerName = "Pqrs";

        assertTrue(homelessSupport.addStaff(managerName, services, volunteer, null), "adding manager name");

        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName), "New manager name");

        managerName = "Tuvw";

        assertTrue(homelessSupport.addStaff(managerName, services, volunteer, null), "adding manager name");

        assertTrue(homelessSupport.addStaff(name, services, volunteer, managerName));
    }

}