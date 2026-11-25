# Журнал запросов и ответов ИИ по разработке

ИИ использовался при реализации генератора на Java, написании тестов, диагностике ошибок и подготовке документации. Ниже приведена переписка по техническим решениям. Сведения о входе в аккаунты и личные данные в публичную выгрузку не включены.

## Выбор Java и проверка сборки

**Запрос**

> Это если что нужно на Java делать. Ты на Java сделал?

**Ответ**

> Первую версию я сделал на JavaScript. Перепишу генератор на Java и проверю его на тех же девяти сценариях.

**Итоговый ответ после проверки** (ссылки на локальные файлы опущены)

> Изменено: первая версия была на JavaScript. Теперь проект переписан на **Java** с интерфейсом Swing.
>
> Проверено: JAR запускается на Java 8. Все 9 сценариев прошли, включая словарь из 100 001 записи.

## Названия методов

**Фрагмент запроса**

> Ты короче его называй типа русскими словами метод, но английскими буквами, чтобы можно было прочитать русскими. Типа метод сам назывался английскими буквами, но из английских букв складывалось русское слово, чтобы можно было понять, что за метод

**Ответ**

> Переименую методы в Java-проекте транслитом и обновлю файл об использовании ИИ по фактической работе над кодом и тестами.

В исходниках результат виден в методах `sozdatUrovni`, `podschitatBukvy` и `khvataetBukv`. Имена стандартных методов Java, включая `main`, сохранены.

## Подготовка задания на ревью

**Запрос**

> Дай мне промпт для другой нейросети, чтобы она мне придумала, короче, список этих, список правок я тебе скину.

**Ответ** (текст предложенного промпта)

```text
Посмотри мой учебный Java-проект: https://github.com/Yuki1i2/words-from-word
Это генератор уровней игры «Слова из слова» на Java 8 со Swing. Изучи исходники и существующие тесты. Если не можешь открыть репозиторий, попроси меня прислать файлы.

Предложи небольшие полезные правки, каждую из которых можно реализовать и проверить отдельно. Рассмотри обработку JSON, правила отбора подслов, сообщения об ошибках, интерфейс и тесты. Сохрани совместимость с Java 8 и формат выходного JSON.

Для каждой правки укажи: файл или метод → что сейчас происходит → что изменить → как проверить результат. Для ошибок приведи пример входных данных и ожидаемое поведение. Не предлагай то, что уже реализовано, и не придумывай ошибки ради количества.

Выведи нумерованный список по приоритету. Код пока не пиши.
```

Адрес в цитате указывает на прежний репозиторий. Ниже сохранён список, который пользователь затем передал на проверку.

## Предложения, переданные на проверку

<details>
<summary>Открыть исходный список из 21 пункта</summary>

