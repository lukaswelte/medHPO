package Main.model;

/**
 * Created by lukas on 06.08.13.
 */
public class Visit {
    private int id;
    private int patientID;
    private String date;
    private String symptoms;
    private String additionalText;

    public Visit(int id, int patientID, String date, String symptoms, String additionalText){
        this.id = id;
        this.patientID = patientID;
        this.date = date;
        this.symptoms = symptoms;
        this.additionalText = additionalText;
    }

    public int getId() {
        return id;
    }

    public int getPatientID() {
        return patientID;
    }

    public String getDate() {
        return date;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public String getAdditionalText() {
        return additionalText;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public void setAdditionalText(String additionalText) {
        this.additionalText = additionalText;
    }
}
