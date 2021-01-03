package org.magicwerk.brownies.collections.animation;

import java.time.ZonedDateTime;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;

import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * Plays several animation following each other.
 * User can control some basic functionality using the mouse buttons:
 * - left button causes the current animation to be finished in finishDuration
 *   (if speedUpAll is set, all remaining animations will be finished in this time)
 * - right mouse buttons causes the current animation to pause / resume
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class AnimationPlayer {
	/**
	 * Time in milliseconds to finish an animation if user decides to do so manually.
	 */
	int finishDuration = 1000;
	boolean speedUpAll = true;
	double prevRate = 0;

	Animation currentAnim;
	/**
	 * Animations ready to play after the current animation.
	 */
	IList<Animation> pendingAnimations = GapList.create();
	/**
	 * Mouse events which have been received when the animation was in the finish phase,
	 * where they could not be processed. These pending events will be processed if the first
	 * animation has finished and automatically the next animation is started.
	 */
	IList<MouseEvent> pendingEvents = GapList.create();
	EventHandler<? super MouseEvent> handler;

	public AnimationPlayer() {
	}

	public AnimationPlayer(Scene scene, Animation anim) {
		attachOnMousePressed(scene);
		addAnimation(anim);
	}

	public Animation getAnimation() {
		return currentAnim;
	}

	public void attachOnMousePressed(Scene scene) {
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				onMousePressed(event);
			}
		});
	}

	public void setOnMousePressed(EventHandler<? super MouseEvent> handler) {
		this.handler = handler;
	}

	public void addAnimation(Animation anim) {
		if (currentAnim == null) {
			setActive(anim);
		} else {
			pendingAnimations.add(anim);
		}
	}

	void setActive(Animation anim) {
		this.currentAnim = anim;
		anim.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				double prevRate = anim.getCurrentRate();
				AnimationPlayer.this.prevRate = 0;

				AnimationPlayer.this.currentAnim = null;
				Animation anim = pendingAnimations.pollFirst();
				if (anim != null) {
					setActive(anim);
				}
				MouseEvent me = pendingEvents.pollFirst();
				if (me != null) {
					AnimationPlayer.this.prevRate = prevRate;
					onMousePressed(me);
				}
			}
		});
	}

	void onMousePressed(MouseEvent event) {
		MouseButton mb = event.getButton();
		if (mb == MouseButton.SECONDARY) {
			if (currentAnim == null) {
				return;
			}
			System.out.println(currentAnim.getTotalDuration());
			System.out.println(currentAnim.getCurrentTime());
			// Pause / resume animation on right mouse click
			if (currentAnim.getStatus() == Status.RUNNING) {
				currentAnim.pause();
			} else {
				currentAnim.setRate(1);
				currentAnim.play();
			}

		} else {
			// If animation rate has been changed, animation is in finish phase
			if (currentAnim == null) {
				if (handler != null) {
					handler.handle(event);
				}
			} else if (currentAnim.getRate() != 1) {
				pendingEvents.add(event);
				return;
			}
			if (currentAnim == null) {
				return;
			}

			// User want to finish animation, so calculate needed rate to finish in time
			Duration total = currentAnim.getTotalDuration();
			Duration current = currentAnim.getCurrentTime();
			double remaining = total.toMillis() - current.toMillis();
			System.out.println("Finishing " + ZonedDateTime.now());

			if (speedUpAll) {
				for (Animation anim : pendingAnimations) {
					remaining += anim.getTotalDuration().toMillis();
				}
				remaining += pendingEvents.size() * finishDuration;
			}

			double rate = remaining / finishDuration;
			if (rate < prevRate) {
				rate = prevRate;
				prevRate = 0;
			}
			currentAnim.setRate(rate);
			if (currentAnim.getStatus() != Status.RUNNING) {
				System.out.println("Playing " + rate);
				currentAnim.play();
			}
		}
	}

}
