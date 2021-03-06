package ge.edu.tsu.hrs.control_panel.server.caching;

import ge.edu.tsu.hrs.control_panel.model.sysparam.Parameter;
import ge.edu.tsu.hrs.control_panel.model.sysparam.SystemParameter;
import ge.edu.tsu.hrs.control_panel.server.dao.systemparameter.SystemParameterDAO;
import ge.edu.tsu.hrs.control_panel.server.dao.systemparameter.SystemParameterDAOImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedSystemParameter {

    private static SystemParameterDAO systemParameterDAO = new SystemParameterDAOImpl();

    private static Map<String,String> cachedParameters;

    public static String getStringParameterValue(Parameter parameter) {
        if (cachedParameters == null) {
            fillParameters();
        }
        if (cachedParameters == null) {
            return parameter.getDefaultValue();
        }
        String value = cachedParameters.get(parameter.getKey());
        if (value != null) {
            return value;
        } else {
            return parameter.getDefaultValue();
        }
    }

    public static void deleteParameter(String key) {
        if (cachedParameters == null) {
            fillParameters();
        }
        cachedParameters.remove(key);
    }

    public static void editOrAddParameter(SystemParameter systemParameter) {
        if (cachedParameters == null) {
            fillParameters();
        }
        cachedParameters.put(systemParameter.getKey(), systemParameter.getValue());
    }

    public static void fillParameters() {
        try {
            cachedParameters = new HashMap<>();
            List<SystemParameter> systemParameterList = systemParameterDAO.getSystemParameters(null, null);
            for (SystemParameter systemParameter : systemParameterList) {
                cachedParameters.put(systemParameter.getKey(), systemParameter.getValue());
            }
        } catch (Exception ex) {
            cachedParameters = null;
            ex.printStackTrace();
            System.out.println("can't catch system parameters... all parameter value mast be default...");
        }
    }
}
