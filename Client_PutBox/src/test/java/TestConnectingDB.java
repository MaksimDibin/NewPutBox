
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.dibin.connectingDB.DataBaseConnection;

import java.sql.SQLException;

public class TestConnectingDB {

    public DataBaseConnection dataBaseConnection;


    @BeforeAll
    public void connectingToOutbox(){
        dataBaseConnection = new DataBaseConnection ();
    }

    @Test
    public void startDb(){
        dataBaseConnection.startBD ();
    }

    @Test
    public  void createAnAccount(){
        try {
            dataBaseConnection.createAnAccount ( "MadMax", "1222", "432" );
        } catch (SQLException throwables) {
            throwables.printStackTrace ( );
        }
    }

    @Test
    public void signIn(){
        try {
            dataBaseConnection.signIn ( "1222", "432" );
        } catch (SQLException throwables) {
            throwables.printStackTrace ( );
        }
    }

    @Test
    public void deletingAnAccount(){
        try {
            dataBaseConnection.deletingAnAccount ( "1222", "432" );
        } catch (SQLException throwables) {
            throwables.printStackTrace ( );
        }
    }
}
