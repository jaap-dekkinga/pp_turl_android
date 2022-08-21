package com.dekidea.tuneurl.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.asha.libresample2.Resample;
import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.TuneURLManager;
import com.dekidea.tuneurl.util.WakeLocker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import okhttp3.Cache;


public class TuneURLService extends Service implements Constants {

	private static final int NOTIFICATION_ID = 1521;
	private static final long TIMEOUT_US = 100000L;
	private static final float SIMILARITY_THRESHOLD = 0.5f;
	private static final int CHANNEL_BPS = 16;
	private static final int FINGERPRINT_BPS = 16;
	private static final int FINGERPRINT_SAMPLE_RATE = 10240;
	private static final int TRIGGER_SIZE_MILLIS = 1400;
	private static final int TUNE_URL_SIZE_MILLIS = 3500;
	private static final int FINGERPRINT_TRIGGER_BUFFER_SIZE = (int)(2 * (double)FINGERPRINT_SAMPLE_RATE * ((double)FINGERPRINT_BPS / 8d) * ((double)TRIGGER_SIZE_MILLIS / 1000d));

	private Context mContext;

	private ByteBuffer referenceTriggerByteBuffer = null;

	private ByteBuffer triggerByteBuffer = null;
	private ByteBuffer tuneUrlByteBuffer = null;

	private ByteBuffer resampledTriggerByteBuffer = null;
	private ByteBuffer resampledTuneUrlByteBuffer = null;
	private int tuneUrlWaveLenght;
	private float mSimilarity;


	private boolean recordTuneUrl = false;
	private boolean isPlaying = false;

	private int sampleRate;
	private int numChannels;

	private MediaExtractor mediaExtractor;
	private MediaCodec mediaCodec;

	private ListenerActionReceiver mListenerActionReceiver;
	private IntentFilter mListenerActionFilter;

	private ExecutorService mExecutorService;

	static {

		System.loadLibrary("native-lib");
	}
	

	@Override
	public void onCreate() {

		super.onCreate();

		mContext = this;

		initializeResources();
	}


	private void initializeResources(){

		System.out.println("initializeResources()");

		InputStream inputStream = null;
		ReadableByteChannel channel = null;

		try {

			mExecutorService = Executors.newFixedThreadPool(1);

			String reference_trigger_file_path = TuneURLManager.fetchStringSetting(this, SETTING_TRIGGER_FILE_PATH, "");

			inputStream = new FileInputStream(reference_trigger_file_path);

			referenceTriggerByteBuffer = ByteBuffer.allocateDirect(FINGERPRINT_TRIGGER_BUFFER_SIZE);
			referenceTriggerByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			channel = Channels.newChannel(inputStream);
			channel.read(referenceTriggerByteBuffer);

			resampledTriggerByteBuffer = ByteBuffer.allocateDirect(FINGERPRINT_TRIGGER_BUFFER_SIZE);
			resampledTriggerByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			mListenerActionReceiver = new ListenerActionReceiver();
			mListenerActionFilter = new IntentFilter(LISTENING_ACTION);

			registerReceiver(mListenerActionReceiver, mListenerActionFilter);

			mSimilarity = 0;
		}
		catch (Exception e){

			e.printStackTrace();
		}
		finally {

			if(channel != null){

				try{
					channel.close();
				}
				catch (Exception e){


				}
			}
			if(inputStream != null){

				try{
					inputStream.close();
				}
				catch (Exception e){


				}
			}
		}
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub

		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId){

		super.onStartCommand(intent, flags, startId);

		WakeLocker.acquirePartialWakeLock(this.getApplicationContext());

		startService();

		return Service.START_STICKY;
	}


	@Override
	public void onDestroy(){

		super.onDestroy();

		stopService();
	}


	private void startService() {

		runAsForeground();
		
		TuneURLManager.updateIntSetting(mContext, SETTING_RUNNING_STATE, SETTING_RUNNING_STATE_STARTED);
	}


