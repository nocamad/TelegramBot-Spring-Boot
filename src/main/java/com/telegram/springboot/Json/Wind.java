package com.telegram.springboot.Json;

public class Wind {

    public float speed;
    public float deg;



    public Wind() {

    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getDeg() {
        return deg;
    }

    public void setDeg(float deg) {
        this.deg = deg;
    }

    @Override
    public String toString() {
        return "Скорость ветра = " + speed +" м/с " ;
    }
}
