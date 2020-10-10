import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Cuckoo hash map implementation.
 * @author Patrick Parker
 * @version Nov 27, 2016
 * @param <K> Class variable for keys.
 * @param <V> Class variable for values.
 */
public class CuckooHashMap<K, V> implements Map<K, V>
{
    
    private ArrayList<MapEntry> data;    
    private int size;    
    private int buckets;
    private int modCount;
    private Set<java.util.Map.Entry<K, V>> entrySet = null;
    private Set<K> keys = null;
    private Collection<V> values = null;
    
    
    /**
     * Constructor for cuckoo hash map.
     * @param numBuckets The number of buckets.
     */
    public CuckooHashMap(int numBuckets)
    {
        data = new ArrayList<MapEntry>(numBuckets);
        size = 0;
        modCount = 0;
        buckets = numBuckets;
        for (int i = 0; i < numBuckets; i++ )
        {
            data.add(null);
        }
        data.trimToSize();
    }
    
    /** 
     * @see java.util.Map#clear()
     */
    public void clear()
    {
        
        for (int i = 0; i < buckets; i++)
        {
            data.set(i, null);
        }
        size = 0;
        modCount = 0;
    }
    
    /** 
     * @see java.util.Map#isEmpty()
     * @return True if empty, else false.
     */
    public boolean isEmpty()
    {        
        return size == 0;
    }

    /** 
     * @see java.util.Map#size()
     * @return The size of the map.
     */
    public int size()
    {
        return size;
    }
    
    /** 
     * @see java.util.Map#get(java.lang.Object)
     * @param key The key.
     * @return The value.
     */
    @SuppressWarnings("unchecked")
    public V get(Object key)
    {
        K key1 = (K) key;
        MapEntry e1 = getEntry(h1(key1));
        MapEntry e2 = getEntry(h2(key1));
        
        if (e1 != null && e1.getKey().hashCode() == key1.hashCode())
        {
            return e1.getValue();
        }
        if (e2 != null && e2.getKey().hashCode() == key1.hashCode())
        {
            return e2.getValue();
        }
        return null;
    }
    
    /**
     * Returns entry at index.
     * @param index The index.
     * @return The entry.
     */
    public MapEntry getEntry(int index)
    {
        return data.get(index);
    }

    /** 
     * @see java.util.Map#containsKey(java.lang.Object)
     * @param key The key.
     * @return True if present, else false.
     */
    public boolean containsKey(Object key)
    {
        V value = get(key);
        return value != null;
    }

    /** 
     * @see java.util.Map#containsValue(java.lang.Object)
     * @param value The value.
     * @return True if present, else false.
     */
    public boolean containsValue(Object value)
    {
        for (MapEntry e : data)
        {
            if (e != null && e.getValue().equals(value))
            {
                return true;
            }
        }
        return false;
    }

    /** 
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     * @param key The key.
     * @param value The value.
     * @return The value replaced or null.
     */
    public V put(K key, V value)
    {
        MapEntry e = new MapEntry(key, value);
        int p1 = h1(key);
        int p2 = h2(key);
        if (data.get(p1) != null && 
                data.get(p1).getKey().hashCode() == key.hashCode())
        {            
            return putHelper(p1, value);            
        }
        if (data.get(p2) != null &&
                data.get(p2).getKey().hashCode() == key.hashCode())
        {
            return putHelper(p2, value);
        }
        int pos = h1(key);        
        for (int i = 0; i < buckets; i++)
        {
            if (data.get(pos) == null)
            {
                data.set(pos, e);
                size++;
                modCount++;
                return null;
            }
            MapEntry temp = data.get(pos);
            data.set(pos, e);
            e = temp;
            pos = (pos == h1(e.getKey())) ? h2(e.getKey()) : h1(e.getKey());
        }        
        resize();
        put(e.getKey(), e.getValue());
        return null;
    }
    
    /**
     * Helper to overwrite entry in case key already exists. 
     * @param index Index of entry
     * @param newValue The new value.
     * @return The old value.
     */
    private V putHelper(int index, V newValue)
    {
        MapEntry entry = data.get(index);
        V oldValue = entry.getValue();
        entry.setValue(newValue);
        data.set(index, entry);
        modCount++;
        return oldValue;
    }
    
