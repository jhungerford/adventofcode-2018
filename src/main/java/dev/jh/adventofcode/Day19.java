package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day19 {
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

    public final BiFunction<Instruction, State, State> instruction;

    Opcode(BiFunction<Instruction, State, State> instruction) {
      this.instruction = instruction;
    }

    public static Opcode fromString(String str) {
      for (Opcode opcode : values()) {
        if (opcode.name().equalsIgnoreCase(str)) {
          return opcode;
        }
      }

      throw new IllegalArgumentException(str + " is not a valid opcode");
    }
  }

  public static class State {
    public static final State INITIAL = new State(new int[]{0, 0, 0, 0, 0, 0});

    private final int[] registers;

    public State(int[] registers) {
      if (registers.length != 6) {
        throw new IllegalArgumentException("State must have 4 values.");
      }

      this.registers = registers;
    }

    /**
     * Returns a new registers with the given register set to the given value.
     *
     * @param register Number of the register to set (0-5).
     * @param value Value to set in the register.
     * @return New State with the value set.
     */
    public State set(int register, int value) {
      if (register < 0 || register > 5) {
        throw new IllegalArgumentException("Register must be between 0 and 5, inclusive.");
      }

      int[] newState = Arrays.copyOf(registers, 6);
      newState[register] = value;
      return new State(newState);
    }

    /**
     * Returns the value of the given register.
     *
     * @param register Register to look up.
     * @return Value of the given register.
     */
    public int get(int register) {
      if (register < 0 || register > 5) {
        throw new IllegalArgumentException("Register must be between 0 and 5, inclusive.");
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
      State state = (State) o;
      return Arrays.equals(registers, state.registers);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(registers);
    }
  }

  public static class Instruction {
    public final Opcode opcode;
    public final int a;
    public final int b;
    public final int c;

    public Instruction(Opcode opcode, int a, int b, int c) {
      this.opcode = opcode;
      this.a = a;
      this.b = b;
      this.c = c;
    }

    public State apply(State previous) {
      return opcode.instruction.apply(this, previous);
    }
  }

  public static class Program {
    public final ImmutableList<Instruction> instructions;
    public final int instructionRegister;

    public Program(int instructionRegister, ImmutableList<Instruction> instructions) {
      this.instructionRegister = instructionRegister;
      this.instructions = instructions;
    }

    public State run() {
      State state = State.INITIAL;

      while (inBounds(state.get(instructionRegister))) {
        state = instructions.get(state.get(instructionRegister)).apply(state);
        int newInstructionPointer = state.get(instructionRegister) + 1;
        if (inBounds(newInstructionPointer)) {
          state = state.set(instructionRegister, newInstructionPointer);
        } else {
          break;
        }
      }

      return state;
    }

    private boolean inBounds(int instructionPointer) {
      return instructionPointer >= 0 && instructionPointer < instructions.size();
    }

    private static final Pattern INSTRUCTION_PATTERN = Pattern.compile("^([a-z]{4}) (\\d+) (\\d+) (\\d+)");
    public static Program parse(ImmutableList<String> lines) {
      // First line indicates the instruction register.  '#ip 0'
      if (! lines.get(0).startsWith("#ip")) {
        throw new IllegalArgumentException("First instruction must be the instruction register.");
      }

      int instructionRegister = lines.get(0).charAt(4) - '0';

      // Remaining lines are instructions.  First line doesn't match - read it again to make iteration easy.
      ImmutableList.Builder<Instruction> instructions = ImmutableList.builder();
      for (String line : lines) {
        Matcher matcher = INSTRUCTION_PATTERN.matcher(line);

        if (matcher.matches()) {
          instructions.add(new Instruction(
              Opcode.fromString(matcher.group(1)),
              Integer.parseInt(matcher.group(2)),
              Integer.parseInt(matcher.group(3)),
              Integer.parseInt(matcher.group(4))
          ));
        }
      }

      return new Program(instructionRegister, instructions.build());
    }
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day19.class.getResource("/day19.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));

    // Part 1: what value is left in register 0 when the background process halts?
    Program program = Program.parse(lines);
    System.out.println("Part 1: " + program.run().get(0));
  }
}
