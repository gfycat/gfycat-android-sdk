/*
 * Copyright (c) 2015-present, Gfycat, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gfycat.common.utils;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.gfycat.common.Function;
import com.gfycat.common.R;
import com.gfycat.common.ToggleImageButton;

/**
 * Created by dekalo on 12.10.16.
 */

public class UIUtils {

    public static final long DEFAULT_ANIMATION_DURATION = 400;
    public static final long SNACKBAR_LONG_DURATION_MS = 3000;


    private static Object SHOW_ANIMATION_STARTED = new Object();
    private static Object HIDE_ANIMATION_STARTED = new Object();

    public static float dpToPx(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }


    /**
     * Get height of navigation bar.
     * For portrait only.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getSoftButtonsBarHeight(Activity activity) {
        return getSoftButtonsBarHeight(activity.getWindowManager());
    }

    /**
     * Get height of navigation bar only if API >= 21.
     * For portrait only.
     */
    public static int getSoftButtonsBarHeightIfTranslucent(Activity activity) {
        if (VersionUtils.isAtLeastLollipop())
            return getSoftButtonsBarHeight(activity.getWindowManager());
        else
            return 0;
    }

    /**
     * Get height of navigation bar.
     * For portrait only.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getSoftButtonsBarHeight(WindowManager windowManager) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public static <T> T findView(View root, int id) {
        if (root == null) return null;
        try {
            return (T) root.findViewById(id);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T findView(Activity root, int id) {
        if (root == null) return null;
        try {
            return (T) root.findViewById(id);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public static void showKeyboardForced(EditText editText) {
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
        imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
    }

    public static void hideKeyboardForced(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * For landscape only.
     */
    public static int getSoftButtonsBarWidth(WindowManager windowManager) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            int usableWidth = metrics.widthPixels;
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
            int realWidth = metrics.widthPixels;
            if (realWidth > usableWidth)
                return realWidth - usableWidth;
            else
                return 0;
        }
        return 0;
    }

    /**
     * Get width of navigation bar
     */
    public static int getSoftButtonsBarWidth(Activity activity) {
        return getSoftButtonsBarWidth(activity.getWindowManager());
    }

    /**
     * Get width of navigation bar only if API >= 21
     */
    public static int getSoftButtonsBarWidthIfTranslucent(Activity activity) {
        if (VersionUtils.isAtLeastLollipop())
            return getSoftButtonsBarWidth(activity.getWindowManager());
        else
            return 0;
    }

    /**
     * From http://blog.raffaeu.com/archive/2015/04/11/android-and-the-transparent-status-bar.aspx
     */
    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * @param resources of Activity
     * @return height of status bar.
     */
    public static int getStatusBarHeightAccordingToVersion(Resources resources) {
        if (VersionUtils.isAtLeastLollipop()) {
            return getStatusBarHeight(resources);
        }
        return 0;
    }

    /**
     * @return height of action bar if can find it in current theme.
     */
    public static int getActionBarSize(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    public static void updateViewVisibility(final View view, boolean show, boolean animate) {
        if (show) {
            showView(view, animate);
        } else {
            hideView(view, animate);
        }
    }

    public static void showView(final View view, boolean animate) {
        showView(view, animate, Function::ignore);
    }

    public static void showView(final View view, boolean animate, Runnable endAction) {

        if (!animate) {
            view.animate().cancel();
            view.setVisibility(View.VISIBLE);
            endAction.run();
        } else {
            if (view.getVisibility() == View.VISIBLE && view.getAlpha() == 1.0f && view.getTag(R.id.ANIMATION_TAG_KEY) == null)
                return;
            view.setTag(R.id.ANIMATION_TAG_KEY, SHOW_ANIMATION_STARTED);
            view.animate().cancel();
            view.animate().alpha(1.0f)
                    .setListener(new SimpleAnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            if (view.getVisibility() != View.VISIBLE) {
                                view.setAlpha(0f);
                                view.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (isCanceled()) return;
                            super.onAnimationEnd(animation);
                            view.setVisibility(View.VISIBLE);
                            view.setTag(R.id.ANIMATION_TAG_KEY, null);
                            endAction.run();
                        }
                    }).start();
        }
    }

    public static void hideView(final View view, boolean animate) {
        hideViewWithAction(view, animate, Function::ignore);
    }

    public static void hideViewWithAction(final View view, boolean animate, Runnable action) {
        if (!animate) {
            view.animate().cancel();
            view.setVisibility(View.GONE);
            Sugar.doIfNotNull(action, Runnable::run);
        } else {
            if (view.getVisibility() == View.GONE && view.getTag(R.id.ANIMATION_TAG_KEY) == null)
                return;

            view.setTag(R.id.ANIMATION_TAG_KEY, HIDE_ANIMATION_STARTED);
            view.animate().cancel();
            view.animate().alpha(0f)
                    .setListener(new SimpleAnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (isCanceled()) return;
                            super.onAnimationEnd(animation);
                            view.setVisibility(View.GONE);
                            view.setTag(R.id.ANIMATION_TAG_KEY, null);
                            Sugar.doIfNotNull(action, Runnable::run);
                        }
                    }).start();
        }
    }

    public static void updateViewAlphaAndVisibility(View view, float alpha) {
        view.setAlpha(alpha);
        if (alpha == 0.0f) {
            view.setVisibility(View.INVISIBLE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void boopToggleButton(ToggleImageButton button, boolean checked, float enlargeScale) {
        button.setChecked(!checked);
        button.setPivotX(button.getWidth() / 2);
        button.setPivotY(button.getHeight() / 2);
        button.animate().scaleX(enlargeScale).scaleY(enlargeScale).setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    button.setChecked(checked);
                    button.animate().scaleX(1.0f).scaleY(1.0f).setInterpolator(new AccelerateInterpolator());
                });
    }

    public static void flipToggleButton(ToggleImageButton button, boolean checked) {
        button.setChecked(!checked);
        button.setPivotX(button.getWidth() / 2);
        button.animate().scaleX(0.0f)
                .withEndAction(() -> {
                    button.setChecked(checked);
                    button.animate().scaleX(1.0f);
                });
    }

    public static void addRightMargin(View view, int rightMargin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin + rightMargin, lp.bottomMargin);
        view.setLayoutParams(lp);
    }

    public static void addBottomMargin(View view, int bottomMargin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin + bottomMargin);
        view.setLayoutParams(lp);
    }

    public static void addBottomPadding(View view, int bottomPadding) {
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), bottomPadding);
    }

    public static boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getScreenHeightIfTranslucent(Context context) {
        if (VersionUtils.isAtLeastLollipop())
            return getScreenHeight(context);
        else
            return getScreenHeight(context) - getStatusBarHeight(context.getResources());
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static void removeBackgroundIfAble(View parent, @IdRes int target) {
        View v = parent.findViewById(target);
        if (v != null)
            v.setBackgroundColor(Color.TRANSPARENT);
    }

    public static boolean isLastRow(int itemsCount, int index, int columnCount) {
        int lastRow = (int) Math.floor(itemsCount / (float) columnCount);
        if (itemsCount % columnCount == 0) lastRow--;
        int firstItemInLastRow = columnCount * lastRow;
        return index >= firstItemInLastRow;
    }

    public static void setDrawableTint(Drawable drawable, int tintColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(DrawableCompat.wrap(drawable), tintColor);
        } else {
            drawable.mutate().setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
        }
    }

    public static void setDrawableTint(Context context,
                                       Drawable drawable, @ColorRes int tintColorResId) {
        setDrawableTint(drawable, ContextCompat.getColor(context, tintColorResId));
    }

    public static abstract class SimpleAnimatorListener implements Animator.AnimatorListener {
        private boolean canceled = false;

        @Override
        public void onAnimationStart(Animator animation) {
            canceled = false;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            canceled = true;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        public boolean isCanceled() {
            return canceled;
        }
    }
}
