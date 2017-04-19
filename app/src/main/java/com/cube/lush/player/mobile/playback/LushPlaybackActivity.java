package com.cube.lush.player.mobile.playback;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.brightcove.player.analytics.Analytics;
import com.brightcove.player.appcompat.BrightcovePlayerActivity;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.media.DeliveryType;
import com.brightcove.player.mediacontroller.BrightcoveMediaController;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BaseVideoView;
import com.cube.lush.player.R;
import com.cube.lush.player.api.model.ContentType;
import com.cube.lush.player.api.model.MediaContent;
import com.cube.lush.player.api.model.Programme;
import com.cube.lush.player.content.handler.ResponseHandler;
import com.cube.lush.player.content.manager.MediaManager;

import java.util.List;

/**
 * Created by Jamie Cruwys of 3 SIDED CUBE on 18/04/2017.
 */
public class LushPlaybackActivity extends BrightcovePlayerActivity
{
	@SuppressWarnings("HardCodedStringLiteral")
	public static final String EXTRA_MEDIA_CONTENT = "media_content";

	@SuppressWarnings("HardCodedStringLiteral")
	public static final String EXTRA_START_TIME = "start_time";

	@NonNull private MediaContent mediaContent;
	@IntRange(from = 0) private int startTimeMilliseconds;

	public static Intent getIntent(@NonNull Context context, @NonNull MediaContent mediaContent, @IntRange(from = 0) int startTimeMilliseconds)
	{
		Intent intent = new Intent(context, LushPlaybackActivity.class);
		intent.putExtra(EXTRA_MEDIA_CONTENT, mediaContent);
		intent.putExtra(EXTRA_START_TIME, startTimeMilliseconds);
		return intent;
	}

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.playback_fragment);
		baseVideoView = (BaseVideoView) findViewById(R.id.brightcove_video_view);

		// Our custom media controller, which is in one line
		BrightcoveMediaController brightcoveMediaController = new BrightcoveMediaController(baseVideoView, R.layout.one_line_brightcove_media_controller);
		baseVideoView.setMediaController(brightcoveMediaController);

		super.onCreate(savedInstanceState);

		Analytics analytics = baseVideoView.getAnalytics();
		analytics.setAccount(com.cube.lush.player.content.BuildConfig.BRIGHTCOVE_ACCOUNT_ID);

		mediaContent = (MediaContent)getIntent().getSerializableExtra(EXTRA_MEDIA_CONTENT);
		startTimeMilliseconds = getIntent().getIntExtra(EXTRA_START_TIME, 0);
	}

	@Override protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		startTimeMilliseconds = savedInstanceState.getInt(EXTRA_START_TIME);
	}

	@Override protected void onResume()
	{
		super.onResume();
		playMediaContent(mediaContent, startTimeMilliseconds);
	}

	private void playMediaContent(@NonNull MediaContent mediaContent, @IntRange(from = 0) int startTimeMilliseconds)
	{
		if (mediaContent.getType() == ContentType.TV)
		{
			playTVContent(mediaContent, startTimeMilliseconds);
		}
		else if (mediaContent.getType() == ContentType.RADIO)
		{
			playRadioContent(mediaContent, startTimeMilliseconds);
		}
	}

	private void playTVContent(@NonNull MediaContent tvContent, @IntRange(from = 0) final int startTimeMilliseconds)
	{
		// TV Content from Brightcove
		MediaManager.getInstance().getCatalog().findVideoByID(tvContent.getId(), new VideoListener()
		{
			@Override
			public void onVideo(Video video)
			{
				playVideo(video, startTimeMilliseconds);
			}

			@Override public void onError(String error)
			{
				super.onError(error);
			}
		});
	}

	private void playRadioContent(@NonNull MediaContent radioContent, @IntRange(from = 0) final int startTimeMilliseconds)
	{
		// Radio Content from mp3 files, shown as videos in the brightcove player
		MediaManager.getInstance().getProgramme(radioContent.getId(), new ResponseHandler<Programme>()
		{
			@Override public void onSuccess(@NonNull List<Programme> items)
			{
				if (items.isEmpty())
				{
					return;
				}

				Programme programme = items.get(0);

				if (programme == null)
				{
					return;
				}

				Video video = Video.createVideo(programme.getUrl(), DeliveryType.MP4);
				playVideo(video, startTimeMilliseconds);
			}

			@Override public void onFailure(@Nullable Throwable t)
			{

			}
		});
	}

	private void playVideo(@NonNull Video video, @IntRange(from = 0) int startTimeMilliseconds)
	{
		baseVideoView.add(video);
		baseVideoView.start();

		if (startTimeMilliseconds > 0)
		{
			baseVideoView.seekTo(startTimeMilliseconds);
		}

		baseVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override public void onCompletion(MediaPlayer mp)
			{
				finish();
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState)
	{
		savedInstanceState.putInt(EXTRA_START_TIME, baseVideoView.getCurrentPosition());
		super.onSaveInstanceState(savedInstanceState);
	}
}