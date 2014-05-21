package Main;

import Main.model.TermSearchCandidate;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TextParser {

    public static String[] chunk(String text) throws IOException {

        POSModel model = new POSModelLoader()
                .load(new File("en-pos-maxent.bin"));
        PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
        POSTaggerME tagger = new POSTaggerME(model);

        ObjectStream<String> lineStream = new PlainTextByLineStream(
                new StringReader(text));

        perfMon.start();
        String line;
        String whitespaceTokenizerLine[] = null;

        String[] tags = null;
        while ((line = lineStream.read()) != null) {

            whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
                    .tokenize(line);
            tags = tagger.tag(whitespaceTokenizerLine);

            //Debug only
            POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
            System.out.println(sample.toString());
            //

            perfMon.incrementCounter();
        }
        perfMon.stopAndPrintFinalResult();

        // chunker
        InputStream is = new FileInputStream("en-chunker.bin");
        ChunkerModel cModel = new ChunkerModel(is);

        ChunkerME chunkerME = new ChunkerME(cModel);

        /*
        String result[] = chunkerME.chunk(whitespaceTokenizerLine, tags);
        System.out.println("Chunks");
        for (String s : result)
            System.out.println(s);
        */

        System.out.println("Spans");
        Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);
        for (Span s : span)
            System.out.println(s.toString());

        System.out.println("Spans strings");
        String[] strings = Span.spansToStrings(span, whitespaceTokenizerLine);
        for (String s : strings) {
            System.out.println(s);
        }

        return strings;
    }

    public static List<TermSearchCandidate> chunkAndReturnCandidates(String string) throws IOException {
        //Start Time Measurement
        long start = System.nanoTime();
        long last = start;

        PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "Sentence Detection");
        perfMon.start();
        // Sentence
        POSModel model = new POSModelLoader()
                .load(new File("en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);

        ObjectStream<String> lineStream = new PlainTextByLineStream(
                new StringReader(string));

        String line;
        String sentence[] = null;

        String[] tags = null;
        while ((line = lineStream.read()) != null) {

            sentence = SimpleTokenizer.INSTANCE
                    .tokenize(line);
            tags = tagger.tag(sentence);

            //Debug only
            POSSample sample = new POSSample(sentence, tags);
            System.out.println(sample.toString());
            //

            perfMon.incrementCounter();
        }
        perfMon.stopAndPrintFinalResult();
        // Print Measurement
        double elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        HPOController.sentence_detection_and_tokenization_in_ms = (int) (elapsedTimeInSec * 1000);
        last = System.nanoTime();


        perfMon = new PerformanceMonitor(System.err, "Anonymize Names");
        perfMon.start();
        // Anonymize Names
        InputStream modelIn = new FileInputStream("en-ner-person.bin");
        TokenNameFinderModel nameFinderModel = new TokenNameFinderModel(modelIn);
        NameFinderME nameFinder = new NameFinderME(nameFinderModel);
        Span nameSpans[] = nameFinder.find(sentence);
        HPOController.numberFoundNames = nameSpans.length;
        System.out.println("Names");
        for (Span s : nameSpans) {
            System.out.println(s.toString());
            for (int i = s.getStart(); i < s.getEnd(); i++) {
                if (sentence != null) {
                    sentence[i] = "[patient]";
                }
            }
        }

        StringBuilder backToSentence = new StringBuilder();
        for (String s : sentence) {
            backToSentence.append(s);
            backToSentence.append(" ");
        }
        HPOController.anonymizedString = backToSentence.toString();
        System.out.println("Text after anonymization: " + HPOController.anonymizedString);
        nameFinder.clearAdaptiveData();

        perfMon.stopAndPrintFinalResult();
        elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        HPOController.name_finding_in_ms = (int) (elapsedTimeInSec * 1000);
        last = System.nanoTime();


        perfMon = new PerformanceMonitor(System.err, "Chunking");
        perfMon.start();

        // chunker
        InputStream is = new FileInputStream("en-chunker.bin");
        ChunkerModel cModel = new ChunkerModel(is);

        ChunkerME chunkerME = new ChunkerME(cModel);

        System.out.println("Spans");
        Span[] spans = chunkerME.chunkAsSpans(sentence, tags);
        for (Span s : spans)
            System.out.println(s.toString());

        perfMon.stopAndPrintFinalResult();
        elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        HPOController.chunking_in_ms = (int) (elapsedTimeInSec * 1000);
        last = System.nanoTime();

        return TextParser.termSearchCandidates(sentence, tags, spans);
    }

    public static List<TermSearchCandidate> termSearchCandidates(String[] words, String[] tags, Span[] spans) {
        List<TermSearchCandidate> result = new ArrayList<TermSearchCandidate>();

        for (Span span : spans) {
            if (span.getType().equals("NP")) {

                String[] w = new String[span.length()];
                String[] t = new String[span.length()];
                int startPoint = span.getStart();
                for (int i = startPoint; i < span.getEnd(); i++) {
                    int index = i - startPoint;
                    w[index] = words[i];
                    t[index] = tags[i];
                }

                result.add(new TermSearchCandidate(w, t, span));
            }
        }

        return result;
    }
}
