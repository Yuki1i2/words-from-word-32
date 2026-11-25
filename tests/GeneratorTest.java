import java.util.*;

public class GeneratorTest {
    static List<?> poluchitUrovni(String dictionary, String main, int pass) {
        String input = "{\"dictionary\":[" + dictionary + "],\"mainWords\":[\"" + main
            + "\"],\"config\":{\"minSubWordLength\":3,\"minWordsToPass\":" + pass
            + ",\"difficultyThresholds\":{\"easyMaxMainWordLength\":7,\"mediumMaxMainWordLength\":10,"
            + "\"easyMaxSubWords\":15,\"mediumMaxSubWords\":30}}}";
        return (List<?>) ((Map<?,?>) Json.razobratJson(Generator.sozdatUrovni(input))).get("levels");
    }
    static void proverit(boolean ok) { if (!ok) throw new AssertionError(); }
    static void proveritPodslova(String dictionary, String main, int pass, String... expected) {
        List<?> levels = poluchitUrovni(dictionary, main, pass);
        if (expected.length < pass) { proverit(levels.isEmpty()); return; }
        proverit(levels.size() == 1);
        Map<?,?> level = (Map<?,?>) levels.get(0);
        proverit(level.size() == 5 && main.toUpperCase(Locale.ROOT).equals(level.get("mainWord")));
        proverit("1".equals(level.get("id").toString()) && String.valueOf(pass).equals(level.get("minWordsToPass").toString()));
        List<?> sub = (List<?>) level.get("subWords");
        proverit(sub.size() == expected.length);
        for (int i = 0; i < sub.size(); i++) {
            Map<?,?> entry = (Map<?,?>)sub.get(i);
            proverit(entry.size() == 2 && expected[i].equals(entry.get("text")) && "".equals(entry.get("hint")));
        }
    }
    public static void main(String[] args) {
        proverit(Collections.emptyMap().equals(Json.razobratJson("\uFEFF{}")));
        proveritPodslova("\"баран\"", "БАРАБАН", 1, "БАРАН");
        proveritPodslova("\"барбар\",\"раб\"", "БАРАБАН", 1, "РАБ");
        proveritPodslova("\"барабан\",\"раб\"", "БАРАБАН", 1, "РАБ");
        proveritPodslova("\"раб\",\"РАБ\"", "БАРАБАН", 1, "РАБ");
        proveritPodslova("\"на\",\"раб\"", "БАРАБАН", 1, "РАБ");
        proveritPodslova("\"ле\u0308н\",\"ЛЕН\"", "кЛёН", 1, "ЛЁН");
        proveritPodslova("\"кит\"", "БАРАБАН", 1);
        proveritPodslova("\"бар\",\"раб\"", "БАРАБАН", 2, "БАР", "РАБ");
        StringBuilder dictionary = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            dictionary.append("\"щ");
            int n = i;
            do { dictionary.append((char)('а' + n % 32)); n /= 32; } while (n > 0);
            dictionary.append("\",");
        }
        long start = System.nanoTime();
        proveritPodslova(dictionary.append("\"раб\"").toString(), "БАРАБАН", 1, "РАБ");
        System.out.println("100,001 entries: " + (System.nanoTime()-start)/1000000 + " ms");
        String[] mains = {"БАРАБАН","БАРАБАНЫ","ДЕМОНСТРАЦИЯ"};
        String[] difficulties = {"EASY","MEDIUM","HARD"};
        for (int i=0; i<mains.length; i++)
            proverit(difficulties[i].equals(((Map<?,?>)poluchitUrovni("\"раб\",\"сон\"", mains[i],1).get(0)).get("difficulty")));
        System.out.println("All tests passed.");
    }
}
