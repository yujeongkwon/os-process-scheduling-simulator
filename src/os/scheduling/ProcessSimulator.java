package os.scheduling;

import os.scheduling.enums.Constants;
import os.scheduling.model.Process;
import os.scheduling.model.ProcessDispatch;
import os.scheduling.model.Processor;
import os.scheduling.service.ResourceManager;

import java.util.*;
import java.util.stream.Collectors;

public class ProcessSimulator {
    private int currentTime = 0;
    private boolean isSolvable = true;
    private final List<Processor> processorList = new ArrayList<>();
    private final List<Process> processWaitingList = new ArrayList<>();
    private final List<ProcessDispatch> processDispatches = new ArrayList<>();
    private final int[] currentResources = Arrays.copyOf(Constants.GIVEN_RESOURCES, Constants.GIVEN_RESOURCES.length);
    private final Random random = new Random();

    public ProcessSimulator() {
        initializeProcessors();
        generateRandomDispatchSchedule();
    }

    private void setSolvable(boolean b) {
        isSolvable = b;
    }

    private void advanceTime() {
        currentTime++;
    }

    public void putProcessToWaitingList(Process processToPut) {
        processWaitingList.add(processToPut);
    }

    private void initializeProcessors() {
        for (int i = 0; i < Constants.NUM_PROCESSORS; i++) {
            processorList.add(new Processor(Constants.timeQuantum, Constants.PROCESSOR_SCHEDULING_ALGORITHM));
        }
    }

    public void generateRandomDispatchSchedule() {
        List<Integer> dispatchTimes = generateSortedRandomTimes();
        printDispatchHeader();

        for (int i = 0; i < Constants.NUM_PROCESSES; i++) {
            ProcessDispatch dispatch = createProcessDispatch(i, dispatchTimes.get(i));
            processDispatches.add(dispatch);
            System.out.println(dispatch);
        }
    }

    private List<Integer> generateSortedRandomTimes() {
        return random.ints(0, Constants.PROCESS_GENERATION_TIME_LIMIT + 1)
                .limit(Constants.NUM_PROCESSES)
                .sorted()
                .boxed()
                .collect(Collectors.toList());
    }

    private ProcessDispatch createProcessDispatch(int processIndex, int dispatchTime) {
        int runTime = random.nextInt(Constants.MAX_PROCESS_RUN_TIME) + 1;
        int[] resources = new int[Constants.NUM_OF_RESOURCE_TYPES];
        for (int j = 0; j < Constants.NUM_OF_RESOURCE_TYPES; j++) {
            resources[j] = random.nextInt((Constants.GIVEN_RESOURCES[j] / 2) + 1);
        }
        return new ProcessDispatch(processIndex + 1, dispatchTime, runTime, resources);
    }

    private void printDispatchHeader() {
        System.out.println("================================================");
        System.out.println("Generated tasks: [task number, dispatch time, expected run time, required resources]");
    }

    public static void main(String[] args) {
        ProcessSimulator simulator = new ProcessSimulator();
        simulator.simulate();
    }

    public void simulate() {
        System.out.println("#PROC #TICK : PROC ID     | # CPU WAIT | # WAITING QUEUE | CURRENT RESOURCES");
        System.out.println("-------------------------------------------------------------------------------");

        while (isSolvable) {
            if (processTick()) break;
        }
        System.out.println("Finish!");
    }

    private boolean processTick() {
        boolean isDispatchedTasksExists = dispatchProcesses();
        boolean isReadyTaskExists = isReadyTaskExists();
        boolean isFinish = processResourceAssignments();
        checkAndAssignProcesses();
        printCurrentState();
        cleanupProcesses();
        advanceTime();
        return checkSimulationCompletion(isDispatchedTasksExists, isReadyTaskExists, isFinish);
    }

    private boolean dispatchProcesses() {
        if (!processDispatches.isEmpty()) {
            dispatchProcessesToWaitingList();
            return true;
        } else {
            return processorList.stream().anyMatch(processor -> processor.getCurrentProcess() != null);
        }
    }

    private void dispatchProcessesToWaitingList() {
        Iterator<ProcessDispatch> dispatchIterator = processDispatches.iterator();
        while (dispatchIterator.hasNext()) {
            ProcessDispatch processDispatch = dispatchIterator.next();
            if (processDispatch.dispatchTime() == currentTime) {
                Process newProcess = generateProcessesWithInfo(processDispatch);
                putProcessToWaitingList(newProcess);
                dispatchIterator.remove();
            }
        }
    }

