package com.nolanlawson.supersaiyan;

/**
 * A set of reasoanble values for overlay width and text size for easier styling.
 * @author nolan
 *
 */
public enum OverlaySizeScheme {

    Small(R.dimen.ssjn__overlay_text_size_small, R.dimen.ssjn__overlay_width_small), 
    Normal(R.dimen.ssjn__overlay_text_size_normal, R.dimen.ssjn__overlay_width_normal), 
    Large(R.dimen.ssjn__overlay_text_size_large, R.dimen.ssjn__overlay_width_large), 
    XLarge(R.dimen.ssjn__overlay_text_size_xlarge, R.dimen.ssjn__overlay_width_xlarge),
    ;
    
    private int textSize;
    private int width;
    
    private OverlaySizeScheme(int textSize, int width) {
        this.textSize = textSize;
        this.width = width;
    }

    public int getTextSize() {
        return textSize;
    }

    public int getWidth() {
        return width;
    }
}