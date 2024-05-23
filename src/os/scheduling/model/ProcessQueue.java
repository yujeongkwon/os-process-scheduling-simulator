package os.scheduling.model;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class ProcessQueue {
    private final LinkedList<Process> queue = new LinkedList<>();

    LinkedList<Process> getQueue() {
        return new LinkedList<>(queue); // 방어적 복사
    }

    @Override
    public String toString() {
        return queue.stream()
                .map(process -> Integer.toString(process.getId()))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    void enqueue(Process process) {
        queue.addLast(process);
    }

    Process dequeue() {
        return queue.pollFirst();
    }

    int getTotalRemainingTime() {
        return queue.stream().mapToInt(Process::getRemainingTime).sum();
    }

    boolean isEmpty() {
        return queue.isEmpty();
    }

    Process remove(int selectedIdx) {
        return queue.remove(selectedIdx);
    }


}
