package Main;

import Main.model.HPOInfo;
import Main.model.Term;
import org.primefaces.model.DualListModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ManagedBean(name = "termEdit")
@ViewScoped
public class TermEdit {
    private HPOInfo hpoInfo;
    private int visitID;
    private DataSource hpoDataSource;
    private Term termToEdit;
    private DualListModel<String> words;
    private int termID;
    private long customID;
    private Term selectedTerm;

    public int getTermID() {
        return termID;
    }

    public void setTermID(int termID) {
        this.termID = termID;
    }

    public long getCustomID() {
        return customID;
    }

    public void setCustomID(long customID) {
        this.customID = customID;
    }

    public int getVisitID() {
        return visitID;
    }

    public void setVisitID(int visitID) {
        this.visitID = visitID;
    }

    public void init() {
        DataSource klinikDataSource = HPOController.getKlinikDataSource();
        hpoDataSource = HPOController.getHpoDataSource();
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        visitID = new Integer(requestParameterMap.get("visit"));
        termID = new Integer(requestParameterMap.get("term"));
        customID = new Long(requestParameterMap.get("customid"));
        hpoInfo = HPOInfo.getLastInfoForVisitWithId(visitID, klinikDataSource);
        if (hpoInfo != null) {
            List<Term> hpoMatches = hpoInfo.getHpoMatches();
            if (hpoMatches == null) {
                hpoMatches = new ArrayList<>();
            }
            for (Term term : hpoMatches) {
                if (term.getId() == termID && term.getCustomID() == customID) {
                    termToEdit = term;
                    selectedTerm = termToEdit;
                    break;
                }
            }

            String[] allWords = hpoInfo.getVisit().getAdditionalText().split("\\s+");
            List<String> selectedWords = termToEdit.getWords();
            List<String> nonTermWords = new ArrayList<>();
            for (String word : allWords) {
                if (!selectedWords.contains(word) && word.length() > 1 && !word.contains("patient")) {
                    nonTermWords.add(word);
                }
            }
            words = new DualListModel<>(nonTermWords, selectedWords);
        }
    }

    public DualListModel<String> getWords() {
        if (words == null) {
            words = new DualListModel<>(new ArrayList<String>(), new ArrayList<String>());
        }
        return words;
    }

    public void setWords(DualListModel<String> words) {
        this.words = words;
    }

    public String save() {
        termToEdit.setWords(words.getTarget());
        hpoInfo.updateTermWords(termToEdit);
        return "/visitDetail.xhtml?faces-redirect=true&id=" + getVisitID();
    }

    public String cancel() {
        return "/visitDetail.xhtml?faces-redirect=true&id=" + getVisitID();
    }

    public Term getSelectedTerm() {
        return selectedTerm;
    }

    public void setSelectedTerm(Term selectedTerm) {
        this.selectedTerm = selectedTerm;
    }

    public List<Term> completeTerms(String query) {
        List<Term> results = new ArrayList<>();

        List<Term> terms = null;
        try {
            terms = Term.getTermStartingWithText(query, hpoDataSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (terms == null) return results;

        for (Term term : terms) {
            results.add(term);
        }

        return results;
    }
}
