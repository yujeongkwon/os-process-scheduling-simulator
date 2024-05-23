package os.scheduling.strategy;

import os.scheduling.model.Process;

import java.util.List;

public class ShortestJobFirstStrategy implements SchedulingStrategy {

    @Override
    public int selectNextProcessIndex(List<Process> readyQueue, Process currentProcess, int currentProcessRunTick, int timeQuantum) {
        if (currentProcess != null || readyQueue.isEmpty()) {
            return -1;
        }

        return findShortestJobIndex(readyQueue);
    }

    private int findShortestJobIndex(List<Process> readyQueue) {
        int minIndex = -1;
        int minRemainingTime = Integer.MAX_VALUE;

        for (int i = 0; i < readyQueue.size(); i++) {
            int remainingTime = readyQueue.get(i).getRemainingTime();
            if (remainingTime < minRemainingTime) {
                minRemainingTime = remainingTime;
                minIndex = i;
            }
        }

        return minIndex;
    }
}
