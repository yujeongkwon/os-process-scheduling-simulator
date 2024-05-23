package os.scheduling.strategy;

import os.scheduling.model.Process;

import java.util.List;

public interface SchedulingStrategy {
    int selectNextProcessIndex(List<Process> readyQueue, Process currentProcess, int currentProcessRunTick, int timeQuantum);
}