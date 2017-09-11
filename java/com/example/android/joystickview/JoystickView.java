package com.example.android.joystickview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View implements Runnable {
    public static final int CIRCLE = 0;
    public static final int RECTANGLE = 1;

    private OnJoystickMoveListener onJoystickMoveListener; // Listener
    private Thread thread = new Thread(this);

    private int borderShape;

    private long loopInterval = 100;

    private float buttonRadius;
    private float circleRadius;
    private float centerX;      // Center view x position
    private float centerY;      // Center view y position
    private float xPosition;    // Touch x position
    private float yPosition;    // Touch y position

    private double radian = 57.2957795; // 1 radian = 57.2957795 degree

    private Paint button;
    private Paint innerCircle;
    private Paint outerCircle;
    private Paint lineX;
    private Paint lineY;
    private Paint rectangle;

    public JoystickView(Context context) {
        super(context);
        initializeView();
    }
    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView();
    }
    public JoystickView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        initializeView();
    }

    protected void initializeView() {
        button = new Paint(Paint.ANTI_ALIAS_FLAG);
        button.setColor(Color.BLACK);
        button.setStyle(Paint.Style.FILL);

        innerCircle = new Paint();
        innerCircle.setColor(Color.WHITE);
        innerCircle.setStyle(Paint.Style.FILL);

        outerCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerCircle.setColor(Color.GRAY);
        outerCircle.setStyle(Paint.Style.FILL);

        lineX = new Paint();
        lineX.setStrokeWidth(2);
        lineX.setColor(Color.BLACK);

        lineY = new Paint();
        lineY.setStrokeWidth(2);
        lineY.setColor(Color.BLACK);

        rectangle = new Paint();
        rectangle.setColor(Color.BLACK);
        rectangle.setStyle(Paint.Style.STROKE);
        rectangle.setStrokeWidth(4);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        int d = Math.min(xNew, yNew);

        xPosition = getWidth() / 2;
        yPosition = getWidth() / 2;
        buttonRadius = (int) (d / 8.0 * 1.2);
        circleRadius = (int) (d / 8.0 * 3.0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int d = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));

        setMeasuredDimension(d, d);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;

        // rectangle
        canvas.drawRect((int) (centerX - circleRadius), (int) (centerY - circleRadius), (int) (centerX + circleRadius), (int) (centerY + circleRadius), rectangle);

        // circle
        canvas.drawCircle((int) centerX, (int) centerY, (int) circleRadius, outerCircle);
        canvas.drawCircle((int) centerX, (int) centerY, (int) circleRadius * 2 / 3, innerCircle);

        // line
        canvas.drawLine(centerX - circleRadius, centerY, centerX + circleRadius, centerY, lineX);
        canvas.drawLine(centerX, centerY - circleRadius, centerX, centerY + circleRadius, lineY);

        // button
        canvas.drawCircle(xPosition, yPosition, (int)buttonRadius, button);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        xPosition = event.getX();
        yPosition = event.getY();

        if(borderShape == CIRCLE) {
            double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX) + (yPosition - centerY) * (yPosition - centerY));

            if (abs > circleRadius) {
                xPosition = (float) ((xPosition - centerX) * circleRadius / abs + centerX);
                yPosition = (float) ((yPosition - centerY) * circleRadius / abs + centerY);
            }
        } else if (borderShape == RECTANGLE) {
            double abs[] = {Math.abs(xPosition - centerX), Math.abs(yPosition - centerY)};

            if (abs[0] > circleRadius) {
                xPosition = (float) ((xPosition - centerX) * circleRadius / abs[0] + centerX);
            }
            if (abs[1] > circleRadius) {
                yPosition = (float) ((yPosition - centerY) * circleRadius / abs[1] + centerY);
            }
        }
        invalidate();

        if (event.getAction() == MotionEvent.ACTION_UP) {
            xPosition = centerX;
            yPosition = centerY;
            thread.interrupt();

            if (onJoystickMoveListener != null)  onJoystickMoveListener.onValueChanged((xPosition - centerX) / circleRadius, (centerY - yPosition) / circleRadius);
        }
        if (onJoystickMoveListener != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            thread = new Thread(this);
            thread.start();

            if (onJoystickMoveListener != null) onJoystickMoveListener.onValueChanged((xPosition - centerX) / circleRadius, (centerY - yPosition) / circleRadius);
        }
        return true;
    }

    public double getAngle(float xPosition, float yPosition) {
        xPosition = xPosition * circleRadius + centerX;
        yPosition = -yPosition * circleRadius + centerY;

        if (xPosition > centerX) {
            if (yPosition < centerY) {
                return Math.atan((yPosition - centerY) / (xPosition - centerX)) * radian + 90;
            } else if (yPosition > centerY) {
                return Math.atan((yPosition - centerY) / (xPosition - centerX)) * radian + 90;
            } else {
                return 90;
            }
        } else if (xPosition < centerX) {
            if (yPosition < centerY) {
                return Math.atan((yPosition - centerY) / (xPosition - centerX)) * radian + 270;
            } else if (yPosition > centerY) {
                return Math.atan((yPosition - centerY) / (xPosition - centerX)) * radian + 270;
            } else {
                return 270;
            }
        }
        return 0;
    }

    public double getPower(float xPosition, float yPosition) {
        xPosition = xPosition * circleRadius + centerX;
        yPosition = -yPosition * circleRadius + centerY;

        return 100 * Math.sqrt((xPosition - centerX) * (xPosition - centerX) + (yPosition - centerY) * (yPosition - centerY)) / circleRadius;
    }

    public interface OnJoystickMoveListener {
        void onValueChanged(float xPosition, float yPosition);
    }

    public void setOnJoystickMoveListener(OnJoystickMoveListener listener, int borderShape) {
        this.onJoystickMoveListener = listener;
        this.borderShape = borderShape;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            post(new Runnable() {
                public void run() {
                    if (onJoystickMoveListener != null) onJoystickMoveListener.onValueChanged((xPosition - centerX) / circleRadius, (centerY - yPosition) / circleRadius);
                }
            });
            try {
                Thread.sleep(loopInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}