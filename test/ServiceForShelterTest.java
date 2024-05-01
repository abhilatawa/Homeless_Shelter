import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceForShelterTest {

    @BeforeEach
    public void resetTable() {
        DatabaseConnector databaseConnector = new DatabaseConnector();

        Connection connection = databaseConnector.getConnection();
        try {
            Statement statement = connection.createStatement();

            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            statement.execute("truncate table shelter_services");
            statement.execute("truncate table staff_services");
            statement.execute("truncate table staff");
            statement.execute("truncate table shelter");
            statement.execute("truncate table service");
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");

        } catch (SQLException e) {
            // Some error occurred.
            System.out.println(e.getMessage());
            System.out.println("Some error occurred");
        }
    }

    @Test
    public void nullShelterName(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String serviceName;

        serviceName = "Xyz";

        assertFalse(homelessSupport.serviceForShelter(null, serviceName));

    }

    @Test
    public void emptyShelterName(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String serviceName;
        String shelterName;

        shelterName = "";
        serviceName = "Xyz";

        assertFalse(homelessSupport.serviceForShelter(shelterName, serviceName));

    }

    @Test
    public void nullServiceName(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String shelterName;

        shelterName = "Abc";

        assertFalse(homelessSupport.serviceForShelter(shelterName, null));

    }

    @Test
    public void emptyServiceName(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String serviceName;
        String shelterName;

        shelterName = "Abc";
        serviceName = "";

        assertFalse(homelessSupport.serviceForShelter(shelterName, serviceName));

    }

    @Test
    public void servicePresent(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String serviceName;
        String shelterName;

        int inspectionFrequency;

        serviceName = "shower";
        inspectionFrequency = 7;
        assertTrue(homelessSupport.defineService(serviceName, inspectionFrequency), "Single service added");

        Point point;
        String staffInChargeName;
        int maxOccupancy;

        point = new Point(2, 5);
        shelterName = "Abc";
        maxOccupancy = 70;
        staffInChargeName = "Xyz";
        assertTrue(homelessSupport.defineShelter(shelterName, point, maxOccupancy, staffInChargeName),"Empty shelter name");


        shelterName = "Abc";
        serviceName = "shower";

        assertTrue(homelessSupport.serviceForShelter(shelterName, serviceName));

    }

    @Test
    public void serviceNotPresent(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String serviceName;
        String shelterName;

        Point point;
        String staffInChargeName;
        int maxOccupancy;

        point = new Point(2, 5);
        shelterName = "Abc";
        maxOccupancy = 70;
        staffInChargeName = "Xyz";
        assertTrue(homelessSupport.defineShelter(shelterName, point, maxOccupancy, staffInChargeName),"Inserting shelter");


        shelterName = "Abc";
        serviceName = "Shower";

        assertFalse(homelessSupport.serviceForShelter(shelterName, serviceName));

    }

    @Test
    public void shelterPresent(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String serviceName;
        String shelterName;

        int inspectionFrequency;

        serviceName = "shower";
        inspectionFrequency = 7;
        assertTrue(homelessSupport.defineService(serviceName, inspectionFrequency), "Single service added");

        Point point;
        String staffInChargeName;
        int maxOccupancy;

        point = new Point(2, 5);
        shelterName = "Town hall";
        maxOccupancy = 70;
        staffInChargeName = "Jessica";
        assertTrue(homelessSupport.defineShelter(shelterName, point, maxOccupancy, staffInChargeName),"Empty shelter name");


        shelterName = "Town hall";
        serviceName = "shower";

        assertTrue(homelessSupport.serviceForShelter(shelterName, serviceName));

    }

    @Test
    public void shelterNotPresent(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String serviceName;
        String shelterName;

        int inspectionFrequency;

        serviceName = "shower";
        inspectionFrequency = 7;
        assertTrue(homelessSupport.defineService(serviceName, inspectionFrequency), "Single service added");

        shelterName = "Town hall";
        serviceName = "shower";

        assertFalse(homelessSupport.serviceForShelter(shelterName, serviceName));

    }
}
