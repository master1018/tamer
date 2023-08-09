public final class LinkedListMultimap<K, V>
    implements ListMultimap<K, V>, Serializable {
  private static final class Node<K, V> {
    final K key;
    V value;
    Node<K, V> next; 
    Node<K, V> previous; 
    Node<K, V> nextSibling; 
    Node<K, V> previousSibling; 
    Node(@Nullable K key, @Nullable V value) {
      this.key = key;
      this.value = value;
    }
    @Override public String toString() {
      return key + "=" + value;
    }
  }
  private transient Node<K, V> head; 
  private transient Node<K, V> tail; 
  private transient Multiset<K> keyCount; 
  private transient Map<K, Node<K, V>> keyToKeyHead; 
  private transient Map<K, Node<K, V>> keyToKeyTail; 
  public static <K, V> LinkedListMultimap<K, V> create() {
    return new LinkedListMultimap<K, V>();
  }
  public static <K, V> LinkedListMultimap<K, V> create(int expectedKeys) {
    return new LinkedListMultimap<K, V>(expectedKeys);
  }
  public static <K, V> LinkedListMultimap<K, V> create(
      Multimap<? extends K, ? extends V> multimap) {
    return new LinkedListMultimap<K, V>(multimap);
  }
  private LinkedListMultimap() {
    keyCount = LinkedHashMultiset.create();
    keyToKeyHead = Maps.newHashMap();
    keyToKeyTail = Maps.newHashMap();
  }
  private LinkedListMultimap(int expectedKeys) {
    keyCount = LinkedHashMultiset.create(expectedKeys);
    keyToKeyHead = Maps.newHashMapWithExpectedSize(expectedKeys);
    keyToKeyTail = Maps.newHashMapWithExpectedSize(expectedKeys);
  }
  private LinkedListMultimap(Multimap<? extends K, ? extends V> multimap) {
    this(multimap.keySet().size());
    putAll(multimap);
  }
  private Node<K, V> addNode(
      @Nullable K key, @Nullable V value, @Nullable Node<K, V> nextSibling) {
    Node<K, V> node = new Node<K, V>(key, value);
    if (head == null) { 
      head = tail = node;
      keyToKeyHead.put(key, node);
      keyToKeyTail.put(key, node);
    } else if (nextSibling == null) { 
      tail.next = node;
      node.previous = tail;
      Node<K, V> keyTail = keyToKeyTail.get(key);
      if (keyTail == null) { 
        keyToKeyHead.put(key, node);
      } else {
        keyTail.nextSibling = node;
        node.previousSibling = keyTail;
      }
      keyToKeyTail.put(key, node);
      tail = node;
    } else { 
      node.previous = nextSibling.previous;
      node.previousSibling = nextSibling.previousSibling;
      node.next = nextSibling;
      node.nextSibling = nextSibling;
      if (nextSibling.previousSibling == null) { 
        keyToKeyHead.put(key, node);
      } else {
        nextSibling.previousSibling.nextSibling = node;
      }
      if (nextSibling.previous == null) { 
        head = node;
      } else {
        nextSibling.previous.next = node;
      }
      nextSibling.previous = node;
      nextSibling.previousSibling = node;
    }
    keyCount.add(key);
    return node;
  }
  private void removeNode(Node<K, V> node) {
    if (node.previous != null) {
      node.previous.next = node.next;
    } else { 
      head = node.next;
    }
    if (node.next != null) {
      node.next.previous = node.previous;
    } else { 
      tail = node.previous;
    }
    if (node.previousSibling != null) {
      node.previousSibling.nextSibling = node.nextSibling;
    } else if (node.nextSibling != null) { 
      keyToKeyHead.put(node.key, node.nextSibling);
    } else {
      keyToKeyHead.remove(node.key); 
    }
    if (node.nextSibling != null) {
      node.nextSibling.previousSibling = node.previousSibling;
    } else if (node.previousSibling != null) { 
      keyToKeyTail.put(node.key, node.previousSibling);
    } else {
      keyToKeyTail.remove(node.key); 
    }
    keyCount.remove(node.key);
  }
  private void removeAllNodes(@Nullable Object key) {
    for (Iterator<V> i = new ValueForKeyIterator(key); i.hasNext();) {
      i.next();
      i.remove();
    }
  }
  private static void checkElement(@Nullable Object node) {
    if (node == null) {
      throw new NoSuchElementException();
    }
  }
  private class NodeIterator implements Iterator<Node<K, V>> {
    Node<K, V> next = head;
    Node<K, V> current;
    public boolean hasNext() {
      return next != null;
    }
    public Node<K, V> next() {
      checkElement(next);
      current = next;
      next = next.next;
      return current;
    }
    public void remove() {
      checkState(current != null);
      removeNode(current);
      current = null;
    }
  }
  private class DistinctKeyIterator implements Iterator<K> {
    final Set<K> seenKeys = new HashSet<K>(Maps.capacity(keySet().size()));
    Node<K, V> next = head;
    Node<K, V> current;
    public boolean hasNext() {
      return next != null;
    }
    public K next() {
      checkElement(next);
      current = next;
      seenKeys.add(current.key);
      do { 
        next = next.next;
      } while ((next != null) && !seenKeys.add(next.key));
      return current.key;
    }
    public void remove() {
      checkState(current != null);
      removeAllNodes(current.key);
      current = null;
    }
  }
  private class ValueForKeyIterator implements ListIterator<V> {
    final Object key;
    int nextIndex;
    Node<K, V> next;
    Node<K, V> current;
    Node<K, V> previous;
    ValueForKeyIterator(@Nullable Object key) {
      this.key = key;
      next = keyToKeyHead.get(key);
    }
    public ValueForKeyIterator(@Nullable Object key, int index) {
      int size = keyCount.count(key);
      Preconditions.checkPositionIndex(index, size);
      if (index >= (size / 2)) {
        previous = keyToKeyTail.get(key);
        nextIndex = size;
        while (index++ < size) {
          previous();
        }
      } else {
        next = keyToKeyHead.get(key);
        while (index-- > 0) {
          next();
        }
      }
      this.key = key;
      current = null;
    }
    public boolean hasNext() {
      return next != null;
    }
    public V next() {
      checkElement(next);
      previous = current = next;
      next = next.nextSibling;
      nextIndex++;
      return current.value;
    }
    public boolean hasPrevious() {
      return previous != null;
    }
    public V previous() {
      checkElement(previous);
      next = current = previous;
      previous = previous.previousSibling;
      nextIndex--;
      return current.value;
    }
    public int nextIndex() {
      return nextIndex;
    }
    public int previousIndex() {
      return nextIndex - 1;
    }
    public void remove() {
      checkState(current != null);
      if (current != next) { 
        previous = current.previousSibling;
        nextIndex--;
      } else {
        next = current.nextSibling;
      }
      removeNode(current);
      current = null;
    }
    public void set(V value) {
      checkState(current != null);
      current.value = value;
    }
    @SuppressWarnings("unchecked")
    public void add(V value) {
      previous = addNode((K) key, value, next);
      nextIndex++;
      current = null;
    }
  }
  public int size() {
    return keyCount.size();
  }
  public boolean isEmpty() {
    return head == null;
  }
  public boolean containsKey(@Nullable Object key) {
    return keyToKeyHead.containsKey(key);
  }
  public boolean containsValue(@Nullable Object value) {
    for (Iterator<Node<K, V>> i = new NodeIterator(); i.hasNext();) {
      if (Objects.equal(i.next().value, value)) {
        return true;
      }
    }
    return false;
  }
  public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
    for (Iterator<V> i = new ValueForKeyIterator(key); i.hasNext();) {
      if (Objects.equal(i.next(), value)) {
        return true;
      }
    }
    return false;
  }
  public boolean put(@Nullable K key, @Nullable V value) {
    addNode(key, value, null);
    return true;
  }
  public boolean remove(@Nullable Object key, @Nullable Object value) {
    Iterator<V> values = new ValueForKeyIterator(key);
    while (values.hasNext()) {
      if (Objects.equal(values.next(), value)) {
        values.remove();
        return true;
      }
    }
    return false;
  }
  public boolean putAll(@Nullable K key, Iterable<? extends V> values) {
    boolean changed = false;
    for (V value : values) {
      changed |= put(key, value);
    }
    return changed;
  }
  public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
    boolean changed = false;
    for (Entry<? extends K, ? extends V> entry : multimap.entries()) {
      changed |= put(entry.getKey(), entry.getValue());
    }
    return changed;
  }
  public List<V> replaceValues(@Nullable K key, Iterable<? extends V> values) {
    List<V> oldValues = getCopy(key);
    ListIterator<V> keyValues = new ValueForKeyIterator(key);
    Iterator<? extends V> newValues = values.iterator();
    while (keyValues.hasNext() && newValues.hasNext()) {
      keyValues.next();
      keyValues.set(newValues.next());
    }
    while (keyValues.hasNext()) {
      keyValues.next();
      keyValues.remove();
    }
    while (newValues.hasNext()) {
      keyValues.add(newValues.next());
    }
    return oldValues;
  }
  private List<V> getCopy(@Nullable Object key) {
    return unmodifiableList(Lists.newArrayList(new ValueForKeyIterator(key)));
  }
  public List<V> removeAll(@Nullable Object key) {
    List<V> oldValues = getCopy(key);
    removeAllNodes(key);
    return oldValues;
  }
  public void clear() {
    head = null;
    tail = null;
    keyCount.clear();
    keyToKeyHead.clear();
    keyToKeyTail.clear();
  }
  public List<V> get(final @Nullable K key) {
    return new AbstractSequentialList<V>() {
      @Override public int size() {
        return keyCount.count(key);
      }
      @Override public ListIterator<V> listIterator(int index) {
        return new ValueForKeyIterator(key, index);
      }
      @Override public boolean removeAll(Collection<?> c) {
        return Iterators.removeAll(iterator(), c);
      }
      @Override public boolean retainAll(Collection<?> c) {
        return Iterators.retainAll(iterator(), c);
      }
    };
  }
  private transient Set<K> keySet;
  public Set<K> keySet() {
    Set<K> result = keySet;
    if (result == null) {
      keySet = result = new AbstractSet<K>() {
        @Override public int size() {
          return keyCount.elementSet().size();
        }
        @Override public Iterator<K> iterator() {
          return new DistinctKeyIterator();
        }
        @Override public boolean contains(Object key) { 
          return keyCount.contains(key);
        }
      };
    }
    return result;
  }
  private transient Multiset<K> keys;
  public Multiset<K> keys() {
    Multiset<K> result = keys;
    if (result == null) {
      keys = result = new MultisetView();
    }
    return result;
  }
  private class MultisetView extends AbstractCollection<K>
      implements Multiset<K> {
    @Override public int size() {
      return keyCount.size();
    }
    @Override public Iterator<K> iterator() {
      final Iterator<Node<K, V>> nodes = new NodeIterator();
      return new Iterator<K>() {
        public boolean hasNext() {
          return nodes.hasNext();
        }
        public K next() {
          return nodes.next().key;
        }
        public void remove() {
          nodes.remove();
        }
      };
    }
    public int count(@Nullable Object key) {
      return keyCount.count(key);
    }
    public int add(@Nullable K key, int occurrences) {
      throw new UnsupportedOperationException();
    }
    public int remove(@Nullable Object key, int occurrences) {
      checkArgument(occurrences >= 0);
      int oldCount = count(key);
      Iterator<V> values = new ValueForKeyIterator(key);
      while ((occurrences-- > 0) && values.hasNext()) {
        values.next();
        values.remove();
      }
      return oldCount;
    }
    public int setCount(K element, int count) {
      return setCountImpl(this, element, count);
    }
    public boolean setCount(K element, int oldCount, int newCount) {
      return setCountImpl(this, element, oldCount, newCount);
    }
    @Override public boolean removeAll(Collection<?> c) {
      return Iterators.removeAll(iterator(), c);
    }
    @Override public boolean retainAll(Collection<?> c) {
      return Iterators.retainAll(iterator(), c);
    }
    public Set<K> elementSet() {
      return keySet();
    }
    public Set<Entry<K>> entrySet() {
      return new AbstractSet<Entry<K>>() {
        @Override public int size() {
          return keyCount.elementSet().size();
        }
        @Override public Iterator<Entry<K>> iterator() {
          final Iterator<K> keyIterator = new DistinctKeyIterator();
          return new Iterator<Entry<K>>() {
            public boolean hasNext() {
              return keyIterator.hasNext();
            }
            public Entry<K> next() {
              final K key = keyIterator.next();
              return new Multisets.AbstractEntry<K>() {
                public K getElement() {
                  return key;
                }
                public int getCount() {
                  return keyCount.count(key);
                }
              };
            }
            public void remove() {
              keyIterator.remove();
            }
          };
        }
      };
    }
    @Override public boolean equals(@Nullable Object object) {
      return keyCount.equals(object);
    }
    @Override public int hashCode() {
      return keyCount.hashCode();
    }
    @Override public String toString() {
      return keyCount.toString(); 
    }
  }
  private transient Collection<V> valuesCollection;
  public Collection<V> values() {
    Collection<V> result = valuesCollection;
    if (result == null) {
      valuesCollection = result = new AbstractCollection<V>() {
        @Override public int size() {
          return keyCount.size();
        }
        @Override public Iterator<V> iterator() {
          final Iterator<Node<K, V>> nodes = new NodeIterator();
          return new Iterator<V>() {
            public boolean hasNext() {
              return nodes.hasNext();
            }
            public V next() {
              return nodes.next().value;
            }
            public void remove() {
              nodes.remove();
            }
          };
        }
      };
    }
    return result;
  }
  private transient Collection<Entry<K, V>> entries;
  public Collection<Entry<K, V>> entries() {
    Collection<Entry<K, V>> result = entries;
    if (result == null) {
      entries = result = new AbstractCollection<Entry<K, V>>() {
        @Override public int size() {
          return keyCount.size();
        }
        @Override public Iterator<Entry<K, V>> iterator() {
          final Iterator<Node<K, V>> nodes = new NodeIterator();
          return new Iterator<Entry<K, V>>() {
            public boolean hasNext() {
              return nodes.hasNext();
            }
            public Entry<K, V> next() {
              final Node<K, V> node = nodes.next();
              return new AbstractMapEntry<K, V>() {
                @Override public K getKey() {
                  return node.key;
                }
                @Override public V getValue() {
                  return node.value;
                }
                @Override public V setValue(V value) {
                  V oldValue = node.value;
                  node.value = value;
                  return oldValue;
                }
              };
            }
            public void remove() {
              nodes.remove();
            }
          };
        }
      };
    }
    return result;
  }
  private class AsMapEntries extends AbstractSet<Entry<K, Collection<V>>> {
    @Override public int size() {
      return keyCount.elementSet().size();
    }
    @Override public Iterator<Entry<K, Collection<V>>> iterator() {
      final Iterator<K> keyIterator = new DistinctKeyIterator();
      return new Iterator<Entry<K, Collection<V>>>() {
        public boolean hasNext() {
          return keyIterator.hasNext();
        }
        public Entry<K, Collection<V>> next() {
          final K key = keyIterator.next();
          return new AbstractMapEntry<K, Collection<V>>() {
            @Override public K getKey() {
              return key;
            }
            @Override public Collection<V> getValue() {
              return LinkedListMultimap.this.get(key);
            }
          };
        }
        public void remove() {
          keyIterator.remove();
        }
      };
    }
  }
  private transient Map<K, Collection<V>> map;
  public Map<K, Collection<V>> asMap() {
    Map<K, Collection<V>> result = map;
    if (result == null) {
      map = result = new AbstractMap<K, Collection<V>>() {
        Set<Entry<K, Collection<V>>> entrySet;
        @Override public Set<Entry<K, Collection<V>>> entrySet() {
          Set<Entry<K, Collection<V>>> result = entrySet;
          if (result == null) {
            entrySet = result = new AsMapEntries();
          }
          return result;
        }
        @Override public boolean containsKey(@Nullable Object key) {
          return LinkedListMultimap.this.containsKey(key);
        }
        @SuppressWarnings("unchecked")
        @Override public Collection<V> get(@Nullable Object key) {
          Collection<V> collection = LinkedListMultimap.this.get((K) key);
          return collection.isEmpty() ? null : collection;
        }
        @Override public Collection<V> remove(@Nullable Object key) {
          Collection<V> collection = removeAll(key);
          return collection.isEmpty() ? null : collection;
        }
      };
    }
    return result;
  }
  @Override public boolean equals(@Nullable Object other) {
    if (other == this) {
      return true;
    }
    if (other instanceof Multimap) {
      Multimap<?, ?> that = (Multimap<?, ?>) other;
      return this.asMap().equals(that.asMap());
    }
    return false;
  }
  @Override public int hashCode() {
    return asMap().hashCode();
  }
  @Override public String toString() {
    return asMap().toString();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeInt(size());
    for (Entry<K, V> entry : entries()) {
      stream.writeObject(entry.getKey());
      stream.writeObject(entry.getValue());
    }
  }
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    keyCount = LinkedHashMultiset.create();
    keyToKeyHead = Maps.newHashMap();
    keyToKeyTail = Maps.newHashMap();
    int size = stream.readInt();
    for (int i = 0; i < size; i++) {
      @SuppressWarnings("unchecked") 
      K key = (K) stream.readObject();
      @SuppressWarnings("unchecked") 
      V value = (V) stream.readObject();
      put(key, value);
    }
  }
  private static final long serialVersionUID = 0;
}
