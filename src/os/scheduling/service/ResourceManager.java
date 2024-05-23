package os.scheduling.service;

import os.scheduling.model.Process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceManager {
    private int[] available;
    private int[][] max;
    private int[][] allocation;
    private boolean[] finished;
    private int[] work;

    private ResourceManager(int[] available, int[][] max, int[][] allocation) {
        this.available = Arrays.copyOf(available, available.length);
        this.max = copy2DArray(max);
        this.allocation = copy2DArray(allocation);
        this.finished = new boolean[max.length];
        this.work = Arrays.copyOf(available, available.length);
    }

    public static Object[] getProcessToAssignResources(int[] currResourceInfo, int[] totalResourceInfo, List<Process> currProcessWaitingList) {
        int[] available = currResourceInfo.clone();
        int[][] max = extractMax(currProcessWaitingList);
        int[][] allocation = extractAllocation(currProcessWaitingList);

        for (int processIndex = 0; processIndex < currProcessWaitingList.size(); processIndex++) {
            int[] request = currProcessWaitingList.get(processIndex).getRemainingResources();

            if (anyGreaterThan(totalResourceInfo, request)) {
                return new Object[]{-1, null};
            }

            ResourceManager resourceManager = new ResourceManager(available, max, allocation);
            SafetyCheckResult safetyCheckResult = resourceManager.simulateRequest(processIndex, request);

            if (safetyCheckResult.isSafe()) {
                return new Object[]{1, getPickedProcesses(currProcessWaitingList, safetyCheckResult.safeSequence(), available)};
            }
        }

        return new Object[]{0, null};
    }

    public SafetyCheckResult simulateRequest(int processIndex, int[] request) {
        int[] availableBackup = Arrays.copyOf(available, available.length);
        int[][] allocationBackup = copy2DArray(allocation);

        if (!canGrantRequest(processIndex, request)) {
            return new SafetyCheckResult(false, new ArrayList<>());
        }
        grantRequest(processIndex, request);
        SafetyCheckResult safetyResult = checkSafety();
        if (!safetyResult.isSafe()) {
            restoreState(availableBackup, allocationBackup);
        }
        return safetyResult;
    }

    private SafetyCheckResult checkSafety() {
        List<Integer> safeSequence = new ArrayList<>();
        boolean foundProcess;

        do {
            foundProcess = false;
            for (int i = 0; i < max.length; i++) {
                if (!finished[i] && canExecuteProcess(i)) {
                    allocateResources(i);
                    finished[i] = true;
                    safeSequence.add(i);
                    foundProcess = true;
                }
            }
        } while (foundProcess);

        return allProcessesFinished() ? new SafetyCheckResult(true, safeSequence) : new SafetyCheckResult(false, new ArrayList<>());
    }

    private boolean canGrantRequest(int processIndex, int[] request) {
        for (int i = 0; i < request.length; i++) {
            if (request[i] > available[i] || request[i] > max[processIndex][i] - allocation[processIndex][i]) {
                return false;
            }
        }
        return true;
    }

    private boolean canExecuteProcess(int i) {
        for (int j = 0; j < available.length; j++) {
            if (max[i][j] - allocation[i][j] > work[j]) {
                return false;
            }
        }
        return true;
    }

    private void allocateResources(int i) {
        for (int j = 0; j < available.length; j++) {
            work[j] += allocation[i][j];
        }
    }

    private void grantRequest(int processIndex, int[] request) {
        for (int i = 0; i < request.length; i++) {
            available[i] -= request[i];
            allocation[processIndex][i] += request[i];
        }
    }

    private void restoreState(int[] availableBackup, int[][] allocationBackup) {
        available = availableBackup;
        allocation = allocationBackup;
    }

    private boolean allProcessesFinished() {
        for (boolean f : finished) {
            if (!f) return false;
        }
        return true;
    }

    private int[][] copy2DArray(int[][] array) {
        int[][] copy = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = Arrays.copyOf(array[i], array[i].length);
        }
        return copy;
    }

    private static int[][] extractMax(List<Process> processList) {
        int[][] max = new int[processList.size()][];
        for (int i = 0; i < processList.size(); i++) {
            max[i] = processList.get(i).getRequiredResources();
        }
        return max;
    }

    private static int[][] extractAllocation(List<Process> processList) {
        int[][] allocation = new int[processList.size()][];
        for (int i = 0; i < processList.size(); i++) {
            allocation[i] = processList.get(i).getAssignedResources();
        }
        return allocation;
    }

    private static List<Process> getPickedProcesses(List<Process> processList, List<Integer> safeSequence, int[] available) {
        List<Process> pickedProcesses = new ArrayList<>();
        for (int picked : safeSequence) {
            Process process = processList.get(picked);

            if (process.canAllocateResources(available)) {
                process.allocateResources(available);
                pickedProcesses.add(process);
            }
        }
        return pickedProcesses;
    }

    private static boolean anyGreaterThan(int[] total, int[] request) {
        for (int i = 0; i < total.length; i++) {
            if (total[i] < request[i]) {
                return true;
            }
        }
        return false;
    }
}
