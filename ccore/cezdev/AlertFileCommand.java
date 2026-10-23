//-------------------------------------------------------
// name: AlertFileCommand
// desc: Shows file content in a Swing alert dialog
//-------------------------------------------------------
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JOptionPane;

//------------------------------------------------------------------------
// name: AlertFileCommand
// desc:
//   args[0] = filename (required)
//   args[1] = title (optional, default: "Message")
//   args[2] = type  (optional: error|warn|plain|info)
//------------------------------------------------------------------------
public class AlertFileCommand {
	public static void main(String[] args) {
		if (args.length < 1)
			return;

		String strfilename = args[0];
		String strtitle = (args.length > 1) ? args[1] : "Message";
		String strtype = (args.length > 2) ? args[2].toLowerCase() : "info";

		Path filepath = Path.of(strfilename);
		if (!Files.exists(filepath)) {
			JOptionPane.showMessageDialog(null, "File not found: " + strfilename, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String strmsg;
		try {
			strmsg = Files.readString(filepath, StandardCharsets.UTF_8);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Failed to read file: " + strfilename, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		int itype = JOptionPane.PLAIN_MESSAGE;
		if (strtype.equals("error")) itype = JOptionPane.ERROR_MESSAGE;
		else if (strtype.equals("warn")) itype = JOptionPane.WARNING_MESSAGE;
		else if (strtype.equals("plain")) itype = JOptionPane.PLAIN_MESSAGE;

		JOptionPane.showMessageDialog(null, strmsg, strtitle, itype);
	}
}
