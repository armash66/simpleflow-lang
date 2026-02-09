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

    public void push(Object value) {
        list.add(value);
    }

    public Object pop() {
        if (list.isEmpty()) return null;
        return list.remove(list.size() - 1);
    }

    public Object shift() {
        if (list.isEmpty()) return null;
        return list.remove(0);
    }

    public void unshift(Object value) {
        list.add(0, value);
    }

    public boolean has(Object key) {
        if (key instanceof Integer i) {
            return i > 0 && i <= list.size();
        }
        return map.containsKey(key);
    }

    public Cell keys() {
        return new Cell(new ArrayList<>(map.keySet()));
    }

    public Cell values() {
        return new Cell(new ArrayList<>(map.values()));
    }

    public Cell slice(int start, int end) {
        int s = Math.max(1, start);
        int e = Math.min(end, list.size());
        List<Object> out = new ArrayList<>();
        for (int i = s; i <= e; i++) {
            out.add(list.get(i - 1));
        }
        return new Cell(out);
    }

    public Cell merge(Cell other) {
        List<Object> mergedList = new ArrayList<>(this.list);
        mergedList.addAll(other.list);
        Cell merged = new Cell(mergedList);
        merged.map.putAll(this.map);
        merged.map.putAll(other.map);
        return merged;
    }

    public List<Object> listSnapshot() {
        return new ArrayList<>(list);
    }

    public Map<Object, Object> mapSnapshot() {
        return new HashMap<>(map);
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
