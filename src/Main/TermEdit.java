package Main;

import Main.model.HPOInfo;
import Main.model.Term;
import org.primefaces.model.DualListModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ManagedBean(name = "termEdit")
@ViewScoped
public class TermEdit {
    private HPOInfo hpoInfo;
    private int visitID;
    private DataSource klinikDataSource;
    private Term termToEdit;
    private DualListModel<String> words;
    private int termID;

    public int getTermID() {
        return termID;
    }

    public void setTermID(int termID) {
        this.termID = termID;
    }

    public int getVisitID() {
        return visitID;
    }

    public void setVisitID(int visitID) {
        this.visitID = visitID;
    }

    public void init() {
        klinikDataSource = HPOController.getKlinikDataSource();
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        visitID = new Integer(requestParameterMap.get("visit"));
        termID = new Integer(requestParameterMap.get("term"));
        hpoInfo = HPOInfo.getLastInfoForVisitWithId(visitID, klinikDataSource);
        if (hpoInfo != null) {
            List<Term> hpoMatches = hpoInfo.getHpoMatches();
            if (hpoMatches == null) {
                hpoMatches = new ArrayList<>();
            }
            for (Term term : hpoMatches) {
                if (term.getId() == termID) {
                    termToEdit = term;
                    break;
                }
            }

            String[] allWords = hpoInfo.getVisit().getAdditionalText().split("\\s+");
            List<String> selectedWords = termToEdit.getWords();
            List<String> nonTermWords = new ArrayList<>();
            for (String word : allWords) {
                if (!selectedWords.contains(word) && word.length() > 1) {
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
}
