
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import domain.AnalyzerReturn;
import domain.analyzer.ExceptionThrownAnalyzer;
import domain.analyzer.ExceptionThrownAnalyzer.ExceptionLinterIssue;
import domain.message.LinterMessage;
import domain.message.WarningLinterMessage;

public class ExceptionLinterTest extends AnalyzerFixture<ExceptionThrownAnalyzer> {
	private final String[] exampleClasses = { "example.exceptionstyle.ExceptionStyleExamples" };

	@Override
	@BeforeEach
	protected void initAnalyzerUUT() {
		this.populateParserData(exampleClasses);
		this.analyzer = new ExceptionThrownAnalyzer(parser);
		analyzer.getRelevantData(exampleClasses);
	}

	// =========================Compliant Methods=========================//
	@ParameterizedTest
	@ValueSource(strings = { "compliantMethodThrowsException", "compliantMethodCallThrowsException",
			"compliantMethodThrowsMultipleExceptions", "compliantMethodCatchException",
			"compliantMethodCallCatchException", "compliantMethodCatchMultipleException",
			"compliantMethodCallCatchMultipleException", "compliantMethodThrowsAndCatchesException" })
	public void testLinterResponseToCompliantMethods(String testedMethodName) {
		List<ExceptionLinterIssue> expected = new ArrayList<>();
		expected.add(ExceptionLinterIssue.NO_VIOLATION);

		assertEquals(expected, analyzer.checkMethodCompliance(exampleClasses[0], testedMethodName));
	}

	// ================== Non-Compliant Methods ===================//
	@ParameterizedTest
	@ValueSource(strings = { "nonCompliantMethodThrowsException", "nonCompliantMethodCallsCompliantButThrowsException",
			"nonCompliantMethodCallsCompliantWithMultipleThrowsButThrowsException",
			"nonCompliantMethodThrowsAndCatchesCompliantException" })
	public void testLinterResponseToNonCompliantMethodBecauseThrowException(String testedMethodName) {
		List<ExceptionLinterIssue> expected = new ArrayList<>();
		expected.add(ExceptionLinterIssue.THROW_EXCEPTION);

		assertEquals(expected, analyzer.checkMethodCompliance(exampleClasses[0], testedMethodName));
	}

	@ParameterizedTest
	@ValueSource(strings = { "nonCompliantMethodCatchesException",
			"nonCompliantMethodCallsCompliantWithMultipleThrowsButCatchesException",
			"nonCompliantMethodCallsCompliantButCatchesException", "nonCompliantMethodCatchMultipleExceptions",
			"nonCompliantMethodThrowsCompliantAndCatchesException" })
	public void testNonCompliantMethodCatchesException(String testedMethodName) {
		// Expected List of Issues
		List<ExceptionLinterIssue> expected = new ArrayList<>();
		expected.add(ExceptionLinterIssue.CATCH_EXCEPTION);

		assertEquals(expected, analyzer.checkMethodCompliance(exampleClasses[0], testedMethodName));
	}

	@Test
	public void testNonCompliantMethodThrowsRuntimeException() {
		// Expected List of Issues
		List<ExceptionLinterIssue> expected = new ArrayList<>();
		expected.add(ExceptionLinterIssue.THROW_RUNTIME_EXCEPTION);

		assertEquals(expected, analyzer.checkMethodCompliance(exampleClasses[0],
				"nonCompliantMethodThrowsRuntimeException"));
	}

	@Test
	public void testNonCompliantMethodThrowsError() {
		// Expected List of Issues
		List<ExceptionLinterIssue> expected = new ArrayList<>();
		expected.add(ExceptionLinterIssue.THROW_ERROR);

		assertEquals(expected, analyzer.checkMethodCompliance(exampleClasses[0], "nonCompliantMethodThrowsError"));
	}

	@Test
	public void testNonCompliantMethodThrowsThrowable() {
		// Expected List of Issues
		List<ExceptionLinterIssue> expected = new ArrayList<>();
		expected.add(ExceptionLinterIssue.THROW_THROWABLE);

		assertEquals(expected, analyzer.checkMethodCompliance(exampleClasses[0],
				"nonCompliantMethodThrowsThrowable"));
	}

	@Test
	public void testNonCompliantMethodThrowsAndCatchesException() {
		// Expected List of Issues
		List<ExceptionLinterIssue> expected = new ArrayList<>();
		expected.add(ExceptionLinterIssue.THROW_EXCEPTION);
		expected.add(ExceptionLinterIssue.CATCH_EXCEPTION);

		assertEquals(expected, analyzer.checkMethodCompliance(exampleClasses[0],
				"nonCompliantMethodThrowsAndCatchesException"));

	}

