package com.github.inzan123;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;

import java.util.ArrayList;

public interface LongArrayComponent extends ComponentV3 {
    ArrayList<Long> getValue();
    void setValue(ArrayList<Long> positions);
    void addValue(long blockPos);
    void removeValue(long blockPos);

}

