package com.nux.bb.near.u.track.ui;

import java.util.Vector;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.nux.bb.near.u.track.field.LabelFieldBold;
import com.nux.bb.near.u.track.field.ManagerField;
import com.nux.bb.near.u.track.field.SpaceField;
import com.nux.bb.near.u.track.field.TombolField;
import com.nux.bb.near.u.track.field.TombolRoomField;
import com.nux.bb.near.u.track.obj.DBStor;
import com.nux.bb.near.u.track.obj.MsgChat;
import com.nux.bb.near.u.track.obj.RoomChat;
import com.nux.bb.near.u.track.obj.Setting;
import com.nux.bb.near.u.track.util.InetConn;
import com.nux.bb.near.u.track.util.LBS;
import com.nux.bb.near.u.track.util.LbsCallback;
import com.nux.bb.near.u.track.util.Util;
import com.nux.bb.near.u.track.util.WebDataCallback;
import com.nux.rim.push.PersistentStorage;
import com.nux.rim.push.PushController;

public class RoomScreen extends BaseScreen implements LbsCallback, WebDataCallback, FieldChangeListener{
	private String url = "";
	TombolField showmaps = new TombolField("BB Near You","nokmap-ico.png", USE_ALL_WIDTH|FIELD_HCENTER);
	TombolField settinng = new TombolField("Setting","Setting-icon.png", USE_ALL_WIDTH|FIELD_HCENTER);
	VerticalFieldManager AktifRoom = new VerticalFieldManager(USE_ALL_WIDTH);
	VerticalFieldManager Favourite = new VerticalFieldManager(USE_ALL_WIDTH);
	VerticalFieldManager ChatRoom = new VerticalFieldManager(USE_ALL_WIDTH);
	Vector favRomm = DBStor.get_roomFav();
	ChatScreen[] chatss = new ChatScreen[5];

	static String pin = Integer.toHexString(DeviceInfo.getDeviceId());

	ManagerField mf1 = new ManagerField();
	ManagerField mf2 = new ManagerField();
	ManagerField mf3 = new ManagerField();
	ManagerField mf4 = new ManagerField();

	HorizontalFieldManager hfm = new HorizontalFieldManager(FIELD_HCENTER);
	ButtonField refresh = new ButtonField("Refresh",FIELD_HCENTER|ButtonField.CONSUME_CLICK);
	ButtonField addroom = new ButtonField("Add Room",FIELD_HCENTER|ButtonField.CONSUME_CLICK);
	public RoomScreen() {
		//DBStor.set_roomFav(null);
		for(int a=0;a<favRomm.size();a++){
			reloadFav((RoomChat)favRomm.elementAt(a));
		}
		setJudul("BlackBerry Near yoU");
		mf1.add(new SpaceField(8));
		mf1.add(showmaps);
		mf1.add(settinng);
		mf1.add(new SpaceField(8));
		settinng.setChangeListener(this);
		showmaps.setChangeListener(this);

		mf2.add(new LabelFieldBold("Active Room",0x424242,FIELD_HCENTER));
		mf2.add(AktifRoom);
		mf2.add(new SpaceField(8));

		mf3.add(new LabelFieldBold("Favourite Room",0x424242,FIELD_HCENTER));
		mf3.add(Favourite);
		mf3.add(new SpaceField(8));

		mf4.add(new LabelFieldBold("Nearest Chat Room",0x424242,FIELD_HCENTER));
		mf4.add(ChatRoom);
		mf4.add(hfm);
		hfm.add(refresh);
		hfm.add(addroom);
		refresh.setChangeListener(this);
		addroom.setChangeListener(this);
		mf4.add(new SpaceField(8));

		add(mf1);
		add(new SpaceField(4));
		add(mf2);
		add(new SpaceField(4));
		add(mf3);
		add(new SpaceField(4));
		add(mf4);
		add(new SpaceField(4));
	}

	public void GetActiveRoom() {
		if(PersistentStorage.isRegistered()){
			if(DBStor.get_setting().isTest()){
				InetConn.getWebData(Util.GetURLChat()+"getactiveRoom.php?pin="+pin, this);
				setShowLoader(true);
			}
		}else if(DeviceInfo.isSimulator()){
			InetConn.getWebData(Util.GetURLChat()+"getactiveRoom.php?pin="+pin, this);
			setShowLoader(true);
		}
	}

	public void callbackLBS(String lat, String lon) {
		Setting set = DBStor.get_setting();
		if(!set.getLat().equals(lat) && !set.getLon().equals(lon))
		{
			set.setLat(lat);
			set.setLon(lon);
			DBStor.set_setting(set);
		}
		this.url = Util.GetURLChat()+"room.php?lat="+lat+"&lon="+lon;
		InetConn.getWebData(url, this);
	}

