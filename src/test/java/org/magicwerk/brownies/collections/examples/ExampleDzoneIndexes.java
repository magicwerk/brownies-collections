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
package org.magicwerk.brownies.collections.examples;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.magicwerk.brownies.collections.Key2List;

/**
 * Example for https://dzone.com/articles/making-extremelly-quick-search-in-java-using-right
 */
public class ExampleDzoneIndexes {

	static class UserList extends Key2List<User, Integer, String> {

		NavigableSet<User> userSortedAgeTreeSet = new TreeSet<>(new Comparator<User>() {
			@Override
			public int compare(User o1, User o2) {
				return o1.age - o2.age == 0 ? o1.id - o2.id : o1.age - o2.age;
			}
		});

		public UserList() {
			getBuilder().withPrimaryKey1Map(User::getId).withKey2Map(User::getName)
					.withAfterInsertTrigger(u -> userSortedAgeTreeSet.add(u)).withAfterDeleteTrigger(u -> userSortedAgeTreeSet.remove(u))
					.build();
		}

		public User getUserById(int id) {
			return getByKey1(id);
		}

		public List<User> getUsersByName(String name) {
			return getAllByKey2(name);
		}

		public Collection<User> getUsersByAgeLessThan(int maxAge) {
			return userSortedAgeTreeSet.headSet(new User(Integer.MAX_VALUE, "", maxAge));
		}
	}

	static class User {
		int id;
		String name;
		int age;

		public User(int id, String name, int age) {
			this.id = id;
			this.name = name;
			this.age = age;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		@Override
		public String toString() {
			return "User [id=" + id + ", name=" + name + ", age=" + age + "]";
		}
	}

	//

	public static void main(String[] args) {
		test();
	}

	static void test() {
		User u1 = new User(1, "A", 11);
		User u2 = new User(2, "B", 12);
		User u3 = new User(3, "B", 13);
		User u4 = new User(4, "C", 14);
		UserList userList = new UserList();
		userList.addArray(u1, u2, u3, u4);
		System.out.println(userList);

		{
			User r1 = userList.getUserById(2);
			System.out.println(r1);

			List<User> r2 = userList.getUsersByName("B");
			System.out.println(r2);

			Collection<User> r3 = userList.getUsersByAgeLessThan(12);
			System.out.println(r3);
		}

		userList.remove(u2);
		{
			User r1 = userList.getUserById(2);
			System.out.println(r1);

			List<User> r2 = userList.getUsersByName("B");
			System.out.println(r2);

			Collection<User> r3 = userList.getUsersByAgeLessThan(12);
			System.out.println(r3);
		}
	}

}
