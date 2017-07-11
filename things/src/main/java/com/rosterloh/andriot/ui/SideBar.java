package com.rosterloh.andriot.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rosterloh.andriot.R;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * SideBar provides a vertical layout to display items.
 *
 * <p>Population of the items to display is
 * done through {@link Item} instances. You create items via {@link #newItem()}. From there you can
 * change the item's icon via {@link Item#setIcon(int)}. To display the item, you need to add it
 * to the layout via one of the {@link #addItem(Item)} methods. For example:
 * <pre>
 * SideBar sideBar = ...;
 * sideBar.addItem(sideBar.newItem().setIcon(R.drawable.icon1));
 * sideBar.addItem(sideBar.newItem().setIcon(R.drawable.icon2));
 * sideBar.addItem(sideBar.newItem().setIcon(R.drawable.icon3));
 * </pre>
 * You should set a listener via {@link #addOnItemSelectedListener(OnItemSelectedListener)} to be
 * notified when any item's selection state has been changed.
 **/
public class SideBar extends FrameLayout {

    static final int MOTION_NON_ADJACENT_OFFSET = 24;

    private static final int ANIMATION_DURATION = 300;

    private static final Pools.Pool<Item> sItemPool = new Pools.SynchronizedPool<>(16);

    /**
     * Callback interface invoked when a item's selection state changes.
     */
    public interface OnItemSelectedListener {

        /**
         * Called when an item enters the selected state.
         *
         * @param item The item that was selected
         */
        public void onItemSelected(Item item);

        /**
         * Called when an item exits the selected state.
         *
         * @param item The item that was unselected
         */
        public void onItemUnselected(Item item);

        /**
         * Called when an item that is already selected is chosen again by the user. Some applications
         * may use this action to return to the top level of a category.
         *
         * @param item The item that was reselected.
         */
        public void onItemReselected(Item item);
    }

    private final ArrayList<Item> mItems = new ArrayList<>();
    private Item mSelectedItem;

    private final ItemStrip mItemStrip;

    private final ArrayList<OnItemSelectedListener> mSelectedListeners = new ArrayList<>();

    // Pool we use as a simple RecyclerBin
    private final Pools.Pool<ItemView> mItemViewPool = new Pools.SimplePool<>(12);

    public SideBar(Context context) {
        this(context, null);
    }

    public SideBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Add the ItemStrip
        mItemStrip = new ItemStrip(context);
        super.addView(mItemStrip, 0, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        //mItemStrip.setSelectedIndicatorHeight(0);
        mItemStrip.setSelectedIndicatorColor(R.color.colorBackground);

        ViewCompat.setPaddingRelative(mItemStrip, 0, 0, 0, 0);
        mItemStrip.setGravity(Gravity.CENTER_VERTICAL);
        updateItemViews(true);
    }

    /**
     * Add an item to this layout. The item will be added at the end of the list.
     * If this is the first item to be added it will become the selected tab.
     *
     * @param item Item to add
     */
    public void addItem(@NonNull Item item) {
        addItem(item, mItems.isEmpty());
    }

    /**
     * Add an item to this layout. The item will be inserted at <code>position</code>.
     * If this is the first item to be added it will become the selected item.
     *
     * @param item Item to add
     * @param position The new position of the tab
     */
    public void addItem(@NonNull Item item, int position) {
        addItem(item, position, mItems.isEmpty());
    }

    /**
     * Add an item to this layout. The item will be added at the end of the list.
     *
     * @param item Item to add
     * @param setSelected True if the added tab should become the selected tab.
     */
    public void addItem(@NonNull Item item, boolean setSelected) {
        addItem(item, mItems.size(), setSelected);
    }

    /**
     * Add an item to this layout. The item will be inserted at <code>position</code>.
     *
     * @param item Item to add
     * @param position The new position of the tab
     * @param setSelected True if the added tab should become the selected tab.
     */
    public void addItem(@NonNull Item item, int position, boolean setSelected) {
        if (item.mParent != this) {
            throw new IllegalArgumentException("Item belongs to a different SideBarLayout.");
        }
        configureItem(item, position);
        addItemView(item);

        if (setSelected) {
            item.select();
        }
    }

    /**
     * Add a {@link SideBar.OnItemSelectedListener} that will be invoked when item selection
     * changes.
     *
     * <p>Components that add a listener should take care to remove it when finished via
     * {@link #removeOnItemSelectedListener(OnItemSelectedListener)}.</p>
     *
     * @param listener listener to add
     */
    public void addOnItemSelectedListener(@NonNull OnItemSelectedListener listener) {
        if (!mSelectedListeners.contains(listener)) {
            mSelectedListeners.add(listener);
        }
    }

    /**
     * Remove the given {@link SideBar.OnItemSelectedListener} that was previously added via
     * {@link #addOnItemSelectedListener(OnItemSelectedListener)}.
     *
     * @param listener listener to remove
     */
    public void removeOnItemSelectedListener(@NonNull OnItemSelectedListener listener) {
        mSelectedListeners.remove(listener);
    }

    /**
     * Remove all previously added {@link SideBar.OnItemSelectedListener}s.
     */
    public void clearOnItemSelectedListeners() {
        mSelectedListeners.clear();
    }

    /**
     * Create and return a new {@link Item}. You need to manually add this using
     * {@link #addItem(Item)} or a related method.
     *
     * @return A new Item
     * @see #addItem(Item)
     */
    @NonNull
    public Item newItem() {
        Item item = sItemPool.acquire();
        if (item == null) {
            item = new Item();
        }
        item.mParent = this;
        item.mView = createItemView(item);
        return item;
    }

    /**
     * Returns the number of items currently registered with the bar.
     *
     * @return Item count
     */
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Returns the item at the specified index.
     */
    @Nullable
    public Item getItemAt(int index) {
        return (index < 0 || index >= getItemCount()) ? null : mItems.get(index);
    }

    /**
     * Returns the position of the current selected item.
     *
     * @return selected item position, or {@code -1} if there isn't a selected item.
     */
    public int getSelectedItemPosition() {
        return mSelectedItem != null ? mSelectedItem.getPosition() : -1;
    }

    /**
     * Remove an item from the layout. If the removed item was selected it will be deselected
     * and another item will be selected if present.
     *
     * @param item The item to remove
     */
    public void removeItem(Item item) {
        if (item.mParent != this) {
            throw new IllegalArgumentException("Item does not belong to this SideBarLayout.");
        }
        removeItemAt(item.getPosition());
    }

    /**
     * Remove an item from the layout. If the removed item was selected it will be deselected
     * and another item will be selected if present.
     *
     * @param position Position of the item to remove
     */
    public void removeItemAt(int position) {
        final int selectedItemPosition = mSelectedItem != null ? mSelectedItem.getPosition() : 0;
        removeItemViewAt(position);

        final Item removedItem = mItems.remove(position);
        if (removedItem != null) {
            removedItem.reset();
            sItemPool.release(removedItem);
        }

        final int newItemCount = mItems.size();
        for (int i = position; i < newItemCount; i++) {
            mItems.get(i).setPosition(i);
        }

        if (selectedItemPosition == position) {
            selectItem(mItems.isEmpty() ? null : mItems.get(Math.max(0, position - 1)));
        }
    }

    /**
     * Remove all items from the bar and deselect the current item.
     */
    public void removeAllItems() {
        // Remove all the views
        for (int i = mItemStrip.getChildCount() - 1; i >= 0; i--) {
            removeItemViewAt(i);
        }

        for (final Iterator<Item> i = mItems.iterator(); i.hasNext();) {
            final Item item = i.next();
            i.remove();
            item.reset();
            sItemPool.release(item);
        }

        mSelectedItem = null;
    }

    private void updateAllItems() {
        for (int i = 0, z = mItems.size(); i < z; i++) {
            mItems.get(i).updateView();
        }
    }

    private ItemView createItemView(@NonNull final Item item) {
        ItemView itemView = mItemViewPool != null ? mItemViewPool.acquire() : null;
        if (itemView == null) {
            itemView = new ItemView(getContext());
        }
        itemView.setItem(item);
        itemView.setFocusable(true);
        return itemView;
    }

    private void configureItem(Item item, int position) {
        item.setPosition(position);
        mItems.add(position, item);

        final int count = mItems.size();
        for (int i = position + 1; i < count; i++) {
            mItems.get(i).setPosition(i);
        }
    }

    private void addItemView(Item item) {
        final ItemView itemView = item.mView;
        mItemStrip.addView(itemView, item.getPosition(), createLayoutParamsForItems());
    }

    private LinearLayout.LayoutParams createLayoutParamsForItems() {
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        updateItemViewLayoutParams(lp);
        return lp;
    }

    private void updateItemViewLayoutParams(LinearLayout.LayoutParams lp) {
        lp.height = 0;
        lp.weight = 1;
    }

    int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * dps);
    }

    private void removeItemViewAt(int position) {
        final ItemView view = (ItemView) mItemStrip.getChildAt(position);
        mItemStrip.removeViewAt(position);
        if (view != null) {
            view.reset();
            mItemViewPool.release(view);
        }
        requestLayout();
    }

    private void animateToItem(int newPosition) {
        if (newPosition == Item.INVALID_POSITION) {
            return;
        }

        // Now animate the indicator
        mItemStrip.animateIndicatorToPosition(newPosition, ANIMATION_DURATION);
    }

    private void setSelectedItemView(int position) {
        final int itemCount = mItemStrip.getChildCount();
        if (position < itemCount) {
            for (int i = 0; i < itemCount; i++) {
                final View child = mItemStrip.getChildAt(i);
                child.setSelected(i == position);
            }
        }
    }

    void selectItem(Item item) {
        selectItem(item, true);
    }

    void selectItem(final Item item, boolean updateIndicator) {
        final Item currentItem = mSelectedItem;

        if (currentItem == item) {
            if (currentItem != null) {
                dispatchItemReselected(item);
                animateToItem(item.getPosition());
            }
        } else {
            final int newPosition = item != null ? item.getPosition() : Item.INVALID_POSITION;
            if (updateIndicator) {
                animateToItem(newPosition);
                if (newPosition != Item.INVALID_POSITION) {
                    setSelectedItemView(newPosition);
                }
            }
            if (currentItem != null) {
                dispatchItemUnselected(currentItem);
            }
            mSelectedItem = item;
            if (item != null) {
                dispatchItemSelected(item);
            }
        }
    }

    private void dispatchItemSelected(@NonNull final Item item) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onItemSelected(item);
        }
    }

    private void dispatchItemUnselected(@NonNull final Item item) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onItemUnselected(item);
        }
    }

    private void dispatchItemReselected(@NonNull final Item item) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onItemReselected(item);
        }
    }

    void updateItemViews(final boolean requestLayout) {
        for (int i = 0; i < mItemStrip.getChildCount(); i++) {
            View child = mItemStrip.getChildAt(i);
            //child.setMinimumWidth(getItemMinWidth());
            updateItemViewLayoutParams((LinearLayout.LayoutParams) child.getLayoutParams());
            if (requestLayout) {
                child.requestLayout();
            }
        }
    }

    /**
     * A item in this layout. Instances can be created via {@link #newItem()}.
     */
    public static final class Item {

        /**
         * An invalid position for a tab.
         *
         * @see #getPosition()
         */
        public static final int INVALID_POSITION = -1;

        private Object mTag;
        private Drawable mIcon;
        private int mPosition = INVALID_POSITION;

        SideBar mParent;
        ItemView mView;

        Item() {
            // Private constructor
        }

        /**
         * @return This Item's tag object.
         */
        @Nullable
        public Object getTag() {
            return mTag;
        }

        /**
         * Give this Item an arbitrary object to hold for later use.
         *
         * @param tag Object to store
         * @return The current instance for call chaining
         */
        @NonNull
        public Item setTag(@Nullable Object tag) {
            mTag = tag;
            return this;
        }

        /**
         * Return the icon associated with this item.
         *
         * @return The item's icon
         */
        @Nullable
        public Drawable getIcon() {
            return mIcon;
        }

        /**
         * Return the current position of this item in the bar.
         *
         * @return Current position, or {@link #INVALID_POSITION} if this tab is not currently in
         * the bar.
         */
        public int getPosition() {
            return mPosition;
        }

        void setPosition(int position) {
            mPosition = position;
        }

        /**
         * Set the icon displayed on this item.
         *
         * @param icon The drawable to use as an icon
         * @return The current instance for call chaining
         */
        @NonNull
        public Item setIcon(@Nullable Drawable icon) {
            mIcon = icon;
            updateView();
            return this;
        }

        /**
         * Set the icon displayed on this item.
         *
         * @param resId A resource ID referring to the icon that should be displayed
         * @return The current instance for call chaining
         */
        @NonNull
        public Item setIcon(@DrawableRes int resId) {
            if (mParent == null) {
                throw new IllegalArgumentException("Item not attached to a SideBarLayout");
            }
            return setIcon(AppCompatResources.getDrawable(mParent.getContext(), resId));
        }

        /**
         * Select this item. Only valid if the item has been added to the bar.
         */
        public void select() {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            mParent.selectItem(this);
        }

        /**
         * Returns true if this item is currently selected.
         */
        public boolean isSelected() {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            return mParent.getSelectedItemPosition() == mPosition;
        }

        void updateView() {
            if (mView != null) {
                mView.update();
            }
        }

        void reset() {
            mParent = null;
            mView = null;
            mTag = null;
            mIcon = null;
            mPosition = INVALID_POSITION;
        }
    }

    class ItemView extends LinearLayout {
        private Item mItem;
        private ImageView mIconView;

        public ItemView(Context context) {
            super(context);
            ViewCompat.setPaddingRelative(this, 8, 0, 8, 0);
            setGravity(Gravity.CENTER);
            setOrientation(VERTICAL);
            setClickable(true);
        }

        @Override
        public boolean performClick() {
            final boolean handled = super.performClick();

            if (mItem != null) {
                mItem.select();
                return true;
            } else {
                return handled;
            }
        }

        @Override
        public void setSelected(boolean selected) {
            final boolean changed = isSelected() != selected;

            super.setSelected(selected);

            // Always dispatch this to the child views, regardless of whether the value has
            // changed
            if (mIconView != null) {
                mIconView.setSelected(selected);
            }
        }

        void setItem(@Nullable final Item item) {
            if (item != mItem) {
                mItem = item;
                update();
            }
        }

        void reset() {
            setItem(null);
            setSelected(false);
        }

        final void update() {
            final Item item = mItem;

            // If there isn't a custom view, we'll use our own in-built layouts
            if (mIconView == null) {
                ImageView iconView = (ImageView) LayoutInflater.from(getContext())
                        .inflate(R.layout.sidebar_icon, this, false);
                addView(iconView, 0);
                mIconView = iconView;
            }
            updateIcon(mIconView);

            // Finally update our selected state
            setSelected(item != null && item.isSelected());
        }

        private void updateIcon(@Nullable final ImageView iconView) {
            final Drawable icon = mItem != null ? mItem.getIcon() : null;

            if (iconView != null) {
                if (icon != null) {
                    iconView.setImageDrawable(icon);
                    iconView.setVisibility(VISIBLE);
                    setVisibility(VISIBLE);
                } else {
                    iconView.setVisibility(GONE);
                    iconView.setImageDrawable(null);
                }
            }

            if (iconView != null) {
                MarginLayoutParams lp = ((MarginLayoutParams) iconView.getLayoutParams());
                int bottomMargin = 0;
                if (bottomMargin != lp.bottomMargin) {
                    lp.bottomMargin = bottomMargin;
                    iconView.requestLayout();
                }
            }
        }

        public Item getItem() {
            return mItem;
        }
    }

    private class ItemStrip extends LinearLayout {

        private final Paint mSelectedIndicatorPaint;

        int mSelectedPosition = -1;
        float mSelectionOffset;

        private int mIndicatorTop = -1;
        private int mIndicatorBottom = -1;

        private ValueAnimator mIndicatorAnimator;

        ItemStrip(Context context) {
            super(context);
            setWillNotDraw(false);
            setOrientation(LinearLayout.VERTICAL);
            mSelectedIndicatorPaint = new Paint();
        }

        void setSelectedIndicatorColor(int color) {
            if (mSelectedIndicatorPaint.getColor() != color) {
                mSelectedIndicatorPaint.setColor(color);
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        float getIndicatorPosition() {
            return mSelectedPosition + mSelectionOffset;
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
                // If we're currently running an animation, lets cancel it and start a
                // new animation with the remaining duration
                mIndicatorAnimator.cancel();
                final long duration = mIndicatorAnimator.getDuration();
                animateIndicatorToPosition(mSelectedPosition,
                        Math.round((1f - mIndicatorAnimator.getAnimatedFraction()) * duration));
            } else {
                // If we've been layed out, update the indicator position
                updateIndicatorPosition();
            }
        }

        private void updateIndicatorPosition() {
            final View selectedTitle = getChildAt(mSelectedPosition);
            int top, bottom;
            if (selectedTitle != null && selectedTitle.getHeight() > 0) {
                top = selectedTitle.getTop();
                bottom = selectedTitle.getBottom();
                if (mSelectionOffset > 0f && mSelectedPosition < getChildCount() - 1) {
                    // Draw the selection partway between the tabs
                    View nextTitle = getChildAt(mSelectedPosition + 1);
                    top = (int) (mSelectionOffset * nextTitle.getTop() +
                            (1.0f - mSelectionOffset) * top);
                    bottom = (int) (mSelectionOffset * nextTitle.getBottom() +
                            (1.0f - mSelectionOffset) * bottom);
                }
            } else {
                top = bottom = -1;
            }
            setIndicatorPosition(top, bottom);
        }

        void setIndicatorPosition(int top, int bottom) {
            if (top != mIndicatorTop || bottom != mIndicatorBottom) {
                // If the indicator's top/bottom has changed, invalidate
                mIndicatorTop = top;
                mIndicatorBottom = bottom;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        void animateIndicatorToPosition(final int position, int duration) {
            if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
                mIndicatorAnimator.cancel();
            }

            final View targetView = getChildAt(position);
            if (targetView == null) {
                // If we don't have a view, just update the position now and return
                updateIndicatorPosition();
                return;
            }

            final int targetTop = targetView.getTop();
            final int targetBottom = targetView.getBottom();
            final int startTop;
            final int startBottom;

            if (Math.abs(position - mSelectedPosition) <= 1) {
                // If the views are adjacent, we'll animate from edge-to-edge
                startTop = mIndicatorTop;
                startBottom = mIndicatorBottom;
            } else {
                // Else, we'll just grow from the nearest edge
                final int offset = dpToPx(MOTION_NON_ADJACENT_OFFSET);
                if (position < mSelectedPosition) {
                    startTop = startBottom = targetBottom + offset;
                } else {
                    startTop = startBottom = targetTop - offset;
                }
            }

            if (startTop != targetTop || startBottom != targetBottom) {
                ValueAnimator animator = mIndicatorAnimator = new ValueAnimator();
                animator.setInterpolator(new FastOutLinearInInterpolator());
                animator.setDuration(duration);
                animator.setFloatValues(0, 1);
                animator.addUpdateListener((anim) -> {
                        final float fraction = anim.getAnimatedFraction();
                        setIndicatorPosition(
                                (int) (startTop + (fraction * (targetTop - startTop))),
                                (startBottom + Math.round(fraction * (targetBottom - startBottom))));
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSelectedPosition = position;
                        mSelectionOffset = 0f;
                    }
                });
                animator.start();
            }
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            // Thick colored underline below the current selection
            if (mIndicatorTop >= 0 && mIndicatorBottom > mIndicatorTop) {
                canvas.drawRoundRect(4, mIndicatorTop, getWidth() - 4,
                        mIndicatorBottom, 16, 16, mSelectedIndicatorPaint);
            }
        }
    }
}
