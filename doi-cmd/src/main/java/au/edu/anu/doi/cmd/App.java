package au.edu.anu.doi.cmd;

/**
 *
 * @author Rahul Khanna
 */
public class App {
	public static void main(String[] args) {
		try {
			new DoiCmd(args).run();
		} catch (DoiCmdException e) {
			e.printStackTrace();
		}
	}

}
