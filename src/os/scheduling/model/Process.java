package os.scheduling.model;

import java.util.Arrays;

public class Process {
    private final int id;
    private final int[] requiredResources;
    private final int runtime;
    private int runtimeAccumulated;
    private int[] assignedResources;
    private boolean isAssigned = false;

    public Process(int id, int[] requiredResources, int runtime) {
        this.id = id;
        this.requiredResources = Arrays.copyOf(requiredResources, requiredResources.length);
        this.runtime = runtime;
        this.runtimeAccumulated = 0;
        this.assignedResources = new int[requiredResources.length];
    }

    // Getters
    public int getId() {
        return id;
    }

    public int[] getRequiredResources() {
        return Arrays.copyOf(requiredResources, requiredResources.length);
    }

    public int[] getAssignedResources() {
        return Arrays.copyOf(assignedResources, assignedResources.length);
    }

    public int getRemainingTime() {
        return runtime - runtimeAccumulated;
    }

    public boolean isAssigned() {
        return isAssigned;
    }

    // Process State Management
    public void assignToProcessor() {
        this.isAssigned = true;
    }

    public void releaseFromProcessor() {
        this.isAssigned = false;
    }

    public void runProcess(int runTimeTick) {
        if (!isFinished()) {
            runtimeAccumulated += runTimeTick;
        }
    }

    public boolean isFinished() {
        return runtimeAccumulated >= runtime;
    }

    // Resource Management
    public int[] getRemainingResources() {
        int[] remainingResources = new int[requiredResources.length];
        for (int i = 0; i < requiredResources.length; i++) {
            remainingResources[i] = requiredResources[i] - assignedResources[i];
        }
        return remainingResources;
    }

    public void assignResources(int[] givenResources) {
        for (int i = 0; i < givenResources.length; i++) {
            this.assignedResources[i] += givenResources[i];
        }
    }

    public int[] releaseResources() {
        int[] releasedResources = Arrays.copyOf(assignedResources, assignedResources.length);
        Arrays.fill(assignedResources, 0);
        return releasedResources;
    }

    public boolean canAllocateResources(int[] available) {
        int[] remainingResources = getRemainingResources();
        for (int i = 0; i < remainingResources.length; i++) {
            if (remainingResources[i] > available[i]) {
                return false;
            }
        }
        return true;
    }

    public void allocateResources(int[] available) {
        int[] remainingResources = getRemainingResources();
        for (int i = 0; i < remainingResources.length; i++) {
            available[i] -= remainingResources[i];
        }
    }
}