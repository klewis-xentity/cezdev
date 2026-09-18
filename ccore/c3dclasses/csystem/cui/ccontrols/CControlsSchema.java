//----------------------------------------------------------------------
// name: CControlSchema.java
// desc: Convenience wrapper around the schema renderer for CControls
//----------------------------------------------------------------------
package c3dclasses;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class CControlSchema {
    public CControlSchema() {}
    public CControls createCControls(String strJSONSchema){}
    public CControl createCControl(String strJSONSchema){}
    public CControl updateCControl(CControl ccontrol, String strJSONSchema){}
    public CHash    retrieveCControlSchema(CControl ccontrol){}
} // end class CControlSchema
