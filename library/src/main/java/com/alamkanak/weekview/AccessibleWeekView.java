package com.alamkanak.weekview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AccessibleWeekView extends WeekView {


    private AccessibleWeekViewTouchHelper touchHelper;

    public AccessibleWeekView(Context context) {
        super(context);
        init();
    }

    public AccessibleWeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AccessibleWeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        touchHelper = new AccessibleWeekViewTouchHelper();
        ViewCompat.setAccessibilityDelegate(this, touchHelper);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        return touchHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return touchHelper.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }
    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        touchHelper.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        touchHelper.invalidateRoot();
    }

    private class AccessibleWeekViewTouchHelper extends ExploreByTouchHelper {

        Map<Integer,WeakReference<EventRect>> index = new HashMap<>();
        AccessibleWeekViewTouchHelper() {
            super(AccessibleWeekView.this);
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            for (EventRect er : AccessibleWeekView.this.getEvents()) {
                if (er.rectF != null && er.rectF.contains(x,y)) {
                    return er.hashCode();
                }
            }
            return HOST_ID;
        }

        @Override
        protected void getVisibleVirtualViews(final List<Integer> virtualViewIds) {
            for( EventRect eventRect : AccessibleWeekView.this.getEvents()) {
                if (eventRect.rectF!= null)
                    virtualViewIds.add(eventRect.hashCode());
            }
        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfoCompat node) {

            EventRect eventRect = getEventRect(virtualViewId);
            if (  eventRect == null || eventRect.rectF == null) {
                node.setContentDescription("invalid event");
                node.setBoundsInParent(new Rect(0,0,0,0));
                return;
            }
            node.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK);
            node.setContentDescription(eventRect.event.getName());
            node.setBoundsInParent(new Rect(
                java.lang.Math.round(eventRect.rectF.left),
                java.lang.Math.round(eventRect.rectF.top),
                java.lang.Math.round(eventRect.rectF.right),
                java.lang.Math.round(eventRect.rectF.bottom)));
        }

        @Nullable
        private EventRect getEventRect(int virtualViewId) {
            final WeakReference<EventRect> eventRectWeakReference = index.get(virtualViewId);
            if (eventRectWeakReference != null && eventRectWeakReference.get() != null) {
                return eventRectWeakReference.get();
            }
            for (EventRect eventRect : AccessibleWeekView.this.getEvents()) {
                if (virtualViewId == eventRect.hashCode()) {
                    index.put(virtualViewId, new WeakReference<>(eventRect));
                    return eventRect;
                }
            }
            return null;
        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            return false;
        }
    }
}
