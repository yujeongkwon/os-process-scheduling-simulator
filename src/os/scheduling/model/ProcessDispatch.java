package os.scheduling.model;

public record ProcessDispatch(int processId, int dispatchTime, int runTime, int[] resources) {

    @Override
    public String toString() {
        return String.format("[%d, %d, %d, %s]", processId, dispatchTime, runTime, java.util.Arrays.toString(resources));
    }
}