package Main.model;

import Main.HPOController;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("termConverter")
public class TermConverter implements Converter {

    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value != null && value.trim().length() > 0) {
            Term term = Term.getTermWithId(Integer.parseInt(value), HPOController.getHpoDataSource());
            if (term == null || term.getId() == 0) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "das", ""));
            }
            return term;
        } else {
            return null;
        }
    }

    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if (object != null) {
            return String.valueOf(((Term) object).getId());
        } else {
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "das", ""));
            //return null;
        }
    }
}
