package com.almostreliable.energymeter.client.screen.widget.base;

public interface ClickedOutsideListener {

    int getX();
    int getY();
    int getWidth();
    int getHeight();
    void onClickedOutside();
    default void receiveClickOutside(double mouseX, double mouseY) {
        if (mouseX < getX() || mouseY < getY() || mouseX > getX() + getWidth() || mouseY > getY() + getHeight()) {
            onClickedOutside();
        }
    }
}
