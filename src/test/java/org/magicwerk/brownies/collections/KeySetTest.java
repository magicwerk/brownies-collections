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
 * $Id$
 */
package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.magictest.client.Assert;
import org.magictest.client.Test;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.slf4j.Logger;

/**
 * Test of KeySet.
 *
 * @author Thomas Mauch
 */
public class KeySetTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testEquals();
	}

	@Test
	public static void testEquals() {
		// Empty set and list are not equal
		{
			Set<String> set = new HashSet<>();
			List<String> list = new ArrayList<>(set);
			Assert.assertTrue(!set.equals(list));
		}
		{
			KeySet<String> set = new KeySet.Builder<String>().build();
			List<String> list = new ArrayList<>(set);
			Assert.assertTrue(!set.equals(list));
		}
	}

}
