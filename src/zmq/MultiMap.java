/*
    Copyright other contributors as noted in the AUTHORS file.
        
    This file is part of 0MQ.
            
    0MQ is free software; you can redistribute it and/or modify it under
    the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.
            
    0MQ is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.
        
    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package zmq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MultiMap<K extends Comparable<? super K>, V> implements Map<K, V> {

    private long id;
    private final HashMap<Long, V> values;
    private final TreeMap<K, ArrayList<Long>> keys;

    public MultiMap () {
        id = 0;
        values = new HashMap<Long, V>();
        keys = new TreeMap<K, ArrayList<Long>>();
    }
    
    public class MultiMapEntry implements Map.Entry<K, V> {

        private K key;
        private V value;
        public MultiMapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        
        public K getKey() {
            return key;
        }

        
        public V getValue() {
            return value;
        }

        
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
        
    }
    public class MultiMapEntrySet implements Set<Map.Entry<K,V>>, Iterator<Map.Entry<K, V>> {

        private MultiMap<K,V> map;
        private Iterator<Map.Entry<K, ArrayList<Long>>> it;
        private Iterator<Long> iit;
        private K key;
        private long id;
        public MultiMapEntrySet(MultiMap<K,V> map) {
            this.map = map;
        }
        
        public boolean add(java.util.Map.Entry<K, V> arg0) {
            throw new UnsupportedOperationException();
        }

        
        public boolean addAll(
                Collection<? extends java.util.Map.Entry<K, V>> arg0) {
            throw new UnsupportedOperationException();
        }

        
        public void clear() {
            throw new UnsupportedOperationException();
        }

        
        public boolean contains(Object arg0) {
            throw new UnsupportedOperationException();
        }

        
        public boolean containsAll(Collection<?> arg0) {
            throw new UnsupportedOperationException();
        }

        
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        
        public Iterator<java.util.Map.Entry<K, V>> iterator() {
            it = map.keys.entrySet().iterator();
            return this;
        }

        
        public boolean remove(Object arg0) {
            throw new UnsupportedOperationException();
        }

        
        public boolean removeAll(Collection<?> arg0) {
            throw new UnsupportedOperationException();
        }

        
        public boolean retainAll(Collection<?> arg0) {
            throw new UnsupportedOperationException();
        }

        
        public int size() {
            throw new UnsupportedOperationException();
        }

        
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        
        public <T> T[] toArray(T[] arg0) {
            throw new UnsupportedOperationException();
        }
        
        public boolean hasNext() {
            if (iit == null || !iit.hasNext()) {
                
                if (!it.hasNext()) {
                    return false;
                }
                
                Map.Entry<K, ArrayList<Long>> item = it.next();
                key = item.getKey();
                iit = item.getValue().iterator();
            } 
            
            return true;
                
        }
        
        public Map.Entry<K, V> next() {
            id = iit.next();
            return new MultiMapEntry(key, map.values.get(id));
        }
        
        
        public void remove() {
            iit.remove();
            map.values.remove(id);
            if (map.keys.get(key).isEmpty())
                it.remove();
        }
        
    }
    
    
    public void clear() {
        keys.clear();
        values.clear();
    }

    
    public boolean containsKey(Object key) {
        return keys.containsKey(key);
    }

    
    public boolean containsValue(Object value) {
        return values.containsValue(value);
    }

    
    public Set<Map.Entry<K, V>> entrySet() {
        return new MultiMapEntrySet(this);
    }

    
    public V get(Object key) {
        ArrayList<Long> l = keys.get(key);
        if (l == null)
            return null;
        return values.get(l.get(0));
    }

    
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    
    public Set<K> keySet() {
        return keys.keySet();
    }

    
    public V put(K key, V value) {
        ArrayList<Long> ids = keys.get(key);
        if (ids == null) {
            ids = new ArrayList<Long>();
            ids.add(id);
            keys.put(key, ids);
        } else {
            ids.add(id);
        }
        values.put(id, value);
        id++;
        
        return null;
    }

    
    public void putAll(Map<? extends K, ? extends V> arg0) {
        throw new UnsupportedOperationException();
    }

    
    public V remove(Object arg0) {
        throw new UnsupportedOperationException();
    }

    
    public int size() {
        return values.size();
    }

    
    public Collection<V> values() {
        return values.values();
    }

}
