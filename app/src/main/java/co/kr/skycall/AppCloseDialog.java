package co.kr.skycall;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class AppCloseDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.create();
    }

    @Override
    public void dismiss() {
        // Do your stuff here
        super.dismiss();
    }


}
