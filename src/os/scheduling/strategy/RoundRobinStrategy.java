package os.scheduling.strategy;

import os.scheduling.model.Process;

import java.util.List;

public class RoundRobinStrategy implements SchedulingStrategy {
    @Override
    public int selectNextProcessIndex(List<Process> readyQueue, Process currentProcess, int currentProcessRunTick, int timeQuantum) {
        if ((currentProcess == null || currentProcessRunTick >= timeQuantum) && !readyQueue.isEmpty()) {
            return 0;
        }
        return -1;
    }
}