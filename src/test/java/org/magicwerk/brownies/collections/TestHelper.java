package org.magicwerk.brownies.collections;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.magicwerk.brownies.core.ObjectTools;

/**
 * Helper classes for tests.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class TestHelper {

	/**
	 * Comparable name.
	 */
	static class ComparableName extends Name implements Comparable<Name> {

		public ComparableName(String name) {
			super(name);
		}

		public ComparableName(String name, int value) {
			super(name, value);
		}

		@Override
		public int compareTo(Name o) {
			return name.compareTo(o.name);
		}
	}

	/**
	 * Name (non comparable).
	 */
	static class Name {

		static Function<Name, String> Mapper = new Function<Name, String>() {
			@Override
			public String apply(Name v) {
				return v.name;
			}
		};

		static Comparator<Name> Comparator = new Comparator<Name>() {
			@Override
			public int compare(Name name1, Name name2) {
				return name1.name.compareTo(name2.name);
			}
		};

		public String name;
		public int value;

		public Name(String name) {
			this.name = name;
		}

		public Name(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		/**
		 * Change name for testing invalidation.
		 *
		 * @param name
		 */
		void setName(String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Name other = (Name) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			//return name;
			return name + " (" + value + ")";
		}

	}

	@SuppressWarnings("serial")
	public static class TicketSerializable extends Ticket implements Serializable {

		public TicketSerializable(Integer id, String extId, String text) {
			super(id, extId, text);
		}

	}

	public static class Ticket implements Comparable<Ticket> {
		static Function<Ticket, Integer> IdMapper = new Function<Ticket, Integer>() {
			@Override
			public Integer apply(Ticket ticket) {
				return ticket.id;
			}
		};
		static Function<Ticket, String> ExtIdMapper = new Function<Ticket, String>() {
			@Override
			public String apply(Ticket ticket) {
				return ticket.extId;
			}
		};
		static Predicate<Ticket> Constraint = new Predicate<Ticket>() {
			@Override
			public boolean test(Ticket ticket) {
				return ticket.extId == null || ticket.extId.startsWith("Ext");
			}
		};
		static Comparator<Ticket> Comparator = new Comparator<Ticket>() {
			@Override
			public int compare(Ticket t1, Ticket t2) {
				return ObjectTools.compareFields(t1.id, t2.id, t1.extId, t2.extId);
			}
		};

		Integer id;
		String extId;
		String text;

		public Ticket(Integer id, String extId, String text) {
			this.id = id;
			this.extId = extId;
			this.text = text;
		}

		public Integer getId() {
			return id;
		}

		public String getExtId() {
			return extId;
		}

		@Override
		public String toString() {
			return "Ticket [id=" + id + ", extId=" + extId + ", text=" + text + "]";
		}

		@Override
		public int compareTo(Ticket that) {
			return ObjectTools.compareFields(this.id, that.id, this.extId, that.extId, this.text, that.text);
		}

		@Override
		public int hashCode() {
			return ObjectTools.hashCode(id, extId, text);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Ticket) {
				Ticket that = (Ticket) obj;
				return ObjectTools.equalsObjects(this, that, this.id, that.id, this.extId, that.extId, this.text, that.text);
			} else {
				return false;
			}
		}

	}

	/**
	 * Checks that lists are equal. If they are not, an assertion is thrown.
	 *
	 * @param l1 first list
	 * @param l2 second list
	 */
	static <T> void checkEquals(List<T> l1, List<T> l2) {
		if (!l1.equals(l2)) {
			print(l1, l2);
			assert (false);
		}
		Object[] array = l2.toArray();
		for (int i = 0; i < array.length; i++) {
			if (!ObjectTools.equals(array[i], l1.get(i))) {
				assert (false);
			}
		}
	}

	static <T> void print(List<T> l1, List<T> l2) {
		String s1 = l1.getClass().getSimpleName();
		String s2 = l2.getClass().getSimpleName();
		int len = Math.max(s1.length(), s2.length());
		s1 = StringUtils.rightPad(s1, len);
		s2 = StringUtils.rightPad(s2, len);
		System.out.println(s1 + ": " + l1);
		System.out.println(s2 + ": " + l2);
	}

}
