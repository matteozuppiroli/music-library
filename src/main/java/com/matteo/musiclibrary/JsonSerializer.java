package com.matteo.musiclibrary;

/**
 * Serializzatore custom per LibraryNode verso JSON.
 *
 * Implementa una serializzazione ricorsiva che riflette la struttura del Composite:
 * una Collection serializza se stessa e ricorsivamente i suoi figli, una Track
 * serializza i propri metadati come oggetto piatto.
 *
 * Scelta progettuale: parser custom invece di Gson/Jackson per:
 *  - evitare dipendenze esterne (riduce superficie di attacco e accoppiamento)
 *  - dominio ristretto e ben definito, non serve un parser general-purpose
 *  - dimostra padronanza dei pattern di serializzazione ricorsiva
 *
 * Classe utility: tutti i metodi sono statici.
 */
public class JsonSerializer {

    private JsonSerializer() {
        throw new UnsupportedOperationException("Classe utility, non istanziabile");
    }

    /**
     * Serializza un LibraryNode in JSON formattato (con indentazione).
     */
    public static String toJson(LibraryNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Il nodo da serializzare non puo' essere nullo");
        }
        StringBuilder sb = new StringBuilder();
        serializeNode(node, sb, 0);
        return sb.toString();
    }

    // ===== Logica ricorsiva =====

    private static void serializeNode(LibraryNode node, StringBuilder sb, int indent) {
        if (node instanceof Track) {
            serializeTrack((Track) node, sb, indent);
        } else if (node instanceof Collection) {
            serializeCollection((Collection) node, sb, indent);
        } else {
            throw new IllegalArgumentException("Tipo di nodo non supportato: " + node.getClass().getName());
        }
    }

    private static void serializeTrack(Track t, StringBuilder sb, int indent) {
        String pad = indent(indent);
        String inner = indent(indent + 1);
        sb.append(pad).append("{\n");
        sb.append(inner).append("\"type\": \"track\",\n");
        sb.append(inner).append("\"title\": ").append(jsonString(t.getTitle())).append(",\n");
        sb.append(inner).append("\"artist\": ").append(jsonString(t.getArtist())).append(",\n");
        sb.append(inner).append("\"bpm\": ").append(t.getBpm()).append(",\n");
        sb.append(inner).append("\"key\": ").append(jsonStringOrNull(t.getKey())).append(",\n");
        sb.append(inner).append("\"durationSeconds\": ").append(t.getDurationSeconds()).append(",\n");
        sb.append(inner).append("\"genre\": ").append(jsonStringOrNull(t.getGenre())).append("\n");
        sb.append(pad).append("}");
    }

    private static void serializeCollection(Collection c, StringBuilder sb, int indent) {
        String pad = indent(indent);
        String inner = indent(indent + 1);
        sb.append(pad).append("{\n");
        sb.append(inner).append("\"type\": \"collection\",\n");
        sb.append(inner).append("\"name\": ").append(jsonString(c.getName())).append(",\n");
        sb.append(inner).append("\"children\": [");

        java.util.List<LibraryNode> children = c.getChildren();
        if (children.isEmpty()) {
            sb.append("]\n");
        } else {
            sb.append("\n");
            for (int i = 0; i < children.size(); i++) {
                serializeNode(children.get(i), sb, indent + 2);
                if (i < children.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append(inner).append("]\n");
        }

        sb.append(pad).append("}");
    }

    // ===== Helper =====

    private static String indent(int level) {
        return "  ".repeat(level);
    }

    private static String jsonString(String s) {
        if (s == null) {
            return "null";
        }
        // Escape minimale per stringhe sicure in JSON
        String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    private static String jsonStringOrNull(String s) {
        return s == null ? "null" : jsonString(s);
    }
}