	public void callbackLBSError(String error) {
		setShowLoader(false);
		if(Dialog.ask(Dialog.D_YES_NO,error+", Try again?")==Dialog.YES){
			setShowLoader(true);
			LBS.getLocation(this);
		}
	}

	public void callback(String data) {
		Util.Log("rooms"+data);
		setShowLoader(false);
		if(data.startsWith("Exception")){
			Dialog.alert("Failed to connect to server.");
		}else if(data.startsWith("room:")){
			String[] temps = Util.split(data.substring(5), "<room>");
			for(int n=0; n<temps.length;n++){
				String[] temp = Util.split(temps[n], "||");
				reloadActive(new RoomChat(temp[1],temp[0]));
			}
			//reloadActive();

			setShowLoader(true);
			if(DeviceInfo.isSimulator()){
				callbackLBS(DBStor.get_setting().getLat(), DBStor.get_setting().getLon());
			}else{
				LBS.getLocation(this);
			}
		}else if(data.startsWith("empty")){
			if(DeviceInfo.isSimulator()){
				callbackLBS(DBStor.get_setting().getLat(), DBStor.get_setting().getLon());
			}else{
				LBS.getLocation(this);
			}
		}else if(data.startsWith("done:")){
			ChatRoom.deleteAll();
			String temps [] = Util.split(data.substring(5), "<<>>");
			String temp[];
			for(int n=0;n<temps.length;n++){
				temp = Util.split(temps[n], "||");
				TombolRoomField tl = new TombolRoomField(new RoomChat(temp[1],temp[0]), temp[2] ,USE_ALL_WIDTH);
				tl.setChangeListener(this);
				ChatRoom.add(tl);
			}
		}
	}

	public void reloadActive(RoomChat tf){
		int n= 0,a=0;
		for(n=0;n<chatss.length;n++){
			ChatScreen css = chatss[n];
			if(css!=null){
				a++;
			}
		}
		if(a<5){
			ChatScreen cs;
			for(n=0;n<chatss.length;n++){
				ChatScreen f2 = chatss[n];
				if(f2!=null)
				if(f2.getIdRoom().equals(tf.getIdRoom())){
					return;
				}
			}

			TombolRoomField tfs = new TombolRoomField(tf,"0",USE_ALL_WIDTH);
			tfs.setChangeListener(this);
			tfs.setActive(true);
			AktifRoom.add(tfs);
			cs = new ChatScreen(tf.getIdRoom(),tf.getNamaRoom(),this,tfs);
			//UiApplication.getUiApplication().pushScreen(cs);

			for(n=0;n<chatss.length;n++){
				ChatScreen css = chatss[n];
				if(css==null){
					this.chatss[n] = cs;
					break;
				}
			}
			cs = null;
		}else{
			//Dialog.alert("Max 5 Room active");
		}
	}

	public void reloadFav(RoomChat tf){
		TombolRoomField tfs = new TombolRoomField(tf,"0",USE_ALL_WIDTH);
		tfs.setChangeListener(this);
		tfs.setFavourite(true);
		Favourite.add(tfs);
	}


	protected boolean onSavePrompt() {
		return true;
	}

	public void fieldChanged(Field f, int context) {
		if(f==showmaps){
			UiApplication.getUiApplication().pushScreen(new MapScreen());
		}else if(f==settinng){
			UiApplication.getUiApplication().pushScreen(new SettingScreen());
		}else if(f==addroom){
			if( PersistentStorage.isRegistered() ) {
				if(DBStor.get_setting().isTest()){
					UiApplication.getUiApplication().pushScreen(new addRoomChat());
				}else{
					Dialog.alert("Please try Test Push, until get Success notification.");
				}
			}else{
				Dialog.alert("Please Activate room Chat");
			}
		}else if(f==refresh){
			if(DeviceInfo.isSimulator()){
				callbackLBS(DBStor.get_setting().getLat(), DBStor.get_setting().getLon());
			}
			if( PersistentStorage.isRegistered() ) {
				if(DBStor.get_setting().isTest()){
					GetActiveRoom();
					LBS.getLocation(this);
					setShowLoader(true);
				}else{
					Dialog.alert("Please try Test Push, until get Success notification.");
				}
			}else{
				Dialog.alert("Please Activate room Chat");
			}
		}else if(f instanceof TombolRoomField){
			try{
				TombolRoomField tf = (TombolRoomField)f;
				addToActive(tf);
			}catch(Exception e){}
		}
	}

