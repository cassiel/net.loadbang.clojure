package net.loadbang.clojure;

import java.util.ArrayList;
import java.util.List;

import net.loadbang.scripting.util.Converters;
import net.loadbang.scripting.util.exn.DataException;

import com.cycling74.max.Atom;

public class ClojureConverters extends Converters {
	private Object objectify(Atom a) throws DataException {
		if (a.isInt()) {
			return a.getInt();
		} else if (a.isFloat()) {
			return a.getFloat();
		} else if (a.isString()) {
			return a.getString();
		} else {
			throw new DataException("objectify: " + a);
		}
	}

	/**	Convert an array of Atoms into a list of objects for Clojure.

		@param args the Atoms
	 	@param start the start position in the Atom array
	 	@return a list of objects
	 	@throws DataException if the Atom cannot be easily represented as a Clojure object
	 */

	@Override
	public List<Object> atomsToObjects(Atom[] args, int start) throws DataException {
		List<Object> result = new ArrayList<Object>(args.length - start);
		
		for (int i = start; i < args.length; i++) {
			result.add(objectify(args[i]));
		}
		
		return result;
	}
}
