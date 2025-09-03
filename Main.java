import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        Classifier classifier = new Classifier();

        try {
            classifier.train(File.readFile("data_isys3/train.txt"));
            classifier.classify(File.readFile("data_isys3/eval.txt"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
