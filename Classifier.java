import java.util.*;

public class Classifier {

    //Intervalle zur Bestimmung der Übergangsmatrizen
    private final Set<Integer> x = new HashSet<>();
    private final Set<Integer> y = new HashSet<>();
    private final Set<Integer> z = new HashSet<>();


    //Matrizen für die Klassen H und G
    private final double[][] matrixH = new double[3][3];
    private final double[][] matrixG = new double[3][3];

    //Prior-Wahrscheinlichkeiten (vorgegeben) - mit log() um sehr kleine Werte zu vermeiden.
    private final double P_H = Math.log(0.9);
    private final double P_G = Math.log(0.1);

    // Listen mit den Werten der zugeordneten Intervalle von den Werten aus train
    private final List<List<String>> intervallListH = new ArrayList<>();
    private final List<List<String>> intervallListG = new ArrayList<>();

    // Kosten
    private final double COST_CONTINUE_WHEN_RETURN_NEEDED = 8.0;
    private final double COST_CORRECT_RETURN = 2.0;
    private final double COST_UNNECESSARY_RETURN = 4.0;

    // Werte zum interpretieren der Kosten
    private double totalCost = 0.0;
    private int correctClassifications = 0;
    private int incorrectClassifications = 0;
    private int totalSequences = 0;
    private int incorrectPredictionsForG = 0;
    private int incorrectPredictionsForH = 0;


    /**
     * Initialisiert den Classifier und definiert die Intervalle für die Wertebereiche x, y und z.
     * Diese Intervalle werden genutzt, um die numerischen Werte aus den Eingabedaten
     * den Kategorien x, y und z zuzuordnen. Diese Zuordnung ist essentiell für die Erstellung
     * der Intervallsequenzen und der Übergangsmatrizen während des Trainings.
     *
     * Die Intervalle sind wie folgt definiert:
     * - x: Werte von 0 bis 28
     * - y: Werte von 29 bis 43
     * - z: Werte von 44 bis 100
     */
    public Classifier(){
        for (int i = 0; i <= 27; i++) x.add(i);
        for (int i = 28; i <= 42; i++) y.add(i);
        for (int i = 43; i <= 100; i++) z.add(i);
    }

    /**
     * Erstellt Sequenzen aus den Trainingsdaten, indem die numerischen Werte den definierten Intervallen (x, y, z) zugeordnet werden.
     * Jede Sequenz repräsentiert eine Abfolge von Kategorien und wird in einer entsprechenden Liste gespeichert,
     * abhängig von der Klasse (Label) der Eingabedaten. Diese Sequenzen bilden die Grundlage zur Berechnung der Übergangswahrscheinlichkeits-Matrizen.
     *
     * Funktionsweise:
     * - Iteriert über die Eingabedaten und ordnet jeden Wert den Intervallen x, y oder z zu.
     * - Speichert die resultierende Sequenz in einer Liste, basierend auf dem Klassenlabel (H oder G).
     * - Werte, die keinem Intervall zugeordnet werden können, lösen eine Ausnahme aus.
     *
     * Befüllte Listen:
     * - `intervallListH`: Enthält Sequenzen für die Klasse H.
     * - `intervallListG`: Enthält Sequenzen für die Klasse G.
     *
     * @param fileInput Datei Inhalt in einer Map.
     */
    private void createIntervallSequenceTrain(Map<String, List<List<Integer>>> fileInput) {
        intervallListH.clear();
        intervallListG.clear();

        for (Map.Entry<String, List<List<Integer>>> entry : fileInput.entrySet()) {
            String label = entry.getKey();
            List<List<Integer>> rows = entry.getValue();

            for (List<Integer> row : rows) {
                List<String> sequence = new ArrayList<>();

                for (Integer value : row) {
                    if (x.contains(value)) sequence.add("x");
                    else if (y.contains(value)) sequence.add("y");
                    else if (z.contains(value)) sequence.add("z");
                    else {
                        throw new IllegalArgumentException("Wert " + value + " passt in kein Intervall!");
                    }
                }

                if ("H".equals(label)) intervallListH.add(sequence);
                else if ("G".equals(label)) intervallListG.add(sequence);
            }
        }
    }

