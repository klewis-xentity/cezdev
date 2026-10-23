//---------------------------------------------------------------------------
// name: CControlStateMemory
// desc: Persists control state changes into CMemory for later retrieval
//---------------------------------------------------------------------------
package c3dclasses;

class CControlStateMemory {
    private static final String MEMORY_ID = "ccontrols-state";

    private CControlStateMemory() {
    }

    static boolean save(CControl ccontrol, Object value) {
        if (ccontrol == null) {
            return false;
        }

        String memoryPath = resolveMemoryPath();
        if (memoryPath == null || memoryPath.trim().isEmpty()) {
            return false;
        }

        if (CMemory.include(MEMORY_ID, memoryPath, "c3dclasses.CJSONMemoryDriver", null) == null) {
            return false;
        }

        CMemory cmemory = CMemory.use(MEMORY_ID);
        if (cmemory == null) {
            return false;
        }

        String key = toMemoryKey(ccontrol);
        
        String strComponent = (String) ccontrol._("m_component");

        // Check if this is a select control
        if (isSelectComponent(strComponent)) {
            // For select controls, persist outer m_value and inner m_ccontrol_value details.
            Object optionsObj = ccontrol._("m_options");
            Object selectedOption = resolveSelectStoredOption(value, optionsObj);
            Object storedSelectValue = resolveSelectStoredValue(value, optionsObj);
            CReturn creturn = cmemory.upsert(key, storedSelectValue, __.typeOf(storedSelectValue), null);
            if (creturn == null || !creturn.isdone()) {
                return false;
            }

            // Enrich the record with control-specific state payload.
            CHash record = null;
            if (creturn.data() instanceof CHash) {
                record = (CHash) creturn.data();
            } else {
                CReturn retrieveReturn = cmemory.retrieve(key);
                if (retrieveReturn != null && retrieveReturn.data() instanceof CHash) {
                    record = (CHash) retrieveReturn.data();
                }
            }

            if (record != null) {
                CHash controlState = new CHash();
                controlState.set("m_value", storedSelectValue);
                controlState.set("m_selectedoption", selectedOption);
                if (optionsObj != null) {
                    controlState.set("m_options", optionsObj);
                }
                record.set("m_ccontrol_value", controlState);
                record.set("m_value", storedSelectValue);
                cmemory.sync();
            }
            
            return true;
        }

        if (isTextBackedComponent(strComponent)) {
            // Persist text controls in both m_value and m_ccontrol_value for compatibility.
            CReturn creturn = cmemory.upsert(key, value, __.typeOf(value), null);
            if (creturn == null || !creturn.isdone()) {
                return false;
            }

            CHash record = null;
            if (creturn.data() instanceof CHash) {
                record = (CHash) creturn.data();
            } else {
                CReturn retrieveReturn = cmemory.retrieve(key);
                if (retrieveReturn != null && retrieveReturn.data() instanceof CHash) {
                    record = (CHash) retrieveReturn.data();
                }
            }

            if (record != null) {
                CHash controlState = new CHash();
                controlState.set("m_value", value);
                record.set("m_ccontrol_value", controlState);
                record.set("m_value", value);
                cmemory.sync();
            }

            return true;
        }
        
        // For all other controls, save the value directly with its proper type
        CReturn creturn = cmemory.upsert(key, value, __.typeOf(value), null);
        return creturn != null && creturn.isdone();
    }

    static Object load(CControl ccontrol) {
        if (ccontrol == null) {
            return null;
        }

        String memoryPath = resolveMemoryPath();
        if (memoryPath == null || memoryPath.trim().isEmpty()) {
            return null;
        }

        if (CMemory.include(MEMORY_ID, memoryPath, "c3dclasses.CJSONMemoryDriver", null) == null) {
            return null;
        }

        CMemory cmemory = CMemory.use(MEMORY_ID);
        if (cmemory == null) {
            return null;
        }

        CReturn creturn = cmemory.retrieve(toMemoryKey(ccontrol));
        if (creturn == null || creturn.data() == null) {
            return null;
        }

        Object storedValue = extractStoredValue(creturn.data());

        // Prefer control payload when present and fallback to m_value for legacy records.
        if (storedValue instanceof CHash) {
            CHash stateHash = (CHash) storedValue;
            Object controlValue = stateHash._("m_ccontrol_value");
            if (controlValue instanceof CHash) {
                return ((CHash) controlValue)._("m_value");
            }
            if (controlValue != null) {
                return controlValue;
            }

            if (stateHash.containsKey("m_value")) {
                return stateHash._("m_value");
            }
        }

        // Select controls store a state hash { m_value, m_options }.
        if (storedValue instanceof CHash) {
            return ((CHash) storedValue)._("m_value");
        }

        // Other controls persist scalar values directly.
        return storedValue;
    }