```text
1. Json.java:13-21 — prochitatChislo range validation
Current: Silently catches ArithmeticException from BigDecimal.intValueExact(), then throws generic IllegalArgumentException without showing the actual value.
Change: Validate BigDecimal int range explicitly before intValueExact(), include actual value in error message.
Verify: JSON with 2147483648 or 3.5 as config value → clear error "Недопустимое значение config, ожидалось целое число: 2147483648" instead of generic message.
2. Generator.java:54 — Difficulty thresholds with value 0
Current: If easyMaxMainWordLength=0 and easyMaxSubWords=0, no word qualifies for EASY (all have length > 0), logic falls through to MEDIUM/HARD incorrectly.
Change: Treat 0 as "not set" and fall back to example defaults (7/15 for lengths/Counts).
Verify: Config with easyMaxMainWordLength:0, easyMaxSubWords:0, mediumMaxMainWordLength:10, mediumMaxSubWords:30 → main words length ≤10 get MEDIUM difficulty, not skipped.
3. Generator.java:70 — Generate level even when < minWordsToPass matches
Current: if (matches.size() < pass) continue; completely skips level generation. minWordsToPass in output is never reached if check fails.
Change: Still generate level with available matches, set minWordsToPass to actual count (or add warning flag).
Verify: Input with 3 matching subwords and minWordsToPass:8 → level generated with 3 subwords and minWordsToPass:3 in output, not omitted.
4. Main.java:39-43 — Better error dialog
Current: output.setText(""); save.setEnabled(false); status.setText(e.getMessage()) — long exceptions may be truncated or unclear.
Change: Use JOptionPane.showMessageDialog with error icon, trim and format the message.
Verify: Input causing IllegalArgumentException with long message → dialog shows formatted error with "Ошибка" icon, not just status bar text.
5. GeneratorTest.java — Boundary difficulty tests
Current: No tests for main word length exactly at easyMaxMainWordLength or mediumMaxMainWordLength thresholds.
Add: Test cases where main.length() == easyMaxMainWordLength → EASY, main.length() == mediumMaxMainWordLength → MEDIUM (if subwords sufficient).
Verify: Test run → new assertions pass for exact threshold boundaries.
6. Json.java:50-54 — Case-insensitive literal parsing
Current: source.startsWith(literal, pos) checks exact case: "TRUE" or "False" not recognized.
Change: Normalize case before comparison: source.regionMatches(true, pos, literal, 0, literal.length()).
Verify: JSON "TRUE" → parsed as Boolean.TRUE; "False" → Boolean.FALSE; "NULL" → null.
7. Main.java:54-65 — Save validation
Current: Saves output.getText() even if empty (user clicked Generate but no result yet).
Change: Check if output is non-empty before saving; if empty, show "Нothing to save" dialog.
Verify: Generate levels → Save → levels.json created. Click Generate, don't change input, Save → dialog "Данные не изменены, сохранять?" or similar.
8. Generator.java:67 — Reorder subword checks
Current: Length check word.length() <= main.length() before letter availability khavataetBukv().
Change: Swap order — validate letter availability first, then length. More intuitive: "can this word be formed?" before "is it too long?".
Verify: Test with words having correct length but wrong letters → correctly rejected; words with correct letters but greater length → correctly rejected.
9. Json.java:62 — NUMBER pattern anchoring
Current: Matcher m = NUMBER.matcher(source).region(pos, source.length()) — could match partial numbers in longer strings.
Change: Append end anchor or use lookAt with bounds check: m.region(pos, source.length()).lookingAt() + verify m.end() == source.length() or next char is ,/}/\n.
Verify: JSON with trailing content after number → parser throws format error instead of silently consuming partial number.
10. Main.java:77 — Missing example.json handling
Current: InputStream in = Main.class.getResourceAsStream("/example.json") — if null, Scanner throws NPE.
Change: Add null check: if (in == null) { example = "{}"; log warning } or show UI error.
Verify: Running without example.json on classpath → graceful fallback to empty JSON, not crash.
11. Generator.java:48-49 — minSubWordLength validation
Current: No validation that minSubWordLength is reasonable relative to main words.
Change: Add check: if minSubWordLength > max(mainWord lengths), warn or adjust to reasonable default.
Verify: Config with minSubWordLength:10 and main words length 3-5 → warning "minSubWordLength exceeds main word lengths, adjusting" or auto-adjust.
12. GeneratorTest.java — Words with 'Ё' test
Current: No test for the 'Ё' character (code 32 in bukvy array).
Add: Test with word "ЛЁДОМ" and main "ЛЁДОМ" → should include subword "ЛЁДОМ" (with 'Ѕ' normalized to 'Е' per NFC).
Verify: Test run → "ЛЁДОМ" recognized and processed correctly.
13. Main.java:13-17 — Font fallback for panel title
Current: Hardcoded new Font("Serif", Font.BOLD, 34) — may fall poorly on systems without Serif.
Change: Use Font.SANS_SERIF or Font.MONOSPACED with fallback, or Font.getFont(String) for platform font.
Verify: UI renders → title visible with reasonable font, not default sans-serif or missing.
14. Generator.java:57-58 — Pre-filter dictionary candidates
Current: All dictionary words ≥ minLength added to candidates map, then filtered per main word.
Change: Pre-filter: remove words with letter counts exceeding any main word's letter counts (quick impossibility check).
Verify: Large dictionary (100k entries) with small main words → faster generation, same output.
15. Main.java:21-26 — setColumns() on text areas
Current: Only setFont() set; preferred width depends on font/monitor, may look awkward.
Change: Add area.setColumns(20) (or calculated width) for consistent text area sizing.
Verify: UI launch → both text areas have consistent column width, layout looks intentional.
16. Json.java:23-27 — propustitProbely additional whitespace
Current: Only skips " \r\n\t". JSON spec also allows form feed \f, vertical tab \v.
Change: Add \f and \v to the skip set: " \r\n\t\f\v".
Verify: JSON with form feed characters → parsed correctly without error.
17. Generator.java:80 — Output JSON newline consistency
Current: Returns "{\n  \"levels\": [\n" + levels + "\n  ]\n}\n" — trailing newline always present.
Change: Ensure consistent format across all output paths (including error returns).
Verify: Multiple sozdatUrovni calls → each output ends with single \n, no extra blank lines.
18. GeneratorTest.java — minWordsToPass=0 edge case
Current: No test for minWordsToPass:0 with various subword counts.
Add: Test that minWordsToPass:0 always passes (level generated even with 0 subwords).
Verify: Test run → level generated with 0 subwords, minWordsToPass:0 in output.
19. Generator.java:76 — Defensive JSON escaping in output
Current: subWords.add("        {\"text\": \"" + word + "\", \"hint\": \"\"}") — words validated to А-ЯЁ only, but defensive.
Change: Use StringEscapeUtils.escapeJson(word) or manual escaping for quotes/backslashes.
Verify: If validation ever allows other characters → output JSON remains valid.
20. Main.java:45-52 — Debounce document listener
Current: Every keystroke immediately triggers Generator.sozdatUrovni(), potentially slow for large inputs.
Change: Add simple debounce (e.g., 300ms timer) or batch changes.
Verify: Rapid typing in input field → generation triggers once after typing stops, not on every keystroke.
21. Json.java:11-14 — BOM handling improvements
Current: if (source.startsWith("\uFEFF")) parser.pos++; — only handles BOM at very start.
Change: Trim BOM more robustly and document that parser expects valid JSON.
Verify: JSON with BOM → parsed correctly; JSON without BOM → unchanged behavior.
```

