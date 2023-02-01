package de.htw.ba.ue03.matching;

/**
 * Basis Klasse mit einigen getter/setter Funktionen
 * 
 * @author Nico
 *
 */
public abstract class TemplateMatcherBase implements TemplateMatcher {

	protected int[] templatePixel; 
	protected int templateWidth;
	protected int templateHeight;
	
	public TemplateMatcherBase(int[] templatePixel, int templateWidth, int templateHeight) {
		this.templatePixel = templatePixel;
		this.templateWidth = templateWidth;
		this.templateHeight = templateHeight;
	}
	
	@Override
	public int getTemplateWidth() {
		return templateWidth;
	}

	@Override
	public int getTemplateHeight() {
		return templateHeight;
	}

}
