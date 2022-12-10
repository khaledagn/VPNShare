package com.agn.vpnshare.service;


import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_IMMUTABLE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.agn.vpnshare.R;
import com.agn.vpnshare.MainActivity;

public class ProxyService extends Service {
	final String LOG_TAG = "myLogs";
	public static final String TAG = "ProxyService";
	public static final int DEFAULT_PORT = 8964;
	public static final int MAX_PORT = 20146;
	private static boolean isRunning;
	private SharedPreferences sp;
	String channelId = "task_channel";
	String channelName = "task_name";
	public static volatile ProxyService instance;

	public IBinder onBind(Intent intent) {
		Log.d(LOG_TAG, "onBind");
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sp = getSharedPreferences("Wifi_Tethering",Context.MODE_PRIVATE);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ProxyService proxyServer = ProxyService.getInstance();
		proxyServer.stop();
	}


	public int onStartCommand(Intent intent, int flags, int startId) {
		isRunning = true;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new
					NotificationChannel(channelId, channelName,
					NotificationManager.IMPORTANCE_DEFAULT);
		}

		showNotification();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int cou=0;
					while (true) {
						Log.i("mohsennnn : ", "me --> " +  cou);
						cou++;
						Thread.sleep(500);
					}
				} catch (Exception e) {
					Log.i("mohsennn Service", e.toString());
				}
			}
		}).start();

		return super.onStartCommand(intent, flags, startId);
	}




	public static ProxyService getInstance() {
		synchronized (ProxyService.class) {
			if (instance == null) {
				instance = new ProxyService();
			}
		}
		return instance;
	}

	public int port;
	public boolean running;
	public Selector selector;
	public ServerSocketChannel server;

	public ProxyService() {
		port = DEFAULT_PORT;
		running = false;
	}

	public int getPort() {
		return port;
	}

	public Selector getSeletor() {
		return selector;
	}

	public boolean isRunning() {
		return running;
	}

	public synchronized boolean start() {
		if (running) {
			return false;
		}

		Log.d(TAG, "start proxy server");
		try {
			selector = Selector.open();
		} catch (Exception e) {
			Log.e(TAG, "create selector exception", e);
			return false;
		}

		try {
			server = ServerSocketChannel.open();
			server.configureBlocking(false);
		} catch (Exception e) {
			Log.e(TAG, "create server channel exception", e);
			return false;
		}

		while (true && port < MAX_PORT) {
			try {
				server.socket().bind(new InetSocketAddress(port));
			} catch (IOException e) {
				++port;
				continue;
			}
			Log.d(TAG, "proxy server listen port " + port);
			break;
		}

		if (port >= MAX_PORT) {
			return false;
		}

		try {
			server.register(selector, SelectionKey.OP_ACCEPT);
		} catch (ClosedChannelException e) {
			Log.e(TAG, "register selector exception", e);
			return false;
		}

		running = true;
		Thread t = new Thread(new Runnable() {
			public void run() {
				doProxy();
				running = false;
			}
		});
		t.setDaemon(false);
		t.setName("ProxyService");
		t.start();
		return true;
	}

	public synchronized boolean stop() {
		isRunning = false;
		if (!running) {
			return false;
		}

		Log.d(TAG, "stop proxy server");
		running = false;

		try {
			selector.wakeup();
			selector.close();
			selector = null;
		} catch (Exception e) {
			Log.e(TAG, "close selector exception.", e);
		}

		try {
			server.close();
			server = null;
		} catch (IOException e) {
			Log.e(TAG, "close server exception.", e);
		}
		return true;
	}

	private void showNotification() {

		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent intentpending =  PendingIntent.getActivity(this, 0, intent, FLAG_CANCEL_CURRENT | FLAG_IMMUTABLE);
		NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new
					NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
			manager.createNotificationChannel(channel);
		}
		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
				.setContentTitle(getString(R.string.vpnshare))
				.setContentText(getString(R.string.vpnsharerunning))
				.setColor(getResources().getColor(R.color.colorPrimary))
				.setContentIntent(intentpending)
				.setSmallIcon(R.drawable.ic_quick_settings_tile_on);
//        manager.notify(1, builder.build());
		startForeground(1 , builder.build());
	}//showNotification

	public static boolean IsRunning() {
		return isRunning;
	}
	public void doProxy() {
		Log.d(TAG, "do proxy server start");
		while (true) {
			if (server == null || selector == null) {
				break;
			}

			Set<SelectionKey> keys = null;
			try {
				selector.select();
				if (!selector.isOpen()) {
					break;
				}
				keys = selector.selectedKeys();
			} catch (Exception e) {
				Log.e(TAG, "selector select exception", e);
				continue;
			}

			Iterator<SelectionKey> iterator = keys.iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();

				Object attr = key.attachment();
				ChannelPair cp = null;
				if (attr instanceof ChannelPair) {
					cp = (ChannelPair) attr;
				} else {
					cp = new ChannelPair();
				}
				try {
					cp.handleKey(key);
				} catch (Exception e) {
					// catch handle key exception
				}
			}

		}
		Log.d(TAG, "do proxy server finish");
	}

}
