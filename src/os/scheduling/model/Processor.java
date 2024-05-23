package os.scheduling.model;

import os.scheduling.service.TaskScheduler;
import os.scheduling.enums.SchedulingAlgorithm;

import java.util.LinkedList;
import java.util.List;

public class Processor {
    private final SchedulingAlgorithm schedulingAlgorithm;
    private final ProcessQueue readyQueue = new ProcessQueue();
    private final int timeQuantum;
    private Process currentProcess = null;
    private int currentTickDuration = 0;

    public Processor(int timeQuantum, SchedulingAlgorithm schedulingAlgorithm) {
        this.schedulingAlgorithm = schedulingAlgorithm;
        this.timeQuantum = timeQuantum;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d, %s]",
                currentProcess == null ? -1 : currentProcess.getId(),
                currentTickDuration,
                readyQueue
        );
    }

    // 상태 확인 메서드
    public boolean isIdle() {
        return currentProcess == null;
    }

    // 현재 작업량 계산 메서드
    public int calculateTotalWorkload() {
        int totalWorkload = currentProcess != null ? currentProcess.getRemainingTime() : 0;
        totalWorkload += readyQueue.getTotalRemainingTime();
        return totalWorkload;
    }

    // 현재 프로세스 관리 메서드
    public Process getCurrentProcess() {
        return currentProcess;
    }

    public void setCurrentProcess(Process currentProcess) {
        this.currentProcess = currentProcess;
    }

    // 준비 큐 관리 메서드
    public List<Process> getReadyQueue() {
        return readyQueue.getQueue();
    }

    public void addProcessToReadyQueue(Process process) {
        readyQueue.enqueue(process);
    }

    private void resetCurrentTick() {
        currentTickDuration = 0;
    }

    // 프로세스 할당 및 실행 메서드
    public void checkAndAssignProcess() {
        int selectedIdx = selectProcessIdxToAssign();
        if (selectedIdx != -1) {
            if (currentProcess != null) {
                readyQueue.enqueue(currentProcess);
            }
            currentProcess = readyQueue.remove(selectedIdx);
            resetCurrentTick();
        }
    }

    public void runProcess() {
        if (currentProcess != null) {
            currentProcess.runProcess(1);
            currentTickDuration++;
        }
    }

    // 프로세스 정리 메서드
    public int[] cleanupProcess() {
        if (currentProcess != null && currentProcess.isFinished()) {
            int[] resourcesToReturn = currentProcess.releaseResources();
            currentProcess = null;
            resetCurrentTick();
            return resourcesToReturn;
        }
        return new int[0];
    }

    // 프로세스 선택 메서드
    private int selectProcessIdxToAssign() {
        return TaskScheduler.getProcessIdxFromQueue(
                currentProcess,
                new LinkedList<>(readyQueue.getQueue()),
                schedulingAlgorithm,
                currentTickDuration,
                timeQuantum
        );
    }
}
