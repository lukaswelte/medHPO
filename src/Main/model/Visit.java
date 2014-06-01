package Main.model;

import javax.sql.DataSource;
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
        try {
            PreparedStatement ps = klinikDataSource.getConnection().prepareStatement("SELECT id,  patient_id, date, symptoms, additional_text FROM visit  WHERE id = " + visitID);
            ResultSet resultSet = ps.executeQuery();


            while (resultSet.next()) {
                visit = new Visit(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return visit;
    }

    public static List<Visit> getAllVisits(DataSource klinikDataSource) {
        List<Visit> visits = null;
        try {
            PreparedStatement ps = klinikDataSource.getConnection().prepareStatement("SELECT id,  patient_id, date, symptoms, additional_text FROM visit");
            ResultSet resultSet = ps.executeQuery();

            visits = new ArrayList<>();

            while (resultSet.next()) {
                visits.add(new Visit(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return visits;
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