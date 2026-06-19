package xyz.nokarin.gui;

public record UpdateState(String message, int progress, double speedKBps, long etaSeconds, boolean indeterminate) {
    public UpdateState(String message, int progress, double speedKBps, long etaSeconds) {
        this(message, progress, speedKBps, etaSeconds, false);
    }

    public UpdateState(String message, int progress) {
        this(message, progress, 0.0, 0, false);
    }

    public UpdateState(String message, int progress, boolean indeterminate) {
        this(message, progress, 0.0, 0, indeterminate);
    }
}