    /** 
     * @see java.util.Map#remove(java.lang.Object)
     * @param key The key.
     * @return The value that was removed.
     */
    @SuppressWarnings("unchecked")
    public V remove(Object key)
    {
        K key1 = (K) key;
        V value = get(key);
        int pos1 = h1(key1);
        int pos2 = h2(key1);
        MapEntry e1 = getEntry(pos1);
        MapEntry e2 = getEntry(pos2);
        if (e1 != null && key1.hashCode() == e1.getKey().hashCode())
        {            
            data.set(pos1, null);
            modCount++;
            size--;
        }
        else if (e2 != null && key1.hashCode() == e2.getKey().hashCode())
        {            
            data.set(pos2, null);
            modCount++;
            size--;
        }        
        return value;
    }

    /** 
     * @see java.util.Map#putAll(java.util.Map)
     * @param map The map.
     */
    @SuppressWarnings("unchecked")
    public void putAll(Map<? extends K, ? extends V> map)
    {
        Set<?> set =
                map.entrySet();
        for (Object e : set)
        {            
            Entry<K, V> f = (Entry<K, V>) e;
            put(f.getKey(), f.getValue());
        }
    }
    
    /**
     * Base class for Entry, value, and key
     * set view iterators.
     * @author Patrick Parker
     * @version Nov 27, 2016
     * @param <E> The class.
     */
    private abstract class CuckooHashIterator<E> implements Iterator<E>
    {
        /** Next entry */
        MapEntry next;
        /** For detecting concurrent modification */
        int expectedModCount;
        /** The current index */
        int index;    
        
        /**
         * Constructor for iterator.
         */
        CuckooHashIterator()
        {
            expectedModCount = modCount;
            index = 0;
            if (size > 0)
            {
                ArrayList<MapEntry> table = data;
                while (table.get(index) == null)
                {
                    index++;                    
                }
                next = table.get(index);                
            }
        }
        
        /** 
         * @see java.util.Iterator#hasNext()
         * @return True if next exists, else false.
         */
        public final boolean hasNext()
        {
            return next != null;
        }
        
        /**
         * Gets next MapEntry.
         * @return Gets next MapEntry.
         */
        final MapEntry nextEntry()
        {
            if (expectedModCount != modCount)
            {
                throw new ConcurrentModificationException();
            }
            MapEntry e = next;
            if (e == null)
            {
                throw new NoSuchElementException();                
            }
            ArrayList<MapEntry> table = data;
            while (index < table.size())
            {
                if (index == (table.size() - 1)) 
                {
                    next = null;
                    break;
                }
                if (table.get(++index) != null)                
                {
                    next = table.get(index);
                    break;
                }
                
            }
            return e;
        }
        /** 
         * @see java.util.Iterator#remove()
         */
        public final void remove()
        {
            throw new UnsupportedOperationException();            
        }
        
    }
    /**
     * Value set iterator class.
     * @author Patrick Parker
     * @version Nov 27, 2016
     *
     */
    private final class ValueIterator extends CuckooHashIterator<V>
    {
        /** 
         * @see java.util.Iterator#next()
         * @return Next value.
         */
        public V next()
        {
            return nextEntry().value;
        }
    }
    
    /**
     * Key set iterator class.
     * @author Patrick Parker
     * @version Nov 27, 2016
     *
     */
    private final class KeyIterator extends CuckooHashIterator<K>
    {
        /** 
         * @see java.util.Iterator#next()
         * @return Next key.
         */
        public K next()
        {
            return nextEntry().getKey();
        }
    }
    
    /**
     * Entry set iterator class.
     * @author Patrick Parker
     * @version Nov 27, 2016
     *
     */
    private final class MapEntryIterator 
        extends CuckooHashIterator<java.util.Map.Entry<K, V>>
    {
        /** 
         * @see java.util.Iterator#next()
         * @return Next entry.
         */
        public CuckooHashMap<K, V>.MapEntry next()
        {
            return nextEntry();
        }
    }

    /** 
     * @see java.util.Map#entrySet()
     * @return Set of entries.
     */
    public Set<java.util.Map.Entry<K, V>> entrySet()
    {
        Set<java.util.Map.Entry<K, V>> set = entrySet;
        if (set != null)
        {
            return set;
        }
        entrySet = new MapEntrySet();
        return  entrySet;
    }
    
