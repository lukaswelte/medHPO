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
import java.sql.SQLException;
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

    private Term term;

    private UIComponent saveButton;

    private boolean customTerm;
    private String customTermName;
    private String customTermDescription;

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

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
        Term term;
        if (getCustomTerm()) {
            term = new Term();
            term.setCustomID(System.currentTimeMillis() / 1000);
            term.setCustomName(getCustomTermName());
            term.setCustomDescription(getCustomTermDescription());
        } else {
            term = Term.getTermWithId(getTermID(), hpoDataSource);
            if (term == null || term.getId() == 0) {
                // invalid
                FacesMessage message = new FacesMessage("Invalid HPO Term ID");
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(saveButton.getClientId(context), message);
                return "/visitDetail.xhtml?faces-redirect=true&id=" + getVisitID();
            }
        }

        term.setWords(words.getTarget());
        hpoInfo.saveTerm(term);
        return "/visitDetail.xhtml?faces-redirect=true&id=" + getVisitID();
    }

    public String cancel() {
        return "/visitDetail.xhtml?faces-redirect=true&id=" + getVisitID();
    }

    public boolean getCustomTerm() {
        return customTerm;
    }

    public void setCustomTerm(boolean customTerm) {
        this.customTerm = customTerm;
    }

    public List<Term> completeTerm(String query) {
        List<Term> terms = new ArrayList<>();
        try {
            terms = Term.getTermStartingWithText(query, hpoDataSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return terms;
    }

    public String getCustomTermName() {
        if (customTermName == null) {
            customTermName = "";
        }
        return customTermName;
    }

    public void setCustomTermName(String customTermName) {
        this.customTermName = customTermName;
    }

    public String getCustomTermDescription() {
        if (customTermDescription == null) {
            customTermDescription = "";
        }
        return customTermDescription;
    }

    public void setCustomTermDescription(String customTermDescription) {
        this.customTermDescription = customTermDescription;
    }
}
