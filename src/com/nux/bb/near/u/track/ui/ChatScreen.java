package com.nux.bb.near.u.track.ui;

import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.nux.bb.near.u.track.field.ChatField;
import com.nux.bb.near.u.track.field.ManagerField;
import com.nux.bb.near.u.track.field.SpaceField;
import com.nux.bb.near.u.track.field.TombolRoomField;
import com.nux.bb.near.u.track.obj.DBStor;
import com.nux.bb.near.u.track.obj.MsgChat;
import com.nux.bb.near.u.track.obj.Setting;
import com.nux.bb.near.u.track.util.InetConn;
import com.nux.bb.near.u.track.util.Util;
import com.nux.bb.near.u.track.util.WebDataCallback;
import com.nux.rim.push.PushController;

public class ChatScreen extends BaseScreenChat implements WebDataCallback, FieldChangeListener{
	Setting st = DBStor.get_setting();
	private static final short[] note = { 2349, 115, 0 };
	EditField chatbox = new EditField("","", 160, AutoTextEditField.NO_NEWLINE){
		protected void makeContextMenu(ContextMenu contextMenu) {
			super.makeContextMenu(contextMenu);
			contextMenu.addItem(SendMessage);
		}
	};
	String idRoom="0",judulRoom = "";
    int count = 0;
    ChatScreen app;
    String html = "";
    RoomScreen roomScreen;
	ManagerField mf1 = new ManagerField();
	TombolRoomField tfs;

	VerticalFieldManager chatManager = new VerticalFieldManager();
	public ChatScreen(String idRoom,String JudulRoom, RoomScreen roomScreen, TombolRoomField tfs) {
		this.idRoom = idRoom;
		this.judulRoom = JudulRoom;
		this.app = this;
		this.roomScreen = roomScreen;
		this.tfs = tfs;
		setJudul(JudulRoom);
		if(tfs.isGbr()){
			tfs.setGbr(null);
		}
		InetConn.getWebData(Util.GetURLChat()+"inout.php?id="+idRoom+"&pin="+Integer.toHexString(DeviceInfo.getDeviceId())+"&in=true&nick="+DBStor.get_setting().getNick(), new WebDataCallback(){
			public void callback(String data) {}
		});

		mf1.add(new SpaceField(8));
		mf1.add(chatManager);
		mf1.add(new SeparatorField());
		mf1.add(chatbox);
		mf1.add(new SpaceField(8));
		add(mf1);
		chatbox.setPadding(0,5,0,5);
		//bf.getScreen().scroll(BOTTOMMOST);
		try{
			chatbox.setFocus();
		}catch(Exception e){}
		chatbox.setChangeListener(this);
		addMenuItem(clear);
		addMenuItem(ShowAllUser);
	}

	MenuItem ShowAllUser = new MenuItem("Show all User",0x000100,0) {
		public void run() {
			UiApplication.getUiApplication().pushScreen(new UserRoomScreen(judulRoom,getIdRoom()));
		}
	};
	MenuItem clear = new MenuItem("Clear Chat",0x000100,0) {
		public void run() {
			chatManager.deleteAll();
		}
	};
	MenuItem SendMessage = new MenuItem("Send",0x000100,0) {
		public void run() {
			if(chatbox.isEditable()){
				if(chatbox.getText().length()>1){
					chatbox.setEditable(false);
					setShowLoader(true);
					InetConn.PostWebData(Util.GetURLChat()+"chat.php?id="+idRoom+"&pin="+Integer.toHexString(DeviceInfo.getDeviceId()), "chat="+chatbox.getText()+"&nama="+DBStor.get_setting().getNick(), app);
				}
			}
		}
	};

	protected boolean keyChar(char c, int status, int time) {
		if(c==Characters.ENTER){
			if(chatbox.isEditable()){
				if(chatbox.getText().length()>1){
					if(DeviceInfo.isSimulator()){
						chatManager.add(new ChatField(new MsgChat("test test http://ibnux.net","test","1232324","",System.currentTimeMillis())));
					}else{
						chatbox.setEditable(false);
						setShowLoader(true);
						InetConn.PostWebData(Util.GetURLChat()+"chat.php?id="+idRoom+"&pin="+Integer.toHexString(DeviceInfo.getDeviceId()), "chat="+chatbox.getText()+"&nama="+DBStor.get_setting().getNick(), this);
						return true;
					}
				}
			}
		}else if(c==Characters.LATIN_SMALL_LETTER_R){
			if(!chatbox.isFocus()){
				chatbox.setFocus();
				return true;
			}
		}else if(c==Characters.ESCAPE){
			if(tfs.isGbr()){
				tfs.setGbr(null);
			}
			PushController.updateIndicator(false);
			UiApplication.getUiApplication().popScreen(this);
			return true;
		}
		return super.keyChar(c, status, time);
	}

	protected boolean onSavePrompt() {
		return true;
	}

	public void callback(final String data) {
		setShowLoader(false);
		if(data.startsWith("Exception:")){
			Dialog.alert("Failed to connect to server");
		}else{
			/*if(!data.equals(app.html)){
				//bf.setURL("");
				//bf.setHtml(data);
				//bf.getURL();
				try{
					bf.displayContent(data, "");
				}catch(Exception e){}
				if(!tfs.isGbr()){
					tfs.setGbr("chat-new.png");
				}
				playTune(60);
				this.html = data;
			}*/
		}
		if(!chatbox.isEditable()){
			chatbox.setText("");
			chatbox.setEditable(true);
			chatbox.setFocus();
		}
	}

	public String getIdRoom() {
		return idRoom;
	}

	public void fieldChanged(Field field, int context) {
		if(tfs.isGbr()){
			tfs.setGbr(null);
		}
	}
	public static final boolean playTune( int volume) {
        if ( Alert.isAudioSupported() ) {
            Alert.startAudio(note, volume);
            return true;
        }
        if ( Alert.isBuzzerSupported() ) {
            Alert.startBuzzer(note, volume );
            return true;
        }
        return false;
    }

	public void addNewMessage(MsgChat msgChat){
		ChatField cf = new ChatField(msgChat);
		if(chatManager.getFieldCount()>50){
			chatManager.delete(chatManager.getField(0));
		}
		chatManager.add(cf);
		cf.setFocus();
		chatbox.setFocus();
		Util.Log("message added to chat room");
		if(!tfs.isGbr()){
			tfs.setGbr("chat-new.png");
		}
		PushController.updateIndicator(true);
		if(DBStor.get_setting().isSound()){
			playTune(100);
		}
	}
}
