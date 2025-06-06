package com.deaddropgames.stuntmountain.sim;

public class BodyType {

    public static final short BodyTypeNone = 0;
    public static final short BodyTypeBiped = 1;
    public static final short BodyTypeTerrain = 2;
    public static final short BodyTypeSki = 3;
    public static final short BodyTypeTree = 4;

    private short type;
    private short id;

    public BodyType(short type) {

        this.type = type;
        id = 0;
    }

    public BodyType(short type, short id) {

        this.type = type;
        this.id = id;
    }

    public short getBodyType() {

        return type;
    }

    public short getId() {

        return id;
    }
}
