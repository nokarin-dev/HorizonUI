package com.nokarin.gui;

public record UpdateState(String message, int progress, double speedKBps, long etaSeconds) {
    public UpdateState(String message, int progress) {
        this(message, progress, 0.0, 0);
    }
}