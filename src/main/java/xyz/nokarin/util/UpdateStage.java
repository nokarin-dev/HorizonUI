package xyz.nokarin.util;

public enum UpdateStage {
    PREPARE(10),
    DELETE_OLD(10),
    DOWNLOAD(60),
    VERIFY(15),
    FINALIZE(5);

    private final int weight;

    UpdateStage(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }
}
