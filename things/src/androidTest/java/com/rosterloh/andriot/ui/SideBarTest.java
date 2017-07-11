package com.rosterloh.andriot.ui;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rosterloh.andriot.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SideBarTest {
    @Rule
    public final ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    @Test
    public void testInflateSideBarWithItems() {
        SideBar sideBar = new SideBar(mActivityTestRule.getActivity());
        sideBar.addItem(sideBar.newItem().setIcon(R.drawable.ic_settings));
        sideBar.addItem(sideBar.newItem().setIcon(R.drawable.ic_timeline));
        sideBar.addItem(sideBar.newItem().setIcon(R.drawable.ic_home));
    }
}
