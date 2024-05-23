package os.scheduling.service;

import java.util.List;

record SafetyCheckResult(boolean isSafe, List<Integer> safeSequence) {
}
