package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day4 {

  public static DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  public static class SleepBlock {
    public final LocalDateTime start;
    public final LocalDateTime end;

    public SleepBlock(LocalDateTime start, LocalDateTime end) {
      this.start = start;
      this.end = end;
    }

    public int minutes() {
      return (int) start.until(end, ChronoUnit.MINUTES);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SleepBlock that = (SleepBlock) o;
      return Objects.equal(start, that.start) &&
          Objects.equal(end, that.end);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(start, end);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("start", start)
          .add("end", end)
          .toString();
    }
  }

  public enum LogEntryType {
    BEGIN_SHIFT, FALL_ASLEEP, WAKE_UP
  }

  public static class LogEntry {
    private static final String DATE_PATTERN = "\\[(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})\\]";

    private static final Pattern BEGIN_SHIFT_PATTERN = Pattern.compile(DATE_PATTERN + " Guard #(\\d+) begins shift");
    private static final Pattern FALL_ASLEEP_PATTERN = Pattern.compile(DATE_PATTERN + " falls asleep");
    private static final Pattern WAKE_UP_PATTERN = Pattern.compile(DATE_PATTERN + " wakes up");

    public final LocalDateTime time;
    public final Optional<Integer> guardId;
    public final LogEntryType type;

    public LogEntry(LocalDateTime time, Optional<Integer> guardId, LogEntryType type) {
      this.time = Preconditions.checkNotNull(time, "Time is required");
      this.guardId = guardId;
      this.type = Preconditions.checkNotNull(type, "Type is required");

      if (type == LogEntryType.BEGIN_SHIFT) {
        Preconditions.checkArgument(guardId.isPresent(), "Guard ID must be present if type is BEGIN_SHIFT");
      } else {
        Preconditions.checkArgument(!guardId.isPresent(), "Guard ID must not be present if type is " + type);
      }
    }

    /**
     * Returns the date that this entry should be logged in.  Guards can begin their shift before
     * midnight, so '1518-11-01 23:58' and '1518-11-02 00:05' both belong to the '1519-11-02' shift.
     *
     * @return Date that the entry belongs to.
     */
    public LocalDate entryDate() {
      return time.plus(1, ChronoUnit.HOURS).toLocalDate();
    }

    public static LogEntry parse(String string) {
      Matcher beginShiftMatcher = BEGIN_SHIFT_PATTERN.matcher(string);
      if (beginShiftMatcher.matches()) {
        return new LogEntry(
            parseDateTime(beginShiftMatcher.group(1)),
            Optional.of(Integer.parseInt(beginShiftMatcher.group(2))),
            LogEntryType.BEGIN_SHIFT
        );
      }

      Matcher fallAsleepMatcher = FALL_ASLEEP_PATTERN.matcher(string);
      if (fallAsleepMatcher.matches()) {
        return new LogEntry(parseDateTime(fallAsleepMatcher.group(1)), Optional.empty(), LogEntryType.FALL_ASLEEP);
      }

      Matcher wakeUpMatcher = WAKE_UP_PATTERN.matcher(string);
      if (wakeUpMatcher.matches()) {
        return new LogEntry(parseDateTime(wakeUpMatcher.group(1)), Optional.empty(), LogEntryType.WAKE_UP);
      }

      throw new IllegalArgumentException("'" + string + "' is not a valid log entry");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      LogEntry logEntry = (LogEntry) o;
      return Objects.equal(time, logEntry.time) &&
          Objects.equal(guardId, logEntry.guardId) &&
          type == logEntry.type;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(time, guardId, type);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("time", time)
          .add("guardId", guardId)
          .add("type", type)
          .toString();
    }
  }

  public static class DateLog {
    public final LocalDate date;
    public final int guardId;
    public final ImmutableList<SleepBlock> sleepBlocks;

    public DateLog(LocalDate date, int guardId, ImmutableList<SleepBlock> sleepBlocks) {
      this.date = date;
      this.guardId = guardId;
      this.sleepBlocks = sleepBlocks;
    }

    public int minutesAsleep() {
      return sleepBlocks.stream().mapToInt(SleepBlock::minutes).sum();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DateLog dateLog = (DateLog) o;
      return Objects.equal(date, dateLog.date) &&
          Objects.equal(guardId, dateLog.guardId) &&
          Objects.equal(sleepBlocks, dateLog.sleepBlocks);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(date, guardId, sleepBlocks);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("date", date)
          .add("guardId", guardId)
          .add("sleepBlocks", sleepBlocks)
          .toString();
    }
  }

  public static LocalDateTime parseDateTime(String string) {
    return LocalDateTime.parse(string, DATE_FMT);
  }

  public static ImmutableList<DateLog> parseLines(ImmutableList<String> lines) {
    Map<LocalDate, List<LogEntry>> byDate = lines.stream()
        .sorted()
        .map(LogEntry::parse)
        .collect(Collectors.groupingBy(LogEntry::entryDate));

    return byDate.entrySet().stream()
        .sorted(Comparator.comparing(Map.Entry::getKey))
        .map(Day4::parseDateEntries)
        .collect(ImmutableList.toImmutableList());
  }

  private static DateLog parseDateEntries(Map.Entry<LocalDate, List<LogEntry>> entry) {
    LocalDate date = entry.getKey();
    List<LogEntry> logEntries = entry.getValue();
    Preconditions.checkArgument(logEntries.size() % 2 == 1,"Log for " + date + " must start with BEGIN_SHIFT and have pairs of FALL_ASLEEP and WAKE_UP entries.");

    LogEntry beginShift = logEntries.get(0);
    Preconditions.checkArgument(beginShift.type == LogEntryType.BEGIN_SHIFT, "First entry for " + date + " must be BEGIN_SHIFT");
    Preconditions.checkArgument(beginShift.guardId.isPresent(), "Begin shift for " + date + " must have a guard id");

    ImmutableList.Builder<SleepBlock> sleepBlocks = ImmutableList.builder();
    for (int i = 1; i < logEntries.size(); i += 2) {
      LogEntry fallAsleep = logEntries.get(i);
      LogEntry wakeUp = logEntries.get(i + 1);

      Preconditions.checkArgument(fallAsleep.type == LogEntryType.FALL_ASLEEP, "Entry " + i + " for " + date + " must be FALL_ASLEEP");
      Preconditions.checkArgument(wakeUp.type == LogEntryType.WAKE_UP, "Entry " + (i+1) + " for " + date + " must be WAKE_UP");

      sleepBlocks.add(new SleepBlock(fallAsleep.time, wakeUp.time));
    }

    return new DateLog(date, beginShift.guardId.get(), sleepBlocks.build());
  }

  public static String printableLog(ImmutableList<DateLog> log) {
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM-dd");

    StringBuilder bldr = new StringBuilder("Date   ID    Minute\n" +
        "             000000000011111111112222222222333333333344444444445555555555\n" +
        "             012345678901234567890123456789012345678901234567890123456789");

    for (DateLog entry : log) {
      bldr.append('\n')
          .append(entry.date.format(dateFormat))
          .append("  #")
          .append(String.format("%-5s", entry.guardId));

      int minute = 0;
      for (SleepBlock sleepBlock : entry.sleepBlocks) {
        for (; minute < sleepBlock.start.getMinute(); minute ++) {
          bldr.append('.');
        }

        for (; minute < sleepBlock.end.getMinute(); minute ++) {
          bldr.append('#');
        }
      }

      for (; minute < 60; minute ++) {
        bldr.append('.');
      }
    }

    return bldr.toString();
  }

  /**
   * Returns the id of the guard who was asleep for the most cumulative minutes over the whole log.
   *
   * @param log Log of which guard was on duty and when they were asleep
   * @return Id of the guard who was asleep the most over the whole log
   */
  public static int mostMinutesAsleepGuardId(ImmutableList<DateLog> log) {
    Map<Integer, Integer> guardIdToMinutesAsleep = log.stream().collect(Collectors.groupingBy(
        entry -> entry.guardId,
        Collectors.summingInt(DateLog::minutesAsleep)
    ));

    return guardIdToMinutesAsleep.entrySet().stream()
        .sorted(Comparator.comparingInt((Map.Entry<Integer, Integer> entry) -> entry.getValue()).reversed())
        .mapToInt(Map.Entry::getKey)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No guards in the log"));
  }

  /**
   * Returns the minute where the guard was asleep on the most days.
   *
   * @param log Log of which guard was on duty and when they were asleep
   * @param guardId Id of the guard to check
   * @return Minute that the guard was asleep on the most days.
   */
  public static int mostAsleepMinute(ImmutableList<DateLog> log, int guardId) {
    ImmutableList<DateLog> guardEntries = log.stream()
        .filter(entry -> entry.guardId == guardId)
        .collect(ImmutableList.toImmutableList());

    // Array of minutes containing the number of days where the guard was asleep on that minute
    int[] sleepMinutes = new int[60];

    for (DateLog entry : guardEntries) {
      for (SleepBlock sleepBlock : entry.sleepBlocks) {
        for (int minute = sleepBlock.start.getMinute(); minute < sleepBlock.end.getMinute(); minute ++) {
          sleepMinutes[minute]++;
        }
      }
    }

    int maxMinute = 0;
    int mostDaysAsleep = 0;
    for (int minute = 0; minute < sleepMinutes.length; minute ++) {
      int daysAsleep = sleepMinutes[minute];
      if (daysAsleep > mostDaysAsleep) {
        maxMinute = minute;
        mostDaysAsleep = daysAsleep;
      }
    }

    return maxMinute;
  }

  public static void main(String[] args) throws Exception {
    File file = new File(Day4.class.getResource("/day4.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8)).stream()
        .sorted()
        .collect(ImmutableList.toImmutableList());

    ImmutableList<DateLog> log = parseLines(lines);
    System.out.println(printableLog(log));

    System.out.println("\n\n\n------------------------------\n\n");

    // Part 1: guard id that was asleep the most, cumulatively * the minute they were asleep on the most days
    int guardId = mostMinutesAsleepGuardId(log);
    int minute = mostAsleepMinute(log, guardId);
    System.out.println("Part 1: " + (guardId * minute));
  }
}
