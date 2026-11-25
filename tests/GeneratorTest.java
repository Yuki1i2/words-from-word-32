import java.util.*;

public class GeneratorTest {
    static List<?> poluchitUrovni(String dictionary, String main, int pass) {
        String input = sformirovatVhod(dictionary, main, pass);
        return (List<?>) ((Map<?,?>) Json.razobratJson(Generator.sozdatUrovni(input))).get("levels");
    }
    static String sformirovatVhod(String dictionary, String main, int pass) {
        return "{\"dictionary\":[" + dictionary + "],\"mainWords\":[\"" + main
            + "\"],\"config\":{\"minSubWordLength\":3,\"minWordsToPass\":" + pass
            + ",\"difficultyThresholds\":{\"easyMaxMainWordLength\":7,\"mediumMaxMainWordLength\":10,"
            + "\"easyMaxSubWords\":15,\"mediumMaxSubWords\":30}}}";
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
    static void proveritGranitsyPoChisluPodslov() {
        String[] slova = {"раб", "бар", "баран"};
        String[] slozhnost = {"EASY", "MEDIUM", "HARD"};
        StringJoiner slovar = new StringJoiner(",");
        for (int i = 0; i < slova.length; i++) {
            slovar.add("\"" + slova[i] + "\"");
            String vhod = sformirovatVhod(slovar.toString(), "БАРАБАН", 1)
                    .replace("\"easyMaxSubWords\":15", "\"easyMaxSubWords\":1")
                    .replace("\"mediumMaxSubWords\":30", "\"mediumMaxSubWords\":2");
            Map<?,?> rezultat = (Map<?,?>) Json.razobratJson(Generator.sozdatUrovni(vhod));
            Map<?,?> uroven = (Map<?,?>) ((List<?>) rezultat.get("levels")).get(0);
            proverit(((List<?>) uroven.get("subWords")).size() == i + 1);
            proverit(slozhnost[i].equals(uroven.get("difficulty")));
        }
    }

    public static void main(String[] args) {
        proveritGranitsyPoChisluPodslov();
        proverit(Collections.emptyMap().equals(Json.razobratJson("\uFEFF{}")));
        proveritPodslova("\"баран\"", "БАРАБАН", 1, "БАРАН");
        proveritPodslova("\"барбар\",\"раб\"", "БАРАБАН", 1, "РАБ");
        proveritPodslova("\"барабан\",\"раб\"", "БАРАБАН", 1, "РАБ");
        proveritPodslova("\"раб\",\"РАБ\"", "БАРАБАН", 1, "РАБ");
        proveritPodslova("\"на\",\"раб\"", "БАРАБАН", 1, "РАБ");
        proveritPodslova("\"ле\u0308н\",\"ЛЕН\"", "кЛёН", 1, "ЛЁН");
        proveritPodslova("\"кит\"", "БАРАБАН", 1);
        proveritPodslova("\"бар\",\"раб\"", "БАРАБАН", 2, "БАР", "РАБ");
        String obrazets = sformirovatVhod("\"раб\"", "БАРАБАН", 1);
        for (String nevernoe : Arrays.asList("3.5", "2147483648", "-1")) {
            try {
                Generator.sozdatUrovni(obrazets.replace("\"minWordsToPass\":1", "\"minWordsToPass\":" + nevernoe));
                throw new AssertionError("Принято недопустимое значение " + nevernoe);
            } catch (IllegalArgumentException e) {
                proverit(e.getMessage().contains("minWordsToPass") && e.getMessage().contains(nevernoe));
            }
        }
        proveritPodslova("\"кит\"", "БАРАБАН", 0);
        proveritPodslova("\"раб\"", "БАРАБАН", 0, "РАБ");
        proveritPodslova("\"лёд\",\"лед\",\"лёдом\"", "ЛЁДОМ", 1, "ЛЁД");
        String dvaSlova = sformirovatVhod("\"бар\",\"кот\",\"бат\"", "БАРАБАН", 1)
                .replace("[\"БАРАБАН\"]", "[\"БАРАБАН\",\"КОТИК\"]");
        List<?> para = (List<?>) ((Map<?,?>)Json.razobratJson(Generator.sozdatUrovni(dvaSlova))).get("levels");
        proverit(para.size() == 2);
        for (int i = 0; i < para.size(); i++) {
            List<?> podslova = (List<?>)((Map<?,?>)para.get(i)).get("subWords");
            proverit(podslova.size() == 1);
            proverit((i == 0 ? "БАР" : "КОТ").equals(((Map<?,?>)podslova.get(0)).get("text")));
        }
        for (String oshibka : Arrays.asList("12abc", "[1e]", "TRUE", "False", "NULL", "[1,\f2]", "[\u000B0]")) {
            try {
                Json.razobratJson(oshibka);
                throw new AssertionError("Принят неверный JSON: " + oshibka);
            } catch (IllegalArgumentException e) {
                proverit(e.getMessage().contains("JSON"));
            }
        }
        String pustoy = Generator.sozdatUrovni(sformirovatVhod("", "БАРАБАН", 1));
        proverit("{\n  \"levels\": []\n}\n".equals(pustoy));
        String gotovyy = Generator.sozdatUrovni(obrazets);
        proverit(gotovyy.endsWith("}\n") && !gotovyy.contains("\r"));
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
        String[] mains = {"БАРАБАН","БАРАБАНЫ","ВЗАИМНОСТЬ","ДЕМОНСТРАЦИЯ"};
        String[] difficulties = {"EASY","MEDIUM","MEDIUM","HARD"};
        for (int i=0; i<mains.length; i++)
            proverit(difficulties[i].equals(((Map<?,?>)poluchitUrovni("\"раб\",\"сон\"", mains[i],1).get(0)).get("difficulty")));
        System.out.println("All tests passed.");
    }
}