    public Process generateProcessesWithInfo(ProcessDispatch processDispatch) {
        return new Process(processDispatch.processId(), processDispatch.resources(), processDispatch.runTime());
    }

    private boolean isReadyTaskExists() {
        return processorList.stream().anyMatch(processor -> !processor.getReadyQueue().isEmpty());
    }

    public boolean processResourceAssignments() {
        while (!processWaitingList.isEmpty()) {
            Object[] result = ResourceManager.getProcessToAssignResources(currentResources, Constants.GIVEN_RESOURCES, processWaitingList);
            int allocationStatus = (int) result[0];
            List<Process> selectedProcesses = (List<Process>) result[1];

            if (allocationStatus == -1) {
                handleUnsolvableState();
                return true;
            } else if (allocationStatus == 0) {
                break;
            } else if (allocationStatus == 1) {
                processSelectedProcesses(selectedProcesses);
            } else {
                handleErrorState();
                return true;
            }
        }
        return false;
    }

    private void processSelectedProcesses(List<Process> processes) {
        updateResourcesAfterAssignment(processes);
        assignProcessesToProcessors(processes);
    }

    private void updateResourcesAfterAssignment(List<Process> processes) {
        for (Process process : processes) {
            int[] requiredResources = process.getRemainingResources();
            process.assignResources(requiredResources);
            for (int i = 0; i < currentResources.length; i++) {
                currentResources[i] -= requiredResources[i];
            }
        }
    }

    private void assignProcessesToProcessors(List<Process> processes) {
        for (Process process : processes) {
            Processor selectedProcessor = selectProcessorWithMinimumWorkload();
            selectedProcessor.addProcessToReadyQueue(process);
            processWaitingList.remove(process);
        }
    }

    private Processor selectProcessorWithMinimumWorkload() {
        return processorList.stream()
                .min(Comparator.comparingInt(Processor::calculateTotalWorkload))
                .orElseThrow(IllegalStateException::new);
    }

    private void handleUnsolvableState() {
        setSolvable(false);
        System.out.println("ERROR: PROBLEM NOT SOLVABLE!! (Resource cannot be assigned forever)");
    }

    private void handleErrorState() {
        setSolvable(false);
        System.out.println("ERROR OCCURRED!!");
    }

    private void printCurrentState() {
        printHeader();
        printCurrentProcesses();
        printReadyQueueSizes();
        printFooter();
    }

    private void printHeader() {
        System.out.print(" P" + String.format("%03d", Constants.NUM_PROCESSORS) + "  T" + String.format("%03d", currentTime) + " : ");
    }

    private void checkAndAssignProcesses() {
        for (Processor processor : processorList) {
            processor.checkAndAssignProcess();
            processor.runProcess();
        }
    }

    private void printCurrentProcesses() {
        for (Processor processor : processorList) {
            if (processor.getCurrentProcess() == null) {
                System.out.print("-- ");
            } else {
                System.out.print(String.format("%2d", processor.getCurrentProcess().getId()) + " ");
            }
        }
        System.out.print("| ");
    }

    private void printReadyQueueSizes() {
        for (Processor processor : processorList) {
            if (processor.getReadyQueue().isEmpty()) {
                System.out.print("* ");
            } else {
                System.out.print(processor.getReadyQueue().size() + " ");
            }
        }
    }

    private void cleanupProcesses() {
        for (Processor processor : processorList) {
            int[] returnedResources = processor.cleanupProcess();
            if (returnedResources.length != 0) {
                for (int j = 0; j < currentResources.length; j++) {
                    currentResources[j] += returnedResources[j];
                }
            }
        }
    }

    private void printFooter() {
        String waitingProcessIds = processWaitingList.stream()
                .map(process -> String.valueOf(process.getId()))
                .collect(Collectors.joining(", "));

        int maxOutputLength = 20;
        if (waitingProcessIds.length() > maxOutputLength) {
            waitingProcessIds = waitingProcessIds.substring(0, maxOutputLength - 3) + "...";
        }

        System.out.print(String.format("   | %-20s", waitingProcessIds));
        System.out.println(" | " + Arrays.toString(currentResources));
    }

    private boolean checkSimulationCompletion(boolean isDispatchedTasksExists, boolean tasksRemaining, boolean isFinish) {
        return !isDispatchedTasksExists && !tasksRemaining && processWaitingList.isEmpty() && processorList.stream().allMatch(p -> p.getReadyQueue().isEmpty() && !isFinish);
    }
}
