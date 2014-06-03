package Main;

import Main.model.Term;
import Main.model.Visit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ValueChangeEvent;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "HPOController")
@ApplicationScoped
public class HPOController implements Serializable {

    public static String anonymizedString;
    /**
     * Statistics  *
     */
    public static int sentence_detection_and_tokenization_in_ms;
    public static int name_finding_in_ms;
    public static int chunking_in_ms;
    public static List<Visit> visits;
    public static Visit selectedVisit;
    public static int numberFoundNames;
    public static String processAllVisits;
    public static HPOController INSTANCE;
    @Resource(lookup = "jdbc/hpo")
    private DataSource hpoDataSource;
    @Resource(lookup = "jdbc/klinik")
    private DataSource klinikDataSource;
    private String textToProcess = "Jerome had large hands. Suffering from psychotic episodes the patient is just in sickbed. A Prolactin excess made it really hard.";

    public DataSource getHpoDataSource() {
        return hpoDataSource;
    }

    public DataSource getKlinikDataSource() {
        return klinikDataSource;
    }

    public String getTextToProcess() {
        return textToProcess;
    }

    public void setTextToProcess(String textToProcess) {
        this.textToProcess = textToProcess;
    }

    /**
     * Retrieve patients and visits while creating the controller
     */
    @PostConstruct
    public void init() {
        visits = Visit.getAllVisits(klinikDataSource);
        INSTANCE = this;
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

    // Getters and Setters
    public List<Visit> getVisits() {
        return visits;
    }

    public Visit getSelectedVisit() {
        return selectedVisit;
    }

    public void setSelectedVisit(Visit visit) {
        selectedVisit = visit;
    }

    public void selectedVisitChanged(ValueChangeEvent evt) {
        setSelectedVisit((Visit) evt.getNewValue());
    }

    public String getProcessAllVisits() {
        List<Visit> allVisits = Visit.getAllVisits(klinikDataSource);
        TextProcessor textProcessor = new TextProcessor(hpoDataSource);
        for (Visit visit : allVisits) {
            textProcessor.processVisit(visit);
        }
        return "Finished Processing all visits";
    }

    public String processSelectedVisit() {
        return "/visitDetail.xhtml?faces-redirect=true&id=" + selectedVisit.getId();
    }
}
