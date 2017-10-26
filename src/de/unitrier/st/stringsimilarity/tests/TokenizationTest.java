package de.unitrier.st.stringsimilarity.tests;

import com.google.common.collect.Multiset;
import de.unitrier.st.stringsimilarity.util.InputTooShortException;
import de.unitrier.st.stringsimilarity.util.MultisetCollector;
import org.junit.jupiter.api.Test;

import java.util.*;

import static de.unitrier.st.stringsimilarity.Normalization.normalizeForNGram;
import static de.unitrier.st.stringsimilarity.Normalization.normalizeForShingle;
import static de.unitrier.st.stringsimilarity.Tokenization.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class TokenizationTest {

    private static final String str = "main(String[] args)  {\n print('Test');\n\texit(0); }";

    // ********** GENERAL TOKENIZATION **********

    @Test
    void testTokenization() {
        assertEquals(" ", DEFAULT_SEPARATOR);

        List<String> expectedTokens = Arrays.asList("main(String[]", "args)", "{\n", "print('Test');\n\texit(0);", "}");
        List<String> tokens = tokens(str);
        assertThat(tokens, is(expectedTokens));
        assertThat(tokens, is(tokens(str, DEFAULT_SEPARATOR)));
    }


    // ********** NGRAM TOKENIZATION **********

    @Test
    void testNgramTokenization() {
        assertEquals(4, NGRAM_SIZE);

        String normalizedStr = normalizeForNGram(str);

        // Tokenization
        List<String> expectedTokens = new ArrayList<>();
        expectedTokens.add("main(string[]args)print('test')exit(0)");
        List<String> tokens = tokens(normalizedStr);
        assertThat(tokens, is(expectedTokens));

        // nGram List
        List<String> nGramList = nGramList(normalizedStr);
        List<String> expectedNGramList10 = Arrays.asList("main", "ain(", "in(s", "n(st", "(str", "stri", "trin", "ring", "ing[", "ng[]");
        assertThat(nGramList.subList(0, 10), is(expectedNGramList10));

        nGramList = nGramList(normalizeForNGram("int i; int i;"));
        List<String> expectedNGramList = Arrays.asList("inti", "ntii", "tiin", "iint", "inti");

        // nGram Multiset
        Multiset<String> nGramMultiset = nGramList.stream().collect(MultisetCollector.toMultiset());
        Multiset<String> expectedNGramMultiset = expectedNGramList.stream().collect(MultisetCollector.toMultiset());
        assertThat(nGramMultiset, is(expectedNGramMultiset));

        // nGram Set
        Set<String> nGramSet = new HashSet<>(nGramList);
        Set<String> expectedNGramSet = new HashSet<>(expectedNGramList);
        assertThat(nGramSet, is(expectedNGramSet));

        // nGramSize > str.length() => InputTooShortException
        assertThrows(InputTooShortException.class, () -> nGramList("abc"));

        // nGram padding
        nGramList = nGramList("intiinti", true);
        expectedNGramList = Arrays.asList(
                PADDING_CHAR + PADDING_CHAR + PADDING_CHAR + "i",
                PADDING_CHAR + PADDING_CHAR + "in",
                PADDING_CHAR + "int",
                "inti", "ntii", "tiin", "iint", "inti",
                "nti" + PADDING_CHAR,
                "ti" + PADDING_CHAR + PADDING_CHAR,
                "i" + PADDING_CHAR + PADDING_CHAR + PADDING_CHAR);
        assertThat(nGramList, is(expectedNGramList));
    }


    // ********** SHINGLE TOKENIZATION **********

    @Test
    void testShingleTokenization() {
        assertEquals(3, SHINGLE_SIZE);

        String normalizedStr = normalizeForShingle(str);

        // Tokenization
        List<String> expectedTokens = Arrays.asList("main", "string", "args", "print", "test", "exit", "0");
        List<String> tokens = tokens(normalizedStr);
        assertThat(tokens, is(expectedTokens));

        // Shingle List
        List<String> shingleList = shingleList(tokens);
        List<String> expectedShingleList = Arrays.asList("main string args", "string args print", "args print test", "print test exit", "test exit 0");
        assertThat(shingleList, is(expectedShingleList));

        normalizedStr = normalizeForShingle("int i; int i; String str");
        shingleList = shingleList(tokens(normalizedStr));
        expectedShingleList = Arrays.asList("int i int", "i int i", "int i string", "i string str");

        // Shingle Multiset
        Multiset<String> shingleMultiset = shingleList.stream().collect(MultisetCollector.toMultiset());
        Multiset<String> expectedShingleMultiset = expectedShingleList.stream().collect(MultisetCollector.toMultiset());
        assertThat(shingleMultiset, is(expectedShingleMultiset));

        // Shingle Set
        Set<String> shingleSet = new HashSet<>(shingleList);
        Set<String> expectedShingleSet = new HashSet<>(expectedShingleList);
        assertThat(shingleSet, is(expectedShingleSet));
    }
}
