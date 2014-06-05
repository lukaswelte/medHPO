package Main;

import Main.model.HPOInfo;
import Main.model.Term;
import Main.model.Visit;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.sql.DataSource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "visitDetail")
@RequestScoped
public class VisitDetail implements Serializable {
    @Resource(lookup = "jdbc/klinik")
    private DataSource klinikDataSource;

    @Resource(lookup = "jdbc/hpo")
    private DataSource hpoDataSource;

    private int visitID;
    private HPOInfo hpoInfo;
    private String additionalText;

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

    public List<Term> getHpoTerms() {

        if (hpoInfo != null && hpoInfo.getHpoMatches() != null) {
            return hpoInfo.getHpoMatches();
        } else {
            return new ArrayList<>();
        }
    }
}
