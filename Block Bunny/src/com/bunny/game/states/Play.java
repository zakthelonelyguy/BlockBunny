package com.bunny.game.states;

import static com.bunny.game.handlers.B2DVars.PPM;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.bunny.game.entities.Crystal;
import com.bunny.game.entities.HUD;
import com.bunny.game.entities.Player;
import com.bunny.game.handlers.B2DVars;
import com.bunny.game.handlers.GameStateManager;
import com.bunny.game.handlers.MyContactListener;
import com.bunny.game.handlers.MyInput;
import com.bunny.game.main.Game;

public class Play extends GameState {
	private boolean debug = false;
	
	private World world;
	private Box2DDebugRenderer b2dr;
	
	private OrthographicCamera b2dCam;
	private MyContactListener cl;
	
	private TiledMap tileMap;
	private float tileSize;
	private OrthogonalTiledMapRenderer tmr;
	
	private Player player;
	private Array<Crystal> crystals;
	
	private HUD hud;
	
	public Play (GameStateManager gsm) {
		super(gsm);
		
		// Set Up Box2D
		world = new World(new Vector2(0, -9.81f), true);
		cl = new MyContactListener();
		world.setContactListener(cl);
		b2dr = new Box2DDebugRenderer();
		
		// Create Player
		createPlayer();
		
		//  Create Tiles
		createTiles();
		
		// Create Crystals
		createCrystals();
		player.setTotalCrystals(crystals.size);
		
		// Box2D Camera
		b2dCam = new OrthographicCamera();
		b2dCam.setToOrtho(false, Game.V_WIDTH / PPM, Game.V_HEIGHT / PPM);
		
		// Set up HUD
		hud = new HUD(player);
	}

	public void handleInput () {
		// Player Jump
		if (MyInput.isPressed(MyInput.BUTTON1) && cl.isPlayerOnGround()) {
			player.getBody().applyForceToCenter(0, 250, true);
		}
		
		// Switch Block Color
		if (MyInput.isPressed(MyInput.BUTTON2)) {
			switchBlocks();
		}
	}
	
	public void update (float dt) {
		// Check Input
		handleInput();
		
		// Update Box2d
		world.step(Game.STEP, 6, 2);
		
		// Remove Crystals
		Array<Body> bodies = cl.getBodiesToRemove();
		for (int i = 0; i < bodies.size; i++) {
			Body b = bodies.get(i);
			crystals.removeValue((Crystal) b.getUserData(), true);
			world.destroyBody(b);
			player.collectCrystal();
		} bodies.clear();
		
		player.update(dt);
		
		for (int i = 0; i < crystals.size; i++) {
			crystals.get(i).update(dt);
		}
	}
	
	public void render () {
		// Clear Screen
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		// Set Camera To Follow Player
		cam.position.set(
			player.getPosition().x * PPM + Game.V_WIDTH / 4,
			Game.V_HEIGHT / 2, 0
		);
		cam.update();
		// Draw Tile Map
		tmr.setView(cam);
		tmr.render();
		// Draw Player
		sb.setProjectionMatrix(cam.combined);
		player.render(sb);
		// Draw Crystals
		for (int i = 0; i < crystals.size; i++)
			crystals.get(i).render(sb);
		// Draw HUD
		sb.setProjectionMatrix(hudCam.combined);
		hud.render(sb);
		// Draw Box2D World
		if (debug) {
			b2dCam.position.x = player.getPosition().x + Game.V_WIDTH / (PPM * 4);
			b2dCam.update();
			b2dr.render(world, b2dCam.combined);
		}
	}
	
	public void dispose () {}
	
	private void createPlayer() {
		BodyDef bdef = new BodyDef();
		FixtureDef fdef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
		
		// Create Player
		bdef.position.set(100 / PPM, 200 / PPM);
		bdef.type = BodyType.DynamicBody;
		bdef.linearVelocity.set(1f, 0);
		Body body = world.createBody(bdef);
				
		shape.setAsBox(13 / PPM, 13 / PPM);
		fdef.shape = shape;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_RED | B2DVars.BIT_CRYSTAL;
		body.createFixture(fdef).setUserData("player");
		//shape.dispose();
				
		// Foot Sensor
		shape.setAsBox(13 / PPM, 2 / PPM, new Vector2(0, -13 / PPM), 0);
		fdef.shape = shape;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_RED;
		fdef.isSensor = true;
		body.createFixture(fdef).setUserData("foot");
		//shape.dispose();
		
		// Create Player
		player = new Player(body);
		body.setUserData(player);
	}
	
