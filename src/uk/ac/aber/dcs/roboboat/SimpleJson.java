package uk.ac.aber.dcs.roboboat;

import java.lang.reflect.Array;
import java.util.LinkedList;

public class SimpleJson extends LinkedList<Object[]> {
	private static final long serialVersionUID = 6423000595087430835L;

	public void addNode(Object[] node) {
		if(contains(node)) return;
		add(node);
	}
	
	public void addNode(String key, Object val) {
		Object[] node = {key, val};
		addNode(node);
	}
	
	public Object getNode(String key) {
		for(Object[] node : this) {
			if(node[0] == key) return node[1];
		}
		return null;
	}
	
	public void removeNode(String key) {
		for(Object[] node : this) {
			if(node[0] == key) remove(node);
		}
	}

	public static String toList(Object[] values) {
		if(values == null) return "[]";
		String out = "[";
		for(int i=0; i<values.length; i++) {
			if(values[i] == null) continue;
			out += values[i].toString();
			if(i<(values.length-1)) out += ", "; 
		}
		out += "]";
		return out;
	}
	
	public static String toList(float[] values) {
		if(values == null) return "[]";
		String out = "[";
		for(int i=0; i<values.length; i++) {
			out += Float.toString(values[i]);
			if(i<(values.length-1)) out += ", "; 
		}
		out += "]";
		return out;		
	}
	
	public String toString() {
		String out = "{ ";
		
		for(Object[] node : this) {
			out += node[0];
			out += ": ";
			if(node[1].getClass() == String.class) {
				out += "\""+node[1]+"\"";
			} else if(node[1].getClass() == Array.class) {
				//out += toList(node[1]);
			} else {
				out += node[1];
			}
			if(getLast() != node) {
				out += ", ";
			}
		}
		
		out += " }";
		return out;
	}
}


