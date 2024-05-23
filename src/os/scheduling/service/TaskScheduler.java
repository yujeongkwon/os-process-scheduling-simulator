package os.scheduling.service;

import os.scheduling.enums.SchedulingAlgorithm;
import os.scheduling.model.Process;
import os.scheduling.strategy.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TaskScheduler {

    private static final Map<SchedulingAlgorithm, SchedulingStrategy> strategies = new EnumMap<>(SchedulingAlgorithm.class);

    static {
        strategies.put(SchedulingAlgorithm.FCFS, new FirstComeFirstServedStrategy());
        strategies.put(SchedulingAlgorithm.RR, new RoundRobinStrategy());
        strategies.put(SchedulingAlgorithm.SJF, new ShortestJobFirstStrategy());
        strategies.put(SchedulingAlgorithm.SRJF, new ShortestRemainingJobFirstStrategy());
    }

    public static int getProcessIdxFromQueue(Process currentProcess, List<Process> readyQueue, SchedulingAlgorithm schedulingAlgorithm, int currentProcessRunTick, int timeQuantum) {
        SchedulingStrategy strategy = strategies.get(schedulingAlgorithm);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported os.scheduling algorithm: " + schedulingAlgorithm);
        }
        return strategy.selectNextProcessIndex(readyQueue, currentProcess, currentProcessRunTick, timeQuantum);
    }
}
