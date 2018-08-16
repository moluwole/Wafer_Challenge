package com.example.oluwole.wafer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public abstract class RecyclerSwipeHelper extends ItemTouchHelper.SimpleCallback {

    public static final int THRESHOLD = 300;
    private RecyclerView recyclerView;
    private List<Deletebutton> buttons;
    private GestureDetector gestureDetector;
    private int swipedPos = -1;
    private float swipeThreshold = 0.5f;
    private Map<Integer, List<Deletebutton>> buttonsBuffer;
    private Queue<Integer> recoverQueue;
    private RecyclerItemTouchHelperListener listener;
    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            for (Deletebutton button : buttons) {
                if (button.onClick(e.getX(), e.getY()))
                    break;
            }

            return true;
        }
    };
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent e) {
            if (swipedPos < 0) return false;
            Point point = new Point((int) e.getRawX(), (int) e.getRawY());

            RecyclerView.ViewHolder swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos);
            View swipedItem = swipedViewHolder.itemView;
            Rect rect = new Rect();
            swipedItem.getGlobalVisibleRect(rect);

            if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_MOVE) {
                if (rect.top < point.y && rect.bottom > point.y)
                    gestureDetector.onTouchEvent(e);
                else {
                    recoverQueue.add(swipedPos);
                    swipedPos = -1;
                    recoverSwipedItem();
                }
            }
            return false;
        }
    };

    RecyclerSwipeHelper(Context context, RecyclerView recyclerView, RecyclerItemTouchHelperListener listener) {
        super(0, ItemTouchHelper.LEFT);
        this.listener = listener;
        this.recyclerView = recyclerView;
        this.buttons = new ArrayList<>();
        this.gestureDetector = new GestureDetector(context, gestureListener);
        this.recyclerView.setOnTouchListener(onTouchListener);
        buttonsBuffer = new HashMap<>();
        recoverQueue = new LinkedList<Integer>() {
            @Override
            public boolean add(Integer o) {
                if (contains(o))
                    return false;
                else
                    return super.add(o);
            }
        };

        attachSwipe();
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();

        if (swipedPos != pos)
            recoverQueue.add(swipedPos);

        swipedPos = pos;

        if (buttonsBuffer.containsKey(swipedPos))
            buttons = buttonsBuffer.get(swipedPos);
        else
            buttons.clear();

        buttonsBuffer.clear();
        swipeThreshold = 0.5f * buttons.size() * THRESHOLD;

        if (swipeThreshold < -300)
            listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
        else
            recoverSwipedItem();
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return swipeThreshold;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 0.1f * defaultValue;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int pos = viewHolder.getAdapterPosition();
        float translationX = dX;
        View itemView = viewHolder.itemView;

        if (pos < 0) {
            swipedPos = pos;
            return;
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                List<Deletebutton> buffer = new ArrayList<>();

                if (!buttonsBuffer.containsKey(pos)) {
                    instantiateDeletebutton(viewHolder, buffer);
                    buttonsBuffer.put(pos, buffer);
                } else {
                    buffer = buttonsBuffer.get(pos);
                }

                translationX = dX * buffer.size() * THRESHOLD / itemView.getWidth();
                drawButtons(c, itemView, buffer, pos, translationX);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }

    private synchronized void recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            int pos = recoverQueue.poll();
            if (pos > -1) {
                Objects.requireNonNull(recyclerView.getAdapter()).notifyItemChanged(pos);
            }
        }
    }

    private void drawButtons(Canvas c, View itemView, List<Deletebutton> buffer, int pos, float dX) {
        float right = itemView.getRight();
        float dButtonWidth = (-1) * dX / buffer.size();

        for (Deletebutton button : buffer) {
            float left = right - dButtonWidth;
            button.onDraw(c, new RectF(left, itemView.getTop(), right, itemView.getBottom()), pos);
            right = left;
        }
    }

    private void attachSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public abstract void instantiateDeletebutton(RecyclerView.ViewHolder viewHolder, List<Deletebutton> Deletebuttons);

    public interface RecyclerItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }

    public interface DeletebuttonClickListener {
        void onClick(int pos);
    }

    public static class Deletebutton {
        private int pos;
        private RectF clickRegion;
        private DeletebuttonClickListener clickListener;
        private Context context;

        Deletebutton(Context context, DeletebuttonClickListener clickListener) {
            this.context = context;
            this.clickListener = clickListener;
        }

        public boolean onClick(float x, float y) {
            if (clickRegion != null && clickRegion.contains(x, y)) {
                clickListener.onClick(pos);
                return true;
            }

            return false;
        }

        void onDraw(Canvas c, RectF rect, int pos) {

            Paint p = new Paint();

            p.setColor(Color.parseColor("#800080"));
            c.drawRect(rect, p);

            Bitmap image_delete = BitmapFactory.decodeResource(context.getResources(), R.drawable.delete);

            c.drawBitmap(image_delete, rect.left + 100, rect.top + 60, p);

            clickRegion = rect;
            this.pos = pos;
        }
    }
}