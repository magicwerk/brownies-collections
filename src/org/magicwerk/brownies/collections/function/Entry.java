package org.magicwerk.brownies.collections.function;

import org.magicwerk.brownies.collections.GapList;


/**
 * Class for storing a value pair.
 *
 * @author Thomas Mauch
 * @version $Id: Mapper.java 1826 2013-08-15 23:38:15Z origo $
 *
 * @param <E1> type of first element
 * @param <E2> type of second element
 */
public class Entry<E1,E2> {
	public static <E1,E2> Mapper<Entry<E1,E2>, E1> FIRST_MAPPER() {
		return (Mapper<Entry<E1,E2>, E1>)(Object) FIRST_MAPPER;
	}
	public static Mapper<Entry, Object> FIRST_MAPPER = new Mapper<Entry,Object>() {
        public Object getKey(Entry entry) {
            return entry.getFirst();
        }
    };

	public static <E1,E2> Mapper<Entry<E1,E2>, E2> SECOND_MAPPER() {
		return (Mapper<Entry<E1,E2>, E2>)(Object) SECOND_MAPPER;
	}
	public static Mapper<Entry, Object> SECOND_MAPPER = new Mapper<Entry,Object>() {
        public Object getKey(Entry entry) {
            return entry.getSecond();
        }
    };

    private E1 e1;
	private E2 e2;

	public Entry(E1 e1, E2 e2) {
		this.e1 = e1;
		this.e2 = e2;
	}

	public E1 getFirst() {
		return e1;
	}

	public E2 getSecond() {
		return e2;
	}

}
