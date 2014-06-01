package Main;

import Main.model.Term;
import Main.model.TermSearchCandidate;
import Main.model.Visit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ManagedBean(name="HPOController")
@ViewScoped
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
    public static String outputText;
    public static String hpoMatches;
    public static String hpoMultipleMatches;
    public static int numberFoundNames;
    public static String processAllVisits;
    @Resource(lookup = "jdbc/hpo")
    private DataSource dataSource;
    @Resource(lookup = "jdbc/klinik")
    private DataSource klinikDataSource;
    private String textToProcess = "Jerome had large hands. Suffering from psychotic episodes the patient is just in sickbed. A Prolactin excess made it really hard.";
    private List<Term> outputHPOTags;

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
    }

    public String posTextToProcess() {
        if (textToProcess == null) {
            return "No Text to process";
        }

        return posText(textToProcess);
    }

    private String posText(String string) {
        //Start Time Measurement
        long start = System.nanoTime();
        long last = start;

        List<TermSearchCandidate> termSearchCandidates = null;
        try {
            termSearchCandidates = TextParser.chunkAndReturnCandidates(string);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert termSearchCandidates != null;

        // Print Measurement
        double elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        System.out.println("Text Processing Time: " + elapsedTimeInSec);
        last = System.nanoTime();

        HashMap<TermSearchCandidate, List<Term>> foundTerms = new HashMap<>();
        List<Term> terms;
        List<Term> hpoMatchList = new ArrayList<>(5);
        List<List<Term>> hpoMultipleMatchList = new ArrayList<>(5);

        for (TermSearchCandidate candidate : termSearchCandidates) {
            try {
                terms = Term.getTermsForCandidate(candidate, dataSource);
                if (terms != null && terms.size() > 0) {
                    if (terms.size() == 1) {
                        hpoMatchList.add(terms.get(0));
                    } else {
                        hpoMultipleMatchList.add(terms);
                    }

                    foundTerms.put(candidate, terms);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Print Measurement
        elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        System.out.println("Search Database: " + elapsedTimeInSec);
        last = System.nanoTime();
        if (foundTerms.size() > 0) {
            System.out.println("\n Found Terms:");
            for (Map.Entry<TermSearchCandidate, List<Term>> entry : foundTerms.entrySet()) {
                System.out.println(entry.getKey() + ":\t \t " + entry.getValue().toString() + "\n");
            }


            // Print Measurement
            elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
            System.out.println("Print results: " + elapsedTimeInSec);

            // Print Measurement
            elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
            System.out.println("Overall Time: " + elapsedTimeInSec);

            if (anonymizedString != null && !anonymizedString.equals("")) {
                string = anonymizedString;
            }

            hpoMatches = "";
            if ((hpoMatchList.size() > 0)) {
                HPOController.outputText = Term.enhanceStringWithTerms(string, hpoMatchList, dataSource);
                outputHPOTags = addDescriptionToTerms(hpoMatchList);

                //Create String from Match Lists
                hpoMatches = Term.getJSONStringFromList(hpoMatchList);

                StringBuilder stringBuilder = new StringBuilder("[");
                for (List<Term> termList : hpoMultipleMatchList) {
                    stringBuilder.append(Term.getJSONStringFromList(termList));
                    stringBuilder.append(",");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                stringBuilder.append("]");
                hpoMultipleMatches = stringBuilder.toString();
            }

            return "processResult";
        }

        // Print Measurement
        elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        System.out.println("Print results: " + elapsedTimeInSec);

        // Print Measurement
        elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
        System.out.println("Overall Time: " + elapsedTimeInSec);
        return "processResult";//return "Finished";
    }



    public List<String> autocompleteHPO(String query) {
        List<String> results = new ArrayList<>();

        List<Term> terms = null;
        try {
            terms = Term.getTermStartingWithText(query, dataSource);
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

    public String getOutputText() {
        this.posTextToProcess();
        return HPOController.outputText;
    }

    public String getProcessAllVisits() {
        return "Finished Processing all visits";
    }

    private List<Term> addDescriptionToTerms(List<Term> terms) {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement ps;
            ResultSet resultSet;

            for (Term currentTerm : terms) {
                ps = connection.prepareStatement("select term_definition from hpo.term_definition where term_definition.term_id = '" + currentTerm.getId() + "'");
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    currentTerm.setDescription(resultSet.getString("term_definition"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return terms;
    }

    public List<Term> getOutputHPOTags() {
        return outputHPOTags;
    }

    public String deleteTerm(Term deleteTerm) {
        outputHPOTags.remove(deleteTerm);
        return null;
    }

    public String processSelectedVisit() {
        return "/visitDetail.xhtml?faces-redirect=true&id=" + selectedVisit.getId();
    }
}
