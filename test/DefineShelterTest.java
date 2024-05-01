import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class DefineShelterTest {

    @BeforeEach
    public void resetTable(){
        DatabaseConnector databaseConnector = new DatabaseConnector();

        Connection connection = databaseConnector.getConnection();
        try {
            Statement statement = connection.createStatement();

            statement.execute("truncate table shelter");
            statement.execute("truncate table staff_services");
            statement.execute("truncate table staff");

        } catch (SQLException e) {
            // Some error occurred.
        }

    }

    @Test
    public void nullShelterName(){
        HomelessSupport homelessSupport = new HomelessSupport();

        Point point;
        String staffInChargeName;
        int maxOccupancy;

        point = new Point(2, 2);
        maxOccupancy = 7;
        staffInChargeName = "Xyz";
        assertFalse(homelessSupport.defineShelter(null, point, maxOccupancy, staffInChargeName),"Null shelter name");

    }

    @Test
    public void emptyShelterName(){
        HomelessSupport homelessSupport = new HomelessSupport();

        Point point;
        String shelterName;
        String staffInChargeName;
        int maxOccupancy;

        point = new Point(2, 2);
        shelterName = "";
        maxOccupancy = 7;
        staffInChargeName = "Xyz";
        assertFalse(homelessSupport.defineShelter(shelterName, point, maxOccupancy, staffInChargeName),"Empty shelter name");

    }

    @Test
    public void nullStaffName(){
        HomelessSupport homelessSupport = new HomelessSupport();

        Point point;
        String shelterName;
        int maxOccupancy;

        point = new Point(2, 2);
        shelterName = "shower";
        maxOccupancy = 7;
        assertFalse(homelessSupport.defineShelter(shelterName, point, maxOccupancy, null),"Null staff name");

    }

    @Test
    public void emptyStaffName(){
        HomelessSupport homelessSupport = new HomelessSupport();

        Point point;
        String shelterName;
        String staffInChargeName;
        int maxOccupancy;

        point = new Point(2, 2);
        shelterName = "Testing";
        maxOccupancy = 7;
        staffInChargeName = "";
        assertFalse(homelessSupport.defineShelter(shelterName, point, maxOccupancy, staffInChargeName),"Empty staff name");

    }

    @Test
    public void invalidOccupancy(){
        HomelessSupport homelessSupport = new HomelessSupport();

        Point point;
        String shelterName;
        String staffInChargeName;
        int maxOccupancy;

        point = new Point(2, 2);
        shelterName = "Abc";
        maxOccupancy = -7;
        staffInChargeName = "Xyz";
        assertFalse(homelessSupport.defineShelter(shelterName, point, maxOccupancy, staffInChargeName),"Invalid occupancy");

    }

    @Test
    public void singleShelter() {
        HomelessSupport homelessSupport = new HomelessSupport();

        Point point;
        String shelterName;
        String staffInChargeName;
        int maxOccupancy;

        point = new Point(2, 5);
        shelterName = "Abc";
        maxOccupancy = 70;
        staffInChargeName = "Xyz";
        assertTrue(homelessSupport.defineShelter(shelterName, point, maxOccupancy, staffInChargeName),"Empty shelter name");
    }


    @Test
    public void staffMemberNotPresent() {
        HomelessSupport homelessSupport = new HomelessSupport();

        Point point;
        String shelterName;
        String staffInChargeName;
        int maxOccupancy;

        point = new Point(2, 5);
        shelterName = "Abc";
        maxOccupancy = 70;
        staffInChargeName = "pqr";
        assertTrue(homelessSupport.defineShelter(shelterName, point, maxOccupancy, staffInChargeName),"Empty shelter name");

    }
}
