import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test class for CuckooHashMap class.
 * @author Patrick Parker
 * @version Nov 27, 2016
 *
 */
public class CuckooHashMapTest extends TestCase
{
   
    private CuckooHashMap<String, String> map;
    private Map<String, String> map1;
    private Map<String, String> map2;
    private Collection<String> c;
    
    /** Set up */
    public void setUp()
    {
        map = new CuckooHashMap<String, String>(8);
        assertTrue(map.isEmpty());
        map.put("Bus", "Stop");
        map.put("Key", "Door");
        map.put("Pen", "Paper");
        map.put("Glue", "Sticky");
        map.put("Water", "Wet");
        map.put("Rock", "Hard");
        map.put("Air", "Light");
        map.put("Salt", "Rock");
        
        map1 = new HashMap<String, String>();
        map1.put("Bus", "Stop");
        map1.put("Key", "Door");
        map1.put("Pen", "Paper");
        map1.put("Glue", "Sticky");
        map1.put("Water", "Wet");
        map1.put("Rock", "Hard");
        map1.put("Air", "Light");
        map1.put("Salt", "Rock");
        
        map2 = new HashMap<String, String>();
        map2.put("Bus", "Key");
        map2.put("Key", "Pen");
        map2.put("Pen", "Glue");
        map2.put("Glue", "Water");
        map2.put("Water", "Rock");
        map2.put("Rock", "Air");
        map2.put("Air", "Salt");
        map2.put("Rock", "Rock");
        
        c = new ArrayList<String>();
        c.add("Testing");
        c.add("Not");
        c.add("Contains");
    }
    
