/*
 * Copyright 2010 by Thomas Mauch
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
 * $Id: BrowniesException.java 265 2011-01-23 22:48:08Z thmauch $
 */
package org.magicwerk.brownies.collections.exceptions;


/**
 * All exceptions explicitly thrown in the Brownies library are of type BrowniesException.
 * Checked exception are caught and wrapped.
 * 
 * @author Thomas Mauch
 * @version $Id: BrowniesException.java 265 2011-01-23 22:48:08Z thmauch $
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
