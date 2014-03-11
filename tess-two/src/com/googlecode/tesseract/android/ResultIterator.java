/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.tesseract.android;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.util.Pair;

import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel;

/**
 * Java interface for the ResultIterator. Does not implement all available JNI
 * methods, but does implement enough to be useful. Comments are adapted from
 * original Tesseract source.
 *
 * @author alanv@google.com (Alan Viverette)
 */
public class ResultIterator extends PageIterator {
    static {
        System.loadLibrary("lept");
        System.loadLibrary("tess");
    }

    /** Pointer to native result iterator. */
    private final int mNativeResultIterator;

    /* package */ResultIterator(int nativeResultIterator) {
        super(nativeResultIterator);

        mNativeResultIterator = nativeResultIterator;
        Log.i("ResultIterator", "the nativeResultIterator is " + mNativeResultIterator);

    }

    /**
     * Returns the text string for the current object at the given level.
     *
     * @param level the page iterator level. See {@link PageIteratorLevel}.
     * @return the text string for the current object at the given level.
     */
    public String getUTF8Text(int level) {
        return nativeGetUTF8Text(mNativeResultIterator, level);
    }

    /**
     * Returns the mean confidence of the current object at the given level. The
     * number should be interpreted as a percent probability (0-100).
     *
     * @param level the page iterator level. See {@link PageIteratorLevel}.
     * @return the mean confidence of the current object at the given level.
     */
    public float confidence(int level) {
        return nativeConfidence(mNativeResultIterator, level);
    }

    /**
     * Returns all possible matching text strings and their confidence level 
     * for the current object at the given level.
     * <p>
     * The default matching text is blank (""). 
     * The default confidence level is zero (0.0) 
     *
     * @param level the page iterator level. See {@link PageIteratorLevel}.
     * @return A list of pairs with the UTF string and the confidence
     */
    public List<Pair <String, Double>> getChoicesAndConfidence(int level){
    	
    	// get the native choices
    	String [] nativeChoices = nativeGetChoices(mNativeResultIterator, level, true);
    	
    	// create the output list
    	ArrayList <Pair <String, Double>> pairedResults = new ArrayList <Pair <String, Double>> ();
    	
    	for(int i = 0; i < nativeChoices.length; i++ ){
    		// The string and the confidence level are separated by a '|'
    		int separatorPosition = nativeChoices[i].lastIndexOf('|');
    		
    		// Create a pair with the choices
    		String utfString = "";
    		Double confidenceLevel = Double.valueOf(0);
    		if(separatorPosition > 0){
    			
    			// if the string contains a '|' separate the UTF string and the confidence level 
        		utfString = nativeChoices[i].substring(0, separatorPosition);
        		try{
        			confidenceLevel = Double.parseDouble(nativeChoices[i].substring(separatorPosition + 1));
        		} catch(NumberFormatException e) {
        			Log.e("ResultIterator","Invalid confidence level for " + nativeChoices[i]);
        		}
    		} else {
    			// if the string contains no '|' just save the full native result as the utfString
    			utfString = nativeChoices[i];
    		}
    		
    		// add the utf string to the results
    		pairedResults.add(new Pair <String, Double> (utfString,confidenceLevel));
    	}
    	
    	return pairedResults; 
    }
    
    /**
     * Returns all possible matching text strings for the current object at the given level.
     * <p>
     * The default matching text is blank ("").  
     *
     * @param level the page iterator level. See {@link PageIteratorLevel}.
     * @return A list of strings (contains matches ordered from most likely to least likely)
     */
    public List<String> getChoices(int level){
    	
    	// get the native choices
    	String [] nativeChoices = nativeGetChoices(mNativeResultIterator, level, false);
    	
    	// create the output list
    	ArrayList <String> stringResults = new ArrayList <String> ();
    	
		// add the utf string to the results
    	for(int i = 0; i < nativeChoices.length; i++ ){ stringResults.add(nativeChoices[i]); }
    	
    	return stringResults;
    }
    
    private static native String [] nativeGetChoices (int nativeResultIterator, int level, boolean addConfidence); 

    private static native String nativeGetUTF8Text(int nativeResultIterator, int level);
    private static native float nativeConfidence(int nativeResultIterator, int level);
}
