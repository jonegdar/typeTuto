package typeTutor.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Generates typing text triplets from words/quotes resources.
 */
public class TextGenerator {
    // Fixed generator dimensions and classpath resource paths.
    private static final int ROWS_PER_TRIPLET = 3;
    private static final int WORDS_PER_ROW = 15;
    private static final String WORDS_EN_PATH = "/text/words/english_1k.json";
    private static final String WORDS_FIL_PATH = "/text/words/filipino.json";
    private static final String QUOTES_EN_PATH = "/text/quotes/english.json";
    private static final String QUOTES_FIL_PATH = "/text/quotes/filipino.json";

    // Shared randomness and JSON mapper.
    private final Random random;
    private final ObjectMapper objectMapper;

    /**
     * Creates generator with default randomness.
     */
    public TextGenerator() {
        this(new Random());
    }

    /**
     * Creates generator with provided randomness (useful for tests).
     */
    public TextGenerator(Random random) {
        this.random = random;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generates session triplets for selected mode/language/time.
     */
    public List<List<String>> generateTriplets(String wordMode, String language, int timeSeconds) {
        int tripletCount = mapTripletCount(timeSeconds);

        if (isMode(wordMode, "quotes")) {
            List<String> quotes = loadQuotes(language);
            return generateQuoteTriplets(quotes, tripletCount);
        }

        List<String> words = loadWords(language);
        if (isMode(wordMode, "numbers")) {
            return generateWordTriplets(words, tripletCount, true);
        }

        return generateWordTriplets(words, tripletCount, false);
    }

    /**
     * Builds triplets where each row is generated from words (+optional numbers).
     */
    private List<List<String>> generateWordTriplets(List<String> words, int tripletCount, boolean includeNumbers) {
        List<List<String>> triplets = new ArrayList<>(tripletCount);
        for (int tripletIndex = 0; tripletIndex < tripletCount; tripletIndex++) {
            List<String> rows = new ArrayList<>(ROWS_PER_TRIPLET);
            for (int row = 0; row < ROWS_PER_TRIPLET; row++) {
                rows.add(generateWordRow(words, includeNumbers));
            }
            triplets.add(rows);
        }
        return triplets;
    }

    /**
     * Builds triplets by sampling random quote lines.
     */
    private List<List<String>> generateQuoteTriplets(List<String> quotes, int tripletCount) {
        if (quotes.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> shuffledQuotes = new ArrayList<>(quotes);
        Collections.shuffle(shuffledQuotes, random);

        List<List<String>> triplets = new ArrayList<>(tripletCount);
        int quoteIndex = 0;
        for (int triplet = 0; triplet < tripletCount; triplet++) {
            List<String> rows = new ArrayList<>(ROWS_PER_TRIPLET);
            for (int row = 0; row < ROWS_PER_TRIPLET; row++) {
                if (quoteIndex >= shuffledQuotes.size()) {
                    Collections.shuffle(shuffledQuotes, random);
                    quoteIndex = 0;
                }
                rows.add(shuffledQuotes.get(quoteIndex++));
            }
            triplets.add(rows);
        }
        return triplets;
    }

    /**
     * Generates one 15-token row. Numbers mode injects 3 numeric tokens.
     */
    private String generateWordRow(List<String> words, boolean includeNumbers) {
        if (words.isEmpty()) {
            return "";
        }

        int numbersPerRow = includeNumbers ? 3 : 0;
        Set<Integer> numberIndexes = pickUniqueIndexes(numbersPerRow, WORDS_PER_ROW);

        StringBuilder rowText = new StringBuilder();
        for (int i = 0; i < WORDS_PER_ROW; i++) {
            if (i > 0) {
                rowText.append(' ');
            }

            if (numberIndexes.contains(i)) {
                rowText.append(randomNumberToken());
            } else {
                rowText.append(randomWord(words));
            }
        }
        return rowText.toString();
    }

    /**
     * Picks unique indexes used for number insertion.
     */
    private Set<Integer> pickUniqueIndexes(int picks, int upperBoundExclusive) {
        Set<Integer> indexes = new HashSet<>();
        while (indexes.size() < picks) {
            indexes.add(random.nextInt(upperBoundExclusive));
        }
        return indexes;
    }

    /**
     * Generates random numeric token.
     */
    private String randomNumberToken() {
        return Integer.toString(1 + random.nextInt(9999));
    }

    /**
     * Samples one random word from loaded list.
     */
    private String randomWord(List<String> words) {
        return words.get(random.nextInt(words.size()));
    }

    /**
     * Converts time mode seconds to number of triplets.
     */
    private int mapTripletCount(int timeSeconds) {
        return switch (timeSeconds) {
            case 120 -> 9;
            case 60 -> 7;
            case 30 -> 5;
            case 15 -> 4;
            default -> throw new IllegalArgumentException("Unsupported time mode: " + timeSeconds);
        };
    }

    /**
     * Case-insensitive mode comparison helper.
     */
    private boolean isMode(String value, String target) {
        return value != null && target.equals(value.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Loads word list file based on selected language.
     */
    private List<String> loadWords(String language) {
        String path = isLanguageFilipino(language) ? WORDS_FIL_PATH : WORDS_EN_PATH;
        try (InputStream stream = TextGenerator.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing words file: " + path);
            }

            WordPayload payload = objectMapper.readValue(stream, WordPayload.class);
            return payload.words == null ? Collections.emptyList() : payload.words;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read words file: " + path, e);
        }
    }

    /**
     * Loads quote list file based on selected language.
     */
    private List<String> loadQuotes(String language) {
        String path = isLanguageFilipino(language) ? QUOTES_FIL_PATH : QUOTES_EN_PATH;
        try (InputStream stream = TextGenerator.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing quotes file: " + path);
            }

            QuotePayload payload = objectMapper.readValue(stream, QuotePayload.class);
            if (payload.quotes == null || payload.quotes.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> values = new ArrayList<>(payload.quotes.size());
            for (Quote quote : payload.quotes) {
                if (quote != null && quote.text != null && !quote.text.isBlank()) {
                    values.add(quote.text.trim());
                }
            }
            return values;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read quotes file: " + path, e);
        }
    }

    /**
     * Detects Filipino language selection token.
     */
    private boolean isLanguageFilipino(String language) {
        return language != null && language.trim().toLowerCase(Locale.ROOT).startsWith("fil");
    }

    /**
     * JSON payload type for words files.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class WordPayload {
        public List<String> words;
    }

    /**
     * JSON payload type for quotes files.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class QuotePayload {
        public List<Quote> quotes;
    }

    /**
     * JSON quote object model.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Quote {
        public String text;
    }
}
