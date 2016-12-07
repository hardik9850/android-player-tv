package com.cube.lush.player.details;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.PlaylistListener;
import com.brightcove.player.event.EventEmitterImpl;
import com.brightcove.player.model.Playlist;
import com.brightcove.player.model.Video;
import com.cube.lush.player.playback.PlaybackActivity;
import com.cube.lush.player.playback.PlaybackMethod;
import com.cube.lush.player.R;
import com.cube.lush.player.browse.BasicMainFragmentAdapter;
import com.cube.lush.player.handler.ResponseHandler;
import com.cube.lush.player.manager.MediaManager;
import com.cube.lush.player.model.MediaContent;

import java.util.List;

/**
 * Displays details and a preview of the current live Lush playlist.
 *
 * @author Jamie Cruwys
 * @project lush-player-android-client
 */
public class LiveMediaDetailsFragment extends BaseMediaDetailsFragment implements BrowseFragment.MainFragmentAdapterProvider
{
	private BrowseFragment.MainFragmentAdapter<LiveMediaDetailsFragment> mainFragmentAdapter;
	private Catalog catalog;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		catalog = new Catalog(new EventEmitterImpl(),
		                      getResources().getString(R.string.brightcove_account_id),
		                      getResources().getString(R.string.brightcove_policy_key));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		startEndTime.setVisibility(View.GONE); // Nothing in the Brightcove metadata to support this
		timeRemaining.setVisibility(View.GONE); // Nothing in the Brightcove metadata to support this
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		fetchPlaylist();
	}

	private void fetchPlaylist()
	{
		MediaManager.getInstance().getLiveContent(new ResponseHandler<MediaContent>()
		{
			@Override
			public void onSuccess(@NonNull List<MediaContent> items)
			{
				if (!items.isEmpty())
				{
					setPlaylistId(items.get(0).getId());
				}
			}

			@Override
			public void onFailure(@Nullable Throwable t)
			{
				populateError();
			}
		});
	}

	public void populateError()
	{
		populateError(new Runnable()
		{
			@Override
			public void run()
			{
				fetchPlaylist();
			}
		});
	}

	private void setPlaylistId(final String playlistId)
	{
		catalog.findPlaylistByID(playlistId, new PlaylistListener()
		{
			@Override
			public void onPlaylist(Playlist playlist)
			{
				if (!playlist.getVideos().isEmpty())
				{
					Video video = playlist.getVideos().get(0);
					MediaContent liveMediaContent = new MediaContent();
					liveMediaContent.setId(playlistId);

					String name = video.getStringProperty("name");
					if (TextUtils.isEmpty(name))
					{
						liveMediaContent.setTitle("Live");
					}
					else
					{
						liveMediaContent.setTitle(String.format("LIVE: %s", name));
					}

					String thumbnail = video.getStringProperty("thumbnail");
					if (!TextUtils.isEmpty(thumbnail))
					{
						liveMediaContent.setThumbnail(thumbnail);
					}

					populateContentView(liveMediaContent);
				}
				else
				{
					populateError();
				}
			}

			@Override
			public void onError(String error)
			{
				super.onError(error);
				populateError();
			}
		});
	}

	@Override
	public void populateContentView(@NonNull MediaContent item)
	{
		super.populateContentView(item);

		// This method is designed to be called from async methods so make sure we've not lost context since then
		if (getActivity() == null)
		{
			return;
		}

		Drawable circleDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.circle);
		liveIndicator.setImageDrawable(circleDrawable);

		int circleColour = ContextCompat.getColor(getActivity(), R.color.material_red);
		liveIndicator.getDrawable().setColorFilter(circleColour, PorterDuff.Mode.MULTIPLY);

		liveIndicator.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.pulse));
	}

	@Override
	public void playButtonClicked(View view)
	{
		if (getActivity() == null)
		{
			return;
		}

		Context context = getActivity();
		String id = null;

		if (mediaContent != null)
		{
			id = mediaContent.getId();
		}

		Intent intent = PlaybackActivity.getIntent(context, PlaybackMethod.PLAYLIST, id, mediaContent.getThumbnail());
		getActivity().startActivity(intent);
	}

	@Override
	public BrowseFragment.MainFragmentAdapter<LiveMediaDetailsFragment> getMainFragmentAdapter()
	{
		if (mainFragmentAdapter == null)
		{
			mainFragmentAdapter = new BasicMainFragmentAdapter<>(this);
		}
		return mainFragmentAdapter;
	}
}