	private void runAsForeground(){

		Intent i = new Intent();

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

		// Create the Foreground Service
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
		Notification notification = notificationBuilder
				.setContentIntent(pendingIntent)
				.setOngoing(true)
				.setSmallIcon(R.drawable.ic_launcher_small)
				.setContentText(getString(R.string.listening_service_label))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setCategory(NotificationCompat.CATEGORY_SERVICE)
				.build();

		startForeground(NOTIFICATION_ID, notification);
	}


	private String createNotificationChannel(NotificationManager notificationManager){
		String channelId = "tune_url_sound_listener_service";
		String channelName = "TuneURL Service";
		NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);

		channel.setImportance(NotificationManager.IMPORTANCE_NONE);
		channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		notificationManager.createNotificationChannel(channel);
		return channelId;
	}


	private void stopService() {

		releaseResources();

		TuneURLManager.updateIntSetting(mContext, SETTING_LISTENING_STATE, Constants.SETTING_LISTENING_STATE_STOPPED);

		WakeLocker.release();
	}


	class ListenerActionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent != null) {

				int action = intent.getIntExtra(TUNEURL_ACTION, -1);

				if(action == ACTION_START_SCANNING){

					String path = intent.getStringExtra("path");
					long positionUs = intent.getLongExtra("positionUs", 0);

					startListening(path, positionUs);
				}
				else if(action == ACTION_STOP_SCANNING){

					stopListening();
				}
			}
		}
	}


	private void startListening(final String path, final long positionUs){

		try {

			recordTuneUrl = false;

			isPlaying = true;

			mExecutorService.execute(new Runnable() {
				@RequiresApi(api = Build.VERSION_CODES.P)
				@Override
				public void run() {

					searchForTrigger(path, positionUs);
				}
			});
		}
		catch (Exception e){

			e.printStackTrace();
		}
	}


	public void initializeMediaExtractor(String path, long positionUs){

		if(path != null) {

			try {

				this.mediaExtractor = new MediaExtractor();

				this.mediaExtractor.setDataSource(path);

				boolean isAudioTrackFound = false;

				for (int i = 0; !isAudioTrackFound && i < this.mediaExtractor.getTrackCount(); i++) {

					MediaFormat format = this.mediaExtractor.getTrackFormat(i);
					String mime = format.getString(MediaFormat.KEY_MIME);

					if (mime.startsWith("audio/")) {

						isAudioTrackFound = true;

						try {

							sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
						} catch (Exception e) {

							e.printStackTrace();
						}

						try {

							numChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
						} catch (Exception e) {

							e.printStackTrace();
						}

						System.out.println("numChannels =" + numChannels);

						initializeBuffers(sampleRate, numChannels);

						this.mediaExtractor.selectTrack(i);

						if (this.mediaCodec == null) {

							this.mediaCodec = MediaCodec.createDecoderByType(mime);
							this.mediaCodec.configure(format, null, null, 0);

							this.mediaCodec.start();
						}
					}
				}

				if (positionUs > 0) {

					this.mediaExtractor.seekTo(positionUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
				}
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}


	private void initializeBuffers(int sampleRate, int numChannels){

		//int triggerByteBufferSize = (int)((double)sampleRate * ((double)CHANNEL_BPS / 8d) * numChannels * (double)(TRIGGER_SIZE_MILLIS / 1000d));
		int triggerByteBufferSize = (int)((double)sampleRate * ((double)CHANNEL_BPS / 8d) * 2 * (double)(TRIGGER_SIZE_MILLIS / 1000d));

		triggerByteBuffer = ByteBuffer.allocateDirect(triggerByteBufferSize);
		triggerByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		//int tuneUrlByteBufferSize = (int)((double)sampleRate * ((double)CHANNEL_BPS / 8d) * numChannels * (double)(TUNE_URL_SIZE_MILLIS / 1000d));
		int tuneUrlByteBufferSize = (int)((double)sampleRate * ((double)CHANNEL_BPS / 8d) * 2 * (double)(TUNE_URL_SIZE_MILLIS / 1000d));

		tuneUrlByteBuffer = ByteBuffer.allocateDirect(tuneUrlByteBufferSize);
		tuneUrlByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		int resampledTuneUrlByteBufferSize = (int)((double)FINGERPRINT_SAMPLE_RATE * ((double)CHANNEL_BPS / 8d) * 2 * ((double)TUNE_URL_SIZE_MILLIS / 1000d));
		tuneUrlWaveLenght = (int)((double)resampledTuneUrlByteBufferSize / 2d);
		resampledTuneUrlByteBuffer = ByteBuffer.allocateDirect(resampledTuneUrlByteBufferSize);
		resampledTuneUrlByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
	}


	public void releaseMediaExtractor(){

		if(this.mediaCodec != null) {

			try {

				this.mediaCodec.stop();

			} catch (Exception e) {

				e.printStackTrace();
			}

			try {

				this.mediaCodec.release();

			} catch (Exception e) {

				e.printStackTrace();
			}

			this.mediaCodec = null;
		}

		if(this.mediaExtractor != null) {

			try {

				this.mediaExtractor.release();

			} catch (Exception e) {

				e.printStackTrace();
			}

			this.mediaExtractor = null;
		}
	}


	private void searchForTrigger(String path, long positionUs){

		if(mediaExtractor == null){

			initializeMediaExtractor(path, positionUs);
		}

		if(mediaExtractor != null) {

			long startTime = Calendar.getInstance().getTimeInMillis();
			long startPlayTime = (long) ((double) mediaExtractor.getSampleTime() / 1000d);

			while (isPlaying) {

				MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

				int inIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_US);

				if (inIndex >= 0) {

					ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inIndex);

					int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);

					if (sampleSize < 0) {

						mediaCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
					} else {

						mediaCodec.queueInputBuffer(inIndex, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
					}

					if (sampleSize >= 0) {

						int outIndex = mediaCodec.dequeueOutputBuffer(info, TIMEOUT_US);

						switch (outIndex) {

							case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:

								System.out.println("INFO_OUTPUT_FORMAT_CHANGED");

								break;

							case MediaCodec.INFO_TRY_AGAIN_LATER:

								System.out.println("INFO_TRY_AGAIN_LATER");

								break;

							default:

								ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outIndex);

								writeToBuffer(outputBuffer);

								mediaCodec.releaseOutputBuffer(outIndex, false);

								mediaExtractor.advance();

								break;
						}
					}
				}

				long currentTime = Calendar.getInstance().getTimeInMillis();
				long currentPlayTime = (long) ((double) mediaExtractor.getSampleTime() / 1000d);

				long timeSpent = currentTime - startTime;
				long timePlaySpent = currentPlayTime - startPlayTime;

				if (!recordTuneUrl && timePlaySpent > (timeSpent + 1000)) {

					try {

						Thread.sleep(timePlaySpent - timeSpent);
					} catch (Exception e) {

						e.printStackTrace();
					}
				}
			}

			releaseMediaExtractor();
		}
	}


	private void writeToBuffer(ByteBuffer outputBuffer){

		System.out.println("TuneURLService.writeToBuffer()");

		if(numChannels == 2) {

			if (recordTuneUrl) {

				if (outputBuffer.remaining() <= tuneUrlByteBuffer.remaining()) {

					tuneUrlByteBuffer.put(outputBuffer);
				}
				else {

					int remaining = tuneUrlByteBuffer.remaining();

					for(int i=0; i<remaining; i++){

						tuneUrlByteBuffer.put(outputBuffer.get(i));
					}

					searchTuneUrlFingerprint();
				}
			}
			else {

				if (outputBuffer.remaining() <= triggerByteBuffer.remaining()) {

					triggerByteBuffer.put(outputBuffer);
				}
				else {

					int remaining = triggerByteBuffer.remaining();

					for(int i=0; i<remaining; i++){

						triggerByteBuffer.put(outputBuffer.get(i));
					}

					checkTriggerFingerprint();
				}
			}
		}
		else{

			byte[] stereo = convertToStereo(outputBuffer);

			if (recordTuneUrl) {

				if (stereo.length <= tuneUrlByteBuffer.remaining()) {

					tuneUrlByteBuffer.put(stereo);
				}
				else {

					int remaining = tuneUrlByteBuffer.remaining();

					for(int i=0; i<remaining; i++){

						tuneUrlByteBuffer.put(stereo[i]);
					}

					searchTuneUrlFingerprint();
				}
			}
			else {

				if (stereo.length <= triggerByteBuffer.remaining()) {

					triggerByteBuffer.put(stereo);
				}
				else {

					int remaining = triggerByteBuffer.remaining();

					for(int i=0; i<remaining; i++){

						triggerByteBuffer.put(stereo[i]);
					}

					checkTriggerFingerprint();
				}
			}
		}
	}


	private byte[] convertToStereo(ByteBuffer outputBuffer){

		byte[] mono = new byte[outputBuffer.remaining()];
		byte[] stereo =  new byte[mono.length * 2];

		outputBuffer.get(mono);

		int indexStereo = 0;
		for(int i=0;i<mono.length; i = i + 2){

			stereo[indexStereo] = mono[i];
			stereo[indexStereo + 1] = mono[i + 1];
			stereo[indexStereo + 2] = mono[i];
			stereo[indexStereo + 3] = mono[i + 1];

			indexStereo = indexStereo + 4;
		}

		return stereo;
	}


	private void stopListening(){

		System.out.println("TuneURLService.stopListening()");

		isPlaying = false;
	}


	private void releaseResources(){

		isPlaying = false;

		try {

			if (mListenerActionReceiver != null) {

				unregisterReceiver(mListenerActionReceiver);
			}
		}
		catch (Exception e){

			e.printStackTrace();
		}
	}
	

	private void checkTriggerFingerprint() {

		System.out.println("TuneURLService.checkTriggerFingerprint()");

		Resample resample = null;

		try {

			triggerByteBuffer.rewind();
			referenceTriggerByteBuffer.rewind();
			resampledTriggerByteBuffer.clear();

			resample = new Resample();
			resample.create(sampleRate, FINGERPRINT_SAMPLE_RATE, 2048, 1);			
			
			int output_len = resample.resampleEx(triggerByteBuffer, resampledTriggerByteBuffer, triggerByteBuffer.remaining());

			if(output_len > 0) {

				mSimilarity = getSimilarity(referenceTriggerByteBuffer, FINGERPRINT_TRIGGER_BUFFER_SIZE / 2, resampledTriggerByteBuffer, FINGERPRINT_TRIGGER_BUFFER_SIZE / 2);

				System.out.println("TuneURL - similarity: " + mSimilarity);

				if (mSimilarity > SIMILARITY_THRESHOLD) {

					recordTuneUrl = true;
				}
			}
		}
		catch (Exception e) {

			e.printStackTrace();
		}
		finally {

			if(resample != null){

				try {

					resample.destroy();
				}
				catch (Exception e){

					e.printStackTrace();
				}
			}
		}
		try {

			triggerByteBuffer.clear();
		}
		catch (Exception e){

			e.printStackTrace();
		}
	}


	private void searchTuneUrlFingerprint(){

		System.out.println("searchTuneUrlFingerprint()");

		recordTuneUrl = false;

		Resample resample = null;

		try {

			tuneUrlByteBuffer.rewind();
			resampledTuneUrlByteBuffer.clear();

			resample = new Resample();
			resample.create(sampleRate, FINGERPRINT_SAMPLE_RATE, 2048, 1);
			
			int output_len = resample.resampleEx(tuneUrlByteBuffer, resampledTuneUrlByteBuffer, tuneUrlByteBuffer.remaining());

			if(output_len > 0) {

				byte[] monoAudio = convertToMono(resampledTuneUrlByteBuffer);

				if(monoAudio != null) {

					ByteBuffer byteBuffer = ByteBuffer.allocateDirect(monoAudio.length);
					byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
					byteBuffer.put(monoAudio);
					byteBuffer.rewind();

					String fingerprint_string = extractFingerprintFromByteBuffer(byteBuffer, (int) ((double) monoAudio.length / 2d));

					byteBuffer.clear();

					byteBuffer = null;

					monoAudio = null;

					try {

						System.out.println("fingerprint_string_1 = " + fingerprint_string.substring(0, 3500));
						System.out.println("fingerprint_string_2 = " + fingerprint_string.substring(3500));
					}
					catch (Exception e) {

						e.printStackTrace();
					}

					searchFingerprint(fingerprint_string);
				}
			}
		}
		catch (Exception e){

			e.printStackTrace();
		}
		finally {

			if(resample != null){

				try {

					resample.destroy();
				}
				catch (Exception e){

					e.printStackTrace();
				}
			}
		}

		try {

			tuneUrlByteBuffer.clear();
		}
		catch (Exception e){

			e.printStackTrace();
		}
	}


	private byte[] convertToMono(ByteBuffer byteBuffer){

		byte[] result = null;
		byte[] original = null;

		try {

			byteBuffer.rewind();

			original = new byte[byteBuffer.remaining()];

			byteBuffer.get(original);

			int resultLenght = (int) ((double) original.length / 2d);

			if ( (resultLenght & 1) != 0 ){

				resultLenght = resultLenght - 1;
			}

			result = new byte[resultLenght];

			int dstIndex = 0;

			for (int i = 0; i < resultLenght; i = i + 2) {

				result[i] = original[dstIndex];
				result[i + 1] = original[dstIndex + 1];

				dstIndex = dstIndex + 4;
			}
		}
		catch (Exception e){

			e.printStackTrace();
		}

		original = null;

		return result;
	}


	private String extractFingerprintFromByteBuffer(ByteBuffer byteBuffer, int waveLength) {

		String fingerprint = "";

		try {

			byte[] result_raw = extractFingerprint(byteBuffer, waveLength);

			String result = "";

			for(int i=0; i<result_raw.length; i++){

				result = result + (result_raw[i] & 0xff);

				if(i < result_raw.length - 1){

					result = result + ",";
				}
			}

			fingerprint = result;
		}
		catch (Exception e) {

			e.printStackTrace();
		}

		return fingerprint;
	}


	private void searchFingerprint(String fingerprint_string){

		Intent i = new Intent(this.getApplicationContext(), APIService.class);
		i.putExtra(TUNEURL_ACTION, ACTION_SEARCH_FINGERPRINT);
		i.putExtra(FINGERPRINT, fingerprint_string);
		startService(i);
	}


	public native byte[] extractFingerprint(ByteBuffer byteBuffer, int waveLength);

	public native float getSimilarity(ByteBuffer byteBuffer1, int waveLength1, ByteBuffer byteBuffer2, int waveLength2);





	//Code for checking what is analysed

	private OutputStream rawAudioDataOutputStream;

	public void initializeRawAudioDataRecording(String rawFilePath){

		try{

			File file = new File(rawFilePath);

			rawAudioDataOutputStream = new FileOutputStream(file);
		}
		catch (Exception e) {

			e.printStackTrace();
		}
	}


	private void writeToFile(byte[] content) {

		if (rawAudioDataOutputStream != null &&
				content != null &&
				content.length > 0) {

			try {

				rawAudioDataOutputStream.write(content);
			}
			catch(Exception e){

				e.printStackTrace();
			}
		}
	}


	private void releaseAudioOutputStream(){

		if (rawAudioDataOutputStream != null) {

			try {

				rawAudioDataOutputStream.flush();
			}
			catch(Exception e){

				e.printStackTrace();
			}

			try {

				rawAudioDataOutputStream.close();
			}
			catch(Exception e){

				e.printStackTrace();
			}

			rawAudioDataOutputStream = null;
		}
	}
}