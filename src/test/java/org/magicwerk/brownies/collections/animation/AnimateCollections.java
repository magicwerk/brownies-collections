package org.magicwerk.brownies.collections.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.magicwerk.brownies.collections.BigList;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.StringTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.fx.FxTools;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;

import ch.qos.logback.classic.Logger;
import javafx.animation.Animation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Animate ArrayList, GapList, BigList.
 * The animation can be controlled using keys available on the eLive mouse pointer.
 * It uses the 3rd party library JIntellitype to catch any Windows key events (see JIntellitypeTester.java).
 *
 * HOME 0x24 - ENTER  0x0A - VOLUME_UP
 * F1	0x70 - ESCPAE 0x1B - VOLUME_DOWN
 *
 * Start application and let it run in background.
 * Start presentation in full screen mode.
 * Press F1 or F2 to bring animation menu up front.
 * Select animation to show
 *
 * Animation is shown:
 * - press LEFT MOUSE BUTTON to progress in animation
 * - press HOME or ESCAPE to show menu
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class AnimateCollections extends Application {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	// JIntellitype

	/**
	 * Class implementing hot key listeners.
	 */
	class HotKeys implements HotkeyListener, IntellitypeListener {

		static private final int HOT_KEY_1 = 1;
		static private final int HOT_KEY_2 = 2;
		static private final int HOT_KEY_3 = 3;
		// Windows virtual-key code of HOME key
		static private final int VK_HOME = 0x24;

		/**
		 * Constructor setting up hot key bindings.
		 */
		public HotKeys() {
			JIntellitype.getInstance().addHotKeyListener(this);
			JIntellitype.getInstance().addIntellitypeListener(this);
			JIntellitype.getInstance().registerHotKey(HOT_KEY_1, "F1");
			JIntellitype.getInstance().registerHotKey(HOT_KEY_2, "F2");
			JIntellitype.getInstance().registerHotKey(HOT_KEY_3, 0, VK_HOME);
		}

		/**
		 * Release hot key bindings.
		 */
		public void shutdown() {
			JIntellitype.getInstance().unregisterHotKey(HOT_KEY_1);
			JIntellitype.getInstance().unregisterHotKey(HOT_KEY_2);
			JIntellitype.getInstance().unregisterHotKey(HOT_KEY_3);
			JIntellitype.getInstance().cleanUp();
		}

		@Override
		public void onHotKey(int id) {
			LOG.debug("onHotKey message received " + id);
			onKeyShowHide();
		}

		@Override
		public void onIntellitype(int id) {
			LOG.debug("onIntellitype message received " + id);
			if (id == JIntellitype.APPCOMMAND_VOLUME_UP || id == JIntellitype.APPCOMMAND_VOLUME_DOWN) {
				onKeyShowHide();
			}
		}

		void onKeyShowHide() {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					AnimateCollections.this.onKeyShowHide();
				}
			});
		}

	}

	// Object Ids

	static HashMap<Integer, Integer> objMap = new HashMap<>();

	public static void initObjIds() {
		objMap = new HashMap<>();
	}

	public static int getObjId(Object obj) {
		int id = System.identityHashCode(obj);
		Integer objId = objMap.get(id);
		if (objId == null) {
			objId = objMap.size() + 1;
			objMap.put(id, objId);
		}
		return objId;
	}

	//

	static int duration = 200;
	static int boxSize = 50;
	static double moveY = 1.5 * boxSize;

	static Color textColor = Color.BLACK;
	static Color marginColor = Color.RED;
	static Color emptySlotColor = Color.LIGHTGRAY;
	static Color gapSlotColor = Color.GRAY;
	static Color usedSlotColor = Color.LIGHTYELLOW;
	static Color activeSlotColor = Color.YELLOW;
	static Color blockPrivateColor = new Color(1, 1, 0.75, 1);
	static Color blockSharedColor = new Color(1, 0.825, 0.825, 1);
	/** Font used for UI elements (not animation) */
	static Font font = Font.font("Arial", 18);

	/**
	 * One of ARRAYLIST, GAPLIST, BIGLIST.
	 */
	enum Mode {
		ARRAYLIST,
		GAPLIST,
		BIGLIST
	}

	abstract static class ListAnimation {
		Mode mode;
		int capacity = -1;
		List<Integer> list;

		/**
		 * Get list at specified number of operations.
		 *
		 * @param index
		 * @return		list at specified number of operation, null if operation is finished an no list must be visualized
		 */
		abstract List<Integer> getList(int index);

		ListAnimation setMode(Mode mode) {
			if (mode == Mode.ARRAYLIST) {
				list = new ArrayList<Integer>(5);
			} else if (mode == Mode.GAPLIST) {
				list = new GapList<Integer>(5);
			} else if (mode == Mode.BIGLIST) {
				list = new BigList<Integer>(10);
			} else {
				throw new AssertionError();
			}
			this.mode = mode;
			return this;
		}
	}

	static class ListAddTailAnimation extends ListAnimation {
		@Override
		public List<Integer> getList(int index) {
			if (index == 0) {
				list.addAll(Arrays.asList(1, 2));
			} else {
				list.add(index + 2);
			}
			return list;
		}
	}

	static class ListAddHeadAnimation extends ListAnimation {
		@Override
		public List<Integer> getList(int index) {
			if (index == 0) {
				list.addAll(Arrays.asList(19, 20));
			} else {
				int elem = 19 - index;
				list.add(0, elem);
			}
			return list;
		}
	}

	static class ListAddMiddleAnimation extends ListAnimation {
		@Override
		public List<Integer> getList(int index) {
			if (index == 0) {
				list.addAll(Arrays.asList(1, 2));
			} else {
				list.add(list.size() / 2, index + 2);
			}
			return list;
		}
	}

	static class ListRemoveMiddleAnimation extends ListAnimation {
		@Override
		public List<Integer> getList(int index) {
			if (index == 0) {
				list.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
			} else {
				if (list.isEmpty()) {
					return null;
				}
				list.remove(list.size() / 2);
			}
			return list;
		}
	}

	static class ListRemoveTailAnimation extends ListAnimation {
		@Override
		public List<Integer> getList(int index) {
			if (index == 0) {
				list.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
			} else {
				if (list.isEmpty()) {
					return null;
				}
				list.remove(list.size() - 1);
			}
			return list;
		}
	}

	static class ListRemoveHeadAnimation extends ListAnimation {
		@Override
		public List<Integer> getList(int index) {
			if (index == 0) {
				list.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
			} else {
				if (list.isEmpty()) {
					return null;
				}
				list.remove(0);
			}
			return list;
		}
	}

	static class ListAnalyticalEngineAnimation extends ListAnimation {
		int add = 0;

		@Override
		public List<Integer> getList(int index) {
			if (index < 5) {
				list.add(0, add);
				add++;
			} else {
				if (index % 2 == 0) {
					list.add(0, add);
					add++;
				} else {
					list.remove(list.size() - 1);
				}
			}
			for (int i = 1; i < list.size(); i++) {
				CheckTools.check(list.get(i - 1) - 1 == list.get(i));
			}
			return list;
		}
	}

	static class ListAddRemoveIterAnimation extends ListAnimation {
		int pos = 1;
		int elem;

		@Override
		public List<Integer> getList(int index) {
			if (index == 0) {
				list.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15));
				elem = 16;
			} else {
				if (pos >= list.size()) {
					return null;
				}
				int mod = (index - 1) % 3;
				if (mod == 0) {
					list.remove(pos);
				} else if (mod == 1) {
					list.remove(pos);
				} else if (mod == 2) {
					list.add(pos, elem);
					pos += 2;
					elem++;
				} else {
					assert (false);
				}
			}
			return list;
		}
	}

	static class ListAddRemoveRandomAnimation extends ListAnimation {
		int pos = 0;
		int elem = 1;
		Random rnd = new Random(0);

		@Override
		public List<Integer> getList(int index) {
			int size = list.size();
			boolean add;
			if (size < 5) {
				add = true;
			} else if (size >= 10) {
				add = false;
			} else {
				add = (rnd.nextInt(2) == 0);
			}

			if (add) {
				int pos = rnd.nextInt(size + 1);
				list.add(pos, elem);
				elem++;
			} else {
				int pos = rnd.nextInt(size);
				list.remove(pos);
			}
			return list;
		}
	}

	// BigList

	static class BigListAnimation extends ListAnimation {
		{
			setMode(Mode.BIGLIST);
		}

		@Override
		public List<Integer> getList(int index) {
			if (index < 12) {
				list.add(index + 1);
			} else if (index < 22) {
				list.remove(0);
			} else {
				return null;
			}
			return list;
		}
	}

	static class BigListCopyAnimation extends ListAnimation {
		{
			setMode(Mode.BIGLIST);
		}

		@Override
		public List<Integer> getList(int index) {
			if (index < 15) {
				list.add(index + 1);
			} else {
				return null;
			}
			return list;
		}
	}

	HotKeys hotKeys;
	/** Window showing menu */
	Stage menuStage;
	/** Window showing animation */
	Stage animStage;
	ToggleGroup radioGroup;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
		this.menuStage = stage;

		hotKeys = new HotKeys();
		showMenu(stage);
	}

	@Override
	public void stop() {
		System.out.println("Stage is closing");
		hotKeys.shutdown();
	}

	GapList<Class<?>> listAnimations = GapList.create(
			ListAddTailAnimation.class, //ListRemoveTailAnimation.class,
			ListAddHeadAnimation.class, //ListRemoveHeadAnimation.class,
			ListAddMiddleAnimation.class, //ListRemoveMiddleAnimation.class,
			ListAddRemoveIterAnimation.class, ListAddRemoveRandomAnimation.class,
			ListAnalyticalEngineAnimation.class);

	HBox createButtons(Mode mode) {
		GapList<Button> buttons = GapList.create();
		for (final Class<?> clazz : listAnimations) {
			String name = clazz.getSimpleName();
			name = StringTools.removeHead(name, "List");
			name = StringTools.removeTail(name, "Animation");
			final String aninName = name;
			Button button = createButton(name);
			buttons.add(button);

			button.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent actionEvent) {
					// If user clicks on button, show selected animation in fullscreen
					ListAnimation listAnimation = (ListAnimation) ReflectTools.create(clazz);
					listAnimation.setMode(mode);
					Scene animScene = new ShowListAnimation().show(listAnimation);
					addInvisiblePoint(animScene, 800, 100);
					showScene(animScene, aninName);
				}
			});
		}

		HBox box = new HBox();
		box.getChildren().add(createLabel(mode.toString() + ": "));
		box.getChildren().addAll(buttons);
		return box;
	}

	Button createButton(String text) {
		Button button = new Button(text);
		button.setFont(font);
		return button;
	}

	RadioButton createRadioButton(String text) {
		RadioButton button = new RadioButton(text);
		button.setFont(font);
		return button;
	}

	Label createLabel(String text) {
		Label label = new Label(text);
		label.setFont(font);
		return label;
	}

	void add(Pane pane, Node node) {
		if (node != null) {
			pane.getChildren().add(node);
		}
	}

	/**
	 * Create menus with animations for ArrayList, GapList, BigList.
	 *
	 * @param stage	stage to show menu on
	 */
	void showMenu(Stage stage) {
		HBox araryListButtons = createButtons(Mode.ARRAYLIST);
		HBox gapListButtons = createButtons(Mode.GAPLIST);

		Button bigListButton = createButton("BigList");
		bigListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				Scene scene = new ShowBigListAnimation().show();
				showScene(scene, "BigList");
			}
		});
		HBox bigListButtons = new HBox();
		add(bigListButtons, createLabel("BigList: "));
		add(bigListButtons, bigListButton);

		VBox box = new VBox();
		box.getChildren().add(araryListButtons);
		box.getChildren().add(gapListButtons);
		box.getChildren().add(bigListButtons);

		VBox radioBox = new VBox();
		radioGroup = new ToggleGroup();
		List<Screen> screens = Screen.getScreens();
		for (int i = 0; i < screens.size(); i++) {
			Screen screen = screens.get(i);
			String str = screen.getBounds().toString();
			boolean primary = FxTools.isPrimaryScreen(screen);
			if (primary) {
				str = "Primary: " + str;
			}
			RadioButton radioButton = createRadioButton(str);
			radioButton.selectedProperty().set(primary);
			radioButton.setUserData(screen);
			radioButton.setToggleGroup(radioGroup);
			radioBox.getChildren().add(radioButton);
		}
		box.getChildren().add(radioBox);

		Scene scene = new Scene(box);
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				System.out.println("Key Pressed: " + ke.getCode());
				if (ke.getCode() == KeyCode.ESCAPE) {
					menuStage.setFullScreen(false);
				}
			}
		});

		HBox buttons = new HBox();
		Button exitButton = createButton("Exit");
		exitButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				Platform.exit();
			}
		});
		Button hideButton = createButton("Hide");
		hideButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				onKeyShowHide();
			}
		});
		Label hideText = createLabel(" (use F1 or F2 key to toggle show/hide, use ESC for leaving fullscreen mode)");
		buttons.getChildren().add(exitButton);
		buttons.getChildren().add(hideButton);
		buttons.getChildren().add(hideText);
		box.getChildren().add(buttons);

		stage.setTitle("Animations");
		stage.setScene(scene);
		stage.sizeToScene();
		//stage.initStyle(StageStyle.UNDECORATED);
		//stage.initStyle(StageStyle.UTILITY);
		stage.show();
	}

	/**
	 * Called if a registered hot key has been pressed.
	 * If animation is shown, close animation screen.
	 * Otherwise, if menu window is in front, send it to back.
	 * Otherwise, show menu window on top using full screen mode.
	 */
	void onKeyShowHide() {
		LOG.debug("onKeyShowHide");

		if (animStage != null) {
			animStage.close();
			animStage = null;
		} else {
			// Using full screen mode seems to be only solution to prevent the windows task bar from
			// jumping in front
			if (menuStage.isFullScreen()) {
				menuStage.toBack();
				menuStage.setFullScreen(false);
			} else {
				menuStage.toFront();
				menuStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
				menuStage.setFullScreenExitHint("");
				menuStage.setFullScreen(true);
			}
		}
	}

	/**
	 * Show scene with animation in full screen on animStage.
	 *
	 * @param scene	scene containing animation
	 * @param title	title of animation
	 */
	void showScene(Scene scene, String title) {
		if (animStage != null) {
			animStage.close();
		}

		// Get screen to use from the selected radio button
		Screen screen = (Screen) radioGroup.getSelectedToggle().getUserData();

		String text = "Animation " + title;
		Font font = Font.font("Arial Black", FontWeight.BOLD, 36);
		Label label = new Label(text);
		label.setTranslateX(20);
		label.setTranslateY(20);
		label.setFont(font);

		Group oldRoot = (Group) scene.getRoot();
		oldRoot.setTranslateY(100);

		Group newRoot = new Group();
		newRoot.getChildren().addAll(label, oldRoot);
		scene.setRoot(newRoot);

		Stage stage = new Stage();

		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.ESCAPE || ke.getCode() == KeyCode.HOME) {
					// Close fullscreen if user presses Escape
					stage.close();
				}
			}
		});

		Rectangle2D bounds = screen.getBounds();
		double screenWidth = bounds.getWidth();
		double screenHeight = bounds.getHeight();
		//screenHeight = 1080;
		bounds = new Rectangle2D(bounds.getMinX(), bounds.getMinY(), screenWidth, screenHeight);

		final double initWidth = newRoot.getBoundsInLocal().getWidth();
		final double initHeight = newRoot.getBoundsInLocal().getHeight();
		Point2D srcPt = new Point2D(initWidth, initHeight);
		System.out.println(initWidth);

		Point2D dstPt = new Point2D(bounds.getWidth(), bounds.getHeight());
		Point2D s = FxTools.getScaleRect(srcPt, dstPt, FxTools.SCALE_BOTH);

		Scale scale = new Scale();
		//scale.xProperty().bind(scene.widthProperty().divide(initWidth));
		//scale.yProperty().bind(scene.heightProperty().divide(initHeight));
		scale.setX(s.getX() / initWidth);
		scale.setY(s.getY() / initHeight);
		scale.setPivotX(0);
		scale.setPivotY(0);
		newRoot.getTransforms().addAll(scale);

		// Show fullscreen
		stage.setScene(scene);
		stage.setX(bounds.getMinX());
		stage.setY(bounds.getMinY());
		stage.setWidth(bounds.getWidth());
		stage.setHeight(bounds.getHeight());
		//stage.setMaximized(true);

		stage.initStyle(StageStyle.UNDECORATED);
		stage.show();
		animStage = stage;
	}

	static void addInvisiblePoint(Scene scene, double x, double y) {
		Group root = (Group) scene.getRoot();
		Circle point = new Circle(x, y, 0);
		// Documentation says the visible property is ignored for computing the layout, but this seems not to be true
		//point.setVisible(false);
		root.getChildren().add(point);
	}

	static class ShowBigListAnimation {
		ListAnimation listAnimation;
		BigList<Integer> list;
		int num = 0;

		Scene show() {
			final Scene scene = new Scene(new Group());
			addInvisiblePoint(scene, 1200, 100);

			listAnimation = new BigListCopyAnimation();
			list = (BigList<Integer>) listAnimation.getList(0);
			final BigListCopiesShape shape = new BigListCopiesShape(list);

			Group root = (Group) scene.getRoot();
			root.getChildren().add(shape);
			shape.setTranslateX(50);
			shape.setTranslateY(50);

			scene.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					initObjIds();
					System.out.println("mouse click detected! " + event.getSource());
					num++;
					BigList<Integer> list2 = (BigList<Integer>) listAnimation.getList(num);
					if (list2 != null) {
						list = list2;
					} else {
						if (num == 15) {
							// Copy BigList: all blocks are shared (refCount becomes 2)
							BigList<Integer> bl = list;
							BigList<Integer> copy = bl.copy();
							shape.setCopy(copy);
						} else if (num == 16) {
							// Change single value (blocks is unshared)
							shape.getCopy().set(1, 99);
						} else if (num == 17) {
							// manually reset refCount
							BigListModel.setBlockRefCount(list, 1, 1);
							shape.setCopy(null);
						} else if (num < 30) {
							if (num == 18 || num % 3 == 2) {
								list.remove(list.size() - 2);
							} else {
								list.remove(1);
							}
						}
					}
					System.out.println("List: " + list);
					shape.update();
				}
			});
			return scene;
		}
	}

	/**
	 * Create scene from list animation to show it.
	 */
	static class ShowListAnimation {

		AnimationPlayer animPlayer;
		Animation anim;
		ListAnimation listAnimation;
		ListShape listShape;
		List<Integer> list;
		int num = 0;
		Random rnd = new Random(0);

		Scene show(ListAnimation listAnimation) {
			this.listAnimation = listAnimation;

			final Scene scene = new Scene(new Group());
			list = listAnimation.getList(0);
			List<Integer> list2 = ReflectTools.cloneDeep(list);
			listShape = new ListShape();
			Group root = (Group) scene.getRoot();
			root.getChildren().add(listShape);
			listShape.init(list2);

			animPlayer = new AnimationPlayer();
			//			for (int i=0; i<100; i++) {
			//				playNextAnim(null);
			//			}
			animPlayer.attachOnMousePressed(scene);
			animPlayer.setOnMousePressed(this::playNextAnim);

			//			scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			//				@Override
			//				public void handle(MouseEvent event) {
			//					playNextAnim(event);
			//				}
			//			});

			return scene;
		}

		void playNextAnim(MouseEvent event) {
			System.out.println("playNextAnim");

			num++;
			list = listAnimation.getList(num);
			if (list == null) {
				return;
			}

			List<Integer> list2 = ReflectTools.cloneDeep(list);
			Animation anim = listShape.transition(list2);
			animPlayer.addAnimation(anim);
			//animPlayer.onMousePressed(event);
		}
	}

}
