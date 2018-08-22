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
}
