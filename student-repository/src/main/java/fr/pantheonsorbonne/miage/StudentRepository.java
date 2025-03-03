package fr.pantheonsorbonne.miage;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;




public class StudentRepository implements Iterable<Student> {

	private String db;

	private StudentRepository(String db) {
		this.db = db;
	}

	public static StudentRepository withDB(String db) {
		if (!Files.exists(Paths.get(db))) {
			throw new UnsupportedOperationException("failed to find" + Paths.get(db).toAbsolutePath().toString());
		}
		return new StudentRepository(db);
	}

	public static List<String> toReccord(Student stu) {

		return Arrays.asList(stu.getName(), stu.getTitle(), "" + stu.getId(),stu.getPassword());
	}

	public StudentRepository add(Student s) {
		Iterator<Student> previousContent = StudentRepository.withDB(this.db).iterator();
		try (FileWriter writer = new FileWriter(this.db)) {
			CSVPrinter csvFilePrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

			previousContent.forEachRemaining(student -> {
				try {
					csvFilePrinter.printRecord(toReccord(student));
				} catch (IOException e) {
					throw new UnsupportedOperationException("failed to update db file");
				}
			});
			csvFilePrinter.printRecord(toReccord(s));
			csvFilePrinter.flush();
			csvFilePrinter.close(true);

		} catch (IOException e) {
			throw new UnsupportedOperationException("failed to update db file");
		}
		return this;

	}
	@Override
	public java.util.Iterator<Student> iterator() {
		try (FileReader reader = new FileReader(this.db)) {
			java.util.Iterator<Student> currentIterator = null;
			CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
			currentIterator = parser.getRecords().stream()
					.map(studentRecord -> new Student(Integer.parseInt(studentRecord.get(2)), studentRecord.get(0), studentRecord.get(1), studentRecord.get(3)))
					.map(Student.class::cast).iterator();
			return currentIterator;

		} catch (IOException e) {
			Logger.getGlobal().info("IO PB" + e.getMessage());
			return Collections.<Student>emptySet().iterator();
		}
	}

}
