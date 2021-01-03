package ru.dibin.server;

public enum State {
    IDLE,
    NAME_LENGTH,
    NAME,
    FILE_LENGTH,
    FILE,
    VERIFY_FAIL_PRESENCE,
    FILE_DISPATCH,
    NAME_WORK_FOLDER,
    LENGTH_WORK_FOLDER,
    LIST_OF_FILES,
    DELETE_FILE,
    DELETE_WORK_FOLDER
}
