package dsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * DESIGN SEARCH AUTOCOMPLETE SYSTEM (LeetCode 642) — LLD design
 * =============================================================
 *
 * PROBLEM
 * -------
 * Maintain a corpus of historical sentences each with a "hot" count and
 * stream characters one at a time:
 * - For any character c != '#', return the top-3 historical sentences
 * that share the prefix typed so far, ordered by hot count DESC, then
 * ASCII ASC.
 * - For the terminator '#', commit the typed buffer to history
 * (hot += 1, creating the entry if new), reset the buffer, return [].
 *
 * ARCHITECTURE
 * ------------
 * AutocompleteSystem ── uses ──> Trie ── orders with ──> SentenceRanker
 * │ │
 * │ └── TrieNode (children + cached sentences)
 * │
 * └── StringBuilder buffer (current input)
 *
 * AutocompleteSystem is the public facade. It owns the input buffer and
 * delegates storage + querying to Trie. It knows nothing about how
 * sentences are stored or ranked.
 *
 * Trie is the search index. It exposes only two operations:
 * - insert(sentence, delta) adjust the hot count by delta
 * - topK(prefix, k) top-k completions for prefix
 * It knows nothing about the input lifecycle ('#' handling, buffering).
 *
 * SentenceRanker is the ordering policy, isolated as a singleton
 * Comparator. Swap it out (e.g., for a recency-weighted ranking) without
 * touching the trie or the facade.
 *
 * SentenceCount is a small immutable value object so the heap and the
 * ranker don't depend on Map.Entry (a leaky abstraction from the
 * sentence-cache map).
 *
 * INDEXING DECISION
 * -----------------
 * Each TrieNode caches every sentence that passes through it in a small
 * `sentences` map. This makes a query O(S log k) where S is the number of
 * distinct sentences sharing the CURRENT prefix (not the whole corpus).
 * Insertion is O(L) where L is the sentence length (one Map.merge per
 * level). The trade-off: every sentence is recorded at L nodes, so total
 * cache space is O(sum L_i^2) over the corpus. Standard autocomplete
 * trade-off: pay write cost up front because the read path (every
 * keystroke) is hot.
 *
 * COMPLEXITY
 * - constructor : O(sum L_i)
 * - input(c)
 * c != '#' : O(S log k) with k = 3
 * c == '#' : O(L) to commit
 * - space : O(sum L_i^2)
 */
public class AutocompleteSystem {

    private static final int TOP_K = 3;
    private static final char TERMINATOR = '#';

    private final Trie trie;
    private final StringBuilder buffer = new StringBuilder();

    public AutocompleteSystem(String[] sentences, int[] times) {
        if (sentences == null || times == null) {
            throw new IllegalArgumentException("sentences and times must be non-null");
        }
        if (sentences.length != times.length) {
            throw new IllegalArgumentException(
                    "sentences (" + sentences.length + ") and times (" + times.length
                            + ") must have the same length");
        }
        this.trie = new Trie();
        for (int i = 0; i < sentences.length; i++) {
            if (sentences[i] == null) {
                throw new IllegalArgumentException("sentences[" + i + "] is null");
            }
            if (times[i] < 0) {
                throw new IllegalArgumentException(
                        "times[" + i + "] must be non-negative, was " + times[i]);
            }
            trie.insert(sentences[i], times[i]);
        }
    }

    public List<String> input(char c) {
        if (c == TERMINATOR) {
            if (buffer.length() > 0) {
                trie.insert(buffer.toString(), 1);
            }
            buffer.setLength(0);
            return Collections.emptyList();
        }
        buffer.append(c);
        return trie.topK(buffer.toString(), TOP_K);
    }
}

/**
 * Character-indexed trie. Each node caches the sentences that pass through
 * it (sentence -> hot count) so prefix queries don't have to DFS the whole
 * subtree.
 */
final class Trie {

    private final TrieNode root = new TrieNode();

    /**
     * Adjust the hot count of `sentence` by `delta`. Creates the entry if absent.
     */
    void insert(String sentence, int delta) {
        if (delta == 0 || sentence.isEmpty()) {
            return;
        }
        TrieNode cur = root;
        for (int i = 0; i < sentence.length(); i++) {
            char c = sentence.charAt(i);
            cur = cur.children.computeIfAbsent(c, k -> new TrieNode());
            cur.sentences.merge(sentence, delta, Integer::sum);
        }
    }

    /** Top-k sentences sharing `prefix`, ranked by SentenceRanker. */
    List<String> topK(String prefix, int k) {
        if (k <= 0) {
            return Collections.emptyList();
        }
        TrieNode cur = root;
        for (int i = 0; i < prefix.length(); i++) {
            cur = cur.children.get(prefix.charAt(i));
            if (cur == null) {
                return Collections.emptyList();
            }
        }

        // Bounded min-heap under the REVERSED ranker: the head is the
        // worst of the k retained items, evicted when a better candidate
        // arrives. This is O(S log k) instead of O(S log S).
        PriorityQueue<SentenceCount> heap = new PriorityQueue<>(SentenceRanker.INSTANCE.reversed());
        for (Map.Entry<String, Integer> e : cur.sentences.entrySet()) {
            heap.offer(new SentenceCount(e.getKey(), e.getValue()));
            if (heap.size() > k) {
                heap.poll();
            }
        }

        List<SentenceCount> ordered = new ArrayList<>(heap);
        ordered.sort(SentenceRanker.INSTANCE);
        List<String> out = new ArrayList<>(ordered.size());
        for (SentenceCount sc : ordered) {
            out.add(sc.text);
        }
        return out;
    }
}

final class TrieNode {
    final Map<Character, TrieNode> children = new HashMap<>();
    final Map<String, Integer> sentences = new HashMap<>();
}

/** Immutable (text, hot) pair carried through ranking. */
final class SentenceCount {
    final String text;
    final int hot;

    SentenceCount(String text, int hot) {
        this.text = text;
        this.hot = hot;
    }
}

/**
 * Total order over historical sentences:
 * 1. higher hot count comes first (descending),
 * 2. lower ASCII order breaks ties (ascending).
 *
 * Stateless singleton: safely shared across all callers / threads.
 */
final class SentenceRanker implements Comparator<SentenceCount> {

    static final SentenceRanker INSTANCE = new SentenceRanker();

    private SentenceRanker() {
    }

    @Override
    public int compare(SentenceCount a, SentenceCount b) {
        int byHot = Integer.compare(b.hot, a.hot);
        if (byHot != 0) {
            return byHot;
        }
        return a.text.compareTo(b.text);
    }
}
