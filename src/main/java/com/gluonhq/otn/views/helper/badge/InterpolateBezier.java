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
package com.gluonhq.otn.views.helper.badge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.geometry.Point2D;

/** Given N 2D points (knots), N-1 Bezier cubic curves will be generated, 
 *  passing through these knots, by adding 2(N-1) control points
 * 
 *  These control points will be found by solving a linear system, given by the 
 *  equations of continuity C1 and C2 in each knot.
 * 
 *  This system can be solved efficiently with this algorithm:
 *  http://www.cfd-online.com/Wiki/Tridiagonal_matrix_algorithm_-_TDMA_(Thomas_algorithm)
 */
public class InterpolateBezier {

    private final List<Point2D> knots;
    private final int numSplines;
    private final List<Bezier> splines;
    
    public InterpolateBezier(List<Point2D> knots) {
        // make sure it is closed
        if (!knots.get(0).equals(knots.get(knots.size() - 1))) {
            knots.add(knots.get(0));
        }
        
        this.knots = knots;
        numSplines = knots.size() - 1;
        
        splines = new ArrayList<>(numSplines);

        calculateControlPoints();
    }
    
    public List<Bezier> getSplines() { return splines; }
    
    private void calculateControlPoints() {

        for (int j = 0; j < numSplines; j++) {
            splines.add(new Bezier(knots.get(j), new Point2D(0, 0), new Point2D(0, 0), knots.get(j + 1)));
        }

        // transform A*x=d into (A'+vt*u)*x=d
        for (int i = 0; i < 2; i++) {
            // 1. A'·y=d
            double[] y1 = new double[numSplines];
            double[] ca = new double[numSplines];
            double[] cb = new double[numSplines];
            double[] cc = new double[numSplines];
            double[] cr = new double[numSplines];
            cb[0] = 8d; 
            cc[0] = 1d; 
            cr[0] = i == 0 ? 4d * knots.get(0).getX() + 2d * knots.get(1).getX() : 
                             4d * knots.get(0).getY() + 2d * knots.get(1).getY();
            for (int j = 1; j < numSplines - 1; j++) {
                ca[j] = 1d;
                cb[j] = 4d;
                cc[j] = 1d;
                cr[j] = i == 0 ? 4d * knots.get(j).getX() + 2d * knots.get(j + 1).getX() : 
                                 4d * knots.get(j).getY() + 2d * knots.get(j + 1).getY();
            }
            ca[numSplines-1] = 1d;
            cb[numSplines-1] = 17d / 4d;
            cr[numSplines-1] = i == 0 ? 4d * knots.get(numSplines - 1).getX() + 2d * knots.get(numSplines).getX() : 
                                        4d * knots.get(numSplines - 1).getY() + 2d * knots.get(numSplines).getY();

            for (int j = 1; j < numSplines; j++) {
                double m = ca[j] / cb[j - 1];
                cb[j] -= m * cc[j - 1];
                cr[j] -= m * cr[j - 1];
            }

            y1[numSplines - 1] = cr[numSplines - 1] / cb[numSplines - 1];
            for (int j = numSplines - 2; j >= 0; j--) {
                y1[j] = (cr[j] - cc[j] * y1[j + 1]) / cb[j];
            }

            // 2. A'·q=u
            double[] q1 = new double[numSplines];
            ca = new double[numSplines];
            cb = new double[numSplines];
            cc = new double[numSplines];
            cr = new double[numSplines];

            cb[0] = 8d; 
            cc[0] = 1d; 
            cr[0] = -4d;
            for (int j = 1; j < numSplines - 1; j++) {
                ca[j] = 1d;
                cb[j] = 4d;
                cc[j] = 1d;
                cr[j] = 0d;
            }
            ca[numSplines-1] = 1d;
            cb[numSplines-1] = 17d / 4d;
            cr[numSplines-1] = 1d;

            for (int j = 1; j < numSplines; j++) {
                double m = ca[j] / cb[j - 1];
                cb[j] -= m * cc[j - 1];
                cr[j] -= m * cr[j - 1];
            }

            q1[numSplines - 1] = cr[numSplines - 1] / cb[numSplines - 1];
            for (int j = numSplines - 2; j >= 0; j--) {
                q1[j] = (cr[j] - cc[j] * q1[j + 1]) / cb[j];
            }

            // 3. x
            double[] x1 = new double[numSplines];
            double[] x2 = new double[numSplines];
            // vt * y / (1 + vt * q)
            double f = (y1[0] - y1[numSplines - 1] / 4) / (1 + (q1[0] - q1[numSplines - 1]/4));

            for (int j = 0; j < numSplines; j++) {
                x1[j] = y1[j] - f * q1[j];
            }

            for (int j = 0; j < numSplines - 1; j++) {
                x2[j] = i == 0 ? 2d * knots.get(j + 1).getX() - x1[j + 1] :
                                 2d * knots.get(j + 1).getY() - x1[j + 1];
            }
            x2[numSplines - 1] = i == 0 ? 2d * knots.get(0).getX() - x1[0] :
                                          2d * knots.get(0).getY() - x1[0];
            // 4
            for (int j = 0; j < numSplines; j++) {
                Bezier bez = splines.get(j);
                if (i == 0){
                    bez.getPoints().set(1, new Point2D(x1[j], 0));
                    bez.getPoints().set(2, new Point2D(x2[j], 0));
                } else {
                    bez.getPoints().set(1, new Point2D(bez.getPoints().get(1).getX(), x1[j]));
                    bez.getPoints().set(2, new Point2D(bez.getPoints().get(2).getX(), x2[j]));
                } 
            }
        }
            
    }
    
    public class Bezier {
    
        private final List<Point2D> points;

        public Bezier(Point2D a, Point2D b, Point2D c, Point2D d) {
            points = Arrays.asList(a, b, c, d);
        }

        public List<Point2D> getPoints() { return points; }

        @Override
        public String toString() {
            return points.toString();
        }

    }
}
