package net.coderodde.util.experimental;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import static junit.framework.Assert.fail;
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
        list.add(2, 3); // 0, 1, 3, 2
        
        assertEquals(0, (int) list.get(0));
        assertEquals(1, (int) list.get(1));
        assertEquals(3, (int) list.get(2));
        assertEquals(2, (int) list.get(3));
        
        list.add(2, 10);
        // Here, list = [0, 1, 10, 3, 2]
        assertEquals(10, (int) list.get(2));
    }
    
    @Test
    public void test3() {
        for (int i = 0; i < 4; i++) {
            list.add(i, i);
        }
        
        list.add(1, 10);
        
        assertEquals(0,  (int) list.get(0));
        assertEquals(10, (int) list.get(1));
        assertEquals(1,  (int) list.get(2));
        assertEquals(2,  (int) list.get(3));
        assertEquals(3,  (int) list.get(4));
        // [0, 10, 1, 2, 3]
        list.add(3, -1);
        // [0, 10, 1, -1, 2, 3]
        assertEquals(-1, (int) list.get(3));
        
        list.remove(2);
        // [0, 10, -1, 2, 3]
        assertEquals(-1, (int) list.get(2));
        
        list.remove(0);
        // [10, -1, 2, 3]
        assertEquals(10, (int) list.get(0));
        
        list.remove(0);
        // [-1, 2, 3]
        assertEquals(-1, (int) list.get(0));
        
        list.remove(2);
        // [-1, 2]
        assertEquals(-1, (int) list.get(0));
        assertEquals(2, (int) list.get(1));
        
        list.remove(1);
        assertEquals(-1, (int) list.get(0));
        
        
    }
    
    
    @Test
    public void bruteForceTest() {
        long seed = 1535020683908L;System.currentTimeMillis();
        Random random = new Random(seed);
        
        System.out.println("Seed = " + seed);
        
        List<Integer> javaList = new LinkedList<>();
        LinkedBlockList<Integer> fingerList = new LinkedBlockList<>();
        
        for (int operationNumber = 0; 
                 operationNumber < 10_000; 
                 operationNumber++) {
            int operationCode = random.nextInt(3);
            System.out.print(operationNumber + " ");
            switch (operationCode) {
                // Remove:
                case 0:
                    if (javaList.size() > 0) {
                        System.out.print("remove ");
                        int index = random.nextInt(javaList.size());
                        javaList.remove(index);
                        
                        try {
                            fingerList.remove(index);
                            System.out.println();
                        } catch (Exception ex) {
                            System.out.println(
                                    ex.getMessage() + " on " + 
                                    operationNumber + " in remove()");
                        }
                        
                        if (!equals(javaList, fingerList)) {
                            fail("Failed while removing at index " + index);
                        }
                    } else {
                        System.out.println();
                    }
                    
                    break;
                    
                // Add:
                case 1:
                    
                    if (javaList.size() < 10) {
                        System.out.print("add ");
                        // Do not create large lists, max. 10 elements.
                        Integer integer = random.nextInt(1000);
                        int index = random.nextInt(javaList.size() + 1);
                        javaList.add(index, integer);
                        
                        try {
                            fingerList.add(index, integer);
                            System.out.println();
                        } catch (Exception ex) {
                            System.out.println(
                                    ex.getMessage() + " on " + 
                                    operationNumber + " in add()");
                        }

                        if (!equals(javaList, fingerList)) {
                            fail("Failed while adding at index " + index + 
                                 " value " + integer);
                        }
                    } else {
                        System.out.println();
                    }
                    
                    break;
                    
                // Get:
                case 2:
                    
                    if (javaList.size() > 0) {
                        System.out.println("get "); 
                        int index = random.nextInt(javaList.size());
                        int javaListInt = javaList.get(index);
                        int fingerListInt = fingerList.get(index);

                        if (javaListInt != fingerListInt) {
                            fail("Failed while getting at index " + index +
                                 ", " + javaListInt + " vs. " + fingerListInt);
                        }
                    } else {
                        System.out.println();
                    }
                    
                    break;
            }
        }
    }
    
    private static boolean equals(List<Integer> javaList,
                                  LinkedBlockList<Integer> linkedBlockList) {
        if (javaList.size() != linkedBlockList.size()) {
            return false;
        }
        
        Iterator<Integer> javaListIterator = javaList.iterator();
        int fingerListIndex = 0;
        
        while (javaListIterator.hasNext()) {
            Integer javaListInteger = javaListIterator.next();
            Integer fingerListInteger = linkedBlockList.get(fingerListIndex);
            
            if (!Objects.equals(javaListInteger, fingerListInteger)) {
                return false;
            }
            
            fingerListIndex++;
        }
        
        return true;
    }
}
