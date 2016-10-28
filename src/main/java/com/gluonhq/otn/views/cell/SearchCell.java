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
package com.gluonhq.otn.views.cell;

import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.otn.model.Service;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.gluonhq.otn.util.OTNSearch.CELL_MAP;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javafx.collections.ObservableSet;
import javafx.css.PseudoClass;

public class SearchCell<T> extends CharmListCell<T> {

    private final List<String> DEFAULT;
    private final Service service;
    
    private PseudoClass oldPseudoClass;
    private static final List<String> COMMON_CELL_PSEUDO_CLASSES = Arrays.asList("odd", "even", "selected", "focused", "empty", "filled");

    private final Map<String, WeakReference<CharmListCell<T>>> cellsCache = 
            Collections.synchronizedMap(new HashMap<>());
    
    public SearchCell(Service service) {
        DEFAULT = getStyleClass();
        this.service = service;
    }
    
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty); 
        if (item != null && !empty) {
            CharmListCell<T> currentCell = getCell(item.getClass().getSimpleName());
            if (currentCell == null) {
                setText(null);
                setGraphic(null);
                setStyle(null);
                getStyleClass().setAll(DEFAULT);
                changePseudoClass(null);
            } else {
                currentCell.updateItem(item, empty);
                setText(currentCell.getText());
                setGraphic(currentCell.getGraphic());
                setStyle(currentCell.getStyle());
                getStyleClass().setAll(currentCell.getStyleClass());
                changePseudoClass(currentCell.getPseudoClassStates());
            }
        } else {
            setText(null);
            setGraphic(null);
            setStyle(null);
            getStyleClass().setAll(DEFAULT);
            changePseudoClass(null);
        }
    }
    
    private void changePseudoClass(ObservableSet<PseudoClass> pseudoClassStates) {
        pseudoClassStateChanged(oldPseudoClass, false);
        if (pseudoClassStates != null) {
            for (PseudoClass pseudoClass : pseudoClassStates) {
                if (!COMMON_CELL_PSEUDO_CLASSES.contains(pseudoClass.getPseudoClassName())) {
                    pseudoClassStateChanged(pseudoClass, true);
                    oldPseudoClass = pseudoClass;
                    break;
                }
            }
        }
    }
    
    private void registerCell(String name) {
        try {
            WeakReference<CharmListCell<T>> cells = cellsCache.get(name);
            if (cells == null) {
                Class<?> clazz = CELL_MAP.get(name);
                CharmListCell<T> charmListCell = null;
                try {
                    charmListCell = (CharmListCell<T>) clazz.getConstructor(Service.class).newInstance(service);
                } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) { }
                
                if (charmListCell == null) {
                    charmListCell = (CharmListCell<T>) clazz.newInstance();
                }
                
                cells = new WeakReference(charmListCell);
                cellsCache.put(name, cells);
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(SearchCell.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private CharmListCell<T> getCell(String name) {
        if (cellsCache.get(name) == null || cellsCache.get(name).get() == null) {
            cellsCache.remove(name);
            registerCell(name);
        }
        return cellsCache.get(name).get();
    }
    
}
