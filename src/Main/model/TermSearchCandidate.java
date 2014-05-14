package Main.model;

import Main.helper.Permutations;
import opennlp.tools.util.Span;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 18.06.13.
 */
public class TermSearchCandidate {
    private String[] words;
    private String[] tags;

    public Span getSpan() {
        return span;
    }

    public void setSpan(Span span) {
        this.span = span;
    }

    private Span span;

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String[] getWords() {
        return words;
    }

    public void setWords(String[] words) {
        this.words = words;
    }

    public TermSearchCandidate(String[] words, String[] tags, Span span) {
        this.words = words;
        this.tags = tags;
        this.span = span;
    }

    public List<String[]> getSearchStrings() {
        List<String[]> result = new ArrayList<String[]>();

        String[] wordsForSearch = null;
        String tag = null;
        for (int i = 0; i<words.length; i++) {
            tag = tags[i];
            if (("NN").equals(tag) || ("NNS").equals(tag) || ("NNP").equals(tag) || ("NNPS").equals(tag)) {
                result.add(new String[]{words[i]});
                if (i>0 && i<words.length-2) {
                    wordsForSearch = new String[3];
                    wordsForSearch[0] = words[i-1];
                    wordsForSearch[1] = words[i];
                    wordsForSearch[2] = words[i+1];
                }  else if (i>0) {
                    wordsForSearch = new String[2];
                    wordsForSearch[0] = words[i-1];
                    wordsForSearch[1] = words[i];
                }  else if (i<(words.length-2)) {
                    wordsForSearch = new String[2];
                    wordsForSearch[1] = words[i];
                    wordsForSearch[2] = words[i+1];
                }
            }
        }

        int i = 0;
        String[] permutations = null;
        Permutations<String> stringPermutations = null;
        if (wordsForSearch != null) {
            permutations = new String[wordsForSearch.length];
            stringPermutations = Permutations.create(wordsForSearch, permutations);
            while (stringPermutations.next()) {
                result.add(permutations.clone());
            }

            if (wordsForSearch.length > 2) {
                String[] subArr = new String[2];
                subArr[0] = wordsForSearch[1];
                subArr[1] = wordsForSearch[2];
                i = 0;
                permutations = new String[subArr.length];
                stringPermutations = Permutations.create(subArr, permutations);
                while (i < 50 && stringPermutations.next()) {
                    result.add(permutations.clone());
                    i++;
                }
            }
        }

        i = 0;
        permutations = new String[words.length];
        stringPermutations = Permutations.create(words, permutations);
        while (i<50 && stringPermutations.next()) {
           result.add(permutations.clone());
           i++;
        }

        return result;
    }

    @Override
    public String toString() {
        return "TermSearchCandidate{" +
                "words=" + Arrays.toString(words) +
                '}';
    }

    public static String arrayToString2(String[] a, String separator) {
        StringBuilder result = new StringBuilder();
        if (a.length > 0) {
            result.append(a[0]);
            for (int i=1; i<a.length; i++) {
                result.append(separator);
                result.append(a[i]);
            }
        }
        return result.toString();
    }
}
