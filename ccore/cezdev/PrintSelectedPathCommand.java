//-------------------------------------------------------
// name: PrintSelectedPathCommand
// desc: Prints selected folder path from select onchange args.
//-------------------------------------------------------
public class PrintSelectedPathCommand {
	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			return;
		}

		// Select onchange passes: <controlId> <selectedKey> <selectedPath>.
		// Print the last arg so output is only the folder path.
		String selectedPath = args[args.length - 1];
		if (selectedPath == null) {
			selectedPath = "";
		}

		System.out.println(selectedPath);
	}
}
