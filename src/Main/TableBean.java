package Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Main.model.Visit;
import org.primefaces.model.SelectableDataModel;

import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


/**
 * Created by lukas on 06.08.13.
 */
@ManagedBean(name="tableBean")
@SessionScoped
public class TableBean {

    @Resource(name="jdbc/klinik") private static DataSource klinikDataSource;

    private List<Visit> visitList;

    private Visit selectedVisit;

    private VisitDataModel visitDataModel;

    public int selectedVisitId;

    public TableBean() {
        visitList = new ArrayList<Visit>();

        Connection klinikConnection;
        try {
            klinikConnection = getJDBCConnection("java:comp/env/jdbc/klinik");
            PreparedStatement ps = klinikConnection.prepareStatement("SELECT * FROM Visit");
            ResultSet resultSet = ps.executeQuery();

            while(resultSet.next()) {
                Visit visit = new Visit(resultSet.getInt("id"),resultSet.getInt("patient_id"),resultSet.getString("date"), resultSet.getString("symptoms"), resultSet.getString("additional_text"));
                visitList.add(visit);
            }

        } catch (SQLException exception) {

        } finally {

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
    public String showVisitDetail( Integer id) {
        this.selectedVisitId = id;
        if ( true ) {
            return "showVisitDetail";
        }
        return null;
    }

    /**
     * Gets connection to the Klinik data source
     * @return
     */
    private static Connection getJDBCConnection(String name) throws SQLException {
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

}


class VisitDataModel extends ListDataModel<Visit> implements SelectableDataModel<Visit> {

    public VisitDataModel() {
    }

    public VisitDataModel(List<Visit> data) {
        super(data);
    }



    public Visit getRowData(String rowKey) {
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data

        List<Visit> visits = (List<Visit>) getWrappedData();

        for(Visit visit : visits) {
            if(visit.toString().equals(rowKey))
                return visit;
        }

        return null;
    }

    public Object getRowKey(Visit visit) {
        return visit.toString();
    }
}