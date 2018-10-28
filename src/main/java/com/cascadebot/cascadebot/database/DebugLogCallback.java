/*
 * Copyright (c) 2018 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package com.cascadebot.cascadebot.database;

import com.mongodb.async.SingleResultCallback;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public  class DebugLogCallback<T> implements SingleResultCallback<T> {

    private final Level LEVEL = Level.DEBUG;
    private final Object OBJECT_TO_LOG;
    private final String MESSAGE;

    public DebugLogCallback(Object toLog) {
        this.OBJECT_TO_LOG = toLog;
        this.MESSAGE = "";
    }

    public DebugLogCallback(String message, Object toLog) {
        this.OBJECT_TO_LOG = toLog;
        this.MESSAGE = message;
    }

    @Override
    public void onResult(T result, Throwable t) {
        if (OBJECT_TO_LOG != null) {
            LoggerFactory.getLogger("mongocallback").debug(MESSAGE + ": " + OBJECT_TO_LOG.toString().replace("\n", ""));
        }
    }

}