</details>

**Уточнение ИИ**

> В списке есть изменения правил задания: создавать уровни ниже порога, принимать нестандартный JSON и включать главное слово в подслова. Как поступить?

**Фрагмент ответа пользователя**

> То есть чтобы он точно по заданию работал. У нас есть вот первое задание, поэтому задание работать.

**Ответ ИИ о правилах генерации** (фрагмент)

> Сохраняю правила первого задания: уровень ниже порога пропускается, главное слово исключается из подслов, JSON остаётся строгим.

## Что подтвердилось при проверке

| Предложение | Результат проверки |
|---|---|
| Улучшить ошибки числовых настроек | Для дробного значения и выхода за диапазон тест проверяет, что сообщение содержит имя поля и переданное значение. |
| Проверить границы сложности | Добавлены проверки точного предела длины главного слова и переходов EASY → MEDIUM → HARD при увеличении числа подслов. |
| Изменить обработку Ё | Проверка со словарём «лёд», «лед», «лёдом» и главным словом «ЛЁДОМ» подтверждает единственное совпадение «ЛЁД». |
| Проверить нулевой порог прохождения | Проверены оба случая: подходящих подслов нет или найдено одно. При пороге 0 уровень создаётся в обоих случаях. |
| Отсеять неподходящие слова раньше | Фильтр учитывает максимальную длину и доступные буквы главных слов. Отдельный тест проверяет набор подслов для каждого из двух уровней. |

Проверки можно повторить по командам из [README](README.md). В репозитории есть [тесты](tests/GeneratorTest.java), [входной файл](example.json) и [ожидаемый результат](levels.json).
