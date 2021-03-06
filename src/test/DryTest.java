import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import domain.AnalyzerReturn;
import domain.analyzer.DryAnalyzer;
import domain.message.LinterMessage;

public class DryTest {
	private DryAnalyzer analyzer;

	@Test
	public void oneClassNoDuplicationTest() {
		String[] classList = { "example/dry/Kitten" };
		this.analyzer = new DryAnalyzer(classList);
		AnalyzerReturn returned = this.analyzer.getFeedback(classList);

		for (LinterMessage l : returned.errorsCaught) {
			System.out.println(l);
		}

		assertEquals("DryAnalyzer", returned.analyzerName);
		assertEquals(0, returned.errorsCaught.size());
	}

	@Test
	public void twoClassNoDuplicationTest() {
		String[] classList = { "example/dry/Puppy", "example/dry/Kitten" };
		this.analyzer = new DryAnalyzer(classList);
		AnalyzerReturn returned = this.analyzer.getFeedback(classList);

		for (LinterMessage l : returned.errorsCaught) {
			System.out.println(l);
		}

		assertEquals("DryAnalyzer", returned.analyzerName);
		assertEquals(0, returned.errorsCaught.size());
	}

	@Test
	public void oneClassWithDuplicationTest() {
		String[] classList = { "example/dry/Dog" };
		this.analyzer = new DryAnalyzer(classList);
		AnalyzerReturn returned = this.analyzer.getFeedback(classList);

		for (LinterMessage l : returned.errorsCaught) {
			System.out.println(l);
		}

		assertEquals("DryAnalyzer", returned.analyzerName);
		assertEquals(2, returned.errorsCaught.size());
		assertEquals("example/dry/Dog", returned.errorsCaught.get(0).className);
		assertEquals("example/dry/Dog", returned.errorsCaught.get(1).className);
		for (LinterMessage e : returned.errorsCaught)
			assertEquals(AnalyzerFixture.WARNING_MSG_TYPE, e.getMessageType());
	}

	@Test
	public void twoClassSlightDuplicationTest() {

		String[] classList = { "example/dry/CatExtendsAnimal", "example/dry/DogExtendsAnimal" };
		this.analyzer = new DryAnalyzer(classList);
		AnalyzerReturn returned = this.analyzer.getFeedback(classList);

		for (LinterMessage l : returned.errorsCaught) {
			System.out.println(l);
		}

		assertEquals("DryAnalyzer", returned.analyzerName);
		assertEquals(2, returned.errorsCaught.size());
		assertEquals("example/dry/DogExtendsAnimal", returned.errorsCaught.get(0).className);
		assertEquals("example/dry/CatExtendsAnimal", returned.errorsCaught.get(1).className);
		for (LinterMessage e : returned.errorsCaught)
			assertEquals(AnalyzerFixture.WARNING_MSG_TYPE, e.getMessageType());
	}

	@Test
	public void twoClassWithDuplicationTest() {
		String[] classList = { "example/dry/Cat", "example/dry/Dog" };
		this.analyzer = new DryAnalyzer(classList);
		AnalyzerReturn returned = this.analyzer.getFeedback(classList);

		for (LinterMessage l : returned.errorsCaught) {
			System.out.println(l);
		}

		assertEquals("DryAnalyzer", returned.analyzerName);
		assertEquals(4, returned.errorsCaught.size());
		assertEquals("example/dry/Cat", returned.errorsCaught.get(0).className);
		assertEquals("example/dry/Cat", returned.errorsCaught.get(1).className);
		assertEquals("example/dry/Dog", returned.errorsCaught.get(2).className);
		assertEquals("example/dry/Dog", returned.errorsCaught.get(3).className);
		for (LinterMessage e : returned.errorsCaught)
			assertEquals(AnalyzerFixture.WARNING_MSG_TYPE, e.getMessageType());
	}

	@Test
	public void allClassesTest() {
		String[] classList = { "example/dry/Cat", "example/dry/Dog", "example/dry/CatExtendsAnimal",
				"example/dry/DogExtendsAnimal" };
		this.analyzer = new DryAnalyzer(classList);
		AnalyzerReturn returned = this.analyzer.getFeedback(classList);

		for (LinterMessage e : returned.errorsCaught) {
			System.out.println(e.toString());
		}

		assertEquals("DryAnalyzer", returned.analyzerName);
		assertEquals(6, returned.errorsCaught.size());
		assertEquals("example/dry/DogExtendsAnimal", returned.errorsCaught.get(0).className);
		assertEquals("example/dry/Cat", returned.errorsCaught.get(1).className);
		assertEquals("example/dry/Cat", returned.errorsCaught.get(2).className);
		assertEquals("example/dry/Dog", returned.errorsCaught.get(3).className);
		assertEquals("example/dry/Dog", returned.errorsCaught.get(4).className);
		assertEquals("example/dry/CatExtendsAnimal", returned.errorsCaught.get(5).className);
		for (LinterMessage e : returned.errorsCaught)
			assertEquals(AnalyzerFixture.WARNING_MSG_TYPE, e.getMessageType());
	}
}