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
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

public class BadgeOutline {

    private final static int POINTS_CURVE = 10;
    private final static double MINIMUM_AREA = 3000;
    private final static double WIDTH_FACTOR = 0.0425; // (8.5 mm / 200 mm)

    public BadgeOutline() {
    }
    
    /**
     * Once we have drawn the path, we call this method to generate two paths 
     * (outer and inner paths) and get a SVGPath with them that can be exported
     * @param drawPath The original path
     * @param svg
     * @return the content string of the SVGPath with two paths
     */
    public boolean generateOutline(Path drawPath, SVGPath svg) {
        Pane pane = (Pane) drawPath.getParent();
        final double width = pane.getWidth() * WIDTH_FACTOR; 
        
        Path outterPath = new Path(drawPath.getElements());
        outterPath.setStroke(drawPath.getStroke());
        outterPath.setStrokeLineJoin(drawPath.getStrokeLineJoin());
        outterPath.setStrokeLineCap(drawPath.getStrokeLineCap());
        outterPath.setStrokeWidth(width);
        Path s1 = (Path) Shape.subtract(outterPath, new Rectangle(0, 0));

        Path innerPath = new Path(drawPath.getElements());
        innerPath.setStrokeWidth(0);
        innerPath.setStroke(drawPath.getStroke());
        innerPath.setStrokeLineJoin(drawPath.getStrokeLineJoin());
        innerPath.setStrokeLineCap(drawPath.getStrokeLineCap());
        Path s2 = (Path) Shape.subtract(innerPath, new Rectangle(0, 0));
        
        Path result = (Path) Shape.subtract(s1, s2);
        clearSmallPolygons(result);
        svg.setContent(pathsToSVGPath());
        return validPaths.size() == 2;
    }
    
    private List<PathElement> elements;
    private List<Path> validPaths;
    private List<Point2D> listPoints;
    
    private void clearSmallPolygons(Path... paths){
        validPaths = new ArrayList<>();
        Point2D p0 = Point2D.ZERO;
        for (Path path : paths) {
            for (PathElement elem : path.getElements()) {
                if (elem instanceof MoveTo) {
                    elements = new ArrayList<>();
                    elements.add(elem);
                    listPoints = new ArrayList<>();
                    p0 = new Point2D(((MoveTo)elem).getX(), ((MoveTo)elem).getY());
                    listPoints.add(p0);
                } else if (elem instanceof CubicCurveTo) {
                    elements.add(elem);
                    Point2D ini = listPoints.size() > 0 ? listPoints.get(listPoints.size() - 1) : p0;
                    listPoints.addAll(evalCubicCurve((CubicCurveTo) elem, ini, POINTS_CURVE));
                } else if (elem instanceof ClosePath) {
                    elements.add(elem);
                    listPoints.add(p0);
                    if (Math.abs(calculateArea()) > MINIMUM_AREA) {
                        validPaths.add(new Path(elements));
                    }
                } 
            }
        }
    }
    
    private String pathsToSVGPath() {
        final StringBuilder sb = new StringBuilder();
        for (Path path : validPaths) {
            for (PathElement element : path.getElements()) {
                if (element instanceof MoveTo) {
                    sb.append("M ").append(((MoveTo) element).getX()).append(" ")
                                   .append(((MoveTo) element).getY());
                } else if (element instanceof CubicCurveTo) {
                    CubicCurveTo curve = (CubicCurveTo) element;
                    sb.append(" C ")
                            .append(curve.getControlX1()).append(" ").append(curve.getControlY1()).append(" ")
                            .append(curve.getControlX2()).append(" ").append(curve.getControlY2()).append(" ")
                            .append(curve.getX()).append(" ").append(curve.getY());
                } else if (element instanceof ClosePath) {
                    sb.append(" Z ");
                }
            }
        }
        return sb.toString();
    }

    private List<Point2D> evalCubicCurve(CubicCurveTo c, Point2D ini, int size){
        List<Point2D> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double t = (double) i / (double) size;
            final double t3 = Math.pow(t, 3);
            final double t2 = Math.pow(t, 2);
            final double ct3 = Math.pow(1 - t, 3);
            final double ct2 = Math.pow(1 - t, 2);
            list.add(new Point2D(ct3 * ini.getX() + 3 * t * ct2 * c.getControlX1() +
                    3 * (1 - t) * t2 * c.getControlX2() + t3 * c.getX(),
                    ct3 * ini.getY() + 3 * t * ct2 * c.getControlY1() +
                    3 * (1 - t) * t2 * c.getControlY2() + t3 * c.getY()));
        }
        return list;
    }
    
    private Point2D getPoint(int i) {
        return listPoints.get(i >= listPoints.size() ? i - listPoints.size() : i);
    }
    
    private double calculateArea(){
        double a = 0d;
        for (int i = 0; i < listPoints.size(); i++) {
            a += getPoint(i).crossProduct(getPoint(i + 1)).getZ();
        } 
        return Math.abs(a/2d);
    }
}
