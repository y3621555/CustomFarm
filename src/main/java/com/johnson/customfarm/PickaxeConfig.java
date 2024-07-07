package com.johnson.customfarm;

public class PickaxeConfig {
    private final int duration;
    private final int requiredSkill;

    public PickaxeConfig(int duration, int requiredSkill) {
        this.duration = duration;
        this.requiredSkill = requiredSkill;
    }

    public int getDuration() {
        return duration;
    }

    public int getRequiredSkill() {
        return requiredSkill;
    }
}
