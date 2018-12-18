package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day16 {

  public enum Opcode {
    /** ADDR stores into register C the result of adding register A and register B */
    ADDR((i, r) -> r.set(i.c, r.get(i.a) + r.get(i.b))),
    /** ADDI stores into register C the result of adding register A and value B */
    ADDI((i, r) -> r.set(i.c, r.get(i.a) + i.b)),
    /** MULR stores into register C the result of multiplying register A and register B */
    MULR((i, r) -> r.set(i.c, r.get(i.a) * r.get(i.b))),
    /** MULI stores into register C the result of multiplying register A and value B */
    MULI((i, r) -> r.set(i.c, r.get(i.a) * i.b)),
    /** BANR stores into register C the result of the bitwise AND of register A and register B */
    BANR((i, r) -> r.set(i.c, r.get(i.a) & r.get(i.b))),
    /** BANI stores into register C the result of the bitwise AND of register A and value B */
    BANI((i, r) -> r.set(i.c, r.get(i.a) & i.b)),
    /** BORR stores into register C the result of the bitwise OR of register A and register B */
    BORR((i, r) -> r.set(i.c, r.get(i.a) | r.get(i.b))),
    /** BORI stores into register C the result of the bitwise OR of register A and value B */
    BORI((i, r) -> r.set(i.c, r.get(i.a) | i.b)),
    /** SETR stores the contents of register A into register C */
    SETR((i, r) -> r.set(i.c, r.get(i.a))),
    /** SETI stores value A into register C */
    SETI((i, r) -> r.set(i.c, i.a)),
    /** GTIR sets register C to 1 if value A is greater than register B.  Otherwise sets register C to 0 */
    GTIR((i, r) -> r.set(i.c, i.a > r.get(i.b) ? 1 : 0)),
    /** GTRI sets register C to 1 if register A is greater than value B.  Otherwise sets register C to 0 */
    GTRI((i, r) -> r.set(i.c, r.get(i.a) > i.b ? 1 : 0)),
    /** GTRR sets register C to 1 if register A is greater than register B.  Otherwise sets register C to 0 */
    GTRR((i, r) -> r.set(i.c, r.get(i.a) > r.get(i.b) ? 1 : 0)),
    /** EQIR sets register C to 1 if value A is equal to register B.  Otherwise sets register C to 0 */
    EQIR((i, r) -> r.set(i.c, i.a == r.get(i.b) ? 1 : 0)),
    /** EQRI sets register C to 1 if register A is equal to value B.  Otherwise sets register C to 0 */
    EQRI((i, r) -> r.set(i.c, r.get(i.a) == i.b ? 1 : 0)),
    /** EQRR sets register C to 1 if register A is equal to register B.  Otherwise sets register C to 0 */
    EQRR((i, r) -> r.set(i.c, r.get(i.a) == r.get(i.b) ? 1 : 0));

    public final BiFunction<NumericInstruction, Registers, Registers> instruction;

    Opcode(BiFunction<NumericInstruction, Registers, Registers> instruction) {
      this.instruction = instruction;
    }
  }

  public static class Registers {
    private final int[] registers;

    public Registers(int[] registers) {
      if (registers.length != 4) {
        throw new IllegalArgumentException("Registers must have 4 values.");
      }

      this.registers = registers;
    }

    /**
     * Returns a new registers with the given register set to the given value.
     *
     * @param register Number of the register to set (0-3).
     * @param value Value to set in the register.
     * @return New Registers with the value set.
     */
    public Registers set(int register, int value) {
      if (register < 0 || register > 3) {
        throw new IllegalArgumentException("Register must be between 0 and 3, inclusive.");
      }

      int[] newRegisters = Arrays.copyOf(registers, 4);
      newRegisters[register] = value;
      return new Registers(newRegisters);
    }

    /**
     * Returns the value of the given register.
     *
     * @param register Register to look up.
     * @return Value of the given register.
     */
    public int get(int register) {
      if (register < 0 || register > 3) {
        throw new IllegalArgumentException("Register must be between 0 and 3, inclusive.");
      }

      return registers[register];
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("registers", registers)
          .toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Registers other = (Registers) o;
      return Arrays.equals(registers, other.registers);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(registers);
    }
  }

  public static class NumericInstruction {
    public final int opcode;
    public final int a;
    public final int b;
    public final int c;

    public NumericInstruction(int opcode, int a, int b, int c) {
      this.opcode = opcode;
      this.a = a;
      this.b = b;
      this.c = c;
    }

    public NumericInstruction(int[] values) {
      if (values.length != 4) {
        throw new IllegalArgumentException("Numeric instruction must have 4 values.");
      }

      this.opcode = values[0];
      this.a = values[1];
      this.b = values[2];
      this.c = values[3];
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("opcode", opcode)
          .add("a", a)
          .add("b", b)
          .add("c", c)
          .toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      NumericInstruction that = (NumericInstruction) o;
      return opcode == that.opcode &&
          a == that.a &&
          b == that.b &&
          c == that.c;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(opcode, a, b, c);
    }
  }

  public static class Sample {
    public final Registers before;
    public final NumericInstruction instruction;
    public final Registers after;

    public Sample(Registers before, NumericInstruction instruction, Registers after) {
      this.before = before;
      this.instruction = instruction;
      this.after = after;
    }

    public ImmutableSet<Opcode> matchingOpcodes() {
      return Arrays.stream(Opcode.values())
          .filter(opcode -> opcode.instruction.apply(instruction, before).equals(after))
          .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("before", before)
          .add("instruction", instruction)
          .add("after", after)
          .toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Sample sample = (Sample) o;
      return Objects.equal(before, sample.before) &&
          Objects.equal(instruction, sample.instruction) &&
          Objects.equal(after, sample.after);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(before, instruction, after);
    }

    /**
     * Parses a before, instruction, and after line in the given lines and returns a sample.
     *
     * @param lines Lines to parse
     * @return Parsed sample
     */
    public static Sample parse(ImmutableList<String> lines) {
      if (lines.size() < 3) {
        throw new IllegalArgumentException("Cannot parse a sample with less than three lines.");
      }

      Registers before = new Registers(readLine(lines.get(0)));
      NumericInstruction instruction = new NumericInstruction(readLine(lines.get(1)));
      Registers after = new Registers(readLine(lines.get(2)));

      return new Sample(before, instruction, after);
    }
  }

  private static final Pattern LINE_PATTERN = Pattern.compile("^[a-zA-Z: \\[]*(\\d+),? (\\d+),? (\\d+),? (\\d+)]?");
  private static int[] readLine(String line) {
    Matcher matcher = LINE_PATTERN.matcher(line);
    if (! matcher.matches()) {
      throw new IllegalArgumentException("'" + line + "' is not a valid sample line");
    }

    return new int[]{
        Integer.parseInt(matcher.group(1)),
        Integer.parseInt(matcher.group(2)),
        Integer.parseInt(matcher.group(3)),
        Integer.parseInt(matcher.group(4))
    };
  }

  private static ImmutableList<Sample> parseSamples(ImmutableList<String> lines) {
    ImmutableList.Builder<Sample> samples = ImmutableList.builder();
    for (int i = 0; i < lines.size() && lines.get(i).startsWith("Before"); i += 4) {
      samples.add(Sample.parse(lines.subList(i, i + 4)));
    }

    return samples.build();
  }

  private static ImmutableList<NumericInstruction> parseInstructions(ImmutableList<String> lines) {
    int index = 0;
    while (lines.get(index).startsWith("Before")) {
      index += 4;
    }

    while (lines.get(index).isEmpty()) {
      index ++;
    }

    ImmutableList.Builder<NumericInstruction> instructions = ImmutableList.builder();
    while (index < lines.size()) {
      instructions.add(new NumericInstruction(readLine(lines.get(index))));
      index ++;
    }

    return instructions.build();
  }

  public static ImmutableMap<Integer, Opcode> mapOpcodes(ImmutableList<Sample> samples) {
    Map<Integer, Set<Opcode>> possibleOpcodes = new HashMap<>();
    ImmutableSet<Opcode> allOpcodes = ImmutableSet.copyOf(Opcode.values());

    // Rip through the samples, narrowing down the possible opcode mappings.
    for (Sample sample : samples) {
      ImmutableSet<Opcode> sampleOpcodes = sample.matchingOpcodes();
      int opcodeNum = sample.instruction.opcode;

      Set<Opcode> narrowedOpcodes = Sets.intersection(sampleOpcodes, possibleOpcodes.getOrDefault(opcodeNum, allOpcodes));
      possibleOpcodes.put(opcodeNum, narrowedOpcodes);
    }

    // Resolve the possibilities.  Numbers can still be mapped to multiple opcodes, so use the exact restrictions.
    ImmutableMap.Builder<Integer, Opcode> opcodeMap = ImmutableMap.builder();

    while(!possibleOpcodes.isEmpty()) {
      Map<Integer, Set<Opcode>> newPossibleOpcodes = new HashMap<>();
      newPossibleOpcodes.putAll(possibleOpcodes);

      for (Map.Entry<Integer, Set<Opcode>> entry : possibleOpcodes.entrySet()) {
        if (entry.getValue().size() == 1) {
          opcodeMap.put(entry.getKey(), entry.getValue().iterator().next());

          for (Integer newNum : possibleOpcodes.keySet()) {
            Set<Opcode> newOpcodes = Sets.difference(newPossibleOpcodes.getOrDefault(newNum, ImmutableSet.of()), entry.getValue());
            if (newOpcodes.isEmpty()) {
              newPossibleOpcodes.remove(newNum);
            } else {
              newPossibleOpcodes.put(newNum, newOpcodes);
            }
          }
        }
      }

      // Map of opcode -> numbers that can map to that opcode.
      Map<Opcode, Set<Integer>> opcodeToNumbers = new HashMap<>();
      possibleOpcodes.values().stream()
          .flatMap(Collection::stream)
          .forEach(opcode -> opcodeToNumbers.put(opcode, new HashSet<>()));

      for (Map.Entry<Integer, Set<Opcode>> entry : newPossibleOpcodes.entrySet()) {
        for (Opcode opcode : entry.getValue()) {
          opcodeToNumbers.get(opcode).add(entry.getKey());
        }
      }

      for (Map.Entry<Opcode, Set<Integer>> entry : opcodeToNumbers.entrySet()) {
        if (entry.getValue().size() == 1) {
          Integer num = entry.getValue().iterator().next();
          opcodeMap.put(num, entry.getKey());
          newPossibleOpcodes.remove(num);
        }
      }

      possibleOpcodes = newPossibleOpcodes;
    }

    return opcodeMap.build();
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day16.class.getResource("/day16.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));
    ImmutableList<Sample> samples = parseSamples(lines);

    // Part 1: Ignoring the opcode numbers, how many samples in your puzzle input behave like 3 or more opcodes?
    long part1 = samples.stream()
        .filter(sample -> sample.matchingOpcodes().size() >= 3)
        .count();

    System.out.println("Part 1: " + part1);

    // Part 2: Work out the opcode numbers.  What is the value of register 0 after executing the test program?
    ImmutableList<NumericInstruction> instructions = parseInstructions(lines);
    ImmutableMap<Integer, Opcode> opcodeMap = mapOpcodes(samples);

    Registers registers = new Registers(new int[]{0, 0, 0, 0});
    for (NumericInstruction instruction : instructions) {
      registers = opcodeMap.get(instruction.opcode).instruction.apply(instruction, registers);
    }

    System.out.println("Part 2: " + registers.get(0));
  }
}
