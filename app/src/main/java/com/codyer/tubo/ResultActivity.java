package com.codyer.tubo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.codyer.tubo.widgets.CircleLayout;
import com.codyer.tubo.widgets.CircleLayout.OnItemClickListener;
import com.codyer.tubo.widgets.CircleLayout.OnItemSelectedListener;

import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class ResultActivity extends Activity {

	private ListView mList;
	private SimpleAdapter mListAdapter;
	private ArrayList<HashMap<String, String>> mListItem;
	private String mCurrentString;
	private Uri uri;
	private ImageButton mCancelBt;
	private RelativeLayout mPopupWindowLayout;
	private ImageButton mBackBt;

	@SuppressLint("CutPasteId")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);

		CircleLayout circleMenu = (CircleLayout) findViewById(R.id.main_circle_layout);
		circleMenu.setOnItemSelectedListener(new CricleClickListener());
		circleMenu.setOnItemClickListener(new CricleClickListener());

		// 绑定Layout里面的ListView
		mList = (ListView) findViewById(R.id.numberListView);
		mCancelBt = (ImageButton) findViewById(R.id.ico_cancel_bt);
		mPopupWindowLayout = (RelativeLayout) findViewById(R.id.main_popupwindow_layout);

		mBackBt = (ImageButton) findViewById(R.id.back);
		Intent intent = getIntent();
		String result = intent.getStringExtra("result");
		// 生成一个ArrayList类型的变量list
		mListItem = new ArrayList<HashMap<String, String>>();

		BufferedReader rdr = new BufferedReader(new StringReader(result));
		int i = 1;
		try {
			for (String line = rdr.readLine(); line != null; line = rdr
					.readLine()) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("numberId", "(" + i++ + ")");
				map.put("number", line);
				mListItem.add(map);
			}
			rdr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 生成一个SimpleAdapter类型的变量来填充数据
		mListAdapter = new SimpleAdapter(this, mListItem,
				R.layout.listview_item, new String[] { "numberId", "number" },
				new int[] { R.id.numberId, R.id.number });
		// 设置显示ListView
		mList.setAdapter(mListAdapter);
		mBackBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mCancelBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPopupWindowLayout.setVisibility(View.INVISIBLE);
			}
		});
		// 添加点击
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mCurrentString = mListItem.get(position).get("number");
				mPopupWindowLayout.setVisibility(View.VISIBLE);
			}
		});
	}

	private class CricleClickListener implements OnItemSelectedListener,
			OnItemClickListener {
		@Override
		public void onItemSelected(View view, int position, long id, String name) {

			Toast.makeText(getApplicationContext(), "position=" + position,
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onItemClick(View view, int position, long id, String name) {
			Intent intent = new Intent();
			switch (view.getId()) {
			case R.id.ico_contact_bt:
				intent.setPackage("com.android.contacts");
				intent.putExtra(SearchManager.QUERY, mCurrentString);
				intent.setAction(Intent.ACTION_SEARCH);
				// intent.setAction(Intent.ACTION_GET_CONTENT);
				// intent.setType("vnd.android.cursor.item/phone");
				startActivity(intent);
				break;
			case R.id.ico_copy_bt:
				ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clip.setText(mCurrentString);
				Toast.makeText(
						getApplicationContext(),
						"\"" + mCurrentString + "\""
								+ getResources().getString(R.string.copy_ok),
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.ico_edit_bt:
				// 不需要权限，跳转到"拔号"中。
				Intent callIntent = new Intent(Intent.ACTION_DIAL,
						Uri.parse("tel:" + mCurrentString));
				startActivity(callIntent);
				break;
			case R.id.ico_dial_bt:
				intent.setAction(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:" + mCurrentString));
				startActivity(intent);
				break;
			case R.id.ico_message_bt:
				uri = Uri.parse("smsto:" + mCurrentString);
				intent = new Intent(Intent.ACTION_SENDTO, uri);
				intent.putExtra("sms_body", "图拨App");
				startActivity(intent);
				break;
			case R.id.ico_setting_bt:
				intent.setClassName("com.android.settings",
						"com.android.settings.Settings");
				startActivity(intent);
				break;
			case R.id.ico_web_bt:
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse("http://" + mCurrentString);
				intent.setData(content_url);
				startActivity(intent);
				break;
			case R.id.ico_mail_bt:
				// 需要 android.permission.SENDTO权限
				Uri uri = Uri.parse("mailto:" + mCurrentString);
				Intent mailIntent = new Intent(Intent.ACTION_SENDTO, uri);
				startActivity(mailIntent);
				break;
			default:
				break;
			}
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mPopupWindowLayout.getVisibility() == View.INVISIBLE) {
				mBackBt.performClick();
			} else {
				mCancelBt.performClick();
			}
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
