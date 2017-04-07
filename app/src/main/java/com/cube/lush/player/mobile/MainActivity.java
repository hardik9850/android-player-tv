package com.cube.lush.player.mobile;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.cube.lush.player.mobile.base.BaseMobileActivity;
import com.cube.lush.player.mobile.channels.ChannelsFragment;
import com.cube.lush.player.mobile.events.EventsFragment;
import com.cube.lush.player.mobile.home.HomeFragment;
import com.cube.lush.player.mobile.live.LiveFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.cube.lush.player.R;
import com.cube.lush.player.mobile.search.SearchFragment;

import java.util.ArrayList;

public class MainActivity extends BaseMobileActivity implements AHBottomNavigation.OnTabSelectedListener
{
    @BindView(R.id.bottom_navigation)
	AHBottomNavigation bottomNavigation;

    @BindView(R.id.container)
    FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_activity_main);
        ButterKnife.bind(this);

		setupNavigation();
    }

    private void setupNavigation()
	{
		ArrayList<AHBottomNavigationItem> items = new ArrayList<>();
		items.add(new AHBottomNavigationItem(R.string.title_home, R.drawable.ic_home, android.R.color.black));
		items.add(new AHBottomNavigationItem(R.string.title_live, R.drawable.ic_live, android.R.color.black));
		items.add(new AHBottomNavigationItem(R.string.title_channels, R.drawable.ic_channels, android.R.color.black));
		items.add(new AHBottomNavigationItem(R.string.title_events, R.drawable.ic_events, android.R.color.black));
		items.add(new AHBottomNavigationItem(R.string.title_search, R.drawable.ic_search, android.R.color.black));

		for (AHBottomNavigationItem item : items)
		{
			bottomNavigation.addItem(item);
		}

		// Colour for selected item
		bottomNavigation.setAccentColor(Color.BLACK);

		// Colour for unselected items
		bottomNavigation.setInactiveColor(ContextCompat.getColor(this, R.color.dark_grey));

		// Background colour
		bottomNavigation.setDefaultBackgroundColor(Color.WHITE);

		// Forces titles to show, which the standard bottom bar does not support
		bottomNavigation.setForceTitlesDisplay(true);

		// Tab selection
		bottomNavigation.setOnTabSelectedListener(this);

		// Auto selected home
		bottomNavigation.setCurrentItem(0);
	}

	@Override public boolean onTabSelected(int position, boolean wasSelected)
	{
		switch (position)
		{
			case 0:
				showNoHistoryFragment(HomeFragment.newInstance());
				return true;
			case 1:
				showNoHistoryFragment(LiveFragment.newInstance());
				return true;
			case 2:
				showNoHistoryFragment(ChannelsFragment.newInstance());
				return true;
			case 3:
				showNoHistoryFragment(EventsFragment.newInstance());
				return true;
			case 4:
				showNoHistoryFragment(SearchFragment.newInstance());
				return true;
			default:
				throw new RuntimeException("Unknown tab selected");
		}
	}

	public void showFragment(@NonNull Fragment fragment)
	{
		showFragment(fragment, true);
	}

	public void showNoHistoryFragment(@NonNull Fragment fragment)
	{
		showFragment(fragment, false);
	}

	private void showFragment(@NonNull Fragment fragment, boolean preserveHistory)
	{
		FragmentManager fragmentManager = getSupportFragmentManager();

		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(container.getId(), fragment);

		if (preserveHistory)
		{
			transaction.addToBackStack(null);
		}

		transaction.commit();
	}

	@Override public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (getSupportFragmentManager().getBackStackEntryCount() == 0)
			{
				finish();
				return false;
			}

			getSupportFragmentManager().popBackStack();
			return false;
		}

		return super.onKeyDown(keyCode, event);
	}
}