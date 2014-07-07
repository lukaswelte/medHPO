package Main;

import Main.model.Visit;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;

@ManagedBean(name = "processAll")
@ViewScoped
public class ProcessAllVisits {
    private String processingStatus = "";

    public String getProcessAllVisits() {
        return processingStatus;
    }

    public String action() throws Exception {
        List<Visit> visits = Visit.getAllVisits(HPOController.getKlinikDataSource());
        for (Visit visit : visits) {
            TextProcessor textProcessor = new TextProcessor(HPOController.getHpoDataSource());
            textProcessor.processVisit(visit);
        }
        processingStatus = "Processed all visits successfully";
        return "";
    }
}
