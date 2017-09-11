package com.example.android.joystickview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{
    private JoystickView joystickView;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text_view);

        joystickView = (JoystickView) findViewById(R.id.joystick_view);
        joystickView.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {

            @Override
            public void onValueChanged(float xPosition, float yPosition) {
                textView.setText("\tx: " + xPosition + "\n" +
                                 "\ty: " + yPosition + "\n" +
                                 "\tangle: " + String.valueOf(joystickView.getAngle(xPosition, yPosition)) + "°\n" +
                                 "\tpower: " + String.valueOf(joystickView.getPower(xPosition, yPosition)) + "%");
            }
        }, JoystickView.CIRCLE);
        // JoystickView.RECTANGLE
    }
}
