package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Day21 {

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

  public static class State {
    private final long[] registers;

    public State(long[] registers) {
      this.registers = registers;
    }

    public State set(int register, long value) {
      long[] newState = Arrays.copyOf(registers, 6);
      newState[register] = value;
      return new State(newState);
    }

    public long get(int register) {
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

    public static State initial() {
      return new State(new long[]{0, 0, 0, 0, 0, 0});
    }
  }

  public static class Instruction {
    private static final Pattern INSTRUCTION_PATTERN = Pattern.compile("^([a-z]{4}) (\\d+) (\\d+) (\\d+)");

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

    public static Instruction parse(String line) {
      Matcher matcher = INSTRUCTION_PATTERN.matcher(line);

      if (!matcher.matches()) {
        throw new IllegalArgumentException(line + " is not a valid instruction");
      }

      return new Instruction(
          Opcode.fromString(matcher.group(1)),
          Integer.parseInt(matcher.group(2)),
          Integer.parseInt(matcher.group(3)),
          Integer.parseInt(matcher.group(4))
      );
    }
  }

  public static class Program {
    public final ImmutableList<Instruction> instructions;
    public final int instructionRegister;

    private Program(ImmutableList<Instruction> instructions, int instructionRegister) {
      this.instructionRegister = instructionRegister;
      this.instructions = instructions;
    }

    public State run(State state) {
      boolean running = true;
      while (running && inBounds(state.get(instructionRegister))) {
        state = instructions.get((int) state.get(instructionRegister)).apply(state);

        long newInstructionPointer = state.get(instructionRegister) + 1;
        if (inBounds(newInstructionPointer)) {
          state = state.set(instructionRegister, newInstructionPointer);
        } else {
          running = false;
        }
      }

      return state;
    }

    private boolean inBounds(long instructionPointer) {
      return instructionPointer >= 0 && instructionPointer < instructions.size();
    }

    public Program replaceInstruction(int num, Instruction instruction) {
      List<Instruction> mutableInstructions = new ArrayList<>(this.instructions);
      mutableInstructions.set(num, instruction);

      return new Program(ImmutableList.copyOf(mutableInstructions), instructionRegister);
    }

    public static Program parse(ImmutableList<String> lines) {
      // First line indicates the instruction register.  '#ip 0'
      if (! lines.get(0).startsWith("#ip")) {
        throw new IllegalArgumentException("First instruction must be the instruction register.");
      }

      int instructionRegister = lines.get(0).charAt(4) - '0';

      ImmutableList<Instruction> instructions = IntStream.range(1, lines.size())
          .mapToObj(i -> Instruction.parse(lines.get(i)))
          .collect(ImmutableList.toImmutableList());

      return new Program(instructions, instructionRegister);
    }
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day21.class.getResource("/day21.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));
    Program program = Program.parse(lines);

    // Part 1: what is the lowest non-negative integer value for register 0 that causes the program to halt
    // after executing the fewest instructions?
    State part1Result = program
        .replaceInstruction(28, Instruction.parse("eqrr 2 2 4"))
        .run(State.initial());

    System.out.println("Part 1: " + part1Result.get(2));

    // Most of the work for this problem was analyzing what the program does.  Analysis:
    /*
#ip 5
 0: seti 123 0 2          reg(2) = 123 (1111011)

 1: bani 2 456 2          reg(2) &= 456 (111001000) - result is (1001000)
 2: eqri 2 72 2           reg(2) = 1 if reg(2) == 72 (1001000), 0 otherwise - initially true
 3: addr 2 5 5            goto 5 if 2 is true, 4 otherwise

 4: seti 0 0 5            goto 1 - infinite loop

 5: seti 0 9 2            reg(2) = 0
 6: bori 2 65536 1        reg(1) = 0 | 65536, so 65536 (10000000000000000)
 7: seti 1250634 6 2      reg(2) = 1250634 (100110001010101001010)

 8: bani 1 255 4          reg(4) = reg(1) & 255 (11111111), so 0      - Loop from 27 and 30
 9: addr 2 4 2            reg(2) += reg(4), so 1250634 (100110001010101001010)
10: bani 2 16777215 2     reg(2) &= 16777215 (111111111111111111111111) , so 1250634 (100110001010101001010)
11: muli 2 65899 2        reg(2) *= 65899, so reg(2) = 1100110111100111111111110011001000011
12: bani 2 16777215 2     reg(2) &= 16777215, so reg(2) = (111111111110011001000011)

13: gtir 256 1 4          reg(4) = 1 if 256 > reg(1), 0 otherwise
14: addr 4 5 5            goto 16 if 256 > reg(1), 15 otherwise

15: addi 5 1 5            goto 17

16: seti 27 2 5           goto 28

17: seti 0 5 4            reg(4) = 0

18: addi 4 1 3            reg(3) = reg(4) + 1    - Loop from 25
19: muli 3 256 3          reg(3) *= 256

20: gtrr 3 1 3            reg(3) = 1 if reg(3) > reg(1), 0 otherwise
21: addr 3 5 5            goto 23 if reg(3) > reg(1), 22 otherwise

22: addi 5 1 5            goto 24

23: seti 25 5 5           goto 26

24: addi 4 1 4            reg(4) ++
25: seti 17 2 5           goto 18

26: setr 4 8 1            reg(1) = reg(4)
27: seti 7 6 5            goto 8

28: eqrr 2 0 4            reg(4) = 1 if reg(2) == reg(0), 0 otherwise
29: addr 4 5 5            break if reg(2) == reg(0), goto 30 otherwise
30: seti 5 7 5            goto 8

Higher-level code:
do {
    // Infinite loop if these instructions / immediate numbers change behavior.  reg(2) = 72 otherwise.
    reg(2) = 123 (0b1111011)
    reg(2) &= 456 (0b111001000), so (0b1001000)
while (reg(2) != 72 (0b1001000))

reg(2) = 0 // Block above is a NOOP.
reg(1) = reg(2) | 65536, so 65536 (0b10000000000000000)
reg(2) = 1250634 (0b100110001010101001010)

# reg(1) = 65536, reg(2) = 1250634
label 8: { // outer loop
    reg(4) = reg(2) & 255 (0b11111111)
    reg(2) += reg(4)
    reg(2) &= 16777215 (0b111111111111111111111111)
    reg(2) *= 65899 (0b10000000101101011)
    reg(2) &= 16777215

    if (reg(1) < 256 && reg(2) == reg(0)) {
        break
    }

    reg(4) = 0

    // Still working this code out - instruction 18
    label 18: { // Inner loop
        reg(3) = reg(4) + 1
        reg(3) *= 256

        if (reg(3) > reg(1)) {
            reg(1) = reg(4)
            goto 8 - outer loop
        } else {
            reg(4) ++
            goto 18 - inner loop
        }
    }
}

Registers:
0 - user-controlled register.  Exit when reg(2) == reg(0) (and reg(1) < 256)
1 - break condition - outer loop runs until reg(1) < 256.  Not affected by reg(0)
2 - binary working register
3 - inner loop working register
4 - conditional register, inner loop incrementing register
5 - instruction pointer

# Part 1: what is the lowest non-negative integer value for register 0 that causes the program to halt
# after executing the fewest instructions?

Register 0 is only used for the exit check in instruction 28 - exit if reg(2) == reg(0)

can run the program with instruction 28 modified to be 'eqrr 2 2 4' - register 2 is the answer to part 1.
     */
  }
}
