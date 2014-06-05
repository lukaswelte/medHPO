package Main;

import Main.model.Visit;
import org.primefaces.model.SelectableDataModel;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.ListDataModel;
import javax.sql.DataSource;
import java.util.List;

@ManagedBean(name = "tableBean")
@ViewScoped
public class TableBean {

    @Resource(lookup = "jdbc/klinik")
    private DataSource klinikDataSource;
    private Visit selectedVisit;
    private VisitDataModel visitDataModel;

    @PostConstruct
    public void init() {
        visitDataModel = new VisitDataModel(Visit.getAllVisits(klinikDataSource));
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
        return "/visitDetail.xhtml?faces-redirect=true&id=" + id;
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