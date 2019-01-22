package com.machao.xiaoaiview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * create by：mc on 2019/1/11 14:16
 * email:
 * 小艾View
 */
public class XiaoAiView extends View {

    private Context context;

    private int ScreenW;

    private float startX;
    private float startY;
    private float endX;
    private float endY;
    private AnimatorSet set;
    private int startSizeW;


    private final RectF roundRect = new RectF();
    private final Paint zonePaint = new Paint();
    private final Paint textPaint = new Paint();
    private Bitmap bitmap;
    private int bitmapW;
    private int bitmapH;
    private int baseLineY;
    private float currentX;
    private float currentY;
    private float currentW;
    private int radius;
    private int sizeH;//默认高度，高度默认保持不变
    private int endSizeW;//结束宽度，与高度相同，为正方形

    private int paddingSize = 15;//内容里边距

    private String text = "默认文本";

    private Bitmap bitmapDisplay = null;
    private Matrix matrix = null;
    private boolean transComplete = false;//移动完成
    private int during = 1000;
    private int margin;

    public XiaoAiView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public XiaoAiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public XiaoAiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;
        ScreenUtils.initScreen(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XiaoAiView, defStyleAttr, 0);
        int backgroundColor = a.getColor(R.styleable.XiaoAiView_backgroundColor, Color.GREEN);
        int icon = a.getResourceId(R.styleable.XiaoAiView_icon, -1);
        int textSize = a.getDimensionPixelSize(R.styleable.XiaoAiView_textSize, 40);
        text = a.getString(R.styleable.XiaoAiView_text);
        margin = a.getDimensionPixelOffset(R.styleable.XiaoAiView_margin, 100);
        paddingSize = a.getDimensionPixelSize(R.styleable.XiaoAiView_padding, 15);
        during = a.getInteger(R.styleable.XiaoAiView_during, 1000);
        a.recycle();

        int screenH = ScreenUtils.getScreenH();
        ScreenW = ScreenUtils.getScreenW();
        zonePaint.setAntiAlias(true);
        zonePaint.setColor(backgroundColor);
        zonePaint.setStyle(Paint.Style.FILL);
        bitmap = BitmapFactory.decodeResource(getResources(), icon);
        bitmapDisplay = bitmap;
        matrix = new Matrix();

        bitmapH = bitmap.getHeight();
        bitmapW = bitmap.getWidth();

        sizeH = (bitmapH > bitmapW ? bitmapH : bitmapW) + paddingSize * 2;

        endSizeW = sizeH;
        radius = sizeH / 2;

        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(2);
        textPaint.setTextSize(textSize);

