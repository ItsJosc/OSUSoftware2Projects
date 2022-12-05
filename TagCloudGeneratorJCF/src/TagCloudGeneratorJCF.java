import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import components.utilities.FormatChecker;

/**
 * Generators an html and css tag cloud from an input text file.
 *
 * @author Josiah Cheung
 * @author Craig Ngo
 *
 */
public final class TagCloudGeneratorJCF {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudGeneratorJCF() {
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
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    }

    /**
     * A class which compares two Integers using the Comparator class.
     */
    private static class IntegerCmp
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o2.getValue().compareTo(o1.getValue());
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
        Set<Character> invalid = new HashSet<>();
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
     * @throws IOException
     */
    private static Map<String, Integer> countWords(BufferedReader in,
            Set<Character> invalid) throws IOException {
        /*
         * Builds words char by char, parsing according to a set of separator
         * characters. Adds each word to a map and updates count of each word as
         * it is parsed.
         */

        StringBuilder builder = new StringBuilder();

        Map<String, Integer> m = new HashMap<>();
        String line = in.readLine();
        while (line != null) {

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (!invalid.contains(c)) {
                    builder.append(c);
                } else {
                    if (builder.length() != 0) {
                        String word = builder.toString().toLowerCase();
                        if (!m.containsKey(word)) {
                            m.put(word, 1);
                        } else {
                            m.put(word, m.get(word) + 1);
                        }
                        builder.delete(0, builder.length());
                    }
                }
            }
            line = in.readLine();
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
     * @throws IOException
     */
    private static void printHeader(PrintWriter output, String fileName,
            int cloudSize) throws IOException {
        output.println("<html>");
        output.write("<head>");

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
    private static void printBody(PrintWriter output,
            Queue<Map.Entry<String, Integer>> sorter) {

        /*
         * Get max and min counts.
         */
        int minCount = 0;
        int maxCount = 0;
        for (Map.Entry<String, Integer> p : sorter) {
            int count = p.getValue();
            if (count > maxCount) {
                maxCount = count;
            } else if (count < minCount) {
                minCount = count;
            }
        }

        /*
         * Print body
         */
        while (sorter.size() > 0) {
            Map.Entry<String, Integer> pair = sorter.poll();

            int count = pair.getValue();
            int fontSize = 1;
            /*
             * Apply font size algorithm
             */

            /*
             * Adjusted max font size using font buffer.
             */
            int adjMax = TagCloudGeneratorJCF.MAXFONT
                    - TagCloudGeneratorJCF.FONTBUFFER;

            if (count > minCount) {
                fontSize = (adjMax * (count - minCount))
                        / (maxCount - minCount);
            }
            fontSize += TagCloudGeneratorJCF.FONTBUFFER;

            output.println("<span style=\"cursor:default\" class=\"f" + fontSize
                    + "\" title=\"count:" + pair.getValue() + "\">"
                    + pair.getKey() + "</span>");
        }
        output.println("</p>\n" + "</div>\n" + "</body>\n" + "</html>\n" + "");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        /*
         * 1. Ask user for input file, output file, number of words N to
         * display. 2. Parse words from input file. 3. Sort parsed words by
         * alphabet. 4. Sort parsed words by count. 5. Construct webpage,
         * displaying N words according to css stylehsheet.
         */

        Scanner in = new Scanner(System.in);
        Comparator<Map.Entry<String, Integer>> strcmp = new StringCmp();
        Comparator<Map.Entry<String, Integer>> intcmp = new IntegerCmp();

        /*
         * Asks for input/output/number of words.
         */
        System.out.println("Enter path to input file name:");
        String input = in.nextLine();
        System.out.println("Enter path to output file name:");
        String output = in.nextLine();

        System.out.println("Enter number of words to include in cloud:");
        String number = in.nextLine();

        // TODO: REMOVE
        input = "./test/importance.txt";
        output = "./test/importance.html";
        int n = 100;
        // TODO: REMOVE

        if (true || FormatChecker.canParseInt(number)) {
//            int n = Integer.parseInt(number);
            /*
             * Checks for various invalid inputs.
             */
            BufferedReader inFile = new BufferedReader(new FileReader(input));

            PrintWriter outFile = new PrintWriter(
                    new BufferedWriter(new FileWriter(output)));

            /*
             * Parses words from input file after input is validated.
             */
            Set<Character> invalidChars = defineWord();
            Map<String, Integer> words = countWords(inFile, invalidChars);

            /*
             * Sorts words by count in a sorting machine. IE puts map pairs into
             * a sorting machine sorting by count.
             */
            Queue<Map.Entry<String, Integer>> sortByCount = new PriorityQueue<>(
                    intcmp);
            sortByCount.addAll(words.entrySet());

            /*
             * Sorts words by alphabet in a sorting machine. IE puts map pairs
             * into a sorting machine sorting by word.
             */
            Queue<Map.Entry<String, Integer>> sortByWords = new PriorityQueue<>(
                    strcmp);
            for (int i = 0; i < n && sortByCount.size() > 0; i++) {
                sortByWords.add(sortByCount.poll());
            }

            /*
             * Constructs webpage.
             */
            printHeader(outFile, input, n);
            printBody(outFile, sortByWords);

            inFile.close();
            outFile.close();
        } else {
            System.out.print("Not a valid integer");
        }

        in.close();

    }

}
