package csm117.remotecommand;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FixedTabViewer {
    private static ViewPager mViewPager;
    private static int mSelectedBarColor, mUnselectedBarColor;

    public static void init(Activity act, ViewPager vp, FragmentPagerAdapter fpa, int tabTextSize, float selectionBarHeight, float textLayoutHeight, int dividerWidth, int selectedBarColor, int unselectedBarColor) {
        mViewPager = vp;
        mSelectedBarColor = selectedBarColor;
        mUnselectedBarColor = unselectedBarColor;
        LinearLayout tabLayout = (LinearLayout) act.findViewById(R.id.tabs),
                selectionBarLayout = (LinearLayout) act.findViewById(R.id.tab_selection_bars);
        final int TAB_COUNT = fpa.getCount();

        //DP to PX conversion for selection bar height
        final int SELECTION_BAR_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, selectionBarHeight, act.getResources().getDisplayMetrics()),
                //SELECTION_MARGIN_WIDTH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics()),
                TEXT_LAYOUT_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textLayoutHeight, act.getResources().getDisplayMetrics()),
                DIVIDER_WIDTH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, act.getResources().getDisplayMetrics());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, TEXT_LAYOUT_HEIGHT),
                barlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SELECTION_BAR_HEIGHT),
                dividerlp = new LinearLayout.LayoutParams(DIVIDER_WIDTH, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        lp.gravity = Gravity.CENTER_VERTICAL;
        barlp.weight = 1;
        //barlp.setMargins(SELECTION_MARGIN_WIDTH, 0, SELECTION_MARGIN_WIDTH, 0);
        barlp.gravity = Gravity.CENTER_HORIZONTAL;

        final ImageView selectionBars[] = new ImageView[TAB_COUNT];

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {}
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_SETTLING || state == ViewPager.SCROLL_STATE_IDLE) {
                    changeSelectionBar(TAB_COUNT, selectionBars, mViewPager.getCurrentItem());
                }
            }
        });

        //Add tabs
        for (int i = 0; i < TAB_COUNT; i++) {
            final int ITEM = i;
            View.OnClickListener click = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(ITEM);
                    changeSelectionBar(TAB_COUNT, selectionBars, ITEM);
                }
            };

            //Add selection bar
            selectionBars[i] = new ImageView(act);
            if (i == 0) //initial selected tab
                selectionBars[i].setBackgroundColor(mSelectedBarColor);
            else
                selectionBars[i].setBackgroundColor(mUnselectedBarColor);
            selectionBars[i].setLayoutParams(barlp);
            selectionBars[i].setOnClickListener(click);
            selectionBars[i].setClickable(true);
            selectionBarLayout.addView(selectionBars[i]);

            //Add tabs
            TextView tabText = new TextView(act);
            tabText.setText(fpa.getPageTitle(i));
            tabText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, tabTextSize);
            tabText.setTypeface(tabText.getTypeface(), Typeface.BOLD);
            tabText.setGravity(Gravity.CENTER);
            tabText.setLayoutParams(lp);
            tabText.setClickable(true);
            tabText.setOnClickListener(click);
            tabLayout.addView(tabText);

            //Add tab divider
            if (i + 1 < TAB_COUNT) {
                ImageView divider = new ImageView(act);
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] { Color.TRANSPARENT, Color.parseColor("#33000000"), Color.TRANSPARENT });
                divider.setImageDrawable(gd);
                divider.setLayoutParams(dividerlp);
                tabLayout.addView(divider);
            }
        }
    }

    private static void changeSelectionBar(int tabCount, ImageView selectionBars[], int item) {
        for (int i = 0; i < tabCount; i++) {
            selectionBars[i].setBackgroundColor(mUnselectedBarColor);
        }
        selectionBars[item].setBackgroundColor(mSelectedBarColor);
    }
}
