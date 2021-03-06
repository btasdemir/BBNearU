package com.nux.bb.near.u.track.ui;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.JPEGEncodedImage;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.NullField;

import com.nux.bb.near.u.track.field.WebBitmapField;
import com.nux.bb.near.u.track.obj.DBStor;
import com.nux.bb.near.u.track.obj.Setting;
import com.nux.bb.near.u.track.util.InetConn;
import com.nux.bb.near.u.track.util.LBS;
import com.nux.bb.near.u.track.util.LbsCallback;
import com.nux.bb.near.u.track.util.Util;
import com.nux.bb.near.u.track.util.WebDataCallback;

public class MapScreen extends BaseScreen implements LbsCallback,WebDataCallback{
	WebBitmapField peta = new WebBitmapField(FOCUSABLE);
	Setting st = DBStor.get_setting();
	int zoom = st.getZoom();
	String[] mapstype = {"roadmap","satellite","hybrid","terrain"};
	private String lat;
	private String lon;
	private String url;

	public MapScreen() {
		setJudul("BlackBerry Near U");
		add(new NullField());
		add(peta);
		add(new NullField());
		setShowLoader(true);
		if(DeviceInfo.isSimulator()){
			callbackLBS(DBStor.get_setting().getLat(), DBStor.get_setting().getLon());
		}else{
			LBS.getLocation(this);
		}
		addMenuItem(ZoomIn);
		addMenuItem(ZoomOut);
		addMenuItem(setwalp);
	}

	MenuItem ZoomIn = new MenuItem("Zoom in",0x000200,1) {
		public void run() {
			doZoom(1);
		}
	};
	MenuItem ZoomOut = new MenuItem("Zoom out",0x000200,2) {
		public void run() {
			doZoom(-1);
		}
	};

	MenuItem setwalp = new MenuItem("Set as wallpaper",0x000200,1) {
		public void run() {
			JPEGEncodedImage img = JPEGEncodedImage.encode(peta.getBitmap(),100);
			if(Util.tulisFile(Util.GetPath()+"maps.jpg", new String(img.getData()))){
				HomeScreen.setBackgroundImage(Util.GetPath()+"maps.jpg");
				Dialog.alert("Maps has been set to wallpaper");
			}else{
				Dialog.alert("Failed to Save Image");
			}
		}
	};


	public void doZoom(int n){
		int a =0;
		String t = "http://maps.google.com/maps/api/staticmap?";
		if(this.url.startsWith(t)){
			this.zoom = zoom +n;
			a = t.length()+lat.length()+lon.length()+14+(zoom+"").length();
			this.peta.reload(t+"center="+lat+","+lon+"&zoom="+zoom+url.substring(a));
		}
	}


	protected boolean onSavePrompt() {
		return true;
	}
	protected boolean touchEvent(TouchEvent message) {
		boolean isConsumed = false;
		TouchGesture touchGesture = message.getGesture();
		if(touchGesture!=null){
			if(touchGesture.getEvent()== TouchGesture.SWIPE){
				switch(touchGesture.getSwipeDirection()){
				case TouchGesture.SWIPE_NORTH:

					break;
				case TouchGesture.SWIPE_SOUTH:

					break;
				case TouchGesture.SWIPE_EAST:

					break;
				case TouchGesture.SWIPE_WEST:

					break;
				}
				isConsumed = true;
			}
		}
		return isConsumed;
	}

	public void callbackLBS(String lat, String lon) {
		Util.Log(lat+","+lon);
		this.lat = lat;
		this.lon = lon;
		st = DBStor.get_setting();
		this.url = Util.GetURL()+"nearu.php?p="+Integer.toHexString(DeviceInfo.getDeviceId())+"&t="+DeviceInfo.getDeviceName()+
		"&o="+DeviceInfo.getPlatformVersion()+"&lat="+lat+"&lon="+lon+
		"&mt="+mapstype[st.getMapsType()]+"&z="+st.getZoom()+
		"&size="+Display.getWidth()+"x"+Display.getHeight();
		InetConn.getWebData(url, this);
	}


	public void callbackLBSError(String error) {
		Util.Log(error);
		setShowLoader(false);
		if(Dialog.ask(Dialog.D_YES_NO,error+", Try again?")==Dialog.YES){
			LBS.getLocation(this);
		}
	}


	public void callback(String data) {
		setShowLoader(false);
		if(data.startsWith("Exception:")){
			Util.Log(data);
			if(Dialog.ask(Dialog.D_YES_NO,"Failed to connect to server, Reconnect?")==Dialog.YES){
				setShowLoader(true);
				InetConn.getWebData(url, this);
			}
		}else if(data.startsWith("url:")){
			String[] temps = Util.split(data.substring(4),":::");
			this.url = temps[1];
			st.setTotal(temps[0]);
			DBStor.set_setting(st);
			this.peta.reload(url);
		}
	}
}