    /** Tests put and get. */
    public void testPutGet()
    {
      
        assertFalse(map.isEmpty());
        assertEquals(8, map.size());
        assertEquals("Door", map.get("Key"));
        assertEquals("Stop", map.get("Bus"));
        assertEquals("Paper", map.get("Pen"));
        assertEquals("Sticky", map.get("Glue"));
        assertEquals("Wet", map.get("Water"));
        assertEquals("Hard", map.get("Rock"));
        assertEquals("Light", map.get("Air"));
        assertEquals("Rock", map.get("Salt"));
       
        assertEquals("Door", map.put("Key", "Door"));
        assertEquals("Stop", map.put("Bus", "Stop"));
        assertEquals("Paper", map.put("Pen", "Paper"));
        assertEquals("Sticky", map.put("Glue", "Sticky"));
        assertEquals("Wet", map.put("Water", "Wet"));
        assertEquals("Hard", map.put("Rock", "Hard"));
        assertEquals("Light", map.put("Air", "Light"));
        assertEquals("Rock", map.put("Salt", "Rock"));
    
    }
    /** Tests containsKey and containsValue. */
    public void testContainsKeyValue()
    {
        assertTrue(map.containsKey("Key"));
        assertTrue(map.containsKey("Bus"));
        assertTrue(map.containsKey("Pen"));
        assertTrue(map.containsKey("Glue"));
        assertTrue(map.containsKey("Water"));
        assertTrue(map.containsKey("Rock"));
        assertTrue(map.containsKey("Air"));
        assertTrue(map.containsKey("Salt"));
        
        assertTrue(map.containsValue("Door"));
        assertTrue(map.containsValue("Stop"));
        assertTrue(map.containsValue("Paper"));
        assertTrue(map.containsValue("Sticky"));
        assertTrue(map.containsValue("Wet"));
        assertTrue(map.containsValue("Hard"));
        assertTrue(map.containsValue("Light"));
        assertTrue(map.containsValue("Rock"));
        assertFalse(map.containsValue("Hammer"));
    }
    /** Tests clear and putAll. */
    public void testClearPutAll()
    {
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());        
        map.putAll(map1);
        assertFalse(map.isEmpty());
        assertEquals(8, map.size());
        assertEquals("Door", map.get("Key"));
        assertEquals("Stop", map.get("Bus"));
        assertEquals("Paper", map.get("Pen"));
        assertEquals("Sticky", map.get("Glue"));
        assertEquals("Wet", map.get("Water"));
        assertEquals("Hard", map.get("Rock"));
        assertEquals("Light", map.get("Air"));
        assertEquals("Rock", map.get("Salt"));
        
    }
    /** Tests remove. */
    public void testRemove()
    {
        map.remove("Key");
        assertEquals(7, map.size());
        assertFalse(map.containsKey("Key"));
        assertFalse(map.containsValue("Door"));
        map.remove("Pen");
        map.remove("Glue");
        map.remove("Water");
        map.remove("Rock");
        map.remove("Bus");
        map.remove("Air");
        Set<Map.Entry<String, String>> set = map.entrySet();
        Iterator<Map.Entry<String, String>> itr =
            set.iterator();
        map.remove("Salt");
        assertTrue(map.isEmpty());
        map.put("Bus", "Stop");
        map.put("Key", "Door");
        map.put("Pen", "Paper");
        map.put("Glue", "Sticky");
        map.put("Water", "Wet");
        map.put("Rock", "Hard");
        
        assertNull(map.remove("Well"));
        assertNull(map.remove("Kite"));
        assertNull(map.remove("Orange"));
        assertNull(map.remove("Yellow"));
        assertNull(map.remove("Brown"));
        assertNull(map.remove("Pimk"));
        assertNull(map.remove("Bubble"));
        assertNull(map.remove("Purple"));
        assertEquals(6, map.size());
        
        
    }
    /** Tests entrySet. */
    public void testEntrySet()
    {
        
        
        Set<Map.Entry<String, String>> set2 = map1.entrySet();
        
        
        Set<Map.Entry<String, String>> set = map.entrySet();
        set = map.entrySet();
        Iterator<Map.Entry<String, String>> itr = set.iterator();
        assertEquals(8, set.size());
        
        assertTrue(set.containsAll(set2));
        assertFalse(set.containsAll(c));
        
        while (itr.hasNext())
        {
            Map.Entry<String, String> e = itr.next();
            assertTrue(map.containsKey(e.getKey()));
            assertTrue(map.containsValue(e.getValue()));
        }
        assertFalse(itr.hasNext());
        
        try
        {
            itr.next();
            fail();
        }
        catch (NoSuchElementException e)
        {
            assertTrue(true);
        }
        
        itr = set.iterator();
        map.remove("Rock");
        
        try
        {
            itr.next();
            fail();
        }
        catch (ConcurrentModificationException e)
        {
            assertTrue(true);
        }
        
        try
        {
            itr.remove();
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        
        try
        {
            set.remove(null);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        try
        {
            set.removeAll(set2);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        try
        {
            set.retainAll(set2);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        try
        {
            set.add(null);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        try
        {
            set.addAll(null);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        set.clear();
    }
    /** Tests keySet. */
    public void testKeySet()
    {
        Set<String> set = map.keySet();
        set = map.keySet();
        assertFalse(set.contains("Something"));
        Set<String> set2 = map1.keySet();
        Iterator<String> itr = set.iterator();
        assertEquals(8, set.size());
        
        assertTrue(set.containsAll(set2));
        
        assertFalse(set.containsAll(c));
        
        while (itr.hasNext())
        {            
            assertTrue(map.containsKey(itr.next()));            
        }
        
        try
        {
            itr.next();
            fail();
        }
        catch (NoSuchElementException e)
        {
            assertTrue(true);
        }       
        try
        {
            itr.remove();
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        
        try
        {
            set.remove(null);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        try
        {
            set.removeAll(set2);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        try
        {
            set.retainAll(set2);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        try
        {
            set.add(null);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        try
        {
            set.addAll(null);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        set.clear();
        
    }
    /** Tests values view of map. */
    public void testValues()
    {
        Collection<String> set = map.values();
        set = map.values();
        Collection<String> set2 = map1.values();
        Iterator<String> itr = set.iterator();
        assertEquals(8, set.size());
        
        assertTrue(set.containsAll(set2));
        
        assertFalse(set.containsAll(c));
        
        while (itr.hasNext())
        {            
            assertTrue(map.containsValue(itr.next()));            
        }        
        try
        {
            itr.remove();
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        
        try
        {
            set.remove(null);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        try
        {
            set.removeAll(set2);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        try
        {
            set.retainAll(set2);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        try
        {
            set.add(null);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        try
        {
            set.addAll(null);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue(true);
        }
        set.clear();
        itr = set.iterator();
    }
    /** Tests contains more thoroughly on views. */
    public void testContains()
    {
        Set<Map.Entry<String, String>> set = map.entrySet();
        assertFalse(set.contains("Something"));
        Iterator<Entry<String, String>> itr = set.iterator();
        Map.Entry<String, String> entry = itr.next();
        assertTrue(set.contains(entry));
        Set<Map.Entry<String, String>> set2 = map2.entrySet();
        int count = 0;
        while (itr.hasNext())
        {
            itr.next();
            count++;
        }
        assertEquals(7, count);
        
        for (Map.Entry<String, String> e : set2)
        {
            assertFalse(set.contains(e));
        }
    }
}