    /**
     * Erstellt eine Übergangswahrscheinlichkeits-Matrix basierend auf einer Liste von Intervallsequenzen.
     * Diese Methode zählt, wie oft Übergänge zwischen den Intervallen x, y und z vorkommen und
     * berechnet die relativen Häufigkeiten der Übergänge.
     *
     * Die resultierende Matrix wird normalisiert, sodass die Summen der Zeilen 1 ergeben.
     *
     * @param intervallList Die Liste der Intervallsequenzen für eine Klasse (H oder G).
     * @param matrix Die Matrix, in der die berechneten Übergangswahrscheinlichkeiten gespeichert werden.
     */
    private void createMatrix(List<List<String>> intervallList, double[][] matrix) {
        Map<String, Integer> indexMap = Map.of("x", 0, "y", 1, "z", 2);

        for (List<String> sequence : intervallList) {
            for (int i = 0; i < sequence.size() - 1; i++) {
                String from = sequence.get(i);
                String to = sequence.get(i + 1);

                if (indexMap.containsKey(from) && indexMap.containsKey(to)) {
                    matrix[indexMap.get(from)][indexMap.get(to)]++;
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            double rowSum = 0;

            for (int j = 0; j < 3; j++) rowSum += matrix[i][j];
            if (rowSum > 0) {
                for (int j = 0; j < 3; j++) matrix[i][j] /= rowSum;
            }
        }
    }

    /**
     * Gibt eine Matrix in lesbarer Form aus.
     *
     * @param matrix Die Matrix, die ausgegeben werden soll.
     */
    private void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            System.out.print("[ ");
            for (double value : row) {
                System.out.printf("%8.4f ", value); // Runde auf 4 Nachkommastellen
            }
            System.out.println("]");
        }
    }

    /**
     * Berechnet die Wahrscheinlichkeit für jede Sequenz in einer Liste, die Klasse G oder H zuzuordnen.
     * Die Methode nutzt Übergangswahrscheinlichkeiten aus der gegebenen Matrix und die Prior-Wahrscheinlichkeit.
     *
     * Für jede Sequenz wird die Wahrscheinlichkeit der Übergänge (mit Logarithmus zur Vermeidung von Unterlauf) berechnet,
     * und die resultierende Wahrscheinlichkeit wird zur Gesamtwahrscheinlichkeit addiert.
     *
     * @param intervallList Die Liste von Intervallsequenzen.
     * @param matrix Die Matrix mit den Übergangswahrscheinlichkeiten für die Klasse.
     * @param p Die Prior-Wahrscheinlichkeit für die Klasse (P_H oder P_G).
     * @return Eine Liste der berechneten Wahrscheinlichkeiten für jede Sequenz.
     */
    private List<Double> calculateProbabilities(List<List<String>> intervallList, double[][] matrix, double p) {
        List<Double> probabilities = new ArrayList<>();
        Map<String, Integer> indexMap = Map.of("x", 0, "y", 1, "z", 2);

        for (List<String> sequence : intervallList) {
            double totalProbability = p;

            for (int i = 0; i < sequence.size() - 1; i++) {
                String from = sequence.get(i);
                String to = sequence.get(i + 1);

                int fromIndex = indexMap.get(from);
                int toIndex = indexMap.get(to);

                double transitionProbability = matrix[fromIndex][toIndex];
                totalProbability += Math.log(transitionProbability > 0 ? transitionProbability : 1e-10);
            }
            probabilities.add(totalProbability);
        }
        return probabilities;
    }

    /**
     * Vorhersage der Klasse (G oder H) für eine gegebene Sequenz basierend auf den Übergangswahrscheinlichkeiten
     * und den Prior-Wahrscheinlichkeiten.
     *
     * Die Methode berechnet die Wahrscheinlichkeit, dass die gegebene Sequenz zur Klasse G oder H gehört, indem
     * sie die Übergangswahrscheinlichkeiten für jede Klasse (G und H) berücksichtigt und die Prior-Wahrscheinlichkeit
     * der jeweiligen Klasse mit einbezieht. Anschließend wird die Klasse mit der höheren Wahrscheinlichkeit als Vorhersage zurückgegeben.
     *
     * @param sequence Die Liste von Intervallkategorien ("x", "y", "z"), die die Sequenz von Eingabewerten repräsentiert.
     * @return Die vorhergesagte Klasse ("G" oder "H") basierend auf den berechneten Wahrscheinlichkeiten.
     */
    private String predictLabel(List<String> sequence) {
        double probG = calculateProbabilities(Collections.singletonList(sequence), matrixG, P_G).get(0);
        double probH = calculateProbabilities(Collections.singletonList(sequence), matrixH, P_H).get(0);
        return probG > probH ? "G" : "H";
    }

    /**
     * Aktualisiert die Klassifikationsstatistiken basierend auf der tatsächlichen und der vorhergesagten Klasse.
     * Die Methode zählt die korrekten und inkorrekten Klassifikationen und berechnet die entstehenden Kosten.
     *
     * @param trueLabel Die tatsächliche Klasse (Label) der Eingabedaten.
     * @param predictedLabel Die vorhergesagte Klasse (Label), basierend auf den berechneten Wahrscheinlichkeiten.
     */
    private void updateClassificationStats(String trueLabel, String predictedLabel) {
        if (trueLabel.equals(predictedLabel)) {
            correctClassifications++;
        } else {
            incorrectClassifications++;
            if ("G".equals(predictedLabel)) {
                incorrectPredictionsForG++;
            } else if ("H".equals(predictedLabel)) {
                incorrectPredictionsForH++;
            }
        }
        totalCost += calculateCost(trueLabel, predictedLabel);
    }

