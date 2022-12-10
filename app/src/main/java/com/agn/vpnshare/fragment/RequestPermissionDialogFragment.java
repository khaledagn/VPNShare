package com.agn.vpnshare.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import com.agn.vpnshare.*;

public class RequestPermissionDialogFragment extends DialogFragment {
	private RequestPermissionListener dialogInterfaceListener;

	public static RequestPermissionDialogFragment newInstance() {
		return new RequestPermissionDialogFragment();
	}

	public interface RequestPermissionListener {
		public void onClickRequestPermissionDialog(DialogInterface dialog);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			dialogInterfaceListener = (RequestPermissionListener) context;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setTitle(R.string.title_permission_request);
		dialog.setMessage(R.string.message_permission_request);
		dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int position) {
				dialogInterfaceListener.onClickRequestPermissionDialog(dialogInterface);
			}
		});
		dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int position) {
				dialogInterface.dismiss();
				getActivity().finish();
			}
		});
		return dialog.create();
	}

}
