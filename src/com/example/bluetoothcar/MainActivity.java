package com.example.bluetoothcar;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = "ProcessInfo";

	// ����������
	private BluetoothAdapter btAdapter;
	private BluetoothDevice btDevice;
	private BluetoothSocket btSocket;
	static OutputStream outputStream;

	// ����ɨ��㲥
	private BroadcastReceiver mReceiver;
	private BroadcastReceiver mReceiver1;

	// ɨ�迪ʼ������ı�־
	private ProgressBar progressBar;

	// ������ʾ��Ϣ��TextView
	private TextView textView1;

	// ������ʾ����Ժ�ɨ�赽���豸��ListView
	private ListView listView1;

	// list1����ʶ��device��list2������ʾdevice��MAC��ַ��������ListView�ϣ�arrayAdapter1
	// װ�ص�list����list2
	private List<BluetoothDevice> list1 = new ArrayList<BluetoothDevice>();
	private List<String> list2 = new ArrayList<String>();
	private ArrayAdapter<String> arrayAdapter1 = null;

	// ����Rfcommͨ����UUDI��
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// ����������Ͽ��Ĺ㲥�ͱ�־
	private BroadcastReceiver btConnectReceiver;
	private BroadcastReceiver btDisonnectReceiver;
	private IntentFilter connectIntentFilter;
	private IntentFilter disconnectIntentFilter;
	private boolean isConnect = false;

	private String deviceName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView1 = (ListView) findViewById(R.id.listview1);
		textView1 = (TextView) findViewById(R.id.textview1);
		progressBar = (ProgressBar) findViewById(R.id.progressbar);

		btAdapter = BluetoothAdapter.getDefaultAdapter();

		// ���ɨ�赽�������豸������MAC��ַ���豸������ӵ�ListView��
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				// �ҵ��豸
				textView1.setText("ɨ�赽�������豸��");
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// ͨ�������ͼ���device
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					list1.add(device);
					list2.add(device.getAddress() + "  " + device.getName());
					arrayAdapter1 = new ArrayAdapter<String>(MainActivity.this,
							android.R.layout.simple_list_item_1, list2);
					listView1.setAdapter(arrayAdapter1);

				}
			}

		};

		IntentFilter intentFilter = new IntentFilter(
				BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, intentFilter);

		// ɨ�����ʱ��ProgressBar���ɼ�
		mReceiver1 = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				progressBar.setVisibility(ProgressBar.INVISIBLE);
				if (list1.size() == 0) {
					textView1.setText("û��ɨ�赽�������豸");
				}
			}

		};

		IntentFilter intentFilter1 = new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver1, intentFilter1);

		// ��������ʱ��isConncet��־λ��ΪTrue
		btConnectReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODO Auto-generated method stub
				Log.e(TAG, "-- connected");
				isConnect = true;
				textView1.setText("������" + deviceName + "����������̨");
			}
		};

		connectIntentFilter = new IntentFilter(
				BluetoothDevice.ACTION_ACL_CONNECTED);
		registerReceiver(btConnectReceiver, connectIntentFilter);

		// �������ӶϿ�ʱ����TryToConnect�̣߳�ȷ���Ͽ�������
		btDisonnectReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODO Auto-generated method stub
				Log.e(TAG, "-- disconnected");
				isConnect = false;
				new TryToConnet().start();
			}
		};

		disconnectIntentFilter = new IntentFilter(
				BluetoothDevice.ACTION_ACL_DISCONNECTED);
		registerReceiver(btDisonnectReceiver, disconnectIntentFilter);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		// �����һ��ɨ�赽��Itemʱ���������ӣ��˴�û�����̣߳�ͨ��list1ʶ���豸
		listView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				// Ҫ���ӵ��豸���豸��
				deviceName = list1.get(arg2).getName();
				// ѡ��itemʱ����ȡ��Ѱ���豸
				btAdapter.cancelDiscovery();
				progressBar.setVisibility(ProgressBar.INVISIBLE);
				// ͨ��list1��arg2ʶ�������ĸ�device
				btDevice = btAdapter.getRemoteDevice(list1.get(arg2)
						.getAddress());

				// �������һ��Dialog��ʾ�Ƿ����ӣ����ӳɹ���textview��ʾ���ӵ��豸����
				new AlertDialog.Builder(MainActivity.this)
						.setIcon(R.drawable.ic_launcher).setTitle("���Ӵ��豸��")
						.setPositiveButton("ȷ��", new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								new TryToConnet().start();
							}
						}).show();
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// �ر�socket
		if (isConnect) {
			try {
				outputStream.close();
				btSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// ȡ���㲥ע��
		unregisterReceiver(btConnectReceiver);
		unregisterReceiver(btDisonnectReceiver);
		unregisterReceiver(mReceiver);
		unregisterReceiver(mReceiver1);
		Log.e(TAG, "-- onDestroy");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(Menu.NONE, 0, Menu.NONE, "������");
		menu.add(Menu.NONE, 1, Menu.NONE, "�ر�����");
		menu.add(Menu.NONE, 2, Menu.NONE, "ɨ����Χ�����豸");
		menu.add(Menu.NONE, 3, Menu.NONE, "������Ӧ����̨");
		menu.add(Menu.NONE, 4, Menu.NONE, "���밴������̨");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case 0:
			if (!btAdapter.isEnabled()) {
				btAdapter.enable();
				Toast.makeText(MainActivity.this, "����������", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case 1:
			if (btAdapter.isEnabled()) {
				btAdapter.disable();
			}
			break;

		case 2:
			if (btAdapter.isEnabled()) {
				btAdapter.startDiscovery();
				Toast.makeText(MainActivity.this, "��ʼɨ��", Toast.LENGTH_SHORT)
						.show();
				progressBar.setVisibility(ProgressBar.VISIBLE);
			} else {
				Toast.makeText(MainActivity.this, "�������", Toast.LENGTH_SHORT)
						.show();

			}
			break;
		case 3:
			// ������ӳɹ���������ɽ������̨
			if (isConnect) {
				Intent intent = new Intent(MainActivity.this,
						ControlActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(MainActivity.this, "������������", Toast.LENGTH_LONG)
						.show();
			}
			break;

		case 4:
			if (isConnect) {
				Intent intent = new Intent(MainActivity.this,
						ButtonControlActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(MainActivity.this, "������������", Toast.LENGTH_LONG)
						.show();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// �Ͽ����Ӻ����������ӵ��̣߳����ӳɹ����˳��߳�
	private class TryToConnet extends Thread {
		public void run() {
			/* �˴��������´���һ��socket�������������Ӻ��޷��������ݣ����˲�����Rfcommͨ���Ѿ��ı� */
			try {
				btSocket = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "-- failed to create btSocket");
			}
			try {
				outputStream = btSocket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (true) {
				try {
					btSocket.connect();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (isConnect) {
					Log.e(TAG, "-- Connect again,ending the TryToConnet thread");
					break;
				}
			}
		}
	}
}
