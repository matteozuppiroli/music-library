package com.matteo.musiclibrary;

/**
 * Deserializzatore custom da JSON a LibraryNode.
 *
 * Parser ricorsivo che riconosce due tipi di oggetti ("track" e "collection")
 * e li traduce nelle classi del dominio attraverso la LibraryNodeFactory,
 * garantendo che ogni oggetto sia sottoposto alle stesse validazioni dei
 * costruttori manuali.
 *
 * NB: questo e' un parser semplificato adatto al formato prodotto da JsonSerializer.
 * Non gestisce ogni edge case del JSON arbitrario (es. spazi insoliti tra token).
 */
public class JsonDeserializer {

    private final String source;
    private int pos;

    public JsonDeserializer(String source) {
        if (source == null) {
            throw new IllegalArgumentException("Il sorgente JSON non puo' essere nullo");
        }
        this.source = source;
        this.pos = 0;
    }

    /**
     * Punto di ingresso pubblico: parsa un LibraryNode dalla stringa JSON.
     */
    public static LibraryNode fromJson(String json) {
        return new JsonDeserializer(json).parseNode();
    }

    // ===== Parsing ricorsivo =====

    private LibraryNode parseNode() {
        skipWhitespace();
        expect('{');
        java.util.Map<String, Object> obj = parseObject();
        return buildNode(obj);
    }

    @SuppressWarnings("unchecked")
    private LibraryNode buildNode(java.util.Map<String, Object> obj) {
        Object typeObj = obj.get("type");
        if (!(typeObj instanceof String)) {
            throw new IllegalArgumentException("Campo 'type' mancante o non stringa");
        }
        String type = (String) typeObj;

        if ("track".equals(type)) {
            String title = asString(obj.get("title"));
            String artist = asString(obj.get("artist"));
            int bpm = asInt(obj.get("bpm"));
            String key = asStringOrNull(obj.get("key"));
            int duration = asInt(obj.get("durationSeconds"));
            String genre = asStringOrNull(obj.get("genre"));
            return LibraryNodeFactory.createTrack(title, artist, bpm, key, duration, genre);
        }

        if ("collection".equals(type)) {
            String name = asString(obj.get("name"));
            Collection c = LibraryNodeFactory.createCollection(name);
            Object childrenObj = obj.get("children");
            if (childrenObj instanceof java.util.List) {
                java.util.List<Object> children = (java.util.List<Object>) childrenObj;
                for (Object child : children) {
                    if (child instanceof java.util.Map) {
                        c.add(buildNode((java.util.Map<String, Object>) child));
                    }
                }
            }
            return c;
        }

        throw new IllegalArgumentException("Tipo di nodo sconosciuto: " + type);
    }

    private java.util.Map<String, Object> parseObject() {
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            Object value = parseValue();
            map.put(key, value);
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                pos++;
            } else if (c == '}') {
                pos++;
                return map;
            } else {
                throw new IllegalArgumentException("Atteso ',' o '}' a posizione " + pos);
            }
        }
    }

    private Object parseValue() {
        skipWhitespace();
        char c = peek();
        if (c == '"') return parseString();
        if (c == '{') { pos++; return parseObject(); }
        if (c == '[') return parseArray();
        if (c == 'n') return parseNull();
        if (c == '-' || Character.isDigit(c)) return parseNumber();
        throw new IllegalArgumentException("Token inatteso a posizione " + pos + ": " + c);
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < source.length()) {
            char c = source.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c == '\\' && pos < source.length()) {
                char next = source.charAt(pos++);
                if (next == '"' || next == '\\') sb.append(next);
                else sb.append('\\').append(next);
            } else {
                sb.append(c);
            }
        }
        throw new IllegalArgumentException("Stringa non terminata");
    }

    private Integer parseNumber() {
        int start = pos;
        if (peek() == '-') pos++;
        while (pos < source.length() && Character.isDigit(source.charAt(pos))) pos++;
        return Integer.parseInt(source.substring(start, pos));
    }

    private java.util.List<Object> parseArray() {
        expect('[');
        java.util.List<Object> list = new java.util.ArrayList<>();
        skipWhitespace();
        if (peek() == ']') { pos++; return list; }
        while (true) {
            skipWhitespace();
            list.add(parseValue());
            skipWhitespace();
            char c = peek();
            if (c == ',') pos++;
            else if (c == ']') { pos++; return list; }
            else throw new IllegalArgumentException("Atteso ',' o ']' a posizione " + pos);
        }
    }

    private Object parseNull() {
        if (source.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        throw new IllegalArgumentException("Atteso 'null' a posizione " + pos);
    }

    // ===== Utility =====

    private void skipWhitespace() {
        while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) pos++;
    }

    private char peek() {
        if (pos >= source.length()) throw new IllegalArgumentException("Fine input inattesa");
        return source.charAt(pos);
    }

    private void expect(char c) {
        if (peek() != c) {
            throw new IllegalArgumentException("Atteso '" + c + "' a posizione " + pos + ", trovato '" + peek() + "'");
        }
        pos++;
    }

    private static String asString(Object o) {
        if (!(o instanceof String)) throw new IllegalArgumentException("Atteso stringa");
        return (String) o;
    }

    private static String asStringOrNull(Object o) {
        return o == null ? null : asString(o);
    }

    private static int asInt(Object o) {
        if (!(o instanceof Integer)) throw new IllegalArgumentException("Atteso intero");
        return (Integer) o;
    }
}