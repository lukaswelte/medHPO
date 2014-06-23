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
    private static DataSource mHpoDataSource;
    private static DataSource mKlinikDataSource;

    public static DataSource getHpoDataSource() {
        if (mHpoDataSource == null) {
            DataSource hpoDataSource = null;
            try {
                hpoDataSource = (DataSource) new InitialContext().lookup("jdbc/hpo");
            } catch (NamingException e) {
                e.printStackTrace();
            }
            mHpoDataSource = hpoDataSource;
        }
        return mHpoDataSource;
    }

    public static DataSource getKlinikDataSource() {
        if (mKlinikDataSource == null) {
            DataSource dataSource = null;
            try {
                dataSource = (DataSource) new InitialContext().lookup("jdbc/klinik");
            } catch (NamingException e) {
                e.printStackTrace();
            }
            mKlinikDataSource = dataSource;
        }
        return mKlinikDataSource;
    }

    public String getProcessAllVisits() {
        return "Processed all visits";
    }
}
