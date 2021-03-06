package Main;

import Main.model.HPOInfo;
import Main.model.Term;
import Main.model.Visit;
import org.primefaces.model.SelectableDataModel;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.ListDataModel;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "visitDetail")
@ViewScoped
public class VisitDetail implements Serializable {
    @Resource(lookup = "jdbc/klinik")
    private DataSource klinikDataSource;

    @Resource(lookup = "jdbc/hpo")
    private DataSource hpoDataSource;
    private TermDataModel termDataModel;

    private int visitID;
    private HPOInfo hpoInfo;
    private String additionalText;
    private List<TermTimes> termTimes;

    public int getVisitID() {
        return visitID;
    }

    public void setVisitID(int visitID) {
        this.visitID = visitID;
    }

    public String getAdditionalText() {
        return additionalText;
    }

    public void setAdditionalText(String additionalText) {
        this.additionalText = additionalText;
    }

    public void init() {
        Visit visit = Visit.getVisitWithId(getVisitID(), klinikDataSource);
        setAdditionalText(visit.getAdditionalText());
        hpoInfo = HPOInfo.getLastInfoForVisitWithId(getVisitID(), klinikDataSource);
        if (hpoInfo != null) {
            List<Term> hpoMatches = hpoInfo.getHpoMatches();
            if (hpoMatches == null) {
                hpoMatches = new ArrayList<>();
            }
            termDataModel = new TermDataModel(hpoMatches);
        }
    }

    public Visit getVisit() {
        return Visit.getVisitWithId(getVisitID(), klinikDataSource);
    }

    public String processText() {
        Visit v = getVisit();
        v.setAdditionalText(getAdditionalText());
        TextProcessor textProcessor = new TextProcessor(hpoDataSource);
        textProcessor.processVisit(v);
        return "/visitDetail.xhtml?faces-redirect=true&id=" + getVisitID();
    }

    public TermDataModel getTermDataModel() {
        return termDataModel;
    }

    public String deleteTerm(Term term) {
        if (term.getId() > 0) {
            hpoInfo.removeMatchedTermWithId(term.getId());
        } else {
            hpoInfo.removeMatchedTermWithCustomID(term.getCustomID());
        }
        return "/visitDetail.xhtml?faces-redirect=true&id=" + getVisitID();
    }

    public String editTerm(Term term) {
        return "/termEdit.xhtml?faces-redirect=true&visit=" + getVisitID() + "&term=" + term.getId() + "&customid=" + term.getCustomID();
    }

    public String addNewTerm() {
        return "/termNew.xhtml?faces-redirect=true&visit=" + getVisitID() + "&term=" + 0;
    }

    public List<String> autocompleteHPO(String query) {
        List<String> results = new ArrayList<>();

        List<Term> terms = null;
        try {
            terms = Term.getTermStartingWithText(query, hpoDataSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (terms == null) return results;

        for (Term term : terms) {
            results.add(term.toString());
        }

        return results;
    }


    public List<TermTimes> getTermTimes() {
        termTimes = new ArrayList<>();
        try {
            termTimes.add(new TermTimes("Chunking:", "" + hpoInfo.getChunking_in_ms()));
            termTimes.add(new TermTimes("Name finding:", "" + hpoInfo.getName_finding_in_ms()));
            termTimes.add(new TermTimes("Sentence Detection:", "" + hpoInfo.getSentence_detection_and_tokenization_in_ms()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return termTimes;
    }

}

class TermDataModel extends ListDataModel<Term> implements SelectableDataModel<Term> {

    public TermDataModel(List<Term> data) {
        super(data);
    }

    public Term getRowData(String rowKey) {
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data

        List<Term> terms = (List<Term>) getWrappedData();

        for (Term term : terms) {
            if (("" + term.getId() + "" + term.getCustomID()).equals(rowKey))
                return term;
        }

        return null;
    }

    public Object getRowKey(Term term) {
        return "" + term.getId() + "" + term.getCustomID();
    }
}
