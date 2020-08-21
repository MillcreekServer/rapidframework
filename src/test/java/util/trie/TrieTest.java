package util.trie;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.trie.TestShare.fillTrie;

public class TrieTest {

    private Trie trie;

    @Before
    public void createExampleTrie() {
        trie = new Trie();
        fillTrie(trie);
    }

    @Test
    public void insert() {
        assertFalse(trie.isEmpty());
    }

    @Test
    public void find() {
        assertFalse(trie.find("3"));
        assertFalse(trie.find("vida"));
        assertTrue(trie.find("life"));
    }

    @Test
    public void delete() {
        assertTrue(trie.find("Programming"));

        trie.delete("Programming");
        assertFalse(trie.find("Programming"));
    }
}