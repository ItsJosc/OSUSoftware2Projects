import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine4;
import components.utilities.FormatChecker;
import components.utilities.Reporter;

/**
 * Generators an html and css tag cloud from an input text file.
 *
 * @author Josiah Cheung
 * @author Craig Ngo
 *
 */
public final class TagCloudGenerator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudGenerator() {
    }

    /**
     * Maximum font size of tag cloud minus 10.
     */
    private static final int MAXFONT = 48;

    /**
     * Determinant of minimum/maximum font size.
     */
    private static final int FONTBUFFER = 10;

    /**
     * Constants to error check for .html and .txt tags.
     */
    private static final int DOTTXT = 4;
    /**
     * Constants to error check for .html and .txt tags.
     */
    private static final int DOTHTML = 5;

    /**
     * A class which compares two strings alphabetically using the Comparator
     * class. (Adapted from Queue Sort lab)
     */
    private static class StringCmp
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> o1,
                Map.Pair<String, Integer> o2) {
            return o1.key().compareTo(o2.key());
        }
    }

    /**
     * A class which compares two Integers using the Comparator class.
     */
    private static class IntegerCmp
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> o1,
                Map.Pair<String, Integer> o2) {
            return o2.value().compareTo(o1.value());
        }
    }

    /**
     * Constructs set of invalid characters to compare against. In this case,
     * words are any sequence of alphabetical characters and the character '.
     *
     * @return a Set of characters considered to be whitespace or separators.
     */
    private static Set<Character> defineWord() {
        /*
         * Creates an array of separator characters. Adds each element of that
         * array into a Set and returns the Set.
         */
        Set<Character> invalid = new Set1L<>();
        char[] separators = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
                '`', '~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '+',
                '=', '[', ']', '{', '}', '|', '\\', ':', ';', '\"', '<', ',',
                '>', '.', '?', '/', '\t', '\n', ' ', '\r', '-' };
        for (char c : separators) {
            invalid.add(c);
        }
        return invalid;
    }

    /**
     * Counts words in file and stores them in a map.
     *
     * @param in
     *            File to be read from,
     * @param invalid
     *            Set of whitespace/separator characters to parse words.
     * @return Unordered Map of words and their counts.
     */
    private static Map<String, Integer> countWords(SimpleReader in,
            Set<Character> invalid) {
        /*
         * Builds words char by char, parsing according to a set of separator
         * characters. Adds each word to a map and updates count of each word as
         * it is parsed.
         */

        StringBuilder builder = new StringBuilder();
        Map<String, Integer> m = new Map1L<>();
        while (!in.atEOS()) {
            char c = in.read();
            if (!invalid.contains(c)) {
                builder.append(c);
            } else {
                if (builder.length() != 0) {
                    String word = builder.toString().toLowerCase();
                    if (!m.hasKey(word)) {
                        m.add(word, 1);
                    } else {
                        m.replaceValue(word, m.value(word) + 1);
                    }
                    builder.delete(0, builder.length());
                }
            }
        }
        return m;
    }

    /**
     * Prints HTML header for tag cloud file.
     *
     * @param output
     *            Output stream on desired output file.
     * @param fileName
     *            Name of source file.
     * @param cloudSize
     *            desired number of words in tag cloud.
     */
    private static void printHeader(SimpleWriter output, String fileName,
            int cloudSize) {
        output.println("<html>");
        output.println("<head>");
        output.println("<title>Top " + cloudSize + " words in " + fileName
                + "</title>");
        output.println(
                "<link href=\"http://web.cse.ohio-state.edu/software/2231/web-"
                        + "sw2/assignments/projects/tag-cloud-generator/data/"
                        + "tagcloud.css\" rel=\"stylesheet\" type=\"text/css\""
                        + ">\r\n"
                        + "<link href=\"tagcloud.css\" rel=\"stylesheet\" type"
                        + "=\"text/css\">");
        output.println("</head>\r\n" + "<body>");
        output.println(
                "<h2>Top " + cloudSize + " words in " + fileName + "</h2>");
        output.println(
                "<hr>\r\n" + "<div class=\"cdiv\">\r\n" + "<p class=\"cbox\">");
    }

    /**
     * Prints HTML body for tag cloud file.
     *
     * @param output
     *            Output stream on output file.
     * @param sorter
     *            Sorted collection of pairs of words and counts.
     */
    private static void printBody(SimpleWriter output,
            SortingMachine<Map.Pair<String, Integer>> sorter) {

        /*
         * Get max and min counts.
         */
        int minCount = 0;
        int maxCount = 0;
        for (Map.Pair<String, Integer> p : sorter) {
            if (p.value() > maxCount) {
                maxCount = p.value();
            } else if (p.value() < minCount) {
                minCount = p.value();
            }
        }

        /*
         * Print body
         */
        while (sorter.size() > 0) {
            Map.Pair<String, Integer> pair = sorter.removeFirst();

            int count = pair.value();
            int fontSize = 1;
            /*
             * Apply font size algorithm
             */

            /*
             * Adjusted max font size using font buffer.
             */
            int adjMax = TagCloudGenerator.MAXFONT
                    - TagCloudGenerator.FONTBUFFER;

            if (count > minCount) {
                fontSize = (adjMax * (count - minCount))
                        / (maxCount - minCount);
            }
            fontSize += TagCloudGenerator.FONTBUFFER;

            output.println("<span style=\"cursor:default\" class=\"f" + fontSize
                    + "\" title=\"count:" + pair.value() + "\">" + pair.key()
                    + "</span>");
        }
        output.println("</p>\n" + "</div>\n" + "</body>\n" + "</html>\n" + "");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        /*
         * 1. Ask user for input file, output file, number of words N to
         * display. 2. Parse words from input file. 3. Sort parsed words by
         * alphabet. 4. Sort parsed words by count. 5. Construct webpage,
         * displaying N words according to css stylehsheet.
         */

        SimpleWriter out = new SimpleWriter1L();
        SimpleReader in = new SimpleReader1L();
        Comparator<Map.Pair<String, Integer>> strcmp = new StringCmp();
        Comparator<Map.Pair<String, Integer>> intcmp = new IntegerCmp();

        /*
         * Asks for input/output/number of words.
         */
        out.println("Enter path to input file name:");
        String input = in.nextLine();
        out.println("Enter path to output file name:");
        String output = in.nextLine();

        out.println("Enter number of words to include in cloud:");
        String number = in.nextLine();
        if (FormatChecker.canParseInt(number)) {
            int n = Integer.parseInt(number);
            /*
             * Checks for various invalid inputs.
             */
            Reporter.assertElseFatalError(input.length() > 0,
                    "Input cannot be empty.");
            Reporter.assertElseFatalError(output.length() > 0,
                    "Output cannot be empty.");
            Reporter.assertElseFatalError(!input.equals(output),
                    "Input and output files should be different.");
            Reporter.assertElseFatalError(
                    input.length() > DOTTXT && input
                            .substring(input.length() - DOTTXT).equals(".txt"),
                    "Input should be a .txt file.");
            Reporter.assertElseFatalError(
                    output.length() > DOTHTML
                            && output.substring(output.length() - DOTHTML)
                                    .equals(".html"),
                    "Output cannot be empty.");
            Reporter.assertElseFatalError(n > 0,
                    "Tag cloud size must be greater than 0.");

            SimpleReader inFile = new SimpleReader1L(input);
            SimpleWriter outFile = new SimpleWriter1L(output);
            /*
             * Parses words from input file after input is validated.
             */
            Set<Character> invalidChars = defineWord();
            Map<String, Integer> words = countWords(inFile, invalidChars);

            /*
             * Sorts words by count in a sorting machine. IE puts map pairs into
             * a sorting machine sorting by count.
             */

            SortingMachine<Map.Pair<String, Integer>> sortByCount = new SortingMachine4<>(
                    intcmp);

            while (words.size() > 0) {
                sortByCount.add(words.removeAny());
            }
            sortByCount.changeToExtractionMode();

            /*
             * Sorts words by alphabet in a sorting machine. IE puts map pairs
             * into a sorting machine sorting by word.
             */

            SortingMachine<Map.Pair<String, Integer>> sortByWords = new SortingMachine4<>(
                    strcmp);
            for (int i = 0; i < n && sortByCount.size() > 0; i++) {
                sortByWords.add(sortByCount.removeFirst());
            }
            sortByWords.changeToExtractionMode();

            /*
             * Constructs webpage.
             */

            printHeader(outFile, input, n);
            printBody(outFile, sortByWords);

            inFile.close();
            outFile.close();
        } else {
            out.print("Not a valid integer");
        }

        out.close();
        in.close();

    }

}
