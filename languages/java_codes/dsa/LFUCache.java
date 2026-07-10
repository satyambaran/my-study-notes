package dsa;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * LFU CACHE (LeetCode 460) — LLD design
 * ======================================
 *
 * PROBLEM
 * -------
 * Implement a Least Frequently Used cache supporting:
 * int g (int key) :   if present (counts as an
 * 
 * void put(int key, int value) : insert/update. On overflow, evict
 * 
 * 
 * Both must run in AVERAGE O(1).
 *
 * ARCHITECTURE
 * ------------
 * ache (facade)
 * p<Integer, CacheEntry> entri  -- O(1  -> entry
 * equencyBuckets bucke -bucket U + minFreq
 *
 * UCache validates input, handles the capacity-0 edge case, and
 * chestrates the two collaborators. It knows nothing about how
 * equencies are bucketed.
 *
 * cheEntry is a small value object holding (key, value, freq).
 * -locating freq with the entry is natural — "an entry has a
 * equency" — and lets FrequencyBuckets read/write it directly without
 * intaining a parallel freq-by-key map.
 *
 * equencyBuckets owns the freq -> LinkedHashSet<key> map AND the
 * nFreq pointer. The minFreq invariant is the #1 source of LFU bugs,
 *  it's encapsulated behind a small API (insert / recordAccess /
 * ictColdest) that maintains it in lockstep with bucket changes.
 *
 * INVARIANTS
 *  entries.keySet() == union of all bucket key-sets.
 *  For every entry e: e.key is in the bucket at freq=e.freq.
 *  minFreq is the smallest freq with a non-empty bucket, OR 0 if the
 *  is empty (never read in the empty case).
 *
 * COMPLEXITY: O(1) average for both get and put.
 * LinkedHashSet add/remove/iterator.next are amortized O(1).
 * HashMap get/put are average O(1).
 *
 * SPACE: O(capacity) for entries + buckets (bucket overhead is O(distinct
 * frequencies present), bounded by capacity).
 */
public class LFUCache {

    private final int capacity;
    private final Map<Integer, CacheEntry> entries;
    private final FrequencyBuckets buckets;

    public LFUCache(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be >= 0, was " + capacity);
        }
        this.capacity = capacity;
        this.entries = new HashMap<>();
        this.buckets = new FrequencyBuckets();
    }

    public int get(int key) {
        CacheEntry entry = entries.get(key);
        if (entry == null) {
            return -1;
        }
        buckets.recordAccess(entry);
        return entry.value;
    }

    public void put(int key, int value) {
        if (capacity == 0) {
            return;
        }
        CacheEntry existing = entries.get(key);
        if (existing != null) {
            existing.value = value;
            buckets.recordAccess(existing);
            return;
        }
        if (entries.size() == capacity) {
            int evictedKey = buckets.evictColdest();
            entries.remove(evictedKey);
        }
        CacheEntry fresh = new CacheEntry(key, value);
        entries.put(key, fresh);
        buckets.insert(fresh);
    }
}

/** Per-entry state: (key, value, freq). Key is immutable; value/freq evolve. */
final class CacheEntry {
    final int key;
    int value;
    int freq;

    CacheEntry(int key, int value) {
        this.key = key;
        this.value = value;
        this.freq = 1;
    }
}

/**
 * Maintains:
 * A map from frequency to an insertion-ordered set of keys at that
 * uency (head = LRU at this freq, tail = MRU at this freq).
 * A `minFreq` pointer to the smallest frequency currently populated.
 *
 * The minFreq invariant is updated INSIDE each mutating operation so
 * callers never have to think about it.
 */
final class FrequencyBuckets {

    private final Map<Integer, LinkedHashSet<Integer>> bucketByFreq = new HashMap<>();
    private int minFreq = 0;

    /** Add a freshly-created entry (freq must be 1). */
    void insert(CacheEntry entry) {
        bucketAt(1).add(entry.key);
        minFreq = 1;
    }

    /**
     * Record an access on `entry`: move it from its current bucket into
     * the next-higher bucket (which makes it the MRU there). Increments
     * `entry.freq`. Updates `minFreq` if its old bucket became empty.
     */
    void recordAccess(CacheEntry entry) {
        LinkedHashSet<Integer> bucket = bucketByFreq.get(entry.freq);
        bucket.remove(entry.key);
        if (bucket.isEmpty()) {
            bucketByFreq.remove(entry.freq);
            if (minFreq == entry.freq) {
                minFreq++;
            }
        }
        entry.freq++;
        bucketAt(entry.freq).add(entry.key);
    }

    /**
     * Remove and return the key of the LRU entry at `minFreq` (i.e., the
     * coldest entry overall). Caller must ensure the cache is non-empty.
     */
    int evictColdest() {
        LinkedHashSet<Integer> bucket = bucketByFreq.get(minFreq);
        int victim = bucket.iterator().next();
        bucket.remove(victim);
        if (bucket.isEmpty()) {
            bucketByFreq.remove(minFreq);
            // minFreq will be reset to 1 by the next insert(); no need
            // to scan for the next-smallest freq here.
        }
        return victim;
    }

    private LinkedHashSet<Integer> bucketAt(int freq) {
        return bucketByFreq.computeIfAbsent(freq, k -> new LinkedHashSet<>());
    }
}
