package Main;

import Main.model.HPOInfo;
import Main.model.Term;
import Main.model.TermSearchCandidate;
import Main.model.Visit;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TextProcessor {
    private DataSource hpoDataSource;

    public TextProcessor(DataSource hpoDataSource) {
        this.hpoDataSource = hpoDataSource;
    }

    public void processVisit(Visit visit) {
        assert visit != null;
        if (visit.getAdditionalText() == null) {
            return;
        }

        TextParser textParser = new TextParser();
        List<TermSearchCandidate> termSearchCandidates = null;
        try {
            termSearchCandidates = textParser.chunkAndReturnCandidates(visit.getAdditionalText());
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert termSearchCandidates != null;
        List<Term> terms;
        List<Term> hpoMatchList = new ArrayList<>(5);
        List<List<Term>> hpoMultipleMatchList = new ArrayList<>(5);

        for (TermSearchCandidate candidate : termSearchCandidates) {
            try {
                terms = Term.getTermsForCandidate(candidate, hpoDataSource);
                if (terms != null && terms.size() > 0) {
                    if (terms.size() == 1) {
                        hpoMatchList.add(terms.get(0));
                    } else {
                        hpoMultipleMatchList.add(terms);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if ((hpoMatchList.size() > 0)) {
            hpoMatchList = Term.addDescriptionToTerms(hpoMatchList, hpoDataSource);
        }

        saveHPOInfoToDatabase(visit, hpoMatchList, hpoMultipleMatchList, textParser);
    }

    private void saveHPOInfoToDatabase(Visit visit, List<Term> hpoMatchList, List<List<Term>> hpoMultipleMatchList, TextParser textParser) {

        HPOInfo hpoInfo = new HPOInfo(visit, hpoMatchList, hpoMultipleMatchList, textParser.getSentence_detection_and_tokenization_in_ms(), textParser.getChunking_in_ms(), textParser.getName_finding_in_ms(), textParser.getFoundNames());
        hpoInfo.save();

        visit.setAdditionalText(textParser.getAnonymizedString());
        visit.saveToDatabase();
    }
}
