package softtest.repair.java;

public class ReadDefect{
	//DefectRepairTest rmf = new DefectRepairTest();
	String Defect;
	String Category;
	int Id;
	String FileName;
	String Variable;
	int StartLine;
	int IPLine;
	String IPLineCode;
	String Description;
	public ReadDefect(){
		
	}
	
	public ReadDefect(String defect, String category,int id,
			String fileName,String variable, int startLine,
			int iPLine,String iPLineCode,String description) {
		super();
		Defect = defect;
		Category = category;
		Id =id;
		FileName = fileName;
		Variable = variable;
		StartLine = startLine;
		IPLine = iPLine;
		IPLineCode = iPLineCode;
		Description = description;
	}
	public String getDefect() {
		return Defect;
	}
	public void setDefect(String defect) {
		Defect = defect;
	}
	public String getCategory() {
		return Category;
	}
	public void setCategory(String category) {
		Category = category;
	}
	public String getFileName() {
		return FileName;
	}
	public void setFileName(String fileName) {
		FileName = fileName;
	}
	public String getVariable() {
		return Variable;
	}
	public void setVariable(String variable) {
		Variable = variable;
	}
	public int getStartLine() {
		return StartLine;
	}
	public void setStartLine(int startLine) {
		StartLine = startLine;
	}
	public int getIPLine() {
		return IPLine;
	}
	public void setIPLine(int iPLine) {
		IPLine = iPLine;
	}
	public String getIPLineCode() {
		return IPLineCode;
	}
	public void setIPLineCode(String iPLineCode) {
		IPLineCode = iPLineCode;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public ReadDefect put(){
		ReadDefect mdb = new ReadDefect();
		return mdb;
				
		
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}


	
}
