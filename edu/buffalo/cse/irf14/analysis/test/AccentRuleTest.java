/**
 * 
 */
package edu.buffalo.cse.irf14.analysis.test;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.buffalo.cse.irf14.analysis.TokenFilterType;
import edu.buffalo.cse.irf14.analysis.TokenizerException;

/**
 * @author chandana
 *
 */
public class AccentRuleTest extends TFRuleBaseTest {

	@Test
	public void testRule() {
		try {

			assertArrayEquals(
					new String[] { "Resumes", "can", "be", "used", "for", "a",
							"variety", "of", "reasons" },
					runTest(TokenFilterType.ACCENT,
							"Résumés can be used for a variety of reasons"));
			assertArrayEquals(
					new String[] { "for", "example", "vis-a-vis", "piece",
							"de", "resistance", "and", "creme", "brulee" },
					runTest(TokenFilterType.ACCENT,
							"for example vis-à-vis pièce de résistance and crème brûlée"));
			assertArrayEquals(
					new String[] { "Spanish", "pinguino", "French", "aigue",
							"or", "aigue" },
					runTest(TokenFilterType.ACCENT,
							"Spanish pingüino French aiguë or aigüe"));
		} catch (TokenizerException e) {
			fail("Exception thrown when not expected");
		}
	}

}