package Main.model;

import Main.HPOController;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("termConverter")
public class TermConverter implements Converter {


    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value != null && value.trim().length() > 0) {
            return Term.getTermWithId(Integer.parseInt(value), HPOController.getHpoDataSource());
        } else {
            return null;
        }
    }

    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if (object != null) {
            return String.valueOf(((Term) object).getId());
        } else {
            return null;
        }
    }
}
