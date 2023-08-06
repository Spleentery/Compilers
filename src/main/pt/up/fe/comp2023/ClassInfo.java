package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.List;

public class ClassInfo {
    private final String name;
    private final String super_class;
    private final List<Symbol> fields;

    public ClassInfo(String name, String super_class, List<Symbol> fields) {
        this.name = name;
        this.super_class = super_class;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public String getSuper_class() {
        return super_class;
    }

    public List<Symbol> getFields() {
        return fields;
    }
}