	public void addToActive(TombolRoomField tf){
		if(!DBStor.get_setting().isTest() && !DeviceInfo.isSimulator()){
			Dialog.alert("Please try Test Push, until get Success notification.");
			return;
		}
		PushController.updateIndicator(false);
		if(Integer.parseInt(tf.getTotalperson())>49){
			Dialog.alert("Room Full!");
		}
		int n= 0,a=0;
		for(n=0;n<chatss.length;n++){
			ChatScreen css = chatss[n];
			if(css!=null){
				a++;
			}
		}
		if(a<5){
			ChatScreen cs;

			for(n=0;n<chatss.length;n++){
				cs = chatss[n];
				if(cs!=null){
					if(cs.getIdRoom().equals(tf.getRoom().getIdRoom())){
						UiApplication.getUiApplication().pushScreen(cs);
						return;
					}
				}
			}
			TombolRoomField tfs = new TombolRoomField(tf.getRoom(),"0",USE_ALL_WIDTH);
			tfs.setChangeListener(this);
			tfs.setActive(true);
			AktifRoom.add(tfs);
			cs = new ChatScreen(tf.getRoom().getIdRoom(),tfs.getLabel(),this,tfs);
			for(n=0;n<chatss.length;n++){
				ChatScreen css = chatss[n];
				if(css==null){
					this.chatss[n] = cs;
					break;
				}
			}
			UiApplication.getUiApplication().pushScreen(cs);
			cs = null;
		}else{
			ChatScreen cs;
			for(n=0;n<chatss.length;n++){
				cs = chatss[n];
				if(cs!=null){
					if(cs.getIdRoom().equals(tf.getRoom().getIdRoom())){
						UiApplication.getUiApplication().pushScreen(cs);
						return;
					}
				}
			}
			Dialog.alert("Max 5 Active Room");
		}
	}

	public void leaveGroup(String idRoom){
		int n= 0;
		ChatScreen cs;
		TombolRoomField trf;
		for(n=0;n<chatss.length;n++){
			cs = chatss[n];
			if(cs!=null)
			if(cs.getIdRoom().equals(idRoom)){
				this.chatss[n] = null;
				break;
			}
		}
		for(n=0;n<AktifRoom.getFieldCount();n++){
			trf = (TombolRoomField)AktifRoom.getField(n);
			if(trf.getRoom().getIdRoom().equals(idRoom)){
				AktifRoom.delete(trf);
			}
		}
		PushController.updateIndicator(false);
		InetConn.getWebData(Util.GetURLChat()+"inout.php?id="+idRoom+"&pin="+pin+"&in=false&nick="+DBStor.get_setting().getNick(), new WebDataCallback(){
			public void callback(String data) {}
		});
		cs=null;trf=null;
	}

	public void addToFav(TombolRoomField tf){
		int n= 0;
		RoomChat tf2;
		boolean ada = false;
		for(n=0;n<favRomm.size();n++){
			tf2 = (RoomChat)favRomm.elementAt(n);
			if(tf2.getIdRoom().equals(tf.getRoom().getIdRoom())){
				ada = true;
				break;
			}
		}
		if(ada){
			Dialog.alert("Room Chat exist");
		}else{
			favRomm.addElement(tf.getRoom());
			DBStor.set_roomFav(favRomm);
			TombolRoomField tfs = new TombolRoomField(tf.getRoom(),"0",USE_ALL_WIDTH);
			tfs.setChangeListener(this);
			tfs.setFavourite(true);
			Favourite.add(tfs);
		}
	}

	public void removeFromFav(RoomChat rc, TombolRoomField field){
		int n= 0;
		RoomChat tf2;
		for(n=0;n<favRomm.size();n++){
			tf2 = (RoomChat)favRomm.elementAt(n);
			if(tf2.getIdRoom().equals(rc.getIdRoom())){
				favRomm.removeElementAt(n);
				favRomm.trimToSize();
				DBStor.set_roomFav(favRomm);
				break;
			}
		}
		Favourite.delete(field);
	}

	public boolean onClose() {
        // check whether we can close the application
        if( PushController.canQuit() ) {
            return super.onClose();
        } else {
            Application.getApplication().requestBackground();
            return false;
        }
    }

	public void addNewMessage(String room,MsgChat msgChat){
		for(int n=0;n<chatss.length;n++){
			if(chatss[n]!=null)
			if(chatss[n].getIdRoom().equals(room)){
				this.chatss[n].addNewMessage(msgChat);
				Util.Log("message send to chat screen");
				return;
			}
		}
		Util.Log("message not send to chat screen. room not found.");
	}

}
