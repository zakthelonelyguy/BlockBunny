package com.bunny.game.handlers;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;

public class MyContactListener implements ContactListener{
	private int numFootContacts;
	private Array<Body> bodiesToRemove;
	
	public MyContactListener () {
		super();
		bodiesToRemove = new Array<Body>();
	}
	
	// When Two Fixtures Start To Collide
	public void beginContact(Contact c) {
		Fixture fa = c.getFixtureA();
		Fixture fb = c.getFixtureB();
		
		if (fa.getUserData() != null && fa.getUserData().equals("foot")) {
			numFootContacts++;
		}
		
		if (fb.getUserData() != null && fb.getUserData().equals("foot")) {
			numFootContacts++;
		}
		
		if (fa.getUserData() != null && fa.getUserData().equals("crystal")) {
			// Remove Crystal
			bodiesToRemove.add(fa.getBody());
		}
		
		if (fb.getUserData() != null && fb.getUserData().equals("crystal")) {
			// Remove Crystal
			bodiesToRemove.add(fb.getBody());
		}
	}
	// When Two Fixtures Are No Longer Colliding
	public void endContact(Contact c) {
		Fixture fa = c.getFixtureA();
		Fixture fb = c.getFixtureB();
		
		if (fa == null || fb == null) return;
		
		if(fa.getUserData() != null && fa.getUserData().equals("foot")) {
			numFootContacts--;
		}
		
		if(fb.getUserData() != null && fb.getUserData().equals("foot")) {
			numFootContacts--;
		}
	}
	
	public boolean isPlayerOnGround() { return numFootContacts > 0; }
	
	public Array<Body> getBodiesToRemove() {
		return bodiesToRemove;
	}
	
	public void preSolve(Contact c, Manifold m) { }
	public void postSolve(Contact c, ContactImpulse ci) { }
}
