package ch.seto.vikdal.java;

import java.util.HashMap;
import java.util.Map;

public final class DescriptorUtils {
	/**
	 * Returns the value corresponding to key from map, or def if it doesn't exist or the map is null.
	 * @throws ClassCastException if the type of def doesn't match the type of value
	 * @param map a map
	 * @param key the key
	 * @param def default value, if key is not found
	 * @return map.get(key) if it exists, def otherwise
	 */
	@SuppressWarnings("unchecked")
	public static <V> V valueForKey(Map<String, Object> map, String key, V def) {
		if (map == null || !map.containsKey(key)) {
			return def;
		}
		return (V) map.get(key);
	}
	
	/**
	 * Convenience flag map constructor, all arguments are inserted into a newly created map in pairs.
	 * @param pairs an interleaved list of flag-value pairs (String/Object)
	 * @return a new HashMap containing the flags
	 * @throws ClassCastException if one of the flags is not of type String
	 */
	public static Map<String, Object> flagList(Object... pairs) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 1; i < pairs.length; i += 2) {
			map.put((String) pairs[i - 1], pairs[i]);
		}
		return map;
	}
	
	// Deny instantiation of this class
	private DescriptorUtils() {
	}
}
