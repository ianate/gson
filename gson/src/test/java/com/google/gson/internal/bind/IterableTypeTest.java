package com.google.gson.internal.bind;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

class IterableImpl<E> implements Iterable<E>{

	private Object[] elements;
	private int readIndex;
	private int writeIndex;
	
	public IterableImpl(){
		elements = new Object[12];
		readIndex = 0;
		writeIndex = 0;
	}
	
	public void addOne(E element){
		Object[] elements = this.elements;
		if(writeIndex >= elements.length){
			elements = Arrays.copyOf(elements, elements.length * 2);
		}
		elements[writeIndex ++] = element;
		this.elements = elements;
	}
	
	@Override
	public Iterator<E> iterator() {
		readIndex = 0;
		return new Iterator<E>() {
			@Override public boolean hasNext() {
				if(readIndex < elements.length && elements[readIndex] != null){
					return true;
				}
				return false;
			}
			@SuppressWarnings("unchecked")
			@Override public E next() {
				return (E) elements[readIndex ++];
			}
		};
	}
	
}

public class IterableTypeTest extends TestCase{

	public void testList(){
		List<String> list = Arrays.asList("stra","strb","strc");
		Gson gson = new GsonBuilder()
				//.registerTypeAdapterFactory(new IterableTypeAdapterFactory(List.class, "add"))//illegal. interface does not have a default construtor
				.registerTypeAdapterFactory(new IterableTypeAdapterFactory(ArrayList.class, "add"))
				.create();
		String json = gson.toJson(list);
		assertEquals("[\"stra\",\"strb\",\"strc\"]", json);
		Type type = new TypeToken<Iterable<String>>(){}.getType();
		List<String> newList = gson.fromJson(json, type);
		assertNotNull(list);
		assertEquals(list.size(), newList.size());
		assertEquals(list.get(0), newList.get(0));
		assertEquals(list.get(1), newList.get(1));
		assertEquals(list.get(2), newList.get(2));
	}
	
	public void testCustomized(){
		IterableImpl<String> strs = new IterableImpl<String>();
		strs.addOne("stra");
		strs.addOne("strb");
		strs.addOne("strc");
		Gson gson = new GsonBuilder()
				.registerTypeAdapterFactory(new IterableTypeAdapterFactory(IterableImpl.class, "addOne"))
				.create();
		String json = gson.toJson(strs);
		assertEquals("[\"stra\",\"strb\",\"strc\"]", json);
		Type type = new TypeToken<Iterable<String>>(){}.getType();
		IterableImpl<String> newStrs = gson.fromJson(json, type);
		assertNotNull(newStrs);
		int i = 0;
		Iterator<?> iter = strs.iterator();
		while(iter.hasNext()){
			String newStr = null;
			Iterator<?> newIter = newStrs.iterator();
			int j = 0;
			do{
				newStr = (String) newIter.next();
			}while(j++ < i);
			String str = (String) iter.next();
			assertEquals(str, newStr);
			i ++;
		}
	}
	
	public void testDifference(){//#issue 1
		if(Collection.class.isAssignableFrom(Exception.class)){
			;
		}
		SQLException se1 = new SQLException("first");
		SQLException se2 = new SQLException("second");
		SQLException se3 = new SQLException("third");
		se1.setNextException(se2);
		se2.setNextException(se3);
		String defaultJson = new Gson().toJson(se1);
		
		for(Throwable t : se1){
			System.out.println(t);
		}
		
		Gson gson = new GsonBuilder()
				.registerTypeAdapterFactory(new IterableTypeAdapterFactory(SQLException.class, "setNextException"))
				.create();
		String json = gson.toJson(se1);
		assertEquals("", defaultJson);
	}
	
}
