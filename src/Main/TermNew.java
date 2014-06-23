package Main;

import Main.model.HPOInfo;
import Main.model.Term;
import org.primefaces.model.DualListModel;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ManagedBean(name = "termNew")
@ViewScoped
public class TermNew {
    private HPOInfo hpoInfo;
    private int visitID;
    private DataSource hpoDataSource;
    private DualListModel<String> words;
    private int termID;

    private UIComponent saveButton;

    public UIComponent getSaveButton() {
        return saveButton;
    }

    public void setSaveButton(UIComponent saveButton) {
        this.saveButton = saveButton;
    }

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
        DataSource klinikDataSource = HPOController.getKlinikDataSource();
        hpoDataSource = HPOController.getHpoDataSource();
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        visitID = new Integer(requestParameterMap.get("visit"));
        termID = new Integer(requestParameterMap.get("term"));
        hpoInfo = HPOInfo.getLastInfoForVisitWithId(visitID, klinikDataSource);
        if (hpoInfo != null) {
            List<Term> hpoMatches = hpoInfo.getHpoMatches();

            String[] allWords = hpoInfo.getVisit().getAdditionalText().split("\\s+");
            List<String> selectedWords = new ArrayList<>();
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
        Term term = Term.getTermWithId(getTermID(), hpoDataSource);
        if (term == null) {
            // invalid
            FacesMessage message = new FacesMessage("Invalid HPO Term ID");
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(saveButton.getClientId(context), message);
            return "#";
        }
        term.setWords(words.getTarget());
        hpoInfo.saveTerm(term);
        return "/visitDetail.xhtml?faces-redirect=true&id=" + getVisitID();
    }

    public String cancel() {
        return "/visitDetail.xhtml?faces-redirect=true&id=" + getVisitID();
    }
}
