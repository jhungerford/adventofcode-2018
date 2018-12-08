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
   * Parses the given list of step restrictions, returning the order that teh steps should be run in. If the
   * prerequisites for more than one step have been met, the step with the lowest alphabetical order goes first.
   *
   * @param lines Lines to parse - one restriction per line
   * @return Order steps should be performed in.
   */
  public static String order(ImmutableList<String> lines) {
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

    // Looking at which elements are in one map and not the other, stepToNextMap's uniques are the first steps
    PriorityQueue<Character> nextSteps = new PriorityQueue<>(Comparator.naturalOrder());
    nextSteps.addAll(Sets.difference(stepToNextMap.keySet(), stepToPreviousMap.keySet()));

    Set<Character> done = new HashSet<>();
    StringBuilder order = new StringBuilder();
    while (!nextSteps.isEmpty()) {
      Character stepName = nextSteps.remove();
      done.add(stepName);
      order.append(stepName);

      stepToNextMap.getOrDefault(stepName, ImmutableSet.of()).stream()
          .filter(nextStepName -> done.containsAll(stepToPreviousMap.get(nextStepName)))
          .forEach(nextSteps::add);
    }

    return order.toString();
  }

  public static void main(String[] args) throws Exception {
    File file = new File(Day7.class.getResource("/day7.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));

    // Part 1: what order should the steps be completed in?
    System.out.println("Part 1: " + order(lines));
  }
}
