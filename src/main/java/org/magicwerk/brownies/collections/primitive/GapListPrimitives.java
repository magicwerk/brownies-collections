/*
 * Copyright 2014 by Thomas Mauch
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
package org.magicwerk.brownies.collections.primitive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.magicwerk.brownies.collections.helper.GapLists;

/**
 * This class implements helper for primitives.
 * Public access is granted through {@link GapLists}.
 *
 * @author Thomas Mauch
 */
public class GapListPrimitives {

	/**
	 * Read specified number of bytes from InputStream into ByteGapList.
	 * 
	 * @param istream	input stream (source)
	 * @param list		list (target)
	 * @param len		maximum number of bytes to read
	 * @return			number of bytes read into the buffer, -1 if end of stream has been reached
	 */
	protected static int read(InputStream istream, ByteGapList list, int len) throws IOException {
		int index = list.size();
		byte[] buf = list.prepareAddBuffer(index, len);

		int read = istream.read(buf, index, len);
		if (read == -1) {
			read = 0;
		}
		list.releaseAddBuffer(index, (read >= -0) ? read : 0);
		return read;
	}

	/**
	 * Write specified number of bytes from ByteGapList into OutputStream.
	 * 
	 * @param ostream	output stream (target)
	 * @param list		list (source)
	 * @param off		offset of first byte to write
	 * @param len		number of bytes to write
	 */
	protected static void write(OutputStream ostream, ByteGapList list, int off, int len) throws IOException {
		int index = list.size();
		byte[] buf = list.prepareAddBuffer(index, 0);
		ostream.write(buf, off, len);
	}

	/**
	 * Read specified number of chars from Reader into CharGapList.
	 * 
	 * @param reader	reader (source)
	 * @param list		list (target)
	 * @param len		maximum number of bytes to read
	 * @return			number of bytes read into the buffer, -1 if end of stream has been reached
	 */
	protected static int read(Reader reader, CharGapList list, int len) throws IOException {
		int index = list.size();
		char[] buf = list.prepareAddBuffer(index, len);

		int read = reader.read(buf, index, len);
		if (read == -1) {
			read = 0;
		}
		list.releaseAddBuffer(index, (read >= -0) ? read : 0);
		return read;
	}

	/**
	 * Write specified number of chars from CharGapList into Writer.
	 * 
	 * @param writer	writer (target)
	 * @param list		list (source)
	 * @param off		offset of first char to write
	 * @param len		number of chars to write
	 */
	protected static void write(Writer writer, CharGapList list, int off, int len) throws IOException {
		int index = list.size();
		char[] buf = list.prepareAddBuffer(index, 0);
		writer.write(buf, off, len);
	}

	/**
	 * Add specified number of chars from CharSequence into CharGapList.
	 * 
	 * @param str		CharSequence (source)
	 * @param list		list (target)
	 * @param start		start position of characters to add in CharSequence
	 * @param end		end position of characters to add in CharSequence
	 */
	protected static void add(CharSequence str, CharGapList list, int start, int end) {
		int index = list.size();
		int len = end - start;
		char[] buf = list.prepareAddBuffer(index, len);
		for (int i = 0; i < len; i++) {
			buf[index + i] = str.charAt(start + i);
		}
	}

}
