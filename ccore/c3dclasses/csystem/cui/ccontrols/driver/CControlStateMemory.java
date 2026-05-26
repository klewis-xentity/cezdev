//--------------------------------------------------------------
// name: CControlStateMemory
// desc: Persists control state changes into CMemory for later retrieval
//--------------------------------------------------------------
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
            stateHash.set("m_value", value);
            
            Object optionsObj = ccontrol._("m_options");
            if (optionsObj != null) {
                stateHash.set("m_options", optionsObj);
            }
            
            // Use the value's type, not "chash"
            String valueType = __.typeOf(value);
            CReturn creturn = cmemory.upsert(key, stateHash, valueType, null);
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

        Object data = creturn.data();
        
        // If data is a CHash, extract m_value (used for select controls)
        if (data instanceof CHash) {
            CHash cvar = (CHash) data;
            return cvar._("m_value");
        }
        
        // Otherwise return the value directly (used for other control types)
        return data;
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
        
        // If data is already a CHash (for select controls), return it
        if (data instanceof CHash) {
            return (CHash) data;
        }
        
        // Otherwise wrap the value in a CHash for consistency
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

    static String toMemoryKey(CControl ccontrol) {
        String pathId = (String) ccontrol._("m_strpathid");
        if (pathId == null || pathId.trim().isEmpty()) {
            pathId = (String) ccontrol._("m_strid");
        }
        if (pathId == null || pathId.trim().isEmpty()) {
            pathId = "unknown";
        }

        String normalized = pathId.trim().replace(" ", ".").replace("/", ".").replace("\\", ".");
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
}
