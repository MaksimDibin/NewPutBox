package ru.dibin;

import ru.dibin.server.Server;

public class Main {
    public static void main(String[] args) {
        try {
            new Server ( 8180 ).start ();
        } catch (Exception e) {
            e.printStackTrace ( );
        }
    }
}
