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
package org.magicwerk.brownies.collections.exceptions;

/**
 * All exceptions thrown in KeyCollection/KeyList implementations are of type KeyException.
 *
 * @author Thomas Mauch
 */
@SuppressWarnings("serial")
public class KeyException extends RuntimeException {

	public KeyException() {
	}

	public KeyException(String msg) {
		super(msg);
	}

	public KeyException(Throwable t) {
		super(t);
	}

	public KeyException(String msg, Throwable t) {
		super(msg, t);
	}

}
