package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day7 {

  public static class Step {
    public final char name;
    public final ImmutableSet<Character> prerequisites;
    public final ImmutableSet<Step> nextSteps;

    public Step(char name, ImmutableSet<Character> prerequisites, ImmutableSet<Step> nextSteps) {
      this.name = name;
      this.prerequisites = prerequisites;
      this.nextSteps = nextSteps;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Step step = (Step) o;
      return name == step.name &&
          Objects.equal(nextSteps, step.nextSteps);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name, nextSteps);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("name", name)
          .add("nextSteps", nextSteps)
          .toString();
    }
  }

  /**
   * Parses the given list of step restrictions, returning the root of the tree of steps that should be run.
   *
   * @param lines Lines to parse - one restriction per line
   * @return Root of the step tree
   */
  public static Step parseLines(ImmutableList<String> lines) {
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

    // Looking at which elements are in one map and not the other, stepToNextMap's unique is the first step name
    // and stepToPreviousMap's unique is the last step name
    final char firstStepName = Sets.difference(stepToNextMap.keySet(), stepToPreviousMap.keySet()).iterator().next();
    final char lastStepName = Sets.difference(stepToPreviousMap.keySet(), stepToNextMap.keySet()).iterator().next();

    // Working backwards from the last step (which has no next steps), build up a map of steps.
    Queue<Character> previousSteps = new ArrayDeque<>();
    previousSteps.add(lastStepName);

    Map<Character, Step> stepMap = new HashMap<>();
    while (! previousSteps.isEmpty()) {
      char name = previousSteps.remove();

      ImmutableSet<Step> nextSteps = stepToNextMap.getOrDefault(name, ImmutableSet.of()).stream()
          .map(stepMap::get)
          .collect(ImmutableSet.toImmutableSet());

      ImmutableSet<Character> prerequisites = ImmutableSet.copyOf(stepToPreviousMap.getOrDefault(name, ImmutableSet.of()));

      stepMap.put(name, new Step(name, prerequisites, nextSteps));

      prerequisites.stream()
          .filter(prereq -> stepToNextMap.get(prereq).stream().allMatch(stepMap::containsKey))
          .forEach(previousSteps::add);
    }

    return stepMap.get(firstStepName);
  }

  /**
   * Returns the order that the steps under the given root should be performed in.  If the prerequisites
   * for more than one step have been met, the step with the lowest alphabetical order will go first.
   *
   * @param root First step
   * @return Order the steps should be performed in.
   */
  public static String order(Step root) {
    StringBuilder order = new StringBuilder();

    Set<Character> completedSteps = new HashSet<>();
    Map<Character, Step> nextSteps = new HashMap<>();
    nextSteps.put(root.name, root);

    while (!nextSteps.isEmpty()) {
      char nextStepName = nextSteps.keySet().stream()
          .min(Comparator.naturalOrder())
          .orElseThrow(() -> new IllegalStateException("Out of steps"));

      order.append(nextStepName);
      completedSteps.add(nextStepName);

      Step step = nextSteps.remove(nextStepName);
      for (Step next : step.nextSteps) {
        if (completedSteps.containsAll(next.prerequisites)) {
          nextSteps.put(next.name, next);
        }
      }
    }

    return order.toString();
  }

  public static void main(String[] args) throws Exception {
    File file = new File(Day7.class.getResource("/day7.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));
    Step root = parseLines(lines);

    // Part 1: what order should the steps be completed in?
    System.out.println("Part 1: " + order(root));
  }
}
