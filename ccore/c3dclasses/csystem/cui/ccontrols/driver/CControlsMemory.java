//---------------------------------------------------------------------------
// name: CControlMemory
// desc: Persists control state changes into CMemory for later retrieval
//---------------------------------------------------------------------------
package c3dclasses;

class CControlMemory {
    private static final String MEMORY_ID = "ccontrols-state";
    private static final String MEMORY_DRIVER = "c3dclasses.CJSONMemoryDriver";
    private static String  m_strcmemoryfile = null;
    private CMemory m_cmemory;
    
    private CControlMemory() {
        m_cmemory = null; 
    } // end CControlMemory()

    private boolean create() {
        // get the memory file path from environment variable
        m_strcmemoryfile = System.getenv("CMETADATA_CVARS");
        if (m_strcmemoryfile == null || m_strcmemoryfile.trim().isEmpty()) 
            return false;
        // get the CMemory instance using the memory file path and driver
        if (CMemory.include(MEMORY_ID, m_strcmemoryfile, MEMORY_DRIVER, null) == null)
            return false;
        m_cmemory = CMemory.use(MEMORY_ID);
        if (m_cmemory == null)
            return false;
        // ensure the CMemory instance is valid
        return true;
    } // end create()

    public boolean loadCControlState(CControl ccontrol) {
        // if the CMemory instance is not available, we cannot load the control state
        if(m_cmemory == null)
            return false;
        
        // if no CControl is provided, we cannot load its state
        if (ccontrol == null) 
            return false;
        
        // get the control ID from the CControl instance
        String controlId = (String) ccontrol._("m_strid");        
        if (controlId == null || controlId.trim().isEmpty())
            return false;
        
        // retrieve the control state from CMemory using the control ID
        CReturn creturn = m_cmemory.retrieve(controlId);
        if (creturn == null || creturn.data() == null)
            return false;
        
        // check if the retrieved data is a CHash containing the control value
        Object controlValue = null;
        if (creturn.data() instanceof CHash)
            controlValue = ((CHash) creturn.data())._("m_value");
        if (controlValue == null)
            return false;

        // set the control value on the CControl instance
        ccontrol._("m_value", controlValue);
        return true;    
    } // end loadCControlState()

    public boolean saveCControlState(CControl ccontrol) {
        // if the CMemory instance is not available, we cannot save the control state
        if(m_cmemory == null)
            return false;

        // if no CControl is provided, we cannot save its state
        if (ccontrol == null) 
            return false;

        // get the control ID from the CControl instance
        String controlId = (String) ccontrol._("m_strid");        
        if (controlId == null || controlId.trim().isEmpty())
            return false;

        // get the control value from the CControl instance
        Object controlValue = ccontrol._("m_value");
        if (controlValue == null)
            return false;

        // Store the control value using the CMemory API.
        CReturn creturn = m_cmemory.upsert(controlId, controlValue, __.typeOf(controlValue), null);
        return creturn != null && creturn.isdone();
    } // end saveCControlState()
} // end class CControlMemory
