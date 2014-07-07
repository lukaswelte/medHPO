package Main.model;

import Main.helper.DatabaseCleanup;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Term implements Serializable {

    private String name;
    private String tag;
    private int id;
    private long customID;
    private String description;
    private List<String> words;

    public static String getSerializedObject(Object object) {
        String serializedObject = "";
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(object);
            so.flush();
            serializedObject = bo.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serializedObject;
    }

    public static Object getObjectFromString(String serializedObject) {
        Object obj = null;
        try {
            byte b[] = serializedObject.getBytes();
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            obj = si.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static Term getTermWithId(int id, DataSource dataSource) {
        if (id == 0) {
            return new Term();
        }
        Term result = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement("SELECT hpo.term.id, hpo.term.name, hpo.term.acc, hpo.term_definition.term_definition FROM hpo.term LEFT JOIN hpo.term_definition ON hpo.term.id = hpo.term_definition.term_id  WHERE hpo.term.id = ?");
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                result = new Term();
                result.setId(resultSet.getInt("id"));
                result.setDescription(resultSet.getString("term_definition"));
                result.setTag(resultSet.getString("acc"));
                result.setName(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseCleanup.closeAll(resultSet, preparedStatement, connection);
        }
        return result;
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
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        for (String[] words : candidateSearchStrings) {

            //Build search values
            StringBuilder buffer = new StringBuilder("%");
            for (String s : words) {
                buffer.append(s);
                buffer.append("%");
            }
            String bufferedString = buffer.toString();


            //Check for too much data
            ps = con.prepareStatement("SELECT DISTINCT count(*) FROM term LEFT JOIN term_synonym ON term.id = term_synonym.term_id WHERE term.name LIKE ? OR term_synonym.term_synonym LIKE ?");
            ps.setString(1, bufferedString);
            ps.setString(2, bufferedString);
            resultSet = ps.executeQuery();
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
                term.setId(resultSet.getInt("id"));
                term.setWords(Arrays.asList(words));

                //store all data into a List
                list.add(term);
            }

            //Too much results
            if (list.size() > 10) continue;

            if (result == null || (result.size() > list.size() && list.size() > 0)) {
                result = list;
            }
        }
        DatabaseCleanup.closeAll(resultSet, ps, con);
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
                "SELECT DISTINCT term_synonym, acc FROM term_synonym JOIN term ON term_synonym.term_id = term.id WHERE term_synonym LIKE ? LIMIT 0,25");
        ps.setString(1, query + "%");

        result = ps.executeQuery();

        while (result.next()) {
            Term term = new Term();

            term.setName(result.getString("term_synonym"));
            term.setTag(result.getString("acc"));

            //store all data into a List
            list.add(term);
        }
        DatabaseCleanup.closeAll(result, ps, con);

        //Too much data
        if (list.size() > 20) list.clear();
        return list;
    }

    public static List<Term> addDescriptionToTerms(List<Term> terms, DataSource dataSource) {
        for (Term term : terms) {
            term.fetchDescription(dataSource);
        }
        return terms;
    }

    public void fetchDescription(DataSource dataSource) {
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement("select term_definition from hpo.term_definition where term_definition.term_id = '" + this.getId() + "'");
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                this.setDescription(resultSet.getString("term_definition"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseCleanup.closeAll(resultSet, ps, connection);
        }
    }

    public String getName() {
        if (getCustomID() > 0) {
            return "Custom Term";
        }
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getWords() {
        if (words == null) {
            words = new ArrayList<>();
        }
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public long getCustomID() {
        return customID;
    }

    public void setCustomID(long customID) {
        this.customID = customID;
    }
}
