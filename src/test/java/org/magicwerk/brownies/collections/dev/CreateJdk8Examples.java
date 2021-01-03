/*
 * Copyright 2013 by Thomas Mauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: KeyCollectionsExamples.java 2096 2014-01-10 07:57:10Z origo $
 */
package org.magicwerk.brownies.collections.dev;

import java.util.List;

import org.magicwerk.brownies.core.StringTools;
import org.magicwerk.brownies.core.files.FileTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.strings.StringParser;
import org.magicwerk.brownies.core.strings.StringParser.Part;
import org.magicwerk.brownies.core.strings.matcher.IStringMatcher;
import org.magicwerk.brownies.core.strings.matcher.RegexStringMatcher;
import org.slf4j.Logger;

/**
 * Create Java examples source for Java 8 by <br/>
 * - removing lines annotated with @java7:begin / @java7:end <br/>
 * - uncommenting lines annotated with @java8:begin / @java8:end <br/>
 * Reads: test/org/magicwerk/brownies/collections/KeyCollectionsExamples.java <br/>
 * Writes: test-jdk8/org/magicwerk/brownies/collections/KeyCollectionsExamples.java
 *
 * @author Thomas Mauch
 * @version $Id: KeyCollectionsExamples.java 2096 2014-01-10 07:57:10Z origo $
 */
public class CreateJdk8Examples {

	static class Parser extends StringParser {
		static IStringMatcher java7Begin = new RegexStringMatcher().setPattern("(?m)^\\s*//\\s*@java7:begin\\s*\\r?\\n");
		static IStringMatcher java7End = new RegexStringMatcher().setPattern("(?m)^\\s*//\\s*@java7:end\\s*\\r?\\n");
		static IStringMatcher java8Begin = new RegexStringMatcher().setPattern("(?m)^\\s*//\\s*@java8:begin\\s*\\r?\\n");
		static IStringMatcher java8End = new RegexStringMatcher().setPattern("(?m)^\\s*//\\s*@java8:end\\s*\\r?\\n");
		static IStringMatcher testBegin = new RegexStringMatcher().setPattern("(?m)^\\s*//\\s*@test:begin\\s*\\r?\\n");
		static IStringMatcher testEnd = new RegexStringMatcher().setPattern("(?m)^\\s*//\\s*@test:end\\s*\\r?\\n");

		static ParserMatcher testParser = new ParserMatcher("test", testBegin, testEnd);
		static ParserMatcher java8Parser = new ParserMatcher("java8", java8Begin, java8End);

		public Parser() {
			addParser(testParser);
			addParser(java8Parser);
		}
	}

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		run();
	}

	static void run() {
		String dir = "org/magicwerk/brownies/collections";
		String jdk7Dir = "test/" + dir;
		String jdk8Dir = "test-jdk8/" + dir;
		String file = "/KeyCollectionsExamples.java";
		String jdk7File = jdk7Dir + file;
		String jdk8File = jdk8Dir + file;

		// Read old file
		String jdk7Text = FileTools.readFile().setFile(jdk7File).readText();

		// Convert source
		StringBuilder buf = new StringBuilder();
		Parser parser = new Parser();
		List<Part> parts = parser.parseParts(jdk7Text);
		for (Part part : parts) {
			String type = part.getType();
			LOG.info("Type= {}: {}", type, part.getString());

			if (type == null) {
				type = "";
			}
			if (type.equals(Parser.testParser.getType())) {
				continue; // skip
			} else if (type.equals(Parser.java8Parser.getType())) {
				buf.append("\r\n        // Java 8\r\n");
				List<String> lines = StringTools.splitLines(part.getString(), true);
				// Skip begin and end line
				for (int i = 2; i < lines.size() - 2; i++) {
					String line = lines.get(i);
					if (line.startsWith("//")) {
						buf.append("  " + line.substring(2));
					} else {
						buf.append(line);
					}
				}
			} else {
				buf.append(part.getString());
			}
		}
		String jdk8Text = buf.toString();

		// Write file new
		FileTools.writeFile().setFile(jdk8File).setText(jdk8Text).write();
	}

}