    static CHash loadState(CControl ccontrol) {
        if (ccontrol == null) {
            return null;
        }

        String memoryPath = resolveMemoryPath();
        if (memoryPath == null || memoryPath.trim().isEmpty()) {
            return null;
        }

        if (CMemory.include(MEMORY_ID, memoryPath, "c3dclasses.CJSONMemoryDriver", null) == null) {
            return null;
        }

        CMemory cmemory = CMemory.use(MEMORY_ID);
        if (cmemory == null) {
            return null;
        }

        CReturn creturn = cmemory.retrieve(toMemoryKey(ccontrol));
        if (creturn == null || creturn.data() == null) {
            return null;
        }

        Object data = creturn.data();
        if (data instanceof CHash) {
            CHash record = (CHash) data;
            Object controlValue = record._("m_ccontrol_value");
            if (controlValue instanceof CHash) {
                return (CHash) controlValue;
            }

            Object directValue = extractStoredValue(record);
            if (directValue instanceof CHash) {
                return (CHash) directValue;
            }

            CHash stateHash = new CHash();
            stateHash.set("m_value", directValue);
            return stateHash;
        }

        // Wrap scalar values for consistency.
        CHash stateHash = new CHash();
        stateHash.set("m_value", data);
        return stateHash;
    }

    static Object loadOptions(CControl ccontrol) {
        CHash stateHash = loadState(ccontrol);
        if (stateHash == null) {
            return null;
        }
        
        // Return m_options if present (typically for select controls)
        return stateHash._("m_options");
    }

    static boolean delete(CControl ccontrol) {
        if (ccontrol == null) {
            return false;
        }

        String memoryPath = resolveMemoryPath();
        if (memoryPath == null || memoryPath.trim().isEmpty()) {
            return false;
        }

        if (CMemory.include(MEMORY_ID, memoryPath, "c3dclasses.CJSONMemoryDriver", null) == null) {
            return false;
        }

        CMemory cmemory = CMemory.use(MEMORY_ID);
        if (cmemory == null) {
            return false;
        }

        CReturn creturn = cmemory.delete(toMemoryKey(ccontrol));
        return creturn != null && creturn.isdone();
    }

    static String toMemoryKey(CControl ccontrol) {
        String controlId = resolveControlId(ccontrol);
        if (controlId == null || controlId.trim().isEmpty()) {
            controlId = "unknown";
        }

        String normalized = controlId.trim().replace(" ", ".").replace("/", ".").replace("\\", ".");
        return "ccontrols.state." + normalized;
    }

    private static String resolveMemoryPath() {
        String cvarsPath = System.getenv("CMETADATA_CVARS");
        if (cvarsPath != null && !cvarsPath.trim().isEmpty()) {
            return cvarsPath;
        }

        String metadataPath = System.getenv("CMETADATA");
        if (metadataPath != null && !metadataPath.trim().isEmpty()) {
            return metadataPath + "/ccontrols.state.json";
        }

        return null;
    }

    private static Object extractStoredValue(Object data) {
        if (!(data instanceof CHash)) {
            return data;
        }

        CHash record = (CHash) data;
        if (record.containsKey("m_strname") || record.containsKey("m_strtype") || record.containsKey("m_icreated")) {
            return record._("m_value");
        }

        return data;
    }

    private static Object resolveSelectStoredValue(Object selectedValue, Object optionsObj) {
        if (selectedValue == null) {
            return null;
        }

        if (!(optionsObj instanceof CHash)) {
            return selectedValue;
        }

        String selected = String.valueOf(selectedValue);
        CHash options = (CHash) optionsObj;

        // If selected value is an option key, store the mapped option value.
        if (options.containsKey(selected)) {
            return options._(selected);
        }

        String normalizedSelected = normalizeValue(selected);
        CArray keys = options.keys();
        for (int i = 0; i < keys.length(); i++) {
            String key = keys._string(i);
            String optionValue = options._string(key);
            if (selected.equals(optionValue) || normalizedSelected.equals(normalizeValue(optionValue))) {
                return optionValue;
            }
        }

        return selected;
    }

    private static Object resolveSelectStoredOption(Object selectedValue, Object optionsObj) {
        if (selectedValue == null) {
            return null;
        }

        if (!(optionsObj instanceof CHash)) {
            return selectedValue;
        }

        String selected = String.valueOf(selectedValue);
        CHash options = (CHash) optionsObj;

        if (options.containsKey(selected)) {
            return selected;
        }

        String normalizedSelected = normalizeValue(selected);
        CArray keys = options.keys();
        for (int i = 0; i < keys.length(); i++) {
            String key = keys._string(i);
            String optionValue = options._string(key);
            if (selected.equals(optionValue) || normalizedSelected.equals(normalizeValue(optionValue))) {
                return key;
            }
        }

        return selected;
    }

    private static String normalizeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace("\\", "/").toLowerCase();
    }

    private static boolean isSelectComponent(String component) {
        return component != null && component.equals("select");
    }

    private static boolean isTextBackedComponent(String component) {
        return component != null && (component.equals("text") || component.equals("textarea"));
    }

    private static String resolveControlId(CControl ccontrol) {
        String controlId = (String) ccontrol._("m_strid");
        if (controlId == null || controlId.trim().isEmpty()) {
            controlId = (String) ccontrol._("m_strpathid");
        }

        if (controlId == null) {
            return null;
        }

        String normalizedId = controlId.trim();
        String strComponent = (String) ccontrol._("m_component");
        boolean isSelect = isSelectComponent(strComponent);

        if (isSelect && !normalizedId.toLowerCase().startsWith("select-")) {
            normalizedId = "select-" + normalizedId;
        }
        return normalizedId;
    }
}
