package ru.dibin.test;

import org.junit.Before;
import org.junit.Test;
import ru.dibin.connectingDB.ConnectingToOutbox;
import ru.dibin.connectingDB.DataBaseConnection;

import java.sql.SQLException;

public class TestConnectingBD {

    public DataBaseConnection dataBaseConnection;
    public ConnectingToOutbox connectingToOutbox;

    @Before
    public void connectingToOutbox(){
        connectingToOutbox = new ConnectingToOutbox ();
        dataBaseConnection = new DataBaseConnection ();
    }

    @Test
    public void start(){ //Одна и таже ошибка в любом тесте, почему?
        try {
            connectingToOutbox.start ();
        } catch (SQLException throwables) {
            throwables.printStackTrace ( );
        }
    }

    @Test
    public void repeatedAuthorization(){
        connectingToOutbox.repeatedAuthorization ();
    }

    @Test
    public void repeatedCreateAnAccount(){
        try {
            connectingToOutbox.repeatedCreateAnAccount ( "1", "2" );
        } catch (SQLException throwables) {
            throwables.printStackTrace ( );
        }
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
            dataBaseConnection.signIn ( "2222", "123" );
        } catch (SQLException throwables) {
            throwables.printStackTrace ( );
        }
    }

    @Test
    public void deletingAnAccount(){
        try {
            dataBaseConnection.deletingAnAccount ( "222", "123" );
        } catch (SQLException throwables) {
            throwables.printStackTrace ( );
        }
    }
}
