package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static dev.jh.adventofcode.Day4.LogEntryType.*;
import static org.assertj.core.api.Assertions.assertThat;

public class Day4Test {

  public static final ImmutableList<String> EXAMPLE_LINES = ImmutableList.of(
      "[1518-11-01 00:25] wakes up",
      "[1518-11-01 00:30] falls asleep",
      "[1518-11-03 00:24] falls asleep",
      "[1518-11-03 00:05] Guard #10 begins shift",
      "[1518-11-04 00:46] wakes up",
      "[1518-11-01 00:05] falls asleep",
      "[1518-11-02 00:40] falls asleep",
      "[1518-11-01 00:00] Guard #10 begins shift",
      "[1518-11-02 00:50] wakes up",
      "[1518-11-05 00:45] falls asleep",
      "[1518-11-05 00:03] Guard #99 begins shift",
      "[1518-11-01 23:58] Guard #99 begins shift",
      "[1518-11-01 00:55] wakes up",
      "[1518-11-03 00:29] wakes up",
      "[1518-11-04 00:36] falls asleep",
      "[1518-11-05 00:55] wakes up",
      "[1518-11-04 00:02] Guard #99 begins shift"
  );

  @Test
  public void parseTime() {
    assertThat(Day4.parseDateTime("1518-11-01 23:58")).isEqualTo(LocalDateTime.of(1518, 11, 1, 23, 58));
  }

  @Test
  public void sleepBlockDuration() {
    assertThat(new Day4.SleepBlock(
        Day4.parseDateTime("1518-11-01 00:05"),
        Day4.parseDateTime("1518-11-01 00:25")
    ).minutes()).isEqualTo(20);
  }

  @Test
  public void parseLogEntry() {
    assertThat(Day4.LogEntry.parse("[1518-11-01 00:00] Guard #10 begins shift"))
        .isEqualTo(new Day4.LogEntry(LocalDateTime.of(1518, 11, 1, 0, 0), Optional.of("#10"), BEGIN_SHIFT));
    assertThat(Day4.LogEntry.parse("[1518-11-01 00:05] falls asleep"))
        .isEqualTo(new Day4.LogEntry(LocalDateTime.of(1518, 11, 1, 0, 5), Optional.empty(), FALL_ASLEEP));
    assertThat(Day4.LogEntry.parse("[1518-11-01 00:25] wakes up"))
        .isEqualTo(new Day4.LogEntry(LocalDateTime.of(1518, 11, 1, 0, 25), Optional.empty(), WAKE_UP));
  }

  @Test
  public void parseLines() {
    ImmutableList<Day4.DateLog> expected = ImmutableList.of(
        new Day4.DateLog(LocalDate.of(1518, 11, 1), "#10", ImmutableList.of(
            new Day4.SleepBlock(Day4.parseDateTime("1518-11-01 00:05"), Day4.parseDateTime("1518-11-01 00:25")),
            new Day4.SleepBlock(Day4.parseDateTime("1518-11-01 00:30"), Day4.parseDateTime("1518-11-01 00:55"))
        )),
        new Day4.DateLog(LocalDate.of(1518, 11, 2), "#99", ImmutableList.of(
            new Day4.SleepBlock(Day4.parseDateTime("1518-11-02 00:40"), Day4.parseDateTime("1518-11-02 00:50"))
        )),
        new Day4.DateLog(LocalDate.of(1518, 11, 3), "#10", ImmutableList.of(
            new Day4.SleepBlock(Day4.parseDateTime("1518-11-03 00:24"), Day4.parseDateTime("1518-11-03 00:29"))
        )),
        new Day4.DateLog(LocalDate.of(1518, 11, 4), "#99", ImmutableList.of(
            new Day4.SleepBlock(Day4.parseDateTime("1518-11-04 00:36"), Day4.parseDateTime("1518-11-04 00:46"))
        )),
        new Day4.DateLog(LocalDate.of(1518, 11, 5), "#99", ImmutableList.of(
            new Day4.SleepBlock(Day4.parseDateTime("1518-11-05 00:45"), Day4.parseDateTime("1518-11-05 00:55"))
        ))
    );

    assertThat(Day4.parseLines(EXAMPLE_LINES)).isEqualTo(expected);
  }

  @Test
  public void entryDateTime() {
    assertThat(Day4.entryDateTime(Day4.parseDateTime("1518-11-01 23:58"))).isEqualTo(LocalDate.of(1518, 11, 2));
    assertThat(Day4.entryDateTime(Day4.parseDateTime("1518-11-02 00:05"))).isEqualTo(LocalDate.of(1518, 11, 2));
    assertThat(Day4.entryDateTime(Day4.parseDateTime("1518-11-02 00:59"))).isEqualTo(LocalDate.of(1518, 11, 2));
  }

  @Test
  public void printableLog() {
    String expected = "Date   ID    Minute\n" +
        "             000000000011111111112222222222333333333344444444445555555555\n" +
        "             012345678901234567890123456789012345678901234567890123456789\n" +
        "11-01  #10   .....####################.....#########################.....\n" +
        "11-02  #99   ........................................##########..........\n" +
        "11-03  #10   ........................#####...............................\n" +
        "11-04  #99   ....................................##########..............\n" +
        "11-05  #99   .............................................##########.....";

    assertThat(Day4.printableLog(Day4.parseLines(EXAMPLE_LINES))).isEqualTo(expected);
  }
}