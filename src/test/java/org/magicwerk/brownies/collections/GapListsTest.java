package org.magicwerk.brownies.collections;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.magictest.client.Capture;
import org.magicwerk.brownies.collections.helper.GapLists;
import org.magicwerk.brownies.collections.primitive.ByteGapList;
import org.magicwerk.brownies.collections.primitive.CharGapList;
import org.magicwerk.brownies.core.PrintTools;
import org.magicwerk.brownies.core.exceptions.WrapperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test of class org.magicwerk.brownies.collections.helper.GapLists.
 *
 * @author Thomas Mauch
 */
public class GapListsTest {

	static final Logger LOG = LoggerFactory.getLogger(GapListsTest.class);

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testInputStream();
		//testReader();
	}

	@Capture
	public static void testInputStream() {
		try {
			byte[] data = { 2, 3 };
			InputStream istream = new ByteArrayInputStream(data);
			StringReader reader = new StringReader("de");
			ByteGapList list = ByteGapList.create((byte) 0, (byte) 1);
			int index = list.size();
			int len = 20;
			int read = GapLists.read(istream, list, len);
			System.out.println(PrintTools.print(list));
		} catch (IOException e) {
			throw new WrapperException(e);
		}
	}

	@Capture
	public static void testReader() {
		try {
			StringReader reader = new StringReader("de");
			CharGapList list = CharGapList.create("ab");
			int index = list.size();
			int len = 20;
			int read = GapLists.read(reader, list, len);
			System.out.println(list);
		} catch (IOException e) {
			throw new WrapperException(e);
		}
	}

}
