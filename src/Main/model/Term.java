package Main.model;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Term {

    private String name;
    private String tag;
    private String id;
    private String description;

    private int startIndex;
    private int endIndex;

    public static String getJSONStringFromList(List<Term> list) {
        StringBuilder stringBuilder = new StringBuilder("[");

        for (Term t : list) {
            stringBuilder.append(t.jsonDescription());
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("]");

        return stringBuilder.toString();
    }

    public static List<Term> getTermsForCandidate(TermSearchCandidate candidate, DataSource dataSource) throws SQLException {
        if (dataSource == null)
            throw new SQLException("Can't get data source");

        //get database connection
        Connection con = dataSource.getConnection();

        if (con == null)
            throw new SQLException("Can't get database connection");

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
                if (numberOfRows > 10) continue;
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
            if (list.size() > 10) continue;

            if (result == null || (result.size() > list.size() && list.size() > 0)) {
                result = list;
            }
        }
        con.close();
        return result;
    }

    public static List<Term> getTermStartingWithText(String query, DataSource dataSource) throws SQLException {
        if (dataSource == null)
            throw new SQLException("Can't get data source");

        Connection con = dataSource.getConnection();
        if (con == null)
            throw new SQLException("Can't get database connection");

        PreparedStatement ps = con.prepareStatement(
                "SELECT DISTINCT acc, name FROM term WHERE term.name LIKE ?");
        ps.setString(1, query + "%");

        ResultSet result = ps.executeQuery();

        List<Term> list = new ArrayList<>();

        while (result.next()) {
            Term term = new Term();

            term.setName(result.getString("name"));
            term.setTag(result.getString("acc"));

            //store all data into a List
            list.add(term);
        }

        ps = con.prepareStatement(
                "SELECT DISTINCT term_synonym, acc FROM term_synonym JOIN term ON term_synonym.term_id = term.id WHERE term_synonym LIKE ?");
        ps.setString(1, query + "%");

        result = ps.executeQuery();

        while (result.next()) {
            Term term = new Term();

            term.setName(result.getString("term_synonym"));
            term.setTag(result.getString("acc"));

            //store all data into a List
            list.add(term);
        }

        //Too much data
        if (list.size() > 20) list.clear();
        con.close();
        return list;
    }

    /**
     * Formats output highlighting terms. Example:
     * 'The patient had large hands. Suffering from psychotic episodes the patient is just in sickbed.'
     * Would be transformed into
     * 'The patient had <em>large hands</em>. Suffering from <em>psychotic episodes</em> the patient is just in sickbed.'
     *
     * @param originalStr The input string
     * @param terms       The term list
     * @return Formatted String
     */
    public static String enhanceStringWithTerms(String originalStr, List<Term> terms, DataSource dataSource) {
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

            for (Term currentTerm : terms) {
                String currentTermName = currentTerm.name;
                if (originalString.toString().toLowerCase().contains(currentTermName.toLowerCase())) {
                    ps = connection.prepareStatement("select term_definition from hpo.term_definition where term_definition.term_id = '" + currentTerm.id + "'");
                    resultSet = ps.executeQuery();
                    definition = "";
                    while (resultSet.next()) {
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return originalString.toString();
    } // formatOutput

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public String jsonDescription() {
        return "{" + "start:" + this.getStartIndex() + ",end:" + this.getEndIndex() + ",id:" + this.getId() + "}";
    }
}
