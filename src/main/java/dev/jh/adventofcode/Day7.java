package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day7 {

  /**
   * Maps containing restrictions on which steps must run before and after other steps.
   */
  private static class Steps {
    public final ImmutableMap<Character, Set<Character>> stepToNextMap;
    public final ImmutableMap<Character, Set<Character>> stepToPreviousMap;

    public Steps(
        ImmutableMap<Character, Set<Character>> stepToNextMap,
        ImmutableMap<Character, Set<Character>> stepToPreviousMap
    ) {
      this.stepToNextMap = stepToNextMap;
      this.stepToPreviousMap = stepToPreviousMap;
    }
  }

  /**
   * Step and the number of seconds it takes to run that step.
   */
  private static class StepSeconds {
    private final char step;
    private final int seconds;

    public StepSeconds(char step) {
      this.step = step;
      this.seconds = 60 + (step - 'A' + 1);
    }

    public StepSeconds(char step, int seconds) {
      this.step = step;
      this.seconds = seconds;
    }

    public StepSeconds subtract(int seconds) {
      return new StepSeconds(step, this.seconds - seconds);
    }
  }

  /**
   * Parses the given list of step restrictions.
   *
   * @param lines Lines to parse - one restriction per line.
   * @return Steps to complete the work described in the lines.
   */
  public static Steps parseLines(ImmutableList<String> lines) {
    Pattern pattern = Pattern.compile("Step ([A-Z]) must be finished before step ([A-Z]) can begin.");

    // Rip through the steps, building maps of step names -> next step names and step names -> previous step names
    Map<Character, Set<Character>> stepToNextMap = new HashMap<>();
    Map<Character, Set<Character>> stepToPreviousMap = new HashMap<>();

    for (String line : lines) {
      Matcher matcher = pattern.matcher(line);
      if (matcher.matches()) {
        char stepName = matcher.group(1).charAt(0);
        char nextStepName = matcher.group(2).charAt(0);

        stepToNextMap.computeIfAbsent(stepName, (c) -> new HashSet<>()).add(nextStepName);
        stepToPreviousMap.computeIfAbsent(nextStepName, (c) -> new HashSet<>()).add(stepName);
      }
    }

    return new Steps(
        ImmutableMap.copyOf(stepToNextMap),
        ImmutableMap.copyOf(stepToPreviousMap)
    );
  }

  /**
   * Returns the order that the steps should be run in. If the prerequisites for more than one step have been met,
   * the step with the lowest alphabetical order goes first.
   *
   * @param steps Step restrictions.
   * @return Order steps should be performed in.
   */
  public static String order(Steps steps) {
    // Looking at which elements are in one map and not the other, stepToNextMap's uniques are the first steps
    PriorityQueue<Character> nextSteps = new PriorityQueue<>(Comparator.naturalOrder());
    nextSteps.addAll(Sets.difference(steps.stepToNextMap.keySet(), steps.stepToPreviousMap.keySet()));

    Set<Character> done = new HashSet<>();
    StringBuilder order = new StringBuilder();
    while (!nextSteps.isEmpty()) {
      Character stepName = nextSteps.remove();
      done.add(stepName);
      order.append(stepName);

      steps.stepToNextMap.getOrDefault(stepName, ImmutableSet.of()).stream()
          .filter(nextStepName -> done.containsAll(steps.stepToPreviousMap.get(nextStepName)))
          .forEach(nextSteps::add);
    }

    return order.toString();
  }

  /**
   * Returns the amount of time that it takes to complete all of the steps with the given number of workers.
   * Each step takes 60 seconds plus an amount corresponding to the letter - A=1, B=2, etc.  If the prerequisites
   * for more than one step have been completed, workers grab steps in alphabetical order.
   *
   * @param steps Step restrictions
   * @param numWorkers Number of workers that can perform steps concurrently
   * @return Number of seconds it takes the workers to complete all of the steps
   */
  public static int time(Steps steps, int numWorkers) {
    PriorityQueue<Character> nextSteps = new PriorityQueue<>(Comparator.naturalOrder());
    nextSteps.addAll(Sets.difference(steps.stepToNextMap.keySet(), steps.stepToPreviousMap.keySet()));

    int totalSeconds = 0;

    StepSeconds[] workers = new StepSeconds[numWorkers];
    Set<Character> done = new HashSet<>();

    while (! nextSteps.isEmpty() || Arrays.stream(workers).anyMatch(Objects::nonNull)) {
      // Idle workers pick up work
      for (int worker = 0; worker < numWorkers && !nextSteps.isEmpty(); worker++) {
        if (workers[worker] == null) {
          workers[worker] = new StepSeconds(nextSteps.remove());
        }
      }

      // Determine which step will be completed next.
      StepSeconds completed = Arrays.stream(workers)
          .filter(Objects::nonNull)
          .min(Comparator.comparing(stepSeconds -> stepSeconds.seconds))
          .orElseThrow(() -> new IllegalStateException("No active workers with work remaining."));

      totalSeconds += completed.seconds;

      // Tick down all of the ongoing work
      for (int worker = 0; worker < numWorkers; worker ++) {
        StepSeconds work = workers[worker];
        if (work == null) {
          // Nothing to do - idle worker.

        } else if (work.seconds == completed.seconds) {
          workers[worker] = null;
          done.add(work.step);
          steps.stepToNextMap.getOrDefault(work.step, ImmutableSet.of()).stream()
              .filter(nextStepName -> done.containsAll(steps.stepToPreviousMap.get(nextStepName)))
              .forEach(nextSteps::add);

        } else {
          workers[worker] = work.subtract(completed.seconds);
        }
      }
    }

    return totalSeconds;
  }

  public static void main(String[] args) throws Exception {
    File file = new File(Day7.class.getResource("/day7.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));
    Steps steps = parseLines(lines);

    // Part 1: what order should the steps be completed in?
    System.out.println("Part 1: " + order(steps));
    System.out.println("Part 2: " + time(steps, 5));
  }
}