    /**
     * Verarbeitet eine Liste von Ganzzahlen und ordnet sie den definierten Intervallen (x, y, z) zu.
     * Diese Methode konvertiert jede Zahl in eine Kategorie ("x", "y" oder "z") und erstellt eine entsprechende Sequenz.
     *
     * @param row Die Liste von Ganzzahlen (Werte aus den Trainingsdaten).
     * @return Eine Liste von Strings, die die Kategorie ("x", "y", "z") für jedes Element in der Eingabeliste darstellt.
     */
    private List<String> processSequence(List<Integer> row) {
        List<String> sequence = new ArrayList<>();
        for (Integer value : row) {
            if (x.contains(value)) sequence.add("x");
            else if (y.contains(value)) sequence.add("y");
            else if (z.contains(value)) sequence.add("z");
        }
        return sequence;
    }

    /**
     * Berechnet die entstehenden Kosten basierend auf der tatsächlichen und vorhergesagten Klasse.
     * Kostenarten:
     * - Korrekte Rückkehr ("G" -> "G"): Niedrige Kosten.
     * - Falsche Nicht-Rückkehr ("G" -> "H"): Hohe Kosten.
     * - Unnötige Rückkehr ("H" -> "G"): Mittlere Kosten.
     *
     * @param trueLabel Die tatsächliche Klasse.
     * @param predictedLabel Die vorhergesagte Klasse.
     * @return Die berechneten Kosten für die Klassifikation.
     */
    private double calculateCost(String trueLabel, String predictedLabel) {
        if ("G".equals(trueLabel)) {
            if ("G".equals(predictedLabel)) {
                return COST_CORRECT_RETURN;
            } else {
                return COST_CONTINUE_WHEN_RETURN_NEEDED;
            }
        } else if ("H".equals(trueLabel)) {
            if ("G".equals(predictedLabel)) {
                return COST_UNNECESSARY_RETURN;
            }
        }
        return 0.0;
    }

    /**
     * Gibt die Klassifikationsstatistiken aus, einschließlich der Gesamtkosten, der Anzahl der Sequenzen,
     * der korrekten und inkorrekten Klassifikationen sowie der Klassifikationsgenauigkeit.
     */
    private void printStats() {
        System.out.println("Gesamtkosten: " + totalCost);
        System.out.println("Anzahl der Sequenzen: " + totalSequences);
        System.out.println("Korrekte Klassifikationen: " + correctClassifications);
        System.out.println("Falsche Klassifikationen: " + incorrectClassifications);
        System.out.println("Falsche Vorhersagen für G: " + incorrectPredictionsForG);
        System.out.println("Falsche Vorhersagen für H: " + incorrectPredictionsForH);
        System.out.printf("Klassifikationsgenauigkeit: %.2f%%%n", ((double) correctClassifications / totalSequences * 100));
    }


    /**
     * Trainiert den Classifier basierend auf den gegebenen Daten.
     *
     * Der Trainingsprozess umfasst folgende Schritte:
     * 1. Erstellung der Intervallsequenzen basierend auf den Trainingsdaten.
     *    (Zuweisung der Werte zu den Intervallen x, y, z und Speicherung als Sequenzen für die Klassen G und H.)
     * 2. Erstellung der Übergangswahrscheinlichkeitsmatrizen (`matrixG` und `matrixH`)
     *    aus den generierten Sequenzen für jede Klasse.
     * 3. Ausgabe der erstellten Matrizen zur Validierung.
     *
     * @param fileInput Map mit den Trainingsdaten, wobei der Schlüssel die Klasse ("G" oder "H")
     *                  und der Wert die Listen von Zahlenwerten (Sequenzen) darstellt.
     */
    public void train(Map<String, List<List<Integer>>> fileInput) {
        createIntervallSequenceTrain(fileInput);
        createMatrix(intervallListH, matrixH);
        createMatrix(intervallListG, matrixG);

        System.out.println("Matrix G:");
        printMatrix(matrixG);
        System.out.println("Matrix H:");
        printMatrix(matrixH);
    }

    /**
     * Klassifiziert die Eingabedaten und berechnet die Klassifikationsstatistiken sowie die Kosten.
     * Die Methode analysiert die Sequenzen, berechnet Wahrscheinlichkeiten basierend auf den Matrizen,
     * bestimmt die vorhergesagte Klasse und aktualisiert die Metriken wie Gesamtkosten und Fehlklassifikationen.
     *
     * @param fileInput Map mit echten Labels als Schlüssel und Sequenzen von Werten als Werte.
     */
    public void classify(Map<String, List<List<Integer>>> fileInput) {
        for (Map.Entry<String, List<List<Integer>>> entry : fileInput.entrySet()) {
            String trueLabel = entry.getKey();
            List<List<Integer>> rows = entry.getValue();

            for (List<Integer> row : rows) {
                totalSequences++;
                List<String> sequence = processSequence(row);

                String predictedLabel = predictLabel(sequence);
                updateClassificationStats(trueLabel, predictedLabel);
            }
        }

        printStats();
    }

}
