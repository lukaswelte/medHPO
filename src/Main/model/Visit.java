package Main.model;

import Main.HPOController;
import Main.helper.DatabaseCleanup;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Visit {
    private int id;
    private int patientID;
    private String date;
    private String symptoms;
    private String additionalText;

    public Visit(int id, int patientID, String date, String symptoms, String additionalText) {
        this.id = id;
        this.patientID = patientID;
        this.date = date;
        this.symptoms = symptoms;
        this.additionalText = additionalText;
    }

    public static Visit getVisitWithId(int visitID, DataSource klinikDataSource) {
        Visit visit = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        Connection connection = null;
        try {
            connection = klinikDataSource.getConnection();
            ps = connection.prepareStatement("SELECT id,  patient_id, date, symptoms, additional_text FROM visit  WHERE id = " + visitID);
            resultSet = ps.executeQuery();


            while (resultSet.next()) {
                visit = new Visit(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseCleanup.closeAll(resultSet, ps, connection);
        }
        return visit;
    }

    public static List<Visit> getAllVisits(DataSource klinikDataSource) {
        List<Visit> visits = null;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            connection = klinikDataSource.getConnection();
            ps = connection.prepareStatement("SELECT id,  patient_id, date, symptoms, additional_text FROM visit");
            resultSet = ps.executeQuery();

            visits = new ArrayList<>();

            while (resultSet.next()) {
                visits.add(new Visit(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5)));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseCleanup.closeAll(resultSet, ps, connection);
        }
        return visits;
    }

    public void saveToDatabase() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = HPOController.getKlinikDataSource().getConnection();
            preparedStatement = connection.prepareStatement("UPDATE klinik.Visit SET additional_text=? WHERE klinik.Visit.id = ?");
            preparedStatement.setString(1, getAdditionalText());
            preparedStatement.setInt(2, getId());
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseCleanup.closeAll(null, preparedStatement, connection);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getAdditionalText() {
        return additionalText;
    }

    public void setAdditionalText(String additionalText) {
        this.additionalText = additionalText;
    }
}