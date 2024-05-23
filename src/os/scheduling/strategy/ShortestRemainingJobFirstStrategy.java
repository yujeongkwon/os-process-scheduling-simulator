package os.scheduling.strategy;

import os.scheduling.model.Process;

import java.util.Comparator;
import java.util.List;

public class ShortestRemainingJobFirstStrategy implements SchedulingStrategy {
    @Override
    public int selectNextProcessIndex(List<Process> readyQueue, Process currentProcess, int currentProcessRunTick, int timeQuantum) {
        if (readyQueue.isEmpty()) {
            return -1;
        }

        int minIndex = findShortestRemainingJobIndex(readyQueue);

        if (currentProcess != null && !currentProcess.isFinished()
                && currentProcess.getRemainingTime() <= readyQueue.get(minIndex).getRemainingTime()) {
            return -1;
        }

        return minIndex;
    }

    private int findShortestRemainingJobIndex(List<Process> readyQueue) {
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
