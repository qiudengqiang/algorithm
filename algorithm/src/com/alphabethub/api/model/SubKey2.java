package com.alphabethub.api.model;

public class SubKey2 extends Key {
    public SubKey2(int value) {
        super(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || (obj.getClass() != SubKey1.class && obj.getClass() != SubKey2.class)) return false;
        Key key = (Key) obj;
        return value == key.value;
    }
}
