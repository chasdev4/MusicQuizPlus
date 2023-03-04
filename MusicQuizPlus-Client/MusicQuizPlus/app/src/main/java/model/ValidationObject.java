package model;

import java.io.Serializable;

import model.type.Severity;

// SUMMARY
// The ValidationObject model is used for the ValidationUtil

public class ValidationObject<T> implements Serializable {
    public final T object;
    public final Class cls;
    public final Severity severity;

    public ValidationObject(T object, Class cls, Severity severity) {
        this.object = object;
        this.cls = cls;
        this.severity = severity;
    }
}
