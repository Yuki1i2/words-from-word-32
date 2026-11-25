import java.util.*;
import java.util.regex.*;

final class Json {
    private final String source;
    private int pos;
    private Json(String source) { this.source = source; }

    static Object razobratJson(String source) {
        Json parser = new Json(source);
        if (source.startsWith("\uFEFF")) parser.pos++;
        Object result = parser.prochitatZnachenie(0);
        parser.propustitProbely();
        if (parser.pos != source.length()) throw parser.oshibkaFormata();
        return result;
    }
    private IllegalArgumentException oshibkaFormata() {
        return new IllegalArgumentException("Некорректный JSON, позиция " + (pos + 1));
    }
    private void propustitProbely() {
        while (pos < source.length() && " \r\n\t".indexOf(source.charAt(pos)) >= 0) pos++;
    }
    private boolean prinyatSimvol(char c) {
        propustitProbely();
        if (pos >= source.length() || source.charAt(pos) != c) return false;
        pos++;
        return true;
    }
    private Object prochitatZnachenie(int depth) {
        if (depth > 128) throw oshibkaFormata();
        if (prinyatSimvol('{')) {
            Map<String,Object> map = new LinkedHashMap<>();
            if (prinyatSimvol('}')) return map;
            do {
                String key = prochitatStroku();
                if (map.containsKey(key) || !prinyatSimvol(':')) throw oshibkaFormata();
                map.put(key, prochitatZnachenie(depth + 1));
            } while (prinyatSimvol(','));
            if (!prinyatSimvol('}')) throw oshibkaFormata();
            return map;
        }
        if (prinyatSimvol('[')) {
            List<Object> list = new ArrayList<>();
            if (prinyatSimvol(']')) return list;
            do { list.add(prochitatZnachenie(depth + 1)); } while (prinyatSimvol(','));
            if (!prinyatSimvol(']')) throw oshibkaFormata();
            return list;
        }
        if (pos < source.length() && source.charAt(pos) == '"') return prochitatStroku();
        for (String literal : Arrays.asList("true", "false", "null")) {
            if (source.startsWith(literal, pos)) {
                pos += literal.length();
                return "null".equals(literal) ? null : Boolean.valueOf(literal);
            }
        }
        Matcher m = NUMBER.matcher(source).region(pos, source.length());
        if (!m.lookingAt()) throw oshibkaFormata();
        pos = m.end();
        return new java.math.BigDecimal(m.group());
    }
    private static final Pattern NUMBER = Pattern.compile("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?");
    private String prochitatStroku() {
        if (!prinyatSimvol('"')) throw oshibkaFormata();
        StringBuilder b = new StringBuilder();
        while (pos < source.length()) {
            char c = source.charAt(pos++);
            if (c == '"') return b.toString();
            if (c < 32) throw oshibkaFormata();
            if (c == '\\') {
                if (pos >= source.length()) throw oshibkaFormata();
                c = source.charAt(pos++);
                if (c == 'u') {
                    int code = 0;
                    for (int i = 0; i < 4; i++) {
                        if (pos >= source.length()) throw oshibkaFormata();
                        int digit = Character.digit(source.charAt(pos++), 16);
                        if (digit < 0) throw oshibkaFormata();
                        code = code * 16 + digit;
                    }
                    c = (char)code;
                } else {
                    int index = "\"\\/bfnrt".indexOf(c);
                    if (index < 0) throw oshibkaFormata();
                    c = "\"\\/\b\f\n\r\t".charAt(index);
                }
            }
            b.append(c);
        }
        throw oshibkaFormata();
    }
}
