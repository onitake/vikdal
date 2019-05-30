package ch.seto.vikdal.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import ch.seto.vikdal.dex.Dex;
import ch.seto.vikdal.dex.DexFormatException;

public class DexArchiveTest {
	public static void main(String[] args) {
		new DexArchiveTest("/classes.dex");
	}
	private DexArchiveTest(String name) {
		try {
			Dex dex = new Dex(new File(getClass().getResource(name).toURI()));
			dex.parse();
			System.out.println(dex);
			System.out.println("String[" + dex.numberOfStrings() + "] {");
			for (int i = 0; i < dex.numberOfStrings(); i++) {
				System.out.println("\t'" + dex.lookupString(i) + "',");
			}
			System.out.println("}");
			System.out.println("Type[" + dex.numberOfTypes() + "] {");
			for (int i = 0; i < dex.numberOfTypes(); i++) {
				System.out.println("\t'" + dex.lookupType(i) + "',");
			}
			System.out.println("}");
			System.out.println("Field[" + dex.numberOfFields() + "] {");
			for (int i = 0; i < dex.numberOfFields(); i++) {
				System.out.println("\t" + dex.lookupField(i).toString(dex) + ",");
			}
			System.out.println("}");
			System.out.println("Method[" + dex.numberOfMethods() + "] {");
			for (int i = 0; i < dex.numberOfMethods(); i++) {
				System.out.println("\t" + dex.lookupMethod(i).toString(dex) + ",");
			}
			System.out.println("}");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e. printStackTrace();
		} catch (DexFormatException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
