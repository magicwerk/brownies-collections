package org.magicwerk.brownies.collections.dev;

import org.magictest.client.Capture;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.slf4j.Logger;

public class MethodSourceTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new MethodSourceTest().test();
	}

	@Capture
	public void test() {
		String src1 = "	public static void main(String[] args) { \n" +
				"		test(); \n" +
				"	}";
		String src2 = "	/** Method */ \n" +
				"	public static void main(String[] args) { \n" +
				"		// comment \n" +
				"		test(); \n" +
				"	}";
		String src3 = "	/** Constructor */ \n" +
				"Name(String str) { \n" +
				"		test(); \n" +
				"	}";
		String src4 = "	/** Method */ \n" +
				"public abstract void main(String[] args); ";
		MethodSource ms = new MethodSource(null, "name", "doc", "header", "body");

		ms.setSourceDoc(src1);
		print(ms);

		ms.setSourceDoc(src2);
		print(ms);

		ms.setSourceDoc(src3);
		print(ms);

		ms.setSourceDoc(src4);
		print(ms);
	}

	void print(MethodSource ms) {
		LOG.info("doc: {}", ms.getDoc());
		LOG.info("header: {}", ms.getHeader());
		LOG.info("body: {}", ms.getBody());
		LOG.info("source: {}", ms.getSource());
		LOG.info("sourceDoc: {}", ms.getSourceDoc());
		LOG.info("---");
	}
}
