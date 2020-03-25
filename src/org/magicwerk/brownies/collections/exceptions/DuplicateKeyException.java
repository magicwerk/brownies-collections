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
 * Exception thrown if an action is not allowed due to a duplicate key error.
 *  
 * @author Thomas Mauch
 * @version $Id$
 */
@SuppressWarnings("serial")
public class DuplicateKeyException extends KeyException {

	public static final String MESSAGE = "Constraint violation: duplicate key not allowed";

	/** Key which is not allowed due to a duplicate */
	Object key;
	/** If false, the exception will not contain a stack trace which makes raising the exception faster */
	boolean needsStackTrace;

	//

	public DuplicateKeyException(Object key, boolean needsStackTrace) {
		super(MESSAGE + ": " + key);

		this.key = key;
		this.needsStackTrace = needsStackTrace;
	}

	public Object getKey() {
		return key;
	}

	@Override
	public Throwable fillInStackTrace() {
		if (needsStackTrace) {
			return super.fillInStackTrace();
		} else {
			return this;
		}
	}

}
