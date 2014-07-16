package Main.model;

import Main.HPOController;
import Main.helper.DatabaseCleanup;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HPOInfo {
    private DataSource klinikDataSource = HPOController.getKlinikDataSource();

    private int id;
    private Visit visit;
    private List<Term> hpoMatches;
    private List<List<Term>> hpoMultipleMatches;
    private int sentence_detection_and_tokenization_in_ms;
    private int name_finding_in_ms;
    private int chunking_in_ms;
    private List<String> foundNames;

    public HPOInfo(Visit visit, List<Term> hpoMatches, List<List<Term>> hpoMultipleMatches, int sentence_detection_and_tokenization_in_ms, int name_finding_in_ms, int chunking_in_ms, List<String> foundNames) {
        this.visit = visit;
        this.hpoMatches = hpoMatches;
        this.hpoMultipleMatches = hpoMultipleMatches;
        this.sentence_detection_and_tokenization_in_ms = sentence_detection_and_tokenization_in_ms;
        this.name_finding_in_ms = name_finding_in_ms;
        this.chunking_in_ms = chunking_in_ms;
        this.foundNames = foundNames;
    }

    public static HPOInfo getLastInfoForVisitWithId(int visitID, DataSource klinikDataSource) {
        HPOInfo result = null;
        PreparedStatement preparedStatement = null;
        Connection klinikDataSourceConnection = null;
        ResultSet resultSet = null;
        try {
            klinikDataSourceConnection = klinikDataSource.getConnection();
            preparedStatement = klinikDataSourceConnection.prepareStatement("SELECT * FROM klinik.HPOInfo WHERE visit_id = ? AND id IN (SELECT MAX(id) FROM klinik.HPOInfo  WHERE visit_id=?)");
            preparedStatement.setInt(1, visitID);
            preparedStatement.setInt(2, visitID);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int hpoInfoId = resultSet.getInt("id");
                String hpoMultiMatches = resultSet.getString("hpo_multiple_matches");
                //  List<List<Term>> hpoMultipleMatches = (List<List<Term>>) Term.getObjectFromString(hpoMultiMatches);
                List<List<Term>> hpoMultipleMatches = new ArrayList<>();
                int sentenceDetection = resultSet.getInt("sentence_detection_and_tokenization_in_ms");
                int nameFinding = resultSet.getInt("name_finding_in_ms");
                int chunking = resultSet.getInt("chunking_in_ms");

                preparedStatement = klinikDataSourceConnection.prepareStatement("SELECT name FROM klinik.found_names WHERE info_id = ?");
                preparedStatement.setInt(1, hpoInfoId);
                resultSet = preparedStatement.executeQuery();
                List<String> foundNames = new ArrayList<>();
                while (resultSet.next()) {
                    foundNames.add(resultSet.getString("name"));
                }

                preparedStatement = klinikDataSourceConnection.prepareStatement("SELECT * FROM klinik.found_terms WHERE info_id = ?");
                preparedStatement.setInt(1, hpoInfoId);
                resultSet = preparedStatement.executeQuery();
                List<Term> terms = new ArrayList<>();
                PreparedStatement wordStatement = klinikDataSourceConnection.prepareStatement("SELECT word FROM klinik.found_term_word WHERE found_term = ?");
                while (resultSet.next()) {
                    wordStatement.setInt(1, resultSet.getInt("id"));
                    ResultSet words = wordStatement.executeQuery();
                    List<String> wordList = new ArrayList<>();
                    while (words.next()) {
                        wordList.add(words.getString("word"));
                    }


                    DataSource hpoDataSource = HPOController.getHpoDataSource();
                    if (hpoDataSource != null) {
                        Term term;
                        int term_id = resultSet.getInt("term_id");
                        if (term_id > 0) {
                            term = Term.getTermWithId(term_id, hpoDataSource);
                        } else {
                            term = new Term();
                        }
                        term.setCustomID(resultSet.getLong("custom_id"));
                        term.setCustomName(resultSet.getString("custom_name"));
                        term.setCustomDescription(resultSet.getString("custom_description"));
                        term.setWords(wordList);
                        terms.add(term);
                    }
                }

                Visit visit = Visit.getVisitWithId(visitID, klinikDataSource);
                result = new HPOInfo(visit, terms, hpoMultipleMatches, sentenceDetection, nameFinding, chunking, foundNames);
                result.id = hpoInfoId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseCleanup.closeAll(resultSet, preparedStatement, klinikDataSourceConnection);
        }
        return result;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public List<Term> getHpoMatches() {
        return hpoMatches;
    }

    public void setHpoMatches(List<Term> hpoMatches) {
        this.hpoMatches = hpoMatches;
    }

    public List<List<Term>> getHpoMultipleMatches() {
        return hpoMultipleMatches;
    }

    public void setHpoMultipleMatches(List<List<Term>> hpoMultipleMatches) {
        this.hpoMultipleMatches = hpoMultipleMatches;
    }

    public int getSentence_detection_and_tokenization_in_ms() {
        return sentence_detection_and_tokenization_in_ms;
    }

    public int getName_finding_in_ms() {
        return name_finding_in_ms;
    }

    public int getChunking_in_ms() {
        return chunking_in_ms;
    }

    public int getId() {
        return id;
    }

    public void save() {
        if (id != 0) {
            updateInfo();
        } else {
            insertInfo();
        }
    }

    private void insertInfo() {
        PreparedStatement ps = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = klinikDataSource.getConnection();
            ps = connection.prepareStatement("INSERT INTO HPOInfo " +
                    "(visit_id,hpo_multiple_matches,sentence_detection_and_tokenization_in_ms,name_finding_in_ms,chunking_in_ms)" +
                    " VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, this.getVisit().getId());
            ps.setString(2, Term.getSerializedObject(this.getHpoMultipleMatches()));
            ps.setInt(3, getSentence_detection_and_tokenization_in_ms());
            ps.setInt(4, getName_finding_in_ms());
            ps.setInt(5, getChunking_in_ms());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating info failed, no rows affected.");
            }

            resultSet = ps.getGeneratedKeys();
            if (resultSet.next()) {
                this.id = resultSet.getInt(1);
            } else {
                throw new SQLException("Creating info failed, no generated key obtained.");
            }

            ps = connection.prepareStatement("INSERT INTO found_terms " +
                    "(info_id, term_id, custom_id, custom_name, custom_description)" +
                    " VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO found_term_word " +
                    "(found_term, word)" +
                    " VALUES (?,?)");
            PreparedStatement nameStatement = connection.prepareStatement("INSERT INTO found_names " +
                    "(info_id, name)" +
                    " VALUES (?,?)");
            for (Term term : hpoMatches) {
                ps.setInt(1, getId());
                ps.setInt(2, term.getId());
                ps.setLong(3, term.getCustomID());
                ps.setString(4, term.getCustomName());
                ps.setString(5, term.getCustomDescription());
                affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating info failed, no rows affected.");
                }

                resultSet = ps.getGeneratedKeys();
                if (resultSet.next()) {
                    int foundTermId = resultSet.getInt(1);
                    for (String word : term.getWords()) {
                        statement.setInt(1, foundTermId);
                        statement.setString(2, word);
                        statement.executeUpdate();
                    }

                } else {
                    throw new SQLException("Creating info failed, no generated key obtained.");
                }
            }

            for (String name : foundNames) {
                nameStatement.setInt(1, getId());
                nameStatement.setString(2, name);
                nameStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseCleanup.closeAll(resultSet, ps, connection);
        }
    }

    public void updateTermWords(Term term) {
        PreparedStatement ps = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = klinikDataSource.getConnection();
            if (term.getId() > 0) {
                ps = connection.prepareStatement("SELECT id FROM found_terms WHERE info_id=? AND term_id=?");
                ps.setInt(1, getId());
                ps.setInt(2, term.getId());

            } else {
                ps = connection.prepareStatement("SELECT id FROM found_terms WHERE info_id=? AND custom_id=?");
                ps.setInt(1, getId());
                ps.setLong(2, term.getCustomID());
            }

            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                ps = connection.prepareStatement("DELETE FROM found_term_word WHERE found_term = ?");
                int foundTermID = resultSet.getInt("id");
                ps.setInt(1, foundTermID);
                ps.executeUpdate();

                ps = connection.prepareStatement("INSERT INTO found_term_word (found_term, word) VALUES (?,?)");
                ps.setInt(1, foundTermID);
                for (String word : term.getWords()) {
                    ps.setString(2, word);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseCleanup.closeAll(resultSet, ps, connection);
        }
    }

    private void updateInfo() {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = klinikDataSource.getConnection();
            ps = connection.prepareStatement("UPDATE HPOInfo SET visit_id = ?, hpo_multiple_matches = ?, sentence_detection_and_tokenization_in_ms = ?, name_finding_in_ms = ?, chunking_in_ms = ? WHERE id = ?");
            ps.setInt(1, this.getVisit().getId());
            ps.setString(2, Term.getSerializedObject(this.getHpoMultipleMatches()));
            ps.setInt(3, getSentence_detection_and_tokenization_in_ms());
            ps.setInt(4, getName_finding_in_ms());
            ps.setInt(5, getChunking_in_ms());
            ps.setInt(6, getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseCleanup.closeAll(null, ps, connection);
        }
    }

    public void removeMatchedTermWithId(int termID) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = HPOController.getKlinikDataSource().getConnection();
            preparedStatement = connection.prepareStatement("DELETE FROM found_terms WHERE info_id = ? AND term_id = ?");

            preparedStatement.setInt(1, getId());
            preparedStatement.setInt(2, termID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseCleanup.closeAll(null, preparedStatement, connection);
        }
    }

    public void removeMatchedTermWithCustomID(long customID) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = HPOController.getKlinikDataSource().getConnection();
            preparedStatement = connection.prepareStatement("DELETE FROM found_terms WHERE info_id = ? AND custom_id = ?");

            preparedStatement.setInt(1, getId());
            preparedStatement.setLong(2, customID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseCleanup.closeAll(null, preparedStatement, connection);
        }
    }

    public void saveTerm(Term term) {
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        PreparedStatement statement = null;
        try {
            connection = klinikDataSource.getConnection();
            ps = connection.prepareStatement("INSERT INTO found_terms " +
                    "(info_id, term_id, custom_id, custom_name, custom_description)" +
                    " VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement = connection.prepareStatement("INSERT INTO found_term_word " +
                    "(found_term, word)" +
                    " VALUES (?,?)");
            ps.setInt(1, getId());
            ps.setInt(2, term.getId());
            ps.setLong(3, term.getCustomID());
            ps.setString(4, term.getCustomName());
            ps.setString(5, term.getCustomDescription());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating info failed, no rows affected.");
            }

            resultSet = ps.getGeneratedKeys();
            if (resultSet.next()) {
                int foundTermId = resultSet.getInt(1);
                for (String word : term.getWords()) {
                    statement.setInt(1, foundTermId);
                    statement.setString(2, word);
                    statement.executeUpdate();
                }

            } else {
                throw new SQLException("Creating info failed, no generated key obtained.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseCleanup.closeAll(null, ps, null);
            DatabaseCleanup.closeAll(resultSet, statement, connection);
        }
    }
}
