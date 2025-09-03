import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class File {

    private static final Map<String, List<List<Integer>>> fileInput = new HashMap<>();

    /**
     * Liest die Datei ein und lädt den Inhalt in eine Map, mit dem Label als Key und der Sequenz als Liste von Listen.
     *
     * @param filename Pfad zur Datei.
     * @return Inhalt der Datei als Map.
     * @throws IOException Wenn Fehler beim Lesen der Datei auftritt.
     */
    public static Map<String, List<List<Integer>>> readFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length < 2) {
                    System.err.println("Invalid line format: " + line);
                    continue;
                }

                String key = parts[0];
                List<Integer> row = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    try {
                        row.add(Integer.parseInt(parts[i]));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format in line: " + line);
                        continue;
                    }
                }
                fileInput.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
            }
        }
        return fileInput;
    }
}
