package Main;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.Serializable;

@ManagedBean(name = "HPOController")
@ApplicationScoped
public class HPOController implements Serializable {

    public static DataSource getHpoDataSource() {
        DataSource hpoDataSource = null;
        try {
            hpoDataSource = (DataSource) new InitialContext().lookup("jdbc/hpo");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return hpoDataSource;
    }

    public static DataSource getKlinikDataSource() {
        DataSource dataSource = null;
        try {
            dataSource = (DataSource) new InitialContext().lookup("jdbc/klinik");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return dataSource;
    }

    public String getProcessAllVisits() {
        return "Processed all visits";
    }
}
