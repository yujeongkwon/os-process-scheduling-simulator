package os.scheduling.strategy;

import os.scheduling.model.Process;

import java.util.List;

public class FirstComeFirstServedStrategy implements SchedulingStrategy {
    @Override
    public int selectNextProcessIndex(List<Process> readyQueue, Process currentProcess, int currentProcessRunTick, int timeQuantum) {
        if (currentProcess != null || readyQueue.isEmpty()) {
            return -1;
        }
        return 0;
    }
}