package com.agn.vpnshare.fragment;

import android.app.Dialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.DialogInterface;
import com.agn.vpnshare.service.ProxyService;

import androidx.fragment.app.DialogFragment;
import android.content.Intent;
import android.app.Activity;
import android.os.Handler;


public class ExitDialogFragment extends DialogFragment {
	private Activity mActivity;
	private Handler mHandler;
	private SharedPreferences sp;


	public ExitDialogFragment(Activity activity) {
		mActivity = activity;
		mHandler = new Handler();
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog dialog = new AlertDialog.Builder(getActivity()).
			create();
		dialog.setTitle("ATTENTION!");
		dialog.setMessage("Are you sure you want to Exit?");
		sp = getActivity().getSharedPreferences("Wifi_Tethering", Context.MODE_PRIVATE);

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Exit",
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					sp.edit().putBoolean("vpnshare", false).commit();
					getActivity().stopService(new Intent(getActivity(), ProxyService.class));
					getActivity().finishAffinity();
					System.exit(0);
				}
			}
		);
		
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Minimize" ,
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					minimizeApp();
				}
			}
		);
		
		return dialog;
	}
	
	private void minimizeApp() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}
}
