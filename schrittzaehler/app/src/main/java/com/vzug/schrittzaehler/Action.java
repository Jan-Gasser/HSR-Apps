package com.vzug.schrittzaehler;

import java.io.Serializable;

/**
 * Created by jan-gasser on 14.11.16.
 */
public class Action implements Serializable {
    private int schritte;
    private String direction;

    public int getSchritte() {
        return schritte;
    }

    public void setSchritte(int schritte) {
        this.schritte = schritte;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
