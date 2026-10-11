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
        
        // Check if this is a select control
        String strComponent = (String) ccontrol._("m_component");
        if (strComponent != null && strComponent.equals("select")) {
            // For select controls, create CHash with m_value and m_options
            CHash stateHash = new CHash();
            Object optionsObj = ccontrol._("m_options");
            Object storedSelectValue = resolveSelectStoredValue(value, optionsObj);
            stateHash.set("m_value", storedSelectValue);
            
            if (optionsObj != null) {
                stateHash.set("m_options", optionsObj);
            }
            
            CReturn creturn = cmemory.upsert(key, stateHash, __.typeOf(stateHash), null);
            return creturn != null && creturn.isdone();
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

        Object storedValue = extractStoredValue(creturn.data());

        if (storedValue instanceof CHash) {
            return (CHash) storedValue;
        }

        // Wrap scalar values for consistency.
        CHash stateHash = new CHash();
        stateHash.set("m_value", storedValue);
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

        // If selected value is already an option key, preserve it.
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
        boolean isSelect = strComponent != null && strComponent.equals("select");

        if (isSelect && !normalizedId.toLowerCase().startsWith("select-")) {
            normalizedId = "select-" + normalizedId;
        }
        return normalizedId;
    }
}