    /**
     * Inner class for Entry set view.
     * @author Patrick Parker
     * @version Nov 27, 2016
     *
     */
    private final class MapEntrySet 
        extends AbstractSet<java.util.Map.Entry<K, V>>
    {

        /** 
         * @see java.util.AbstractCollection#iterator()
         * @return Iterator over entries.
         */
        @Override
        public Iterator<java.util.Map.Entry<K, V>> iterator() 
        {           
            return new MapEntryIterator();
        }

        /** 
         * @see java.util.AbstractCollection#size()
         * @return The current size.
         */
        @Override
        public int size() 
        {
            return size;
        }
        
        /** 
         * @see java.util.AbstractCollection#clear()
         */
        public void clear()
        {
            CuckooHashMap.this.clear();
        }
        
        /** 
         * @see java.util.AbstractCollection#contains(java.lang.Object)
         * @param o The object.
         * @return True if present, else false.
         */
        public boolean contains(Object o)
        {
            if (!(o instanceof Map.Entry<?, ?>))
            {
                return false;
            }
            @SuppressWarnings("unchecked")
            Map.Entry<K, V> e = (Map.Entry<K, V>) o;
            V value = e.getValue();
            return containsKey(e.getKey()) &&
                value.equals(get(e.getKey()));
        }
        /** 
         * @see java.util.AbstractCollection#containsAll(java.util.Collection)
         * @param c The Collection.
         * @return True if successful, else false.
         */
        public boolean containsAll(Collection<?> c)
        {
            for (Object e : c)
            {
                if (!contains(e))
                {
                    return false;
                }
            }
            return true;
        }
        
        /** 
         * @see java.util.AbstractCollection#remove(java.lang.Object)
         * @param o The object.
         * @return True if successful, else false.
         */
        public boolean remove(Object o) 
        {
            throw new UnsupportedOperationException();
        }
        /** 
         * @see java.util.AbstractSet#removeAll(java.util.Collection)
         * @param c Unsupported
         * @return unsupported
         */
        public boolean removeAll(Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }
        /** 
         * @see java.util.AbstractCollection#add(java.lang.Object)
         * @param e Unsupported.
         * @return unsupported.
         */
        public boolean add(java.util.Map.Entry<K, V> e)
        {
            throw new UnsupportedOperationException();
        }
        /** 
         * @see java.util.AbstractCollection#addAll(java.util.Collection)
         * @param c Unsupported
         * @return unsupported
         */
        public boolean addAll(Collection<? extends java.util.Map.Entry<K, V>> c)
        {
            throw new UnsupportedOperationException();
        }
        /**
         * @see java.util.AbstractCollection#retainAll(java.util.Collection)
         * @param c Unsupported
         * @return unsupported
         */
        public boolean retainAll(Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }
        
    }

    /** 
     * @see java.util.Map#keySet()
     * @return The set view of keys.
     */
    public Set<K> keySet()
    {
        Set<K> set = keys;
        if (set != null)
        {
            return set;
        }
        keys = new KeySet();
        return keys;
    }
    
    /**
     * Inner class for KeySet view.
     * @author Patrick Parker
     * @version Nov 27, 2016
     *
     */
    private final class KeySet extends AbstractSet<K> 
    {
        /** 
         * @see java.util.AbstractCollection#iterator()
         * @return The iterator.
         */
        public Iterator<K> iterator() 
        {
            return new KeyIterator();
        }
        /** 
         * @see java.util.AbstractCollection#size()
         * @return The current size.
         */
        public int size() 
        {
            return size;
        }
        /** 
         * @see java.util.AbstractCollection#contains(java.lang.Object)
         * @param o The object.
         * @return True if present, else false.
         */
        public boolean contains(Object o) 
        {
            return containsKey(o);
        }
        /** 
         * @see java.util.AbstractCollection#containsAll(java.util.Collection)
         * @param c The Collection.
         * @return True if successful, else false.
         */
        public boolean containsAll(Collection<?> c)
        {
            for (Object e : c)
            {
                if (!contains(e))
                {
                    return false;
                }
            }
            return true;
        }
        /** 
         * @see java.util.AbstractCollection#remove(java.lang.Object)
         * @param o The object.
         * @return True if successful, else false.
         */
        public boolean remove(Object o) 
        {
            throw new UnsupportedOperationException();
        }
        /** 
         * @see java.util.AbstractCollection#clear()
         */
        public void clear() 
        {
            CuckooHashMap.this.clear();
        }
        /** 
         * @see java.util.AbstractSet#removeAll(java.util.Collection)
         * @param c Unsupported
         * @return unsupported
         */
        public boolean removeAll(Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }
        /** 
         * @see java.util.AbstractCollection#add(java.lang.Object)
         * @param k Unsupported.
         * @return unsupported.
         */
        public boolean add(K k)
        {
            throw new UnsupportedOperationException();
        }
        /** 
         * @see java.util.AbstractCollection#addAll(java.util.Collection)
         * @param c Unsupported
         * @return unsupported
         */
        public boolean addAll(Collection<? extends K> c)
        {
            throw new UnsupportedOperationException();
        }
        /**
         * @see java.util.AbstractCollection#retainAll(java.util.Collection)
         * @param c Unsupported
         * @return unsupported
         */
        public boolean retainAll(Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }
    }

