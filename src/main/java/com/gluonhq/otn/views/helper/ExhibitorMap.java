/**
 * Copyright (c) 2016, Gluon Software
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse
 *    or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.otn.views.helper;

import java.util.HashMap;
import java.util.Map;

public class ExhibitorMap {
    /**
     * Map of exhibitors, based on the rotated and updated image 
     * from https://www.oracle.com/us/assets/javaone-2016-exhibition-hall-2757131.pdf
     * "javaone-2016-exhibition-hall.jpg"
     * 
     * Each exhibitor has a name and a location: x1, y1, x2, y2 
     * referred in pixels from the top-left corner
     * 
     * Note: any change in the image will imply updating these coordinates accordingly
     */
    
    private static final Map<String, int[]> EXHIBITORS_MAP = new HashMap<>();

    static {
        EXHIBITORS_MAP.put("5000", new int[]{ 375,  334,  497,  457});
        EXHIBITORS_MAP.put("5002", new int[]{ 497,  334,  616,  457});
        EXHIBITORS_MAP.put("5004", new int[]{ 616,  334,  737,  457});
        EXHIBITORS_MAP.put("5006", new int[]{ 737,  334,  978,  457});
        EXHIBITORS_MAP.put("5010", new int[]{ 978,  334, 1098,  457});
        EXHIBITORS_MAP.put("5012", new int[]{1098,  334, 1460,  457});
        
        EXHIBITORS_MAP.put("5001", new int[]{ 375,  575,  617,  696});
        EXHIBITORS_MAP.put("5007", new int[]{ 737,  575,  857,  696});
        EXHIBITORS_MAP.put("5009", new int[]{ 857,  575, 1098,  696});
        EXHIBITORS_MAP.put("5013", new int[]{1098,  575, 1219,  696});
        EXHIBITORS_MAP.put("5015", new int[]{1219,  575, 1339,  696});
        EXHIBITORS_MAP.put("5017", new int[]{1339,  575, 1460,  696});
        
        EXHIBITORS_MAP.put("5102", new int[]{ 496,  696,  617,  818});
        EXHIBITORS_MAP.put("5106", new int[]{ 737,  696,  978,  818});
        EXHIBITORS_MAP.put("5110", new int[]{ 978,  696, 1219,  818});
        EXHIBITORS_MAP.put("5114", new int[]{1219,  696, 1340,  818});
        
        EXHIBITORS_MAP.put("5101", new int[]{ 375,  924,  617,  1167});
        EXHIBITORS_MAP.put("5107", new int[]{ 736,  924,  979, 1046});
        EXHIBITORS_MAP.put("5113", new int[]{1098,  924, 1219, 1046});
        EXHIBITORS_MAP.put("5115", new int[]{1219,  924, 1460, 1046});
        
        EXHIBITORS_MAP.put("5206", new int[]{ 736, 1046,  979, 1167});
        EXHIBITORS_MAP.put("5212", new int[]{1098, 1046, 1219, 1167});
        EXHIBITORS_MAP.put("5214", new int[]{1219, 1046, 1460, 1167});
        
        EXHIBITORS_MAP.put("5201", new int[]{ 375, 1273,  617, 1515});
        EXHIBITORS_MAP.put("5207", new int[]{ 736, 1273, 1342, 2213});
        EXHIBITORS_MAP.put("5301", new int[]{ 375, 1622,  617, 1864});
        EXHIBITORS_MAP.put("5401", new int[]{ 375, 1970,  617, 2213});
        
        EXHIBITORS_MAP.put("5501", new int[]{ 375, 2320,  617, 2442});
        EXHIBITORS_MAP.put("5507", new int[]{ 737, 2320,  978, 2442});
        EXHIBITORS_MAP.put("5511", new int[]{ 978, 2320, 1219, 2442});
        EXHIBITORS_MAP.put("5515", new int[]{1219, 2320, 1460, 2442});
        
        EXHIBITORS_MAP.put("5600", new int[]{ 375, 2442,  617, 2562});
        EXHIBITORS_MAP.put("5606", new int[]{ 737, 2442,  857, 2562});
        EXHIBITORS_MAP.put("5608", new int[]{ 857, 2442,  978, 2562});
        EXHIBITORS_MAP.put("5610", new int[]{ 978, 2442, 1098, 2562});
        EXHIBITORS_MAP.put("5612", new int[]{1098, 2442, 1219, 2562});
        EXHIBITORS_MAP.put("5614", new int[]{1219, 2442, 1460, 2562});
        
        EXHIBITORS_MAP.put("5601", new int[]{ 375, 2680,  617, 2801});
        EXHIBITORS_MAP.put("5607", new int[]{ 737, 2680,  978, 2801});
        EXHIBITORS_MAP.put("5611", new int[]{ 978, 2680, 1219, 2801});
        EXHIBITORS_MAP.put("5615", new int[]{1219, 2680, 1460, 2801});
        
        EXHIBITORS_MAP.put("5700", new int[]{ 375, 2801,  497, 2923});
        EXHIBITORS_MAP.put("5702", new int[]{ 497, 2801,  617, 2923});
        EXHIBITORS_MAP.put("5706", new int[]{ 737, 2801,  978, 2923});
        EXHIBITORS_MAP.put("5710", new int[]{ 978, 2801, 1098, 2923});
        EXHIBITORS_MAP.put("5712", new int[]{1098, 2801, 1339, 2923});
        EXHIBITORS_MAP.put("5716", new int[]{1339, 2801, 1460, 2923});
        
        EXHIBITORS_MAP.put("6001", new int[]{ 252, 3055,  284, 3089});
        EXHIBITORS_MAP.put("6002", new int[]{ 252, 3142,  284, 3175});
        EXHIBITORS_MAP.put("6003", new int[]{ 252, 3237,  284, 3271});
        
        EXHIBITORS_MAP.put("6004", new int[]{ 347, 3267,  381, 3299});
        EXHIBITORS_MAP.put("6005", new int[]{ 424, 3265,  458, 3297});
        EXHIBITORS_MAP.put("6006", new int[]{ 487, 3267,  521, 3299});
        EXHIBITORS_MAP.put("6007", new int[]{ 557, 3265,  591, 3297});
        EXHIBITORS_MAP.put("6008", new int[]{ 628, 3267,  662, 3299});
        EXHIBITORS_MAP.put("6009", new int[]{ 698, 3265,  732, 3297});
        EXHIBITORS_MAP.put("6010", new int[]{ 769, 3267,  803, 3299});
        
        EXHIBITORS_MAP.put("6011", new int[]{ 604, 3096,  639, 3128});
        EXHIBITORS_MAP.put("6012", new int[]{ 499, 3096,  533, 3128});
        EXHIBITORS_MAP.put("6013", new int[]{ 393, 3096,  427, 3128});
        
        EXHIBITORS_MAP.put("6014", new int[]{ 393, 3064,  427, 3096});
        EXHIBITORS_MAP.put("6015", new int[]{ 499, 3064,  533, 3096});
        EXHIBITORS_MAP.put("6016", new int[]{ 604, 3064,  639, 3096});
    }
    
    public static Map<String, int[]> getExhibitorMap() {
        return EXHIBITORS_MAP;
    }
    
}
