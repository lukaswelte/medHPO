package Main.model;

import Main.HPOController;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class HPOInfo {
    private DataSource klinikDataSource = HPOController.INSTANCE.getKlinikDataSource();

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
        PreparedStatement ps;
        HPOInfo result = null;
        try {
            ps = klinikDataSource.getConnection().prepareStatement("");
            ResultSet resultSet = ps.executeQuery();
            Visit visit = Visit.getVisitWithId(resultSet.getInt("visit_id"), klinikDataSource);
            //result = new HPOInfo(visit,)
        } catch (SQLException e) {
            e.printStackTrace();
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
        PreparedStatement ps;
        try {
            ps = klinikDataSource.getConnection().prepareStatement("INSERT INTO HPOInfo " +
                    "(visit_id,hpo_multiple_matches,sentence_detection_and_tokenization_in_ms,name_finding_in_ms,chunking_in_ms)" +
                    " VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, this.getVisit().getId());
            ps.setString(2, Term.getSerializedObject(this.getHpoMultipleMatches()));
            ps.setInt(3, getSentence_detection_and_tokenization_in_ms());
            ps.setInt(4, getName_finding_in_ms());
            ps.setInt(5, getChunking_in_ms());
            ps.executeUpdate();
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating info failed, no rows affected.");
            }

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                this.id = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating info failed, no generated key obtained.");
            }

            ps = klinikDataSource.getConnection().prepareStatement("INSERT INTO found_terms " +
                    "(info_id, term_id)" +
                    " VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement statement = klinikDataSource.getConnection().prepareStatement("INSERT INTO found_term_word " +
                    "(found_term, word)" +
                    " VALUES (?,?)");
            PreparedStatement nameStatement = klinikDataSource.getConnection().prepareStatement("INSERT INTO found_names " +
                    "(info_id, name)" +
                    " VALUES (?,?)");
            for (Term term : hpoMatches) {
                ps.setInt(1, getId());
                ps.setInt(2, term.getId());
                ps.executeUpdate();

                affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating info failed, no rows affected.");
                }

                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int foundTermId = generatedKeys.getInt(1);
                    for (String word : term.getWords()) {
                        statement.setInt(1, foundTermId);
                        statement.setString(2, word);
                        statement.executeUpdate();
                    }

                    for (String name : foundNames) {
                        nameStatement.setInt(1, foundTermId);
                        nameStatement.setString(2, name);
                        nameStatement.executeUpdate();
                    }

                } else {
                    throw new SQLException("Creating info failed, no generated key obtained.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void updateInfo() {
        PreparedStatement ps;
        try {
            ps = klinikDataSource.getConnection().prepareStatement("UPDATE HPOInfo SET visit_id = ?, hpo_multiple_matches = ?, sentence_detection_and_tokenization_in_ms = ?, name_finding_in_ms = ?, chunking_in_ms = ? WHERE id = ?");
            ps.setInt(1, this.getVisit().getId());
            ps.setString(2, Term.getSerializedObject(this.getHpoMultipleMatches()));
            ps.setInt(3, getSentence_detection_and_tokenization_in_ms());
            ps.setInt(4, getName_finding_in_ms());
            ps.setInt(5, getChunking_in_ms());
            ps.setInt(6, getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
