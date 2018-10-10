package com.github.darych.moneytransfer.model;

import java.io.IOException;

public class StorageException extends IOException {
    public StorageException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public StorageException(String msg) {
        super(msg, null);
    }
}
