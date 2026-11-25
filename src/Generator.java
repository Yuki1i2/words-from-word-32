import java.util.*;
import java.text.Normalizer;
import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class Generator {
    private static final Pattern WORD = Pattern.compile("[А-ЯЁ]+");

    static Map<?,?> prochitatObekt(Object value, String name) {
        if (!(value instanceof Map)) throw new IllegalArgumentException("Ожидался объект " + name);
        return (Map<?,?>) value;
    }
    private static int prochitatChislo(Map<?,?> map, String key, int minimum) {
        Object value = map.get(key);
        if (value instanceof BigDecimal) {
            BigDecimal n = (BigDecimal)value;
            if (n.compareTo(BigDecimal.valueOf(minimum)) >= 0
                    && n.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0
                    && n.stripTrailingZeros().scale() <= 0) return n.intValue();
        }
        throw new IllegalArgumentException("Для " + key + " ожидалось целое число от " + minimum
                + " до " + Integer.MAX_VALUE + ", получено: " + value);
    }
    private static Set<String> prochitatSlova(Map<?,?> input, String key) {
        Object array = input.get(key);
        if (!(array instanceof List)) throw new IllegalArgumentException("Ожидался массив " + key);
        Set<String> result = new LinkedHashSet<>();
        for (Object item : (List<?>)array) {
            if (!(item instanceof String)) throw new IllegalArgumentException("Ожидалось слово в " + key);
            String word = Normalizer.normalize(((String)item).trim(), Normalizer.Form.NFC).toUpperCase(Locale.ROOT);
            if (!WORD.matcher(word).matches()) throw new IllegalArgumentException("Недопустимое слово: " + word);
            result.add(word);
        }
        return result;
    }
    private static int[] podschitatBukvy(String word) {
        int[] counts = new int[33];
        for (char c : word.toCharArray()) counts[c == 'Ё' ? 32 : c - 'А']++;
        return counts;
    }
    private static boolean khvataetBukv(int[] candidate, int[] main) {
        for (int i = 0; i < main.length; i++) if (candidate[i] > main[i]) return false;
        return true;
    }
    public static String sozdatUrovni(String inputJson) {
        Map<?,?> input = prochitatObekt(Json.razobratJson(inputJson), "input");
        Map<?,?> config = prochitatObekt(input.get("config"), "config");
        Map<?,?> t = prochitatObekt(config.get("difficultyThresholds"), "difficultyThresholds");
        int minLength = prochitatChislo(config, "minSubWordLength", 1);
        int pass = prochitatChislo(config, "minWordsToPass", 0);
        int easyLength = prochitatChislo(t, "easyMaxMainWordLength", 0);
        int mediumLength = prochitatChislo(t, "mediumMaxMainWordLength", 0);
        int easyCount = prochitatChislo(t, "easyMaxSubWords", 0);
        int mediumCount = prochitatChislo(t, "mediumMaxSubWords", 0);
        if (easyLength > mediumLength || easyCount > mediumCount)
            throw new IllegalArgumentException("Порог EASY превышает MEDIUM.");
        Map<String,int[]> candidates = new LinkedHashMap<>();
        for (String word : prochitatSlova(input, "dictionary"))
            if (word.length() >= minLength) candidates.put(word, podschitatBukvy(word));

        StringJoiner levels = new StringJoiner(",\n");
        int id = 0;
        for (String main : prochitatSlova(input, "mainWords")) {
            int[] available = podschitatBukvy(main);
            List<String> matches = new ArrayList<>();
            for (Map.Entry<String,int[]> candidate : candidates.entrySet()) {
                String word = candidate.getKey();
                if (!word.equals(main) && word.length() <= main.length() && khvataetBukv(candidate.getValue(), available))
                    matches.add(word);
            }
            if (matches.size() < pass) continue;
            String difficulty = "HARD";
            if (main.length() <= easyLength && matches.size() <= easyCount) difficulty = "EASY";
            else if (main.length() <= mediumLength && matches.size() <= mediumCount) difficulty = "MEDIUM";
            // All interpolated words are restricted to the Russian alphabet by prochitatSlova().
            StringJoiner subWords = new StringJoiner(",\n");
            for (String word : matches) subWords.add("        {\"text\": \"" + word + "\", \"hint\": \"\"}");
            levels.add("    {\n      \"id\": " + (++id) + ",\n      \"mainWord\": \"" + main
                + "\",\n      \"difficulty\": \"" + difficulty + "\",\n      \"minWordsToPass\": "
                + pass + ",\n      \"subWords\": [\n" + subWords + "\n      ]\n    }");
        }
        return "{\n  \"levels\": [\n" + levels + "\n  ]\n}\n";
    }
}