	private void createTiles () {
		// Load Tile Map
		tileMap = new TmxMapLoader().load("res/maps/test.tmx");
		tmr = new OrthogonalTiledMapRenderer(tileMap);
		
		tileSize = (int) tileMap.getProperties().get("tilewidth");
		
		TiledMapTileLayer layer; 


		layer = (TiledMapTileLayer) tileMap.getLayers().get("Red");
		createLayer(layer, B2DVars.BIT_RED);
		layer = (TiledMapTileLayer) tileMap.getLayers().get("Green");
		createLayer(layer, B2DVars.BIT_GREEN);
		layer = (TiledMapTileLayer) tileMap.getLayers().get("Blue");
		createLayer(layer, B2DVars.BIT_BLUE);
	}
	
	private void createLayer(TiledMapTileLayer layer, short bits) {
		BodyDef bdef = new BodyDef();
		FixtureDef fdef = new FixtureDef();
		
		// Go Through All The Cells
		for (int row = 0; row < layer.getHeight(); row++) {
			for (int col = 0; col < layer.getWidth(); col++) {
				// Get Cell
				Cell cell = layer.getCell(col, row);
				
				// Check If Cell Exists
				if (cell == null) continue;
				if (cell.getTile() == null) continue;
				
				// Create Body + Fixture From Cell
				bdef.type = BodyType.StaticBody;
				bdef.position.set(
						(col + 0.5f) * tileSize / PPM,
						(row + 0.5f) * tileSize / PPM
				);
				
				ChainShape cs = new ChainShape();
				Vector2[] v = new Vector2[3];
				v[0] = new Vector2
						(-tileSize / 2 / PPM, -tileSize / 2 / PPM);
				v[1] = new Vector2(
						-tileSize / 2 / PPM, tileSize / 2 / PPM);
				v[2] = new Vector2(
						tileSize / 2 / PPM, tileSize / 2 / PPM);
				
				cs.createChain(v);
				
				fdef.friction = 0;
				fdef.shape = cs;
				fdef.filter.categoryBits = bits;
				fdef.filter.maskBits = B2DVars.BIT_PLAYER;
				fdef.isSensor = false;
				world.createBody(bdef).createFixture(fdef);
			}
		}
	}
	
	private void createCrystals() {
		crystals = new Array<Crystal>();
		MapLayer layer = tileMap.getLayers().get("Crystals");
		
		BodyDef bdef = new BodyDef();
		FixtureDef fdef = new FixtureDef();
		
		if (layer == null) return;
		
		for (MapObject mo: layer.getObjects()) {
			bdef.type = BodyType.StaticBody;
			
			float x = (float) mo.getProperties().get("x") / PPM;
			float y = (float) mo.getProperties().get("y") / PPM;
			bdef.position.set(x, y);
			
			CircleShape cshape = new CircleShape();
			cshape.setRadius(8 / PPM);
			
			fdef.shape = cshape;
			fdef.isSensor = true;
			fdef.filter.categoryBits = B2DVars.BIT_CRYSTAL;
			fdef.filter.maskBits = B2DVars.BIT_PLAYER;
			
			Body body = world.createBody(bdef);
			body.createFixture(fdef).setUserData("crystal");
			
			Crystal c = new Crystal(body);
			crystals.add(c);
			
			body.setUserData(c);
		}
	}
	
	private void switchBlocks() {
		Filter filter = player.getBody().getFixtureList().first().getFilterData();
		short bits = filter.maskBits;
		
		// Switch To Next Color
		// Red -> Green -> Blue -> Red
		if ((bits & B2DVars.BIT_RED) != 0) {
			bits &= ~B2DVars.BIT_RED;
			bits |= B2DVars.BIT_GREEN;
		} else if ((bits & B2DVars.BIT_GREEN) != 0) {
			bits &= ~B2DVars.BIT_GREEN;
			bits |= B2DVars.BIT_BLUE;
		} else if ((bits & B2DVars.BIT_BLUE) != 0) {
			bits &= ~B2DVars.BIT_BLUE;
			bits |= B2DVars.BIT_RED;
		}
		
		// Set New Mask Bits
		filter.maskBits = bits;
		player.getBody().getFixtureList().first().setFilterData(filter);
		
		// Set New Mask Bits For Foot
		filter = player.getBody().getFixtureList().get(1).getFilterData();
		bits &= ~B2DVars.BIT_CRYSTAL;
		filter.maskBits = bits;
		player.getBody().getFixtureList().get(1).setFilterData(filter);
	}
}
