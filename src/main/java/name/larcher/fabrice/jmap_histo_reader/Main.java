/*
 * Copyright 2017 - Fabrice LARCHER
 */
package name.larcher.fabrice.jmap_histo_reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQueries;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author larcher
 */
public class Main {
	
	public static void main(String... args) {
		
		if (args.length == 0 || args[0].isEmpty()) {
			error("No input directory", 1);
		}

		Path inputDir = Paths.get(args[0]);
		System.out.println("Reading files of " + inputDir);

		// Parsing
		final SortedMap<Instant, HeapHisto> histoMap =
			/*Collections.synchronizedSortedMap(*/new TreeMap<>()/*)*/;
		try {
			Files.list(inputDir)
				.sequential()
				.unordered()
				.filter((Path path) -> !path.getFileName().endsWith(".histo"))
				.peek((Path path) -> System.out.println("reading " + path))
				.forEach((Path path) -> histoMap.put(parseTime(path), readJmapHisto(path)));

		} catch (IOException e) {
			error(e.getMessage(), 2);
		}

		if (histoMap.isEmpty()) {
			error("No file processed", 4);
		}

		System.out.println("Done parsing of " + histoMap.size() + " files");

		long durationSeconds = (histoMap.lastKey().toEpochMilli() -  histoMap.firstKey().toEpochMilli()) / 1000;
		System.out.println("Over a duration of " + durationSeconds + " seconds");

		stats("Instance count growth / type", "instances",
			(HeapHistoEntry histoLine) -> (long) histoLine.getInstanceCount(),
			histoMap, durationSeconds);
		stats("Allocated memory growth / type", "bytes",
			(HeapHistoEntry histoLine) -> histoLine.getMemorySize(),
			histoMap, durationSeconds);
	}

	private static void stats(String msg, String unit, Function<HeapHistoEntry, Long> function,
		SortedMap<Instant, HeapHisto> histoMap, long durationSeconds) {

		Map<String, Long> instanceCountProgress = new HashMap<>();
		Map<String, Long> lastInstanceCount = new HashMap<>();
		for (HeapHisto histo : histoMap.values()) {
			for (HeapHistoEntry histoEntry : histo) {
				Long lastCount = lastInstanceCount.put(histoEntry.getClassName(), function.apply(histoEntry));
				if (lastCount != null) {
					Long lastProgress = instanceCountProgress.get(histoEntry.getClassName());
					long progress = (lastProgress == null ? 0 : lastProgress)
						+ function.apply(histoEntry)
						- lastCount;
					instanceCountProgress.put(histoEntry.getClassName(), progress);
				}
			}
		}

		SortedMap<Long, String> classesByProgress = new TreeMap<>(Comparator.reverseOrder());
		for (Entry<String, Long> progressEntry : instanceCountProgress.entrySet()) {
			classesByProgress.put(progressEntry.getValue(), progressEntry.getKey());
		}
		if (!classesByProgress.isEmpty()) {

			System.out.println("--- " + msg);

			for (Entry<Long, String> sortedProgressEntry : classesByProgress.entrySet()) {
				double ratio = sortedProgressEntry.getKey() / durationSeconds;
				if (ratio > 0) {
					System.out.println(String.format("%s %s/second - %s",
						ratio, unit,
						sortedProgressEntry.getValue()));
				}
			}
		}
	}

	private static void error(String msg, int exitCode) {
		assert exitCode > 0;
		System.err.println(msg);
		System.exit(exitCode);
	}

	private static final DateTimeFormatter PATH_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH-mm-ss");
	private static final DateTimeFormatter PATH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static Instant parseTime(Path path) {
		String fileName = path.getFileName().toString();
		int indexOfT = fileName.indexOf('T');
		int indexOf_ = fileName.indexOf('_');
		int indexOfDot = fileName.indexOf('.');
		String date = fileName.substring(indexOf_ + 1, indexOfT);
		String time = fileName.substring(indexOfT + 1, indexOfT + 9);
		String offset = fileName.substring(indexOfT + 9, indexOfDot);
		return LocalDateTime.of(
			PATH_DATE_FORMATTER.parse(date, TemporalQueries.localDate()),
			PATH_TIME_FORMATTER.parse(time, TemporalQueries.localTime()))
				.toInstant(ZoneOffset.of(offset));
	}

	private static HeapHisto readJmapHisto(Path path) {
		try {
			return new HeapHisto(Files.lines(path)
				.map(Main::readJmapHistoLine)
				.filter(Objects::nonNull)
				.collect(Collectors.toList()));

		} catch (IOException e) {
			error("While processing " + path + ": " + e.getMessage(), 3);
			return null;
		}
	}

	private static final Pattern LINE_PATTERN = Pattern.compile("^ *(\\d+)\\: +(\\d+) +(\\d+) {2}(.+)$");

	private static HeapHistoEntry readJmapHistoLine(String line) {
		if (line.isEmpty()) {
			return null;
		}
		Matcher match = LINE_PATTERN.matcher(line);
		if (!match.matches()) {
			return null;
		}
		return new HeapHistoEntry(match.group(4),
			Integer.parseInt(match.group(2)),
			Long.parseLong(match.group(3)));
	}

}
