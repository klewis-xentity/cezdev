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

        CHash cvar = (CHash) creturn.data();
        return (cvar != null) ? cvar._("m_value") : null;
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
