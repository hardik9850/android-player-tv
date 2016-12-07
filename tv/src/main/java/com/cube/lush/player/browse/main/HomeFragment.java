package com.cube.lush.player.browse.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.widget.ArrayObjectAdapter;

import com.cube.lush.player.browse.BaseMediaBrowseFragment;
import com.cube.lush.player.browse.MediaPresenter;
import com.cube.lush.player.handler.ResponseHandler;
import com.cube.lush.player.manager.MediaManager;
import com.cube.lush.player.model.MediaContent;
import com.cube.lush.player.util.MediaSorter;

import java.util.List;

/**
 * Fragment shown on the launch page of the app when the "Home" menu item is selected.
 *
 * Created by tim on 30/11/2016.
 */
public class HomeFragment extends BaseMediaBrowseFragment
{
	private ArrayObjectAdapter mediaAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mediaAdapter = new ArrayObjectAdapter(new MediaPresenter());
		setAdapter(mediaAdapter);
	}

	@Override
	protected void fetchData()
	{
		MediaManager.getInstance().getMedia(new ResponseHandler<MediaContent>()
		{
			@Override public void onSuccess(@NonNull List<MediaContent> items)
			{
				setLoadingFinished(false);
				items = MediaSorter.MOST_RECENT_FIRST.sort(items);
				mediaAdapter.clear();
				mediaAdapter.addAll(0, items);
			}

			@Override public void onFailure(@Nullable Throwable t)
			{
				setLoadingFinished(true);
				mediaAdapter.clear();
			}
		});
	}
}