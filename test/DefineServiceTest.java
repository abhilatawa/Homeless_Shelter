import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class DefineServiceTest {

    @BeforeEach
    public void resetTable(){
        DatabaseConnector databaseConnector = new DatabaseConnector();

        Connection connection = databaseConnector.getConnection();
        try {
            Statement statement = connection.createStatement();

            statement.execute("truncate table service");

        } catch (SQLException e) {
            // Some error occurred.
        }

    }

    @Test
    public void nullServiceName(){
        HomelessSupport homelessSupport = new HomelessSupport();
        assertFalse(homelessSupport.defineService(null, 0), "Null service name");
    }

    @Test
    public void emptyServiceName(){
        HomelessSupport homelessSupport = new HomelessSupport();
        assertFalse(homelessSupport.defineService("", 2), "Empty service name");
    }

    @Test
    public void blankSpacesInServiceName(){
        HomelessSupport homelessSupport = new HomelessSupport();
        assertFalse(homelessSupport.defineService("  ", 5), "Empty service name");

    }

    @Test
    public void InvalidFrequency(){
        HomelessSupport homelessSupport = new HomelessSupport();
        assertFalse(homelessSupport.defineService("shower", -2), "Invalid Frequency");
    }

    @Test
    public void addingSingleService(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String serviceName;
        int inspectionFrequency;


        serviceName = "shower";
        inspectionFrequency = 7;
        assertTrue(homelessSupport.defineService(serviceName, inspectionFrequency), "Single service added");
    }

    @Test
    public void addingMultipleService(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String serviceName;
        int inspectionFrequency;


        serviceName = "shower";
        inspectionFrequency = 7;
        assertTrue(homelessSupport.defineService(serviceName, inspectionFrequency), "First service added");

        serviceName = "food";
        inspectionFrequency = 3;
        assertTrue(homelessSupport.defineService(serviceName, inspectionFrequency), "Second service added");
    }

    @Test
    public void serviceRepeated(){
        HomelessSupport homelessSupport = new HomelessSupport();

        String serviceName;
        int inspectionFrequency;

        serviceName = "shower";
        inspectionFrequency = 7;
        assertTrue(homelessSupport.defineService(serviceName, inspectionFrequency), "Service added");

        inspectionFrequency = 3;
        assertTrue(homelessSupport.defineService(serviceName, inspectionFrequency), "same service updated");
    }

}
