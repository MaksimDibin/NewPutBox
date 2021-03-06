package ru.dibin.enumClass;

public enum Signal {

    COPY ( (byte) 14 ),
    ERROR ( (byte) 20 ),
    DELETE_FILE ( (byte) 16 ),
    DELETE_WORK_FOLDER ( (byte) 17 ),
    SEND ( (byte) 13 ),
    LIST ( (byte) 15 );

    public byte signalNumber() {
        return i;
    }

    private final byte i;

    Signal(byte i) {
        this.i = i;
    }

}
