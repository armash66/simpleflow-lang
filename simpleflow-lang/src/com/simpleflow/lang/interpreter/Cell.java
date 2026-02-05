package com.simpleflow.lang.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cell {
    private final List<Object> list = new ArrayList<>();
    private final Map<Object, Object> map = new HashMap<>();

    public Cell(List<Object> initial) {
        if (initial != null) {
            list.addAll(initial);
        }
    }

    public Object get(Object index) {
        if (index instanceof Integer i) {
            int idx = i;
            if (idx <= 0) {
                throw new RuntimeException("Cell index must be >= 1.");
            }
            if (idx <= list.size()) {
                return list.get(idx - 1);
            }
            return null;
        }

        return map.get(index);
    }

    public void set(Object index, Object value) {
        if (index instanceof Integer i) {
            int idx = i;
            if (idx <= 0) {
                throw new RuntimeException("Cell index must be >= 1.");
            }
            while (list.size() < idx - 1) {
                list.add(null);
            }
            if (idx - 1 < list.size()) {
                list.set(idx - 1, value);
            } else {
                list.add(value);
            }
            return;
        }

        map.put(index, value);
    }

    public int length() {
        return list.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@(");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            Object value = list.get(i);
            sb.append(value == null ? "null" : value.toString());
        }
        sb.append(")");
        return sb.toString();
    }
}
