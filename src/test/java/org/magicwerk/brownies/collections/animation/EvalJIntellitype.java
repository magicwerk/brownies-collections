package org.magicwerk.brownies.collections.animation;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;

/**
 * Animate ArrayList, GapList, BigList.
 *
 * @author Thomas Mauch
 * @version $Id: AnimateCollections.java 2948 2015-09-11 09:27:04Z origo $
 */
public class EvalJIntellitype extends Application {

	// JIntellitype

	class HotKeys implements HotkeyListener, IntellitypeListener {

		static private final int HOT_KEY_HOME = 1;
		static private final int HOT_KEY_F1 = 2;
		static private final int HOT_KEY_ENTER = 3;
		static private final int HOT_KEY_ESCAPE = 4;
		static private final int HOT_KEY_VOLUME_UP = 5;
		static private final int HOT_KEY_VOLUME_DOWN = 6;

		// See JIntellitype.getKey2KeycodeMapping() too see the supported key strings
		// See https://msdn.microsoft.com/en-us/library/windows/desktop/dd375731%28v=vs.85%29.aspx
		// for a definition of Windows Virtual-Key Codes
		static private final int VK_HOME = 0x24;

		public HotKeys() {
			JIntellitype.getInstance().addHotKeyListener(this);
			JIntellitype.getInstance().addIntellitypeListener(this);

			JIntellitype.getInstance().registerHotKey(HOT_KEY_HOME, "home");
			JIntellitype.getInstance().registerHotKey(HOT_KEY_F1, "f1");
			JIntellitype.getInstance().registerHotKey(HOT_KEY_ENTER, "enter");
			JIntellitype.getInstance().registerHotKey(HOT_KEY_ESCAPE, "escape");

			//JIntellitype.getInstance().registerHotKey(HOT_KEY_HOME, 0, VK_HOME);
			//JIntellitype.getInstance().registerHotKey(HOT_KEY_2, "shif+f1");
		}

		public void shutdown() {
			JIntellitype.getInstance().unregisterHotKey(HOT_KEY_HOME);
			JIntellitype.getInstance().unregisterHotKey(HOT_KEY_F1);
			JIntellitype.getInstance().unregisterHotKey(HOT_KEY_ENTER);
			JIntellitype.getInstance().unregisterHotKey(HOT_KEY_ESCAPE);
			JIntellitype.getInstance().unregisterHotKey(HOT_KEY_VOLUME_UP);
//			JIntellitype.getInstance().unregisterHotKey(HOT_KEY_ESCAPE);
			JIntellitype.getInstance().cleanUp();
		}

		@Override
		public void onHotKey(int id) {
			System.out.println("WM_HOTKEY message received " + id);
		}

		@Override
		public void onIntellitype(int command) {
			System.out.println("intellitype " + command);
			if (command == JIntellitype.APPCOMMAND_VOLUME_UP) {
				onHotKey(HOT_KEY_VOLUME_UP);
			} else if (command == JIntellitype.APPCOMMAND_VOLUME_DOWN) {
				onHotKey(HOT_KEY_VOLUME_DOWN);
			}
		}

	}

	HotKeys hotKeys;

	public static void main(String[] args) {
		Application.launch(args);
	}

	// Keys on eLive Mousepointer:
	// HOME - ENTER   - (VOLUME UP)
	// F1   - ESCAPE  - (VOLUME DOWN)

	@Override
	public void start(Stage stage) {
		System.out.println("Start");

		hotKeys = new HotKeys();

		// If a key is defined as hot key, the application with focus will not receive it
		Group root = new Group();
		Scene scene = new Scene(root, 640, 400);
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent ke) {
				System.out.println("Key Pressed: " + ke.getCode());
			}
		});

		stage.setTitle("EvalJIntellitype");
		stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
	}

	@Override
	public void stop() {
		System.out.println("Stop");
		if (hotKeys != null) {
			hotKeys.shutdown();
		}
	}


}
