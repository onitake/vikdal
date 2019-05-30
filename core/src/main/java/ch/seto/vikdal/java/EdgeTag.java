package ch.seto.vikdal.java;

public enum EdgeTag {
	DEFAULT(""),
	CATCH("catch"),
	CATCHALL("catchall"),
	TRYCATCH("trycatch"),
	GOTO("goto"),
	ENTRY("entry"),
	CASE("case"),
	;
	
	private String description;
	
	EdgeTag(String desc) {
		description = desc;
	}
	
	@Override
	public String toString() {
		return description;
	}
	
}
