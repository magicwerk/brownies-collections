package org.magicwerk.brownies.collections.animation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tools for java.util.stream.
 *
 * @author Thomas Mauch
 */
public class StreamTools {

	public static <T> T getValue(Stream<? super T> stream, Class<T> clazz) {
		return (T) getValue(stream);
	}

	public static <T> T getValueIf(Stream<? super T> stream, Class<T> clazz) {
		return (T) getValueIf(stream);
	}

	public static <T> T getValue(Stream<T> stream) {
		return stream.findFirst().get();
	}

	public static <T> T getValueIf(Stream<T> stream) {
		Optional<T> optional = stream.findFirst();
		if (optional.isPresent()) {
			return optional.get();
		} else {
			return null;
		}
	}

	public static <T> List<T> getList(Stream<T> stream) {
		return stream.collect(Collectors.toList());
	}

	public static <T> List<T> getList(Stream<? super T> stream, Class<T> clazz) {
		return (List<T>) getList(stream);
	}

}
