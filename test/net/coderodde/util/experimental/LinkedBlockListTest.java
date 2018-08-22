package net.coderodde.util.experimental;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class LinkedBlockListTest {
    
    private LinkedBlockList<Integer> list;
    
    @Before
    public void before() {
        list = new LinkedBlockList<>(4);
    }
    
    @Test
    public void test1() {
        for (int i = 0; i < 5; i++) {
            list.add(i, i);
        }
        
        for (int i = 0; i < 5; i++) {
            assertEquals(i, (int) list.get(i));
        }
    }
    
    @Test
    public void test2() {
        list.add(0, 1);
        list.add(0, 0);
        list.add(2, 2);
        list.add(2, 3);
        
        assertEquals(0, (int) list.get(0));
        assertEquals(1, (int) list.get(1));
        assertEquals(3, (int) list.get(2));
        assertEquals(2, (int) list.get(3));
        
        list.add(2, 10);
        
        assertEquals(10, (int) list.get(2));
        
    }
}
