/*
 * Copyright 2023 by Thomas Mauch
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

/**
 * Interface {@link IListable} offers a minimalist interface for accessing a list or an array.
 *
 * @author Thomas Mauch
 */
public interface IListable<E> {

	/** Return size of list */
	int size();

	/** Return element at specified position */
	E get(int index);
}
