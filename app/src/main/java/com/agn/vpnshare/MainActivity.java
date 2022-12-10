package com.agn.vpnshare;



import com.agn.vpnshare.fragment.AboutFragment;
import com.agn.vpnshare.fragment.ExitDialogFragment;
import com.agn.vpnshare.fragment.HomeFragment;
import com.agn.vpnshare.fragment.RequestPermissionDialogFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
	public static final String TAG = "MainActivity";

	private static final int PERMISSION_REQUEST_CODE = 1;

	BottomNavigationView bottomNavigationView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bottomNavigationView = findViewById(R.id.bottom_menu);
		bottomNavigationView.setOnNavigationItemSelectedListener( MainActivity.this);
		bottomNavigationView.setSelectedItemId(R.id.home);
		Window window = this.getWindow();
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

		window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		int requestCode = Build.VERSION.SDK_INT;
		if (requestCode >= 33 && !permissionGranted()) {
			requestPermissionNotifInfo();
		}

	    }



	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		new ExitDialogFragment(this)
				.show(getSupportFragmentManager(), "alertExit");
	}


	public void backStackRemove() {
		for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
			getSupportFragmentManager().popBackStack();
		}
	}

	@RequiresApi(api = 33)
	private void requestPermissionNotifInfo() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS)) {
			// Dialog de informação para caso o usuário já tenha negado as permissões pelo menos uma vez
			RequestPermissionDialogFragment dialog = RequestPermissionDialogFragment.newInstance();
			dialog.show(getSupportFragmentManager(), dialog.getTag());
		} else {
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
		}
	}


	@RequiresApi(api = 33)
	private boolean permissionGranted() {
		int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS);
		if (result == PackageManager.PERMISSION_GRANTED) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()){

			case R.id.home:
				backStackRemove();
				getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new HomeFragment()).commitAllowingStateLoss();

				break;

			case R.id.about:
				backStackRemove();
				getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new AboutFragment()).commitAllowingStateLoss();
				break;
		}
		return true;
	}
}