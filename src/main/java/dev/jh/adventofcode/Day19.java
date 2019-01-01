package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.*;
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

    public final String name;
    public final BiFunction<Instruction, State, State> instruction;

    Opcode(BiFunction<Instruction, State, State> instruction) {
      this.name = name().toLowerCase();
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

  public interface State {
    State set(int register, int value);
    int get(int register);
  }

  public static class ImmutableState implements State {
    private static final ImmutableState INITIAL = new ImmutableState(new int[]{0, 0, 0, 0, 0, 0});

    private final int[] registers;

    public ImmutableState(int[] registers) {
      if (registers.length != 6) {
        throw new IllegalArgumentException("State must have 6 values.");
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
    @Override
    public State set(int register, int value) {
      if (register < 0 || register > 5) {
        throw new IllegalArgumentException("Register must be between 0 and 5, inclusive.");
      }

      int[] newState = Arrays.copyOf(registers, 6);
      newState[register] = value;
      return new ImmutableState(newState);
    }

    /**
     * Returns the value of the given register.
     *
     * @param register Register to look up.
     * @return Value of the given register.
     */
    @Override
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
      ImmutableState state = (ImmutableState) o;
      return Arrays.equals(registers, state.registers);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(registers);
    }

    public static ImmutableState initial() {
      return INITIAL;
    }
  }

  public static class MutableState implements State {
    private final int[] registers;

    public MutableState(int[] registers) {
      if (registers.length != 6) {
        throw new IllegalArgumentException("State must have 6 values.");
      }

      this.registers = registers;
    }

    @Override
    public State set(int register, int value) {
      registers[register] = value;
      return this;
    }

    @Override
    public int get(int register) {
      return registers[register];
    }

    public static MutableState initial() {
      return new MutableState(new int[]{0, 0, 0, 0, 0, 0});
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
    public final PrintStream debugOut;

    private Program(ImmutableList<Instruction> instructions, int instructionRegister, PrintStream debugOut) {
      this.instructionRegister = instructionRegister;
      this.instructions = instructions;
      this.debugOut = debugOut;
    }

    public Program withDebug(PrintStream debugOut) {
      return new Program(instructions, instructionRegister, debugOut);
    }

    public State run(State state) {
      boolean running = true;
      while (running && inBounds(state.get(instructionRegister))) {
        State beforeState = state;
        int beforeInstructionPointer = state.get(instructionRegister);
        Instruction instruction = instructions.get(state.get(instructionRegister));

        state = instruction.apply(state);

        if (debugOut != null) {
          debugOut.printf("ip=%d [%d, %d, %d, %d, %d, %d] %s %d %d %d [%d, %d, %d, %d, %d, %d]\n",
              beforeInstructionPointer,
              beforeState.get(0),
              beforeState.get(1),
              beforeState.get(2),
              beforeState.get(3),
              beforeState.get(4),
              beforeState.get(5),
              instruction.opcode.name,
              instruction.a,
              instruction.b,
              instruction.c,
              state.get(0),
              state.get(1),
              state.get(2),
              state.get(3),
              state.get(4),
              state.get(5)
          );
        }

        int newInstructionPointer = state.get(instructionRegister) + 1;
        if (inBounds(newInstructionPointer)) {
          state = state.set(instructionRegister, newInstructionPointer);
        } else {
          running = false;
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

      return new Program(instructions.build(), instructionRegister, null);
    }
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day19.class.getResource("/day19.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));
    Program program = Program.parse(lines);

    // Part 1: what value is left in register 0 when the background process halts?
    try (PrintStream out = new PrintStream(new FileOutputStream("day19_part1.log"))) {
      long start = System.currentTimeMillis();
      System.out.println("Part 1: " + program.withDebug(out).run(ImmutableState.initial()).get(0));
      System.out.println("Took " + (System.currentTimeMillis() - start) + " ms");
    }

    // Part 2: same question, but register 0 starts with value 1.
    try (PrintStream out = new PrintStream(new FileOutputStream("day19_part2.log"))) {
      long start = System.currentTimeMillis();
      System.out.println("Part 2: " + program.withDebug(out).run(MutableState.initial().set(0, 1)).get(0));
      System.out.println("Took " + (System.currentTimeMillis() - start) + " ms");
    }

    // Part 1 takes 10 minutes to run, part 2 doesn't complete.  I did this one by hand by analyzing what the program
    // is doing - here are my notes:
    /*
    Annotated Program:
 0: addi 5 16 5     IR is 5 and starts at 0, so jump to instruction 17

 1: seti 1 9 1      reg(1) = 1

 # Outer loop (from 15 - reg(1) <= reg(2))
 2: seti 1 5 4      reg(4) = 1

 # Inner loop (from 11 - reg(4) <= reg(2))
 3: mulr 1 4 3      reg(3) = reg(1) * reg(4)
 4: eqrr 3 2 3      reg(3) = 1 if reg(3) == reg(2), 0 otherwise - reg(3) is 0 the first time through
 5: addr 3 5 5      reg(5) += result of ^^^, so goto 7 when reg(3) == reg(2) and goto 6 when !=

 6: addi 5 1 5      goto 8 (skip 7) when reg(3) != reg(2)

 7: addr 1 0 0      reg(0) += reg(1) - run when reg(3) == reg(2)

 8: addi 4 1 4      reg(4) ++
 9: gtrr 4 2 3      reg(3) = 1 if reg(4) > reg(2), 0 otherwise
10: addr 5 3 5      reg(5) += reg(3) - goto 12 if reg(4) > reg(2), goto 11 otherwise

11: seti 2 4 5      goto 3

12: addi 1 1 1      reg(1) ++
13: gtrr 1 2 3      reg(3) = 1 if reg(1) > reg(2), 0 otherwise
14: addr 3 5 5      reg(5) += reg(3) - goto 16 (end) if reg(1) > reg(2), goto 15 otherwise
15: seti 1 9 5      goto 2

16: mulr 5 5 5     Square the instruction pointer - definitely going to be out of bounds

# reg(2) = 1028 (2^2 * 257), reg(3) = 192 (2^6 * 3)
17: addi 2 2 2     reg(2) += 2, instruction 18 next
18: mulr 2 2 2     reg(2) = reg(2)^2, instruction 19 next
19: mulr 5 2 2     reg(2) = 19 * reg(2), instruction 20 next
20: muli 2 11 2    reg(2) = reg(2) * 11, instruction 21 next
21: addi 3 8 3     reg(3) += 8, instruction 22 next
22: mulr 3 5 3     reg(3) *= 22, instruction 23 next
23: addi 3 16 3    reg(3) += 16, instruction 24 next
24: addr 2 3 2     reg(2) = reg(2) + reg(3), instruction 25 next

# Execute 26 for part 1, 27 for part 2
25: addr 5 0 5     reg(5) = reg(5) + reg(0) - this is where part 1 and part 2 differ.  Part 1 will go to 26, part 2 will go to 27

# Jump to 1 for part 1, reg(3) = 27 and go to 28 for part 2.
26: seti 0 7 5     reg(5) = 0 - instruction 1 will be next
27: setr 5 3 3     reg(3) = 27, instruction 28 will be next

28: mulr 3 5 3     reg(3) = 27 * 28
29: addr 5 3 3     reg(3) = (27*28) + 29
30: mulr 5 3 3     reg(3) = ((28*28)+29) * 30
31: muli 3 14 3    reg(3) *= 14
32: mulr 3 5 3     reg(3) *= 32
33: addr 2 3 2     reg(2) = 1028 + reg(3), which is 10550400, so reg(2) = 10551428
34: seti 0 1 0     reg(0) = 0
35: seti 0 6 5     goto 1

     Higher-level code:
# reg(2) = 1028 (2^2 * 257), reg(3) = 192 (2^6 * 3)
# Part 1: 1806 = sum of all combinations of divisors of reg(2) (1 + 2 + 4 + 257 + 514 + 1028)

reg(1) = 1
do {
    reg(4) = 1
    do {
        reg(3) = reg(1) * reg(4)
        if (reg(3) == reg(2)) {
            reg(0) += reg(1) # Part of our answer
        }

        reg(4) ++
    } while (reg(2) > reg(4))

    reg(1) ++
} while (reg(1) <= reg(2))

# Part 2:
# Lines 28-31 set reg(2) = 10551428 (2^2 * 67 * 39371) - the O(n^2) approach is going to take a while.

Answer to part 2:
1 + 2 + 4 + 67 + 67*2 + 67*2*2 + 39371 + 39371*2 + 39371*2*2 + 39371*67 + 39371*67*2 + 39371*67*2*2 = 18741072
     */
  }
}
