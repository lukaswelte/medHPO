package Main;

import Main.model.Visit;
import org.primefaces.model.SelectableDataModel;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.ListDataModel;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "tableBean")
@SessionScoped
public class TableBean {

    public int selectedVisitId;
    @Resource(lookup = "jdbc/klinik")
    private DataSource klinikDataSource;
    private Visit selectedVisit;
    private VisitDataModel visitDataModel;

    @PostConstruct
    public void init() {
        List<Visit> visitList = new ArrayList<>();

        Connection klinikConnection;
        try {
            klinikConnection = klinikDataSource.getConnection();
            PreparedStatement ps = klinikConnection.prepareStatement("SELECT * FROM Visit");
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                Visit visit = new Visit(resultSet.getInt("id"), resultSet.getInt("patient_id"), resultSet.getString("date"), resultSet.getString("symptoms"), resultSet.getString("additional_text"));
                visitList.add(visit);
            }

        } catch (SQLException ignored) {

        }

        visitDataModel = new VisitDataModel(visitList);
    }

    public Visit getSelectedVisit() {
        return selectedVisit;
    }

    public void setSelectedVisit(Visit selectedVisit) {
        this.selectedVisit = selectedVisit;
        this.showVisitDetail(this.selectedVisit.getId());
    }

    public VisitDataModel getVisitDataModel() {
        return visitDataModel;
    }

    /**
     * Show visit detail view
     *
     * @return the navigation string
     */
    public String showVisitDetail(Integer id) {
        this.selectedVisitId = id;
        return "showVisitDetail";
    }
}


class VisitDataModel extends ListDataModel<Visit> implements SelectableDataModel<Visit> {

    public VisitDataModel(List<Visit> data) {
        super(data);
    }

    public Visit getRowData(String rowKey) {
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data

        List<Visit> visits = (List<Visit>) getWrappedData();

        for (Visit visit : visits) {
            if (visit.toString().equals(rowKey))
                return visit;
        }

        return null;
    }

    public Object getRowKey(Visit visit) {
        return visit.toString();
    }
}