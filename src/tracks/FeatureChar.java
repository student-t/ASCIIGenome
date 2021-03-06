package tracks;

import coloring.Config;
import coloring.ConfigKey;
import coloring.Xterm256;
import exceptions.InvalidColourException;
import htsjdk.variant.variantcontext.Genotype;

/**Class to model a *single* character printed on terminal representing 
 * an interval feature. 
 * */
class FeatureChar {

	private char text; 	        /** The ASCII char printed on screen */
	private String bgColor; 	/** The background colour to use */
	private String fgColor;
	private boolean invertFgBgColor= false;
	private boolean underline= false;
	
	/*  C O N S T R U C T O R  */
	
	protected FeatureChar(){
		this.fgColor= Config.get(ConfigKey.foreground);
		this.bgColor= Config.get(ConfigKey.background);
	}
	
	/*  M E T H O D S  */
	
	/** Return a self contained string ready to be printed as ANSI formatted text.
	 * @throws InvalidColourException 
	 * */
	protected String format(boolean noFormat) throws InvalidColourException{
		StringBuilder sb= new StringBuilder();
		if(noFormat){
			return sb.append(this.getText()).toString();
		}
		sb.append("\033[");
		if(this.invertFgBgColor){
			sb.append("7;");
		}
		if(this.isUnderline()){
			sb.append("4;");
		}
		sb.append("48;5;");
		sb.append(Xterm256.colorNameToXterm256(this.getBgColor()));
		sb.append(";38;5;");
		sb.append(Xterm256.colorNameToXterm256(this.getFgColor()));
		sb.append("m");
		sb.append(text);
		// Reset formatting
		sb.append("\033[0;48;5;");
		sb.append(Xterm256.colorNameToXterm256(Config.get(ConfigKey.background)));
		sb.append("m");
		return sb.toString();
	}
	
	/**Add format to this instance according to input and default configuration.
	 * */
	protected void addFormatGFF(char txt, char strand) {
		this.setText(txt);
		this.setFgColor(Config.get(ConfigKey.foreground));
		if(strand == '+') {
			this.setBgColor(Config.get(ConfigKey.feature_background_positive_strand));
		} else if(strand == '-') {
			this.setBgColor(Config.get(ConfigKey.feature_background_negative_strand));
		} else {
			this.setBgColor(Config.get(ConfigKey.feature_background_no_strand));
		}
	}
	
	protected void addFormatVCF(char textForVariant) {
		this.setText(textForVariant);
		if(textForVariant == 'A' || textForVariant == 'a'){
			this.setFgColor(Config.get(ConfigKey.seq_a));
		} else if(textForVariant == 'C' || textForVariant == 'c') {
			this.setFgColor(Config.get(ConfigKey.seq_c));
		} else if(textForVariant == 'G' || textForVariant == 'g') {
			this.setFgColor(Config.get(ConfigKey.seq_g));
		} else if(textForVariant == 'T' || textForVariant == 't') {
			this.setFgColor(Config.get(ConfigKey.seq_t));
		} else {
			this.setFgColor(Config.get(ConfigKey.seq_other));
		}
		this.setBgColor(Config.get(ConfigKey.background));
		this.setInvertFgBgColor(true);
	}
	
	public void addFormatGenotype(Genotype gt) {
		if(gt == null){
			this.setText(' ');
		}
		else if(gt.isHomRef()){
			this.setText('.');
			this.setBgColor(Config.get(ConfigKey.feature_background_no_strand));
		}
		else if(gt.isHomVar()){
			this.setText('O');
			this.setBgColor(Config.get(ConfigKey.feature_background_negative_strand));
		}
		else if(gt.isHet()){
			this.setText('E');
			this.setBgColor(Config.get(ConfigKey.feature_background_positive_strand));
		} 
		else {
			this.setText('?');
		}
	}

	
	@Override
	public String toString(){
		try {
			return this.format(true);
		} catch (InvalidColourException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*  S E T T E R S   A N D   G E T T E R S  */
	
	protected char getText() {
		return text;
	}
	
	protected void setText(char text) {
		this.text = text;
	}
	
	protected String getBgColor() {
		return this.bgColor;
	}
	
	protected void setBgColor(String bgColor) {
		if(bgColor == null){
			bgColor= Config.get(ConfigKey.background);
		}
		this.bgColor = bgColor;
	}
	
	protected String getFgColor() {
		return this.fgColor;
	}
	
	protected void setFgColor(String fgColor) {
		if(fgColor == null){
			fgColor= Config.get(ConfigKey.foreground);
		}
		this.fgColor = fgColor;
	}

	public boolean isUnderline() {
		return underline;
	}

	public void setUnderline(boolean underline) {
		this.underline = underline;
	}

	public void setInvertFgBgColor(boolean invert) {
		this.invertFgBgColor= invert;
	}

}
