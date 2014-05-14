package Main;

import Main.model.Term;
import Main.model.TermSearchCandidate;
import Main.model.Visit;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
@SessionScoped
public class HPOController implements Serializable {

    @Resource(name="jdbc/hpo")
    private DataSource dataSource;

    @Resource(name="jdbc/klinik")
    private DataSource klinikDataSource;

    public String getTextToProcess() {
        return textToProcess;
    }

    public void setTextToProcess(String textToProcess) {
        this.textToProcess = textToProcess;
    }

    private String textToProcess = "Jerome had large hands. Suffering from psychotic episodes the patient is just in sickbed. A Prolactin excess made it really hard.";

    private POSModel model;

    /**
     * Retrieve patients and visits while creating the controller
     */
    public HPOController() {
        Connection klinikConnection;
        visits = null;
        try {
            klinikConnection = getJDBCConnection("java:comp/env/jdbc/klinik");
            PreparedStatement ps = klinikConnection.prepareStatement("SELECT id,  patient_id, date FROM visit");
            ResultSet resultSet =  ps.executeQuery();

            visits = new ArrayList<Visit>();

            while(resultSet.next()){
                visits.add(new Visit(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3), "", ""));
            }
        }  catch (SQLException e){
            e.printStackTrace();
        }
    } // end constructor HPOController

    public List<String> run( String sentence ) {
        POSTaggerME tagger = new POSTaggerME( getModel() );
        String[] words = sentence.split( "\\s+" );
        String[] tags = tagger.tag( words );
        double[] probs = tagger.probs();

        List<String> result = new ArrayList<>();
        for( int i = 0; i < tags.length; i++ ) {
            if (tags[i].equals("NN"))
                result.add(words[i]);
            System.out.println( words[i] + " => " + tags[i] + " @ " + probs[i] );
        }

        return result;
    }

    private void setModel( POSModel model ) {
        this.model = model;
    }

    private POSModel getModel() {
        return this.model;
    }

    public String posTextToProcess() {
        if (textToProcess == null) {
            return "No Text to process";
        }


        return posText(textToProcess);
    }


    public String posText(String string) {
        //Start Time Measurement
        long start = System.nanoTime();
        long last = start;

        HPOController hpoController = new HPOController();

        /*
        POSModel model;
        model = new POSModelLoader().load(new File("en-pos-maxent.bin"));
        hpoController.setModel(model);
        List<String> words = hpoController.run(string);
        */

        List<TermSearchCandidate> termSearchCandidates = null;
        try {
            termSearchCandidates = TextParser.chunkAndReturnCandidates(string);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n\n\n128: termSearchCandidates.size = " + termSearchCandidates.size()
            + ",  termSearchCandidates: " + termSearchCandidates + "\n\n");

        // Print Measurement
        double elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        System.out.println("Text Processing Time: "+elapsedTimeInSec);
        last = System.nanoTime();

        HashMap<TermSearchCandidate,List<Term>> foundTerms = new HashMap<TermSearchCandidate, List<Term>>();


        List<Term> terms;
        List<Term> hpoMatchList = new ArrayList<Term>(5);
        List<List<Term>> hpoMultipleMatchList = new ArrayList<List<Term>>(5);
        if (termSearchCandidates == null) return "Finished";

        //Open Database Connection
        Connection con = null;
        try
        {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/hpo");

            if(dataSource==null)
                throw new SQLException("Can't get data source");

            //get database connection
            con = dataSource.getConnection();

            if(con==null)
                throw new SQLException("Can't get database connection");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (con == null) {
            return "No Database connection";
        }

        // Print Measurement
        elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        System.out.println("Opening Database: "+elapsedTimeInSec);
        last = System.nanoTime();

        for (TermSearchCandidate candidate : termSearchCandidates) {
            try {
                terms  =  hpoController.getTermsForCandidate(candidate,con);
                if (terms != null && terms.size()>0) {
                    //System.out.println("\n\n177: candidate: " + candidate + " -------\nterms.size: " + terms.size() + ", terms: " + terms + " ------\n\n");
                    if (terms.size()==1) {
                        hpoMatchList.add(terms.get(0));
                    } else {
                        hpoMultipleMatchList.add(terms);
                    }

                    foundTerms.put(candidate,terms);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Print Measurement
        elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        System.out.println("Search Database: "+elapsedTimeInSec);
        last = System.nanoTime();

        //Close Database Connection
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Print Measurement
        elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        System.out.println("Close Database: "+elapsedTimeInSec);
        last = System.nanoTime();

        if (foundTerms != null && foundTerms.size()>0) {
           System.out.println("\n Found Terms:");
            for (Map.Entry<TermSearchCandidate,List<Term>> entry : foundTerms.entrySet()) {
                System.out.println(entry.getKey()+":\t \t "+entry.getValue().toString()+"\n");
            }


            // Print Measurement
            elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
            System.out.println("Print results: "+elapsedTimeInSec);
            last = System.nanoTime();

            // Print Measurement
            elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
            System.out.println("Overall Time: "+elapsedTimeInSec);

            debugMessage = foundTerms.toString();

            if (anonymizedString != null && !anonymizedString.equals("")) {
                string = anonymizedString;
            }

            hpoMatches = "";
            if((hpoMatchList != null)&&(hpoMatchList.size() > 0)) {
                HPOController.outputText = formatOutput(string, hpoMatchList);

                //Create String from Match Lists
                hpoMatches = HPOController.getJSONStringFromList(hpoMatchList);

                StringBuilder stringBuilder = new StringBuilder("[");
                for (List<Term> termList : hpoMultipleMatchList) {
                     stringBuilder.append(HPOController.getJSONStringFromList(termList));
                    stringBuilder.append(",");
                }
                stringBuilder.deleteCharAt(stringBuilder.length()-1);
                stringBuilder.append("]");
                hpoMultipleMatches = stringBuilder.toString();
            }

            saveVisitAndHPOInfo(); // save data to klinik database

            return "processResult";
        }

        // Print Measurement
        elapsedTimeInSec = (System.nanoTime() - last) * 1.0e-9;
        System.out.println("Print results: "+elapsedTimeInSec);
        last = System.nanoTime();

        // Print Measurement
        elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
        System.out.println("Overall Time: "+elapsedTimeInSec);
        // TODO xxx
        return "processResult";//return "Finished";
    }


    public static String getJSONStringFromList(List<Term> list) {
        StringBuilder stringBuilder = new StringBuilder("[");

        for (Term t : list) {
            stringBuilder.append(t.jsonDescription());
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append("]");

        return stringBuilder.toString();
    }

    public List<Term> getTermsForCandidate(TermSearchCandidate candidate,Connection con) throws SQLException {



        List<Term> result = null;
        List<String[]> candidateSearchStrings = candidate.getSearchStrings();
        for (String[] words : candidateSearchStrings) {

            //Build search values
            StringBuilder buffer = new StringBuilder("%");
            for (String s : words) {
                buffer.append(s);
                buffer.append("%");
            }
            String bufferedString = buffer.toString();


            //Check for too much data
            PreparedStatement ps = con.prepareStatement("SELECT DISTINCT count(*) FROM term LEFT JOIN term_synonym ON term.id = term_synonym.term_id WHERE term.name LIKE ? OR term_synonym.term_synonym LIKE ?");
            ps.setString(1, bufferedString);
            ps.setString(2, bufferedString);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                int numberOfRows = resultSet.getInt(1);
                if (numberOfRows>10) continue;
            } else {
                System.out.println("Error fetching count");
            }
            resultSet.close();
            ps.close();

            //Fetch Data
            ps = con.prepareStatement("SELECT DISTINCT term.id, term.acc, term.name FROM term LEFT JOIN term_synonym ON term.id = term_synonym.term_id WHERE term.name LIKE ? OR term_synonym.term_synonym LIKE ?");
            ps.setString(1, bufferedString);
            ps.setString(2, bufferedString);


            //get customer data from database
            resultSet = ps.executeQuery();

            List<Term> list = new ArrayList<>();

            while (resultSet.next()) {
                Term term = new Term();

                term.setName(resultSet.getString("name"));
                term.setTag(resultSet.getString("acc"));
                term.setId(resultSet.getString("id"));

                //store all data into a List
                list.add(term);
            }

            //Too much results
            if (list.size()>10) continue;

            if (result == null || (result.size() > list.size() && list.size()>0)) {
                result = list;
            }
        }
        return result;
    }

    public List<Term> getTermWithName(String name) throws SQLException {
        if (dataSource == null) {
            try
            {
                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/hpo");
            }
            catch (NamingException e)
            {
                e.printStackTrace();
            }
        }


        if(dataSource==null)
            throw new SQLException("Can't get data source");

        //get database connection
        Connection con = dataSource.getConnection();

        if(con==null)
            throw new SQLException("Can't get database connection");

        PreparedStatement ps
                = con.prepareStatement(
                "SELECT DISTINCT term.acc, term.name FROM term LEFT JOIN term_synonym ON term.id = term_synonym.term_id WHERE term.name LIKE ? OR term_synonym.term_synonym LIKE ?");
        ps.setString(1,"%"+name+"%");
        ps.setString(2,"%"+name+"%");

        //get customer data from database
        ResultSet result =  ps.executeQuery();

        List<Term> list = new ArrayList<>();

        while(result.next()){
            Term term = new Term();

            term.setName(result.getString("name"));
            term.setTag(result.getString("acc"));

            //store all data into a List
            list.add(term);
        }

        return list;
    }


    public List<Term> getTermStartingWithText(String query) throws SQLException {
        if (dataSource == null) {
            try
            {
                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/hpo");
            }
            catch (NamingException e)
            {
                e.printStackTrace();
            }
        }

        if(dataSource==null)
            throw new SQLException("Can't get data source");

        //get database connection
        Connection con = dataSource.getConnection();

        if(con==null)
            throw new SQLException("Can't get database connection");

        PreparedStatement ps = con.prepareStatement(
                "SELECT DISTINCT acc, name FROM term WHERE term.name LIKE ?");
        ps.setString(1,query+"%");

        //get customer data from database
        ResultSet result =  ps.executeQuery();

        List<Term> list = new ArrayList<Term>();

        while(result.next()){
            Term term = new Term();

            term.setName(result.getString("name"));
            term.setTag(result.getString("acc"));

            //store all data into a List
            list.add(term);
        }

        ps = con.prepareStatement(
                "SELECT DISTINCT term_synonym, acc FROM term_synonym JOIN term ON term_synonym.term_id = term.id WHERE term_synonym LIKE ?");
        ps.setString(1,query+"%");

        //get customer data from database
        result =  ps.executeQuery();

        while(result.next()){
            Term term = new Term();

            term.setName(result.getString("term_synonym"));
            term.setTag(result.getString("acc"));

            //store all data into a List
            list.add(term);
        }

        //Too much data
        if (list.size()>20) list.clear();

        return list;
    }

    public List<String> autocompleteHPO(String query) {
        List<String> results = new ArrayList<>();

        HPOController hpoController = new HPOController();
        List<Term> terms = null;
        try {
           terms  = hpoController.getTermStartingWithText(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (terms == null) return results;

        for (Term term : terms) {
            results.add(term.toString());
        }

        return results;
    }


    public static String anonymizedString;

    /** Statistics  **/
    public static int sentence_detection_and_tokenization_in_ms;
    public static int name_finding_in_ms;
    public static int chunking_in_ms;
    public static List<Visit> visits;
    public static int selectedVisit;
    public static String outputText;
    public static String debugMessage;
    public static String hpoMatches;
    public static String hpoMultipleMatches;
    public static int numberFoundNames;

    // Getters and Setters
    public List<Visit> getVisits() {
        return visits;
    }
    public int getSelectedVisit() {
        return selectedVisit;
    }
    public void setSelectedVisit(int visitId) {
        selectedVisit = visitId;
    }
    public String getOutputText() {
        this.posTextToProcess();
        return HPOController.outputText;
    }
    public String getDebugMessage() {
        return HPOController.debugMessage;
    }

    private Boolean saveToDatabase() throws SQLException {

        Connection con = getJDBCConnection("java:comp/env/jdbc/klinik");

        /* Done: Save Anonymized Text back into Visits */
        /* TODO: Save Statistics and found data in HPO-Infos Table and reference it to the visit. Save absolut matches (one term in the match) in the hpo_matches field and matches with multiple terms in the hpo_multiple_matches */

        return true;
    }

    /**
     * Gets connection to the Klinik data source
      * @return
     */
    private Connection getJDBCConnection(String name) throws SQLException {
        if (klinikDataSource == null) {
            try
            {
                Context ctx = new InitialContext();
                klinikDataSource = (DataSource) ctx.lookup(name);//"java:comp/env/jdbc/klinik");
            }
            catch (NamingException e)
            {
                e.printStackTrace();
            }
        }

        if(klinikDataSource==null)
            throw new SQLException("Can't get data source");

        //get database connection
        Connection con = klinikDataSource.getConnection();

        if(con==null)
            throw new SQLException("Can't get database connection");

        return con;
    } // getJDBCConnection


    /**
     * Save data to visit and hpoinfo table
     */
    private void saveVisitAndHPOInfo() {
        try {
            Connection klinikConnection = getJDBCConnection("java:comp/env/jdbc/klinik");
            PreparedStatement ps = klinikConnection.prepareStatement("UPDATE visit SET additional_text = '"
                + anonymizedString + "' WHERE id = " + selectedVisit);
            ps.executeUpdate();

            if (selectedVisit != 0) {
                ps = klinikConnection.prepareStatement("SELECT MAX(id) FROM hpoinfo");
                ResultSet resultSet = ps.executeQuery();

                int highestID = 0;
                if (resultSet.next()) {
                    highestID = resultSet.getInt(1);
                }

                String hpoMatchesValueToInsert = "";
                if (hpoMatches != null) {
                    hpoMatchesValueToInsert = hpoMatches;
                }

                String hpoMultipleMatchesValueToInsert = "";
                if (hpoMultipleMatches != null) {
                    hpoMultipleMatchesValueToInsert = hpoMultipleMatches;
                }

                String SQL = "INSERT INTO hpoinfo VALUES ("
                        + (highestID + 1) + ", " + selectedVisit + ", '"
                        + hpoMatchesValueToInsert + "', "+numberFoundNames+", '"+hpoMultipleMatchesValueToInsert+"',"
                        + sentence_detection_and_tokenization_in_ms + ", "
                        + name_finding_in_ms + ", "
                        + chunking_in_ms + ")";
                System.out.println("INSERT SQL:  " + SQL);


                ps = klinikConnection.prepareStatement(SQL);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String processAllVisits;

    public String getProcessAllVisits() {
        try {
            Connection connection = klinikDataSource.getConnection();



        } catch (SQLException e) {
            e.printStackTrace();
            return "Error on Processing all visits";
        }

        return "Finished Processing all visits";
    }


    /**
     * Formats output highlighting terms. Example:
     * 'The patient had large hands. Suffering from psychotic episodes the patient is just in sickbed.'
     * Would be transformed into
     * 'The patient had <em>large hands</em>. Suffering from <em>psychotic episodes</em> the patient is just in sickbed.'
     * @param originalStr
     * @param terms
     * @return
     */
    private String formatOutput(String originalStr, List<Term> terms) {
        StringBuffer originalString = new StringBuffer(originalStr);
        //boolean found = true;
        int beginIndex;
        int endIndex;
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement ps;
            ResultSet resultSet;
            String definition;
            String titleTag;

            for (Term currentTerm: terms) {
                String currentTermName = currentTerm.name;
                if (originalString.toString().toLowerCase().contains(currentTermName.toLowerCase())) {
                    ps = connection.prepareStatement("select term_definition from hpo.term_definition where term_definition.term_id = '"+currentTerm.id+"'");
                    resultSet =  ps.executeQuery();
                    definition = "";
                    while(resultSet.next()) {
                        definition += resultSet.getString("term_definition");
                    }

                    titleTag = "title='Name: " + currentTerm.name + "-br-" + currentTerm.tag;
                    if (!(definition.equals(""))) {
                        titleTag += "-br--br-Definition: " + definition + "'";
                    }
                    titleTag += "'";

                    beginIndex = originalString.toString().toLowerCase().indexOf(currentTermName.toLowerCase());
                    endIndex = beginIndex + currentTermName.length();
                    currentTerm.startIndex = beginIndex;
                    currentTerm.endIndex = endIndex;
                    originalString = originalString.insert(endIndex, "</span>");
                    originalString = originalString.insert(beginIndex, "<span " + titleTag + " style='text-decoration: underline;'>");
                }
            } // for

        } catch(SQLException e) {
            e.printStackTrace();
        }
        return originalString.toString();
    } // formatOutput

    /* Done: Add interface to select the patient and the visit in addition to the existing text field that has the info of additional_text */
    /* Done: Fill the Klinik database with some sample data. Feel like you are in a english hospital. You can also look at the HPO table to see term examples */
    /* TODO: Create Format where the term and the text where it was found are connected */
    /* Done: Show tooltip with the Term and its description when a user moves the mouse over the associated text. Underline all of these associated texts */
    /* TODO: Create Button where automated the latest visit of each patient is processed */
}
