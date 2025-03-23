import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class PatientDataAnonymizer {
    // Precompiled regex patterns for anonymization
    private static final Pattern FULL_NAME_PATTERN = Pattern.compile("\\b(Dr|Mr|Mrs|Miss|Ms)\\.?\\s+[A-Z][a-z]+\\s+[A-Z][a-z]+\\b");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("\\b\\d+\\s+[A-Z][a-z]+(?:\\s[A-Z][a-z]+)*(?:\\s(?:Avenue|Street|Boulevard|Road|Lane|Drive|Way))?(?:\\s(?:Apartment|Suite|Unit)\\s?\\d+)?(?:,\\s+[A-Z][a-z]+(?:\\s[A-Z][a-z]+)*)(?:,\\s+[A-Z]{2})?");
    private static final Pattern AGE_PATTERN = Pattern.compile("\\b(aged\\s\\d{1,3}|(\\d{1,3})[- ]?(year[s]?[- ]old|years old))\\b");
    private static final Pattern CAPITALIZED_WORD_PATTERN = Pattern.compile("\\b[A-Z][a-z]+\\b");

    // Titles to be ignored in standalone word detection
    private static final Set<String> IGNORED_TITLES = Set.of("Dr", "Mr", "Mrs", "Miss", "Ms");

    public static void main(String[] args) {
        String inputFileName = "PatientNotes.txt";  // Use relative or absolute path for server compatibility
        String anonymizedOutputFile = "AnonymizedNotes.txt";
        String mappingOutputFile = "MappingDocument.txt";

        try {
            // Use streaming to process the file line-by-line
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
                 BufferedWriter anonymizedWriter = new BufferedWriter(new FileWriter(anonymizedOutputFile));
                 BufferedWriter mappingWriter = new BufferedWriter(new FileWriter(mappingOutputFile))) {

                Map<String, String> globalMapping = new ConcurrentHashMap<>();
                Set<String> detectedFullNames = ConcurrentHashMap.newKeySet();

                int nameCounter = 1; // Counter for full names (1.x)
                int addressCounter = 1; // Counter for addresses (2.x)
                int ageCounter = 1; // Counter for ages (3.x)
                int standaloneCounter = 1; // Counter for standalone words (4.x)

                String line;
                while ((line = reader.readLine()) != null) {
                    StringBuilder lineMapping = new StringBuilder();

                    // Process full names
                    line = processPattern(line, FULL_NAME_PATTERN, "1", nameCounter, globalMapping, lineMapping, detectedFullNames);
                    nameCounter = updateCounter(globalMapping, "1");

                    // Process addresses
                    line = processPattern(line, ADDRESS_PATTERN, "2", addressCounter, globalMapping, lineMapping, null);
                    addressCounter = updateCounter(globalMapping, "2");

                    // Process ages
                    line = processPattern(line, AGE_PATTERN, "3", ageCounter, globalMapping, lineMapping, null);
                    ageCounter = updateCounter(globalMapping, "3");

                    // Process standalone words
                    line = processStandaloneWords(line, detectedFullNames, "4", standaloneCounter, globalMapping, lineMapping);
                    standaloneCounter = updateCounter(globalMapping, "4");

                    // Write anonymized line
                    anonymizedWriter.write(line);
                    anonymizedWriter.newLine();

                    // Write mappings for this line
                    if (lineMapping.length() > 0) {
                        mappingWriter.write(lineMapping.toString());
                        mappingWriter.newLine();
                    }
                }

                System.out.println("Anonymization complete. Check 'AnonymizedNotes.txt' and 'MappingDocument.txt'.");

            }
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    // Process and anonymize text based on a regex pattern
    private static String processPattern(String text, Pattern pattern, String prefix, int counter,
                                         Map<String, String> globalMapping, StringBuilder lineMapping, Set<String> detectedSet) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String match = matcher.group();
            if (!globalMapping.containsKey(match)) {
                String replacement = prefix + "." + counter++;
                globalMapping.put(match, replacement);
                if (lineMapping != null) {
                    lineMapping.append(replacement).append(" -> ").append(match).append(System.lineSeparator());
                }
                if (detectedSet != null) {
                    detectedSet.add(match);
                }
            }
            text = text.replaceAll(Pattern.quote(match), globalMapping.get(match));
        }
        return text;
    }

    // Process standalone capitalized words
    private static String processStandaloneWords(String text, Set<String> detectedFullNames, String prefix, int counter,
                                                 Map<String, String> globalMapping, StringBuilder lineMapping) {
        Matcher matcher = CAPITALIZED_WORD_PATTERN.matcher(text);
        while (matcher.find()) {
            String match = matcher.group();
            if (!globalMapping.containsKey(match) && !IGNORED_TITLES.contains(match) &&
                detectedFullNames.stream().anyMatch(fullName -> fullName.contains(match))) {
                String replacement = prefix + "." + counter++;
                globalMapping.put(match, replacement);
                if (lineMapping != null) {
                    lineMapping.append(replacement).append(" -> ").append(match).append(System.lineSeparator());
                }
                text = text.replaceAll("\\b" + Pattern.quote(match) + "\\b", replacement);
            }
        }
        return text;
    }

    // Update counter for the specified prefix
    private static int updateCounter(Map<String, String> globalMapping, String prefix) {
        return (int) globalMapping.values().stream()
                .filter(value -> value.startsWith(prefix + "."))
                .count() + 1;
    }
}
