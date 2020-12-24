// ! rx1310 <rx1310@inbox.ru> | Copyright (c) rx1310, 2020 | MIT License

package ru.devreader.app.task;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import ru.devreader.app.Constants;
import ru.devreader.app.R;
import ru.devreader.app.util.AppUtils;
import ru.devreader.app.util.HttpUtils;

public class OTACheckTask extends AsyncTask<Void, Void, String> {

    private Context mContext;
	private String mChannel;

	private ProgressDialog progressDialog;

    public OTACheckTask(Context context, String isChannel) {
        this.mContext = context;
		this.mChannel = isChannel;
    }

    protected void onPreExecute() {

		progressDialog = new ProgressDialog(mContext);
		progressDialog.setTitle(mContext.getString(R.string.ota_dlg_checking));
		progressDialog.setMessage(mContext.getString(R.string.ota_dlg_checking_description));
		progressDialog.setCancelable(false);
		progressDialog.show();
		
    }

    @Override
    protected void onPostExecute(String result) {

		progressDialog.dismiss();

        if (!TextUtils.isEmpty(result)) {
            parseJson(result);
        }
		
		AppUtils.Log(mContext, "d", "onPostExecute: " + result);

    }

    private void parseJson(String result) {

        try {

            JSONObject obj = new JSONObject(result);

			String 
				versionName = obj.getString(Constants.OTA.VERSION_NAME),
				updateMessage = obj.getString(Constants.OTA.MESSAGE),
				urlApk = obj.getString(Constants.OTA.URL_APK),
				urlChangelog = obj.getString(Constants.OTA.URL_CHANGELOG);

			int 
				versionCode = obj.getInt(Constants.OTA.VERSION_CODE),
				versionCodeInstalled = AppUtils.getVersionCode(mContext, mContext.getPackageName());

			if (versionCode > versionCodeInstalled) {

				updateDialog(mContext, versionName, updateMessage, urlApk, urlChangelog);

			} else {
				AppUtils.showToast(mContext, mContext.getString(R.string.ota_msg_used_latest_release));
			}

        } catch (JSONException e) {
			AppUtils.Log(mContext, "e", "parseJson: " + e);
        }

    }

	void updateDialog(final Context context, String updateVersion, String updateMessage, final String apkUrl, final String changelogUrl) {

		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);

		alertBuilder.setTitle(updateVersion);
		alertBuilder.setMessage(updateMessage);
		alertBuilder.setPositiveButton(R.string.ota_action_download, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int i) {
					mContext.startActivity(new Intent (Intent.ACTION_VIEW, Uri.parse(apkUrl)));
				}
			});
		alertBuilder.setNegativeButton(R.string.ota_action_changelog, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int i) {
					context.startActivity(new Intent (Intent.ACTION_VIEW, Uri.parse(changelogUrl)));
				}
			});
		alertBuilder.setNeutralButton(R.string.ota_action_copy_url, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int i) {
					ClipboardManager mClipboardMng = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData mClipData = ClipData.newPlainText(null, apkUrl);
					mClipboardMng.setPrimaryClip(mClipData);
					AppUtils.showToast(context, context.getString(R.string.ota_msg_url_copied));
				}
			});
		alertBuilder.show();
		
		AppUtils.Log(mContext, "d", "updateDialog = show()");

	}

    @Override
    protected String doInBackground(Void... args) {

		if (mChannel == "beta") {
			return HttpUtils.get(mContext, Constants.OTA.CHANNEL_BETA);
		} else if (mChannel == "stable") {
			return HttpUtils.get(mContext, Constants.OTA.CHANNEL_RELEASE);
		}
		
		// -> new OTACheckTask(getContext(), "stable").execute();

		return HttpUtils.get(mContext, Constants.OTA.CHANNEL_RELEASE);

	}

}