        //=============默认设置==========//
        startSizeW = (int) (ScreenW * 0.8);
        startX = (float) ((ScreenW * 0.2) / 2);
        startY = (float) screenH / 2;
        endX = ScreenW - margin - sizeH;
        endY = ScreenUtils.getDrawHeight() - margin - sizeH;//屏幕高度 - 虚拟按键高度 - 边界 - 控件高度 - 状态栏高度
        //控件X,Y起始于状态栏之下，所以需要减去状态栏高度
        currentX = startX;
        currentY = startY;
        currentW = startSizeW;
        //=============默认设置==========//


        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
        baseLineY = (int) (sizeH / 2 - top / 2 - bottom / 2);
        System.out.println("总高度" + screenH + "：虚拟按键高度" + ScreenUtils.getVirtualH() + "：margin" + margin + "：控件高度" + sizeH + "：endy" + endY);
    }

    private float downX;
    private float downY;
    private boolean isTouch = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!transComplete) {
            if (listener!=null){
                listener.onClick();
            }
            return super.onTouchEvent(event);
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isTouch = false;
                    currentX = getX();
                    currentY = getY();
                    downX = event.getRawX();
                    downY = event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float offsetX = event.getRawX() - downX;
                    float offsetY = event.getRawY() - downY;
                    if (offsetX != 0 || offsetY != 0) {
                        isTouch = true;
                    }
                    moveView(currentX + offsetX, currentY + offsetY);
                    break;
                case MotionEvent.ACTION_UP:
                    currentX = getX();
                    currentY = getY();
                    startTranslateAnima();
                    if (isTouch) {
                        return true;
                    } else {//点击事件
                        if (listener!=null){
                            listener.onClick();
                        }
                        return super.onTouchEvent(event);
                    }

            }
            return super.onTouchEvent(event);
        }
    }

    /**
     * 边框附着动画
     */
    private void startTranslateAnima() {
        if (currentX > 0 && currentX < ScreenW - endSizeW) {//需要附着边框
            ValueAnimator animator = ValueAnimator.ofFloat(currentX, currentX < ScreenW / 2 ? 0 : ScreenW - endSizeW);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setX((Float) animation.getAnimatedValue());
                    setY(currentY);
                }
            });
            animator.setDuration(500);
            animator.start();
        }
    }

    private void moveView(float endX, float endy) {
        if (endX < 0) endX = 0;
        if (endX + endSizeW > ScreenW) endX = ScreenW - endSizeW;
        if (endy < 0) endy = 0;
        if (endy > endY + margin) endy = endY + margin;
        setX(endX);
        setY(endy);
    }

    /**
     * 开始动画
     */
    public void startAnimation() {
        set.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension((int) currentW, sizeH);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!transComplete) {
            setX(currentX);
            setY(currentY);
            roundRect.set(0, 0, currentW, sizeH);
            canvas.drawRoundRect(roundRect, radius, radius, zonePaint);
            canvas.drawText(text, startX + paddingSize, baseLineY, textPaint);
            canvas.drawCircle(currentW - (sizeH / 2), sizeH / 2, radius, zonePaint);
            canvas.drawBitmap(bitmapDisplay, currentW - sizeH + (sizeH - bitmapW) / 2, (sizeH - bitmapH) / 2, zonePaint);
        } else {
            canvas.drawCircle(currentW - (sizeH / 2), sizeH / 2, radius, zonePaint);
            canvas.drawBitmap(bitmapDisplay, currentW - sizeH + (sizeH - bitmapW) / 2, (sizeH - bitmapH) / 2, zonePaint);
        }
    }

    /**
     * 只有在view绘制完成过后才能获取x,y,left,right
     *
     * @param hasWindowFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        ValueAnimator animatorX = ValueAnimator.ofFloat(startX, endX);
        ValueAnimator animatorY = ValueAnimator.ofFloat(startY, endY);
        ValueAnimator animatorW = ValueAnimator.ofFloat(startSizeW, endSizeW);
        ValueAnimator animatorRotate = ValueAnimator.ofFloat(0f, 360f);
        animatorRotate.setDuration(5000);
        animatorRotate.setInterpolator(new LinearInterpolator());//线性渐变
        animatorRotate.setRepeatCount(ObjectAnimator.INFINITE);//无限循环
        animatorRotate.setRepeatMode(ObjectAnimator.RESTART);
        animatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                currentX = (float) valueAnimator.getAnimatedValue();
                requestLayout();
                invalidate();
            }
        });
        animatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                currentY = (Float) valueAnimator.getAnimatedValue();
            }
        });

        animatorW.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                currentW = (float) valueAnimator.getAnimatedValue();
            }
        });
        animatorRotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                matrix.setRotate((Float) valueAnimator.getAnimatedValue());
                bitmapDisplay = Bitmap.createBitmap(bitmap, 0, 0, bitmapW, bitmapH, matrix, true);
                invalidate();
            }
        });

        set = new AnimatorSet();
        set.playTogether(animatorX, animatorY, animatorW);
        set.setDuration(during);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                transComplete = true;
//                animatorRotate.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    /**
     * 点击事件
     */
    public interface onClickListener{
        void onClick();
    }

    private onClickListener listener;

    public void setOnClickListener(onClickListener listener){
        this.listener = listener;
    }
}