	@Test
	public void testExceptionAnalyzerReturnType() {

		analyzer.analyzeData();

		AnalyzerReturn expectedReturnType = analyzer.composeReturnType();
		List<String> LinterMessageStrings = new ArrayList<>();

		for (LinterMessage err : expectedReturnType.errorsCaught) {
			LinterMessageStrings.add(err.toString());
		}

		// All of the LinterMessages we expect to get returned
		LinterMessage error0Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodThrowsException",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodThrowsException() throws java.lang.Exception in its method signature instead of a specific exception class");
		LinterMessage error1Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodThrowsRuntimeException",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodThrowsRuntimeException() throws java.lang.RuntimeException in its method signature instead of a specific exception class");

		LinterMessage error2Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodThrowsError",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodThrowsError() throws java.lang.Error in its method signature instead of a specific exception class");

		LinterMessage error3Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodThrowsThrowable",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodThrowsThrowable() throws java.lang.Throwable in its method signature instead of a specific exception class");

		LinterMessage error4Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodCallsCompliantButThrowsException",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodCallsCompliantButThrowsException() throws java.lang.Exception in its method signature instead of a specific exception class");

		LinterMessage error5Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodCallsCompliantWithMultipleThrowsButThrowsException",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodCallsCompliantWithMultipleThrowsButThrowsException() throws java.lang.Exception in its method signature instead of a specific exception class");

		LinterMessage error6Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodCatchesException",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodCatchesException() has at least one catch block that catches java.lang.Exception instead of a specific exception class");

		LinterMessage error7Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodCallsCompliantButCatchesException",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodCallsCompliantButCatchesException() has at least one catch block that catches java.lang.Exception instead of a specific exception class");

		LinterMessage error8Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodCallsCompliantWithMultipleThrowsButCatchesException",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodCallsCompliantWithMultipleThrowsButCatchesException() has at least one catch block that catches java.lang.Exception instead of a specific exception class");

		LinterMessage error9Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodCatchMultipleExceptions",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodCatchMultipleExceptions() has at least one catch block that catches java.lang.Exception instead of a specific exception class");

		LinterMessage error10Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodThrowsCompliantAndCatchesException",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodThrowsCompliantAndCatchesException() has at least one catch block that catches java.lang.Exception instead of a specific exception class");
		LinterMessage error11Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodThrowsAndCatchesCompliantException",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodThrowsAndCatchesCompliantException() throws java.lang.Exception in its method signature instead of a specific exception class");

		LinterMessage error12Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodThrowsAndCatchesException",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodThrowsAndCatchesException() throws java.lang.Exception in its method signature instead of a specific exception class");

		LinterMessage error13Expected = new WarningLinterMessage("example.exceptionstyle.ExceptionStyleExamples",
				"nonCompliantMethodThrowsAndCatchesException",
				"example.exceptionstyle.ExceptionStyleExamples.nonCompliantMethodThrowsAndCatchesException() has at least one catch block that catches java.lang.Exception instead of a specific exception class");

		assertEquals(expectedReturnType.analyzerName, "Generic Exception Linter");
		assertEquals(expectedReturnType.errorsCaught.size(), 14);
		assertTrue(LinterMessageStrings.contains(error0Expected.toString()));
		assertTrue(LinterMessageStrings.contains(error1Expected.toString()));
		assertTrue(LinterMessageStrings.contains(error2Expected.toString()));
		assertTrue(LinterMessageStrings.contains(error3Expected.toString()));
		assertTrue(LinterMessageStrings.contains(error4Expected.toString()));
		assertTrue(LinterMessageStrings.contains(error5Expected.toString()));
		assertTrue(LinterMessageStrings.contains(error6Expected.toString()));
		assertTrue(LinterMessageStrings.contains(error7Expected.toString()));
		assertTrue(LinterMessageStrings.contains(error8Expected.toString()));
		assertTrue(LinterMessageStrings.contains(error9Expected.toString()));
		// If a throw and a catch violation is detected in the same file , throw
		// violations come first, then catch violations
		assertTrue(LinterMessageStrings.contains(error10Expected.toString()));
		assertTrue(LinterMessageStrings.contains(error11Expected.toString()));
		assertTrue(LinterMessageStrings.contains(error12Expected.toString()));
		assertTrue(LinterMessageStrings.contains(error13Expected.toString()));
	}

}
