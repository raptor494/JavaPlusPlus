package jtree.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import jtree.util.LookAheadListIterator;
import lombok.RequiredArgsConstructor;

class TestLookAheadListIterator {

	@Test
	void test1() {
		var iter = new LookAheadListIterator<>(List.of(1,2,3,4,5,6));
		
		assertEquals(1, iter.next());
		assertEquals(2, iter.next());
		assertEquals(3, iter.look(0));
		assertEquals(2, iter.look(-1));
		assertEquals(3, iter.next());
		assertEquals(6, iter.look(2));
		assertEquals(4, iter.next());
		assertEquals(3, iter.look(-2));
		assertEquals(6, iter.look(1));
		assertEquals(5, iter.next());
	}
	
	@Test
	void test2() {
		@RequiredArgsConstructor
		class Element {
			private final String id;
			
			@Override
			public String toString() {
				return id;
			}
		}
		Element obj1 = new Element("obj1"),
				obj2 = new Element("obj2"),
				obj3 = new Element("obj3"),
				obj4 = new Element("obj4"),
				obj5 = new Element("obj5"),
				obj6 = new Element("obj6");
		var iter = new LookAheadListIterator<>(
				List.of(obj1, obj2, obj3, obj4, obj5, obj6)
				.stream()
				.map(obj -> { System.out.println("get " + obj); return obj; })
				.collect(Collectors.toList())
			);
		
		try(var state = iter.enter()) {
		
    		assertEquals(obj1, iter.next());
    		assertEquals(obj2, iter.next());
    		assertEquals(obj3, iter.next());
    		assertEquals(obj4, iter.next());
    		
    		state.reset();    		
		}
		
		assertEquals(obj1, iter.next());
		assertEquals(obj2, iter.look(0));
		assertEquals(obj1, iter.previous());
		assertEquals(obj1, iter.look(0));
		
		assertEquals(obj1, iter.next());
		assertEquals(obj2, iter.next());
		assertEquals(obj3, iter.next());
		
		try(var state = iter.enter()) {
		
    		assertEquals(obj4, iter.next());
    		assertEquals(obj5, iter.next());
		
    		state.reset();
		}
		
		assertEquals(obj4, iter.next());
		assertEquals(obj4, iter.previous());
		assertEquals(obj3, iter.previous());
		assertEquals(obj2, iter.previous());
		assertEquals(obj1, iter.previous());
		assertEquals(obj1, iter.next());
		
		
	}

}
