package Main;

import Main.model.Visit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.sql.DataSource;

@ManagedBean(name = "visitDetail")
@RequestScoped
public class VisitDetail {
    @Resource(lookup = "jdbc/klinik")
    private DataSource klinikDataSource;

    @ManagedProperty(value = "#{param['id']}")
    private int visitID;
    private Visit visit;

    public int getVisitID() {
        return visitID;
    }

    public void setVisitID(int visitID) {
        this.visitID = visitID;
    }

    @PostConstruct
    private void init() {
        visit = Visit.getVisitWithId(getVisitID(), klinikDataSource);
    }

    public Visit getVisit() {
        return visit;
    }
}
