package com.github.darych.moneytransfer.model;

import com.google.inject.AbstractModule;

public class StorageModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Storage.class).to(StorageImpl.class);
    }
}
