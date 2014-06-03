package Main;

import Main.model.TermSearchCandidate;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TextParser {

    private static POSModel mModel;
    private static POSTaggerME mTagger;
    private static ChunkerME mChunker;

    private List<String> foundNames;
    private String anonymizedString;
    private int sentence_detection_and_tokenization_in_ms;
    private int name_finding_in_ms;
    private int chunking_in_ms;

    private static POSModel posModel() {
        if (mModel == null) {
            mModel = new POSModelLoader()
                    .load(new File("en-pos-maxent.bin"));
        }
        return mModel;
    }

    private static POSTaggerME posTaggerME() {
        if (mTagger == null) {
            mTagger = new POSTaggerME(posModel());
        }
        return mTagger;
    }

    private static ChunkerME chunkerME() {
        if (mChunker == null) {
            InputStream is;
            ChunkerModel cModel = null;
            try {
                is = new FileInputStream("en-chunker.bin");
                cModel = new ChunkerModel(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mChunker = new ChunkerME(cModel);
        }
        return mChunker;
    }

    public List<TermSearchCandidate> chunkAndReturnCandidates(String string) throws IOException {
        //Start Time Measurement
        long last = System.nanoTime();

        PerformanceMonitor performanceMonitor = new PerformanceMonitor(System.err, "Sentence Detection");
        performanceMonitor.start();
        // Sentence
        POSTaggerME tagger = TextParser.posTaggerME();

        ObjectStream<String> lineStream = new PlainTextByLineStream(
                new StringReader(string));

        String line;
        String sentence[] = null;
        String[] tags = null;
        while ((line = lineStream.read()) != null) {

            sentence = SimpleTokenizer.INSTANCE
                    .tokenize(line);
            tags = tagger.tag(sentence);

            performanceMonitor.incrementCounter();
        }
        performanceMonitor.stopAndPrintFinalResult();
        // Print Measurement
        double elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        sentence_detection_and_tokenization_in_ms = (int) (elapsedTimeInSec * 1000);
        last = System.nanoTime();


        performanceMonitor = new PerformanceMonitor(System.err, "Anonymize Names");
        performanceMonitor.start();
        // Anonymize Names
        InputStream modelIn = new FileInputStream("en-ner-person.bin");
        TokenNameFinderModel nameFinderModel = new TokenNameFinderModel(modelIn);
        NameFinderME nameFinder = new NameFinderME(nameFinderModel);
        Span nameSpans[] = nameFinder.find(sentence);
        foundNames = new ArrayList<>();
        for (Span s : nameSpans) {
            System.out.println(s.toString());
            for (int i = s.getStart(); i < s.getEnd(); i++) {
                if (sentence != null) {
                    foundNames.add(sentence[i]);
                    sentence[i] = "[patient]";
                }
            }
        }

        StringBuilder backToSentence = new StringBuilder();
        assert sentence != null;
        for (String s : sentence) {
            backToSentence.append(s);
            backToSentence.append(" ");
        }
        anonymizedString = backToSentence.toString();
        nameFinder.clearAdaptiveData();

        performanceMonitor.stopAndPrintFinalResult();
        elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        name_finding_in_ms = (int) (elapsedTimeInSec * 1000);
        last = System.nanoTime();


        performanceMonitor = new PerformanceMonitor(System.err, "Chunking");
        performanceMonitor.start();

        // chunker
        ChunkerME chunkerME = TextParser.chunkerME();

        System.out.println("Spans");
        Span[] spans = chunkerME.chunkAsSpans(sentence, tags);

        performanceMonitor.stopAndPrintFinalResult();
        elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        chunking_in_ms = (int) (elapsedTimeInSec * 1000);
        last = System.nanoTime();

        return TermSearchCandidate.termSearchCandidates(sentence, tags, spans);
    }

    public List<String> getFoundNames() {
        return foundNames;
    }

    public String getAnonymizedString() {
        return anonymizedString;
    }

    public int getSentence_detection_and_tokenization_in_ms() {
        return sentence_detection_and_tokenization_in_ms;
    }

    public int getName_finding_in_ms() {
        return name_finding_in_ms;
    }

    public int getChunking_in_ms() {
        return chunking_in_ms;
    }
}
