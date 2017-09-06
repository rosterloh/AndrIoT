package com.rosterloh.thingsclient.ui.interact;

import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.rosterloh.thingsclient.R;
import com.rosterloh.thingsclient.nearby.ConnectionsClient;
import com.rosterloh.thingsclient.ui.SingleFragmentActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class InteractFragmentTest {

    @Rule
    public ActivityTestRule<SingleFragmentActivity> activityRule =
            new ActivityTestRule<>(SingleFragmentActivity.class, true, true);

    private MutableLiveData<Location> location = new MutableLiveData<>();
    private InteractFragment interactFragment;
    private InteractViewModel viewModel;

    @Before
    public void init() {
        interactFragment = new InteractFragment();
        viewModel = mock(InteractViewModel.class);
        ConnectionsClient client = mock(ConnectionsClient.class);
        FusedLocationProviderClient locationProvider = mock(FusedLocationProviderClient.class);

        when(viewModel.getLocation()).thenReturn(location);

        interactFragment.mViewModelFactory = new InteractViewModelFactory(client, locationProvider);

        activityRule.getActivity().setFragment(interactFragment);
    }

    @Test
    public void testLocationClick() {
        onView(withId(R.id.btn_location)).perform(click());
        verify(viewModel).setLocation();
    }
}