    /** 
     * @see java.util.Map#values()
     * @return Collection of values.
     */
    public Collection<V> values()
    {
        Collection<V> v = values;
        if (v != null)
        {
            return v;
        }
        values = new Values();
        return values;
    }
    
    /**
     * Inner class for values view.
     * @author Patrick Parker
     * @version Nov 27, 2016
     *
     */
    private final class Values extends AbstractCollection<V> 
    {
        /** 
         * @see java.util.AbstractCollection#iterator()
         * @return Iterator over values.
         */
        public Iterator<V> iterator() 
        {
            return new ValueIterator();
        }
        /** 
         * @see java.util.AbstractCollection#size()
         * @return The current size.
         */
        public int size() 
        {
            return size;
        }
        /** 
         * @see java.util.AbstractCollection#contains(java.lang.Object)
         * @param o The object.
         * @return True if present, else false.
         */
        public boolean contains(Object o) 
        {
            return containsValue(o);
        }
        /** 
         * @see java.util.AbstractCollection#containsAll(java.util.Collection)
         * @param c The Collection.
         * @return True if successful, else false.
         */
        public boolean containsAll(Collection<?> c)
        {
            for (Object e : c)
            {
                if (!contains(e))
                {
                    return false;
                }
            }
            return true;
        }
        /** 
         * @see java.util.AbstractCollection#clear()
         */
        public void clear() 
        {
            CuckooHashMap.this.clear();
        }
        /** 
         * @see java.util.AbstractCollection#remove(java.lang.Object)
         * @param o The object.
         * @return True if successful, else false.
         */
        public boolean remove(Object o) 
        {
            throw new UnsupportedOperationException();
        }
        /** 
         * @see java.util.AbstractCollection#removeAll(java.util.Collection)
         * @param c Unsupported
         * @return unsupported
         */
        public boolean removeAll(Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }
        /** 
         * @see java.util.AbstractCollection#add(java.lang.Object)
         * @param v Unsupported.
         * @return unsupported.
         */
        public boolean add(V v)
        {
            throw new UnsupportedOperationException();
        }
        /** 
         * @see java.util.AbstractCollection#addAll(java.util.Collection)
         * @param c Unsupported
         * @return unsupported
         */
        public boolean addAll(Collection<? extends V> c)
        {
            throw new UnsupportedOperationException();
        }
        /**
         * @see java.util.AbstractCollection#retainAll(java.util.Collection)
         * @param c Unsupported
         * @return unsupported
         */
        public boolean retainAll(Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }
    }
    /**
     * First hash function.
     * @param key
     * @return The index of bucket.
     */
    private int h1(K key)
    {
        return (Math.abs(key.hashCode()) % buckets);
    }
    
    /**
     * Second hash function.
     * @param key
     * @return The index of bucket.
     */
    private int h2(K key)
    {
        return (((h1(key)) + 3) % buckets);
    }
    
    /**
     * Resizes ArrayList.
     */
    private void resize()
    {        
        ArrayList<MapEntry> list = new ArrayList<MapEntry>();
        for (MapEntry e : data)
        {
            if (e != null)
            {
                list.add(e);
            }
        }
        buckets *= 2;
        data.clear();
        size = 0;
        data.ensureCapacity(buckets);
        for (int i = 0; i < buckets; i++)
        {
            data.add(null);
        }
        data.trimToSize();
        for (MapEntry e : list)
        {
            put(e.getKey(), e.getValue());
        }
        
    }
    
    /**
     * Inner class for entries.
     * @author Patrick Parker
     * @version Nov 27, 2016
     *
     */
    public class MapEntry implements Entry<K, V>
    {
        private K key;
        private V value;
 
        /**
         * Creates a MapEntry.
         * 
         * @param akey the key
         * @param avalue the value
         */
        public MapEntry(K akey, V avalue)
        {
            this.key = akey;
            this.value = avalue;
        }

        /**
         * Returns the key for this entry.
         * 
         * @see java.util.Map$Entry#getKey()
         * @return the key for this entry
         */
        public K getKey()
        {
            return key;
        }

        /**
         * Returns the value for this entry.
         * 
         * @see java.util.Map$Entry#getValue()
         * @return the value for this entry
         */
        public V getValue()
        {
            return value;
        }

        /**
         * Sets the value for this entry.
         * 
         * @see java.util.Map$Entry#setValue(V)
         * @param newValue New value.
         * @return the previous value for this entry.
         */
        public V setValue(V newValue)
        {
            V oldVal = value;
            value = newValue;
            return oldVal;
        }
    }
}
