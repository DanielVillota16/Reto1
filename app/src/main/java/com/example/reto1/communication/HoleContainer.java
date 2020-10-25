package com.example.reto1.communication;

import com.example.reto1.model.Hole;

public class HoleContainer {

    private Hole hole;

    public HoleContainer() {
    }

    public HoleContainer(Hole hole) {
        this.hole = hole;
    }

    public Hole getHole() {
        return hole;
    }

    public void setHole(Hole hole) {
        this.hole = hole;
    }
